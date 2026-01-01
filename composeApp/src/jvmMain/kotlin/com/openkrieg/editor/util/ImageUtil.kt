package com.openkrieg.editor.util

import java.awt.*
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.text.AttributedString
import kotlin.math.sqrt


object ImageUtil {
	/**
	 * This function uses a fill algorithm to get the relevant region, and then loops through the region to find the point closest to (0, 0).
	 * This is done in order to get a consistent pixel result for each region so that if you click any pixel in the region, the algorithm can tell which region it is.
	 */
	fun getRootPixel(image: BufferedImage, point: Point): Point {
		val origin = Point(0, 0)
		var result = Point(point.x, point.y)
		var shortestDistance = distance(point, origin)

		val check = MilazzoCheck(image, point)

		val array = check.array()
		for (x in array.indices) {
			for (y in array[x].indices) {
				if (array[x][y]) {
					val distanceToOrigin = distance(Point(x, y), origin)
					if (distanceToOrigin < shortestDistance) {
						shortestDistance = distanceToOrigin
						result = Point(x, y)
					}
				}
			}
		}

		return result
	}

	private fun distance(one: Point, two: Point): Double { // Used in getRootPixel
		val dx = two.x - one.x
		val dy = two.y - one.y
		return sqrt((dx * dx + dy * dy).toDouble())
	}

	fun drawCenteredString(g: Graphics2D, text: String, rect: Rectangle, font: Font) {
		g.font = font
		val metrics: FontMetrics = g.getFontMetrics(font)
		val x: Int = rect.x + (rect.width - metrics.stringWidth(text)) / 2
		val y: Int = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
		val result = AttributedString(text)
		result.addAttribute(TextAttribute.FONT, font, 0, text.length)
		g.drawString(result.iterator, x, y)
	}

	fun convert(image: BufferedImage, imageType: Int): BufferedImage {
		val result = BufferedImage(image.width, image.height, imageType)
		val g = result.graphics
		g.drawImage(image, 0, 0, null)
		return result
	}

	fun createCopy(source: BufferedImage): BufferedImage {
		val result = BufferedImage(source.width, source.height, source.type)
		val g2 = result.createGraphics()
		g2.drawImage(source, 0, 0, null)
		g2.dispose()
		return result
	}

	fun createCopy(source: BufferedImage, imageType: Int): BufferedImage {
		val result = BufferedImage(source.width, source.height, imageType)
		val g2 = result.createGraphics()
		g2.drawImage(source, 0, 0, null)
		g2.dispose()
		return result
	}

}

/**
 * The purpose of this class is to use the Milazzo bucket fill algorithm to return a 2D boolean array of all pixels that are part of the region.
 * The (x, y) coordinate in the array is set to "true" if it is a pixel that is part of the fill region.
 */
private class MilazzoCheck(private var image: BufferedImage, private val start: Point) {

	private val width: Int = image.width
	private val height: Int = image.height
	private val visited = Array(image.width) { BooleanArray(image.height) }
	private val original: Color = Color(image.getRGB(start.x, start.y))
	private val fill: Color = Color.BLACK // Doesn't matter as long as it's not the territory color

	fun array(): Array<BooleanArray> {
		fill(start)
		return visited
	}

	private fun fill(seed: Point) {
		if (original.rgb == fill.rgb) {
			return
		}
		if (canPaint(seed.x, seed.y)) {
			fill_(seed.x, seed.y)
		}
	}

	private fun fill_(x: Int, y: Int) {
		var thisX = x
		var thisY = y
		while (true) {
			val ox: Int = thisX
			val oy: Int = thisY
			while (thisY != 0 && canPaint(thisX, thisY - 1)) thisY--
			while (thisX != 0 && canPaint(thisX - 1, thisY)) thisX--
			if (thisX == ox && thisY == oy) break
		}
		fillCore(thisX, thisY)
	}

	private fun fillCore(x: Int, y: Int) {
		var thisX = x
		var thisY = y
		var lastRowLength = 0
		do {
			var rowLength = 0
			var sx: Int = thisX
			if (lastRowLength != 0 && !canPaint(thisX, thisY)) {
				do {
					if (--lastRowLength == 0) {
						return
					}
				} while (!canPaint(++thisX, thisY))
				sx = thisX
			} else {
				while (thisX != 0 && canPaint(thisX - 1, thisY)) {
					setPixel(--thisX, thisY)
					if (thisY != 0 && canPaint(thisX, thisY - 1)) {
						fill_(thisX, thisY - 1)
					}
					rowLength++
					lastRowLength++
				}
			}

			while (sx < width && canPaint(sx, thisY)) {
				setPixel(sx, thisY)
				rowLength++
				sx++
			}

			if (rowLength < lastRowLength) {
				val end = thisX + lastRowLength
				while (++sx < end) {
					if (canPaint(sx, thisY)) {
						fillCore(sx, thisY)
					}
				}
			} else if (rowLength > lastRowLength && thisY != 0) {
				var ux = thisX + lastRowLength
				while (++ux < sx) {
					if (canPaint(ux, thisY - 1)) {
						fill_(ux, thisY - 1)
					}
				}
			}
			lastRowLength = rowLength
		} while (lastRowLength != 0 && ++thisY < height)
	}

	/* Private Methods */

	private fun canPaint(x: Int, y: Int): Boolean {
		return !visited[x][y] && getPixel(x, y) == original.rgb
	}

	private fun getPixel(x: Int, y: Int): Int {
		return image.getRGB(x, y)
	}

	private fun setPixel(x: Int, y: Int) {
		visited[x][y] = true
	}

}