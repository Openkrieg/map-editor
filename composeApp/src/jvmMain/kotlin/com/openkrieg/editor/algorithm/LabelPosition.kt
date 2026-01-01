package com.openkrieg.editor.algorithm

import com.monst.polylabel.PolyLabel
import com.openkrieg.editor.constant.Constants
import com.openkrieg.editor.util.ImageUtil
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage

class LabelPosition(private val base: BufferedImage, private val regionSeeds: Set<Point>, private val precision: Double) {

	private var mInnerPointMap: HashMap<Point, HashSet<Point>>? = null
	private var mLabelPosition: Point? = null

	fun canLabelFit(label: String, font: Font): Boolean {
		return testLabelFit(base, mInnerPointMap ?: createInnerPointMap(regionSeeds.toMutableSet()), mLabelPosition ?: calculatePosition(), label, font)
	}

	fun calculatePosition(): Point { // TODO: Return mLabelPosition if it's not null
		val innerPointMap = mInnerPointMap ?: createInnerPointMap(regionSeeds.toMutableSet())
		val shape = createShape(createOutlinePointMap(innerPointMap))

		val polyPoint = PolyLabel.polyLabel(shape, precision)
		val result = Point(polyPoint.x.toInt(), polyPoint.y.toInt())
		mLabelPosition = result
		return result
	}

	private fun createInnerPointMap(seedPoints: MutableSet<Point>): HashMap<Point, HashSet<Point>> { // Get the inner point map
		val innerPointMap = HashMap<Point, HashSet<Point>>()

		val temp = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB) // TODO: Change this so image isn't needed
		val g2d = temp.createGraphics()
		g2d.drawImage(base, 0, 0, null)
		g2d.dispose()

		for (seedPoint in seedPoints) {
			val innerPoints = HashSet<Point>()
			val filler = InnerPointFiller(temp)
			filler.fill(seedPoint, Constants.SELECT_COLOR) // TODO: Change this so it doesn't actually need to fill?
			innerPoints.addAll(filler.allPoints)
			innerPointMap[seedPoint] = innerPoints
		}

		mInnerPointMap = innerPointMap
		return innerPointMap
	}

	private fun createOutlinePointMap(innerPointMap: HashMap<Point, HashSet<Point>>): HashMap<Point, HashSet<Point>> { // Get the outline point map
		val outlinePointMap = HashMap<Point, HashSet<Point>>()
		for ((seed, innerPoints) in innerPointMap) {
			val outlinePoints = getOutlinePoints(innerPoints)
			outlinePointMap[seed] = outlinePoints
		}
		return outlinePointMap
	}

	private fun createShape(outlinePointMap: HashMap<Point, HashSet<Point>>): Array<Array<Array<Number>>> { // Form the data to be compatible with PolyLabel
		val shape: MutableList<Array<Array<Number>>> = ArrayList()

		for ((_, outlinePoints) in outlinePointMap) {

			// Sort the coordinates so they link up like a chain
			// TODO: This sorting algorithm seems to sometimes cut lines across the shape, so might need to be changed eventually. Works well enough for now.
			val sortedOutlinePoints = sortByNearest(outlinePoints)

			val pointList: ArrayList<Array<Number>> = ArrayList()
			for (point in sortedOutlinePoints) {
				val pointAsArray: Array<Number> = arrayOf(point.x, point.y)
				pointList.add(pointAsArray)
			}

			val firstPoint = sortedOutlinePoints.first() // Have to add the first point to the end to make it a ring
			val firstPointAsArray: Array<Number> = arrayOf(firstPoint.x, firstPoint.y)
			pointList.add(firstPointAsArray)

			shape.add(pointList.toTypedArray())
		}

		return shape.toTypedArray()
	}

	/* Utilities for the above methods */

	private fun getOutlinePoints(innerPoints: HashSet<Point>): HashSet<Point> { // Get the outline points for just one region
		val outlinePoints = HashSet<Point>()

		for (y in 0 until base.height) {
			for (x in 0 until base.width) {

				if (innerPoints.contains(Point(x, y))) {

					val north = y - 1
					val south = y + 1
					val west = x - 1
					val east = x + 1

					if (north >= 0 && !innerPoints.contains(Point(x, north)) && !outlinePoints.contains(Point(x, north))) {
						outlinePoints.add(Point(x, north))
					} else if (south < base.height && !innerPoints.contains(Point(x, south)) && !outlinePoints.contains(Point(x, south))) {
						outlinePoints.add(Point(x, south))
					} else if (west >= 0 && !innerPoints.contains(Point(west, y)) && !outlinePoints.contains(Point(west, y))) {
						outlinePoints.add(Point(west, y))
					} else if (east < base.width && !innerPoints.contains(Point(east, y)) && !outlinePoints.contains(Point(east, y))) {
						outlinePoints.add(Point(east, y))
					}

				}

			}
		}
		return outlinePoints
	}

	private fun sortByNearest(points: HashSet<Point>): List<Point> { // Sort the points by the ones nearest to them, making a chain
		val startingList = points.toMutableList()
		val result = mutableListOf(startingList.removeAt(0))

		while (startingList.size > 0) {
			val nearestIndex = findNearestIndex(result[result.size - 1], startingList)
			result.add(startingList.removeAt(nearestIndex))
		}

		return result.toList()
	}

	private fun findNearestIndex(point: Point, points: List<Point>): Int { // Find the index of the point closest to the given point
		var nearestDistance = Integer.MAX_VALUE
		var nearestIndex = 0

		for (i in points.indices) {
			val p = points[i]
			val d = (point.x - p.x) * (point.x - p.x) + (point.y - p.y) * (point.y - p.y)
			if (d < nearestDistance) {
				nearestDistance = d
				nearestIndex = i
			}
		}
		return nearestIndex
	}

	// New

	private fun testLabelFit(base: BufferedImage, innerPointMap: HashMap<Point, HashSet<Point>>, labelPosition: Point, label: String, font: Font): Boolean {
		val temp = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
		val g2d = temp.createGraphics()

		// Fill in the territory
		for ((_, innerPoints) in innerPointMap) {
			for (point in innerPoints) {
				temp.setRGB(point.x, point.y, Color.BLACK.rgb)
			}
		}

		// Draw the label
		g2d.paint = Color.PINK
		ImageUtil.drawCenteredString(g2d, label.trim(), Rectangle(labelPosition.x, labelPosition.y, 1, 1), font)
		g2d.dispose()

		// Fill the territory areas in with transparency
		for ((_, innerPoints) in innerPointMap) {
			for (point in innerPoints) {
				temp.setRGB(point.x, point.y, Color(0, 0, 0, 0).rgb)
			}
		}

		// Check if any pixels are not transparent. If there are any, the label doesn't fit.
		for (y in 0 until temp.height) {
			for (x in 0 until temp.width) {
				if (temp.getRGB(x, y) != Color(0, 0, 0, 0).rgb) {
					return false
				}
			}
		}

		return true
	}

}