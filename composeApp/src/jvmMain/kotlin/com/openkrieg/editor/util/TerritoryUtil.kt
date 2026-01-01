package com.openkrieg.editor.util

import com.openkrieg.editor.algorithm.InnerPointFiller
import com.openkrieg.editor.constant.Constants
import java.awt.Point
import java.awt.image.BufferedImage

object TerritoryUtil {

	fun createInnerPointMap(seedPoints: MutableSet<Point>, base: BufferedImage): HashMap<Point, HashSet<Point>> { // Get the inner point map
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

		return innerPointMap
	}

}