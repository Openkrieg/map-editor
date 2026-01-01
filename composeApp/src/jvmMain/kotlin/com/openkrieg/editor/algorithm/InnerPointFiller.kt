package com.openkrieg.editor.algorithm

import io.github.aaronjyoder.fill.Filler
import io.github.aaronjyoder.fill.util.FillUtil
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

class InnerPointFiller(private var image: BufferedImage) : Filler {

	val allPoints: HashSet<Point> = HashSet()

	override fun fill(x: Int, y: Int, fill: Color) {
		if (!FillUtil.isBounded(x, y, image.width, image.height)) {
			return
		}
		val originalRGB: Int = image.getRGB(x, y)
		if (originalRGB == fill.rgb) {
			return
		}

		if (canPaint(x, y, originalRGB)) {
			fillStart(x, y, originalRGB, fill.rgb, image.width, image.height)
		}
	}

	private fun fillStart(x: Int, y: Int, originalRGB: Int, fillRGB: Int, width: Int, height: Int) {
		var mx = x
		var my = y
		while (true) {
			val xPrev = mx
			val yPrev = my
			while (my != 0 && canPaint(mx, my - 1, originalRGB)) {
				my--
			}
			while (mx != 0 && canPaint(mx - 1, my, originalRGB)) {
				mx--
			}
			if (mx == xPrev && my == yPrev) {
				break
			}
		}

		fillCore(x, y, originalRGB, fillRGB, width, height)
	}

	private fun fillCore(x: Int, y: Int, originalRGB: Int, fillRGB: Int, width: Int, height: Int) {
		var mx = x
		var my = y
		var prevRowLength = 0

		do {
			var rowLength = 0
			var xStart = mx

			if (prevRowLength != 0 && !canPaint(mx, my, originalRGB)) {
				do {
					if (--prevRowLength == 0) return
				} while (!canPaint(++mx, my, originalRGB))
				xStart = mx
			} else {
				while (mx != 0 && canPaint(mx - 1, my, originalRGB)) {
					setRGB(--mx, my, fillRGB, image)
					if (my != 0 && canPaint(mx, my - 1, originalRGB)) {
						fillStart(mx, my - 1, originalRGB, fillRGB, width, height)
					}
					rowLength++
					prevRowLength++
				}
			}

			while (xStart < width && canPaint(xStart, my, originalRGB)) {
				setRGB(xStart, my, fillRGB, image)
				rowLength++
				xStart++
			}

			if (rowLength < prevRowLength) {
				val prevRowEnd = mx + prevRowLength
				while (++xStart < prevRowEnd) {
					if (canPaint(xStart, my, originalRGB)) fillCore(xStart, my, originalRGB, fillRGB, width, height)
				}
			} else if (rowLength > prevRowLength && my != 0) {
				var xUpper = mx + prevRowLength
				while (++xUpper < xStart) {
					if (canPaint(xUpper, my - 1, originalRGB)) fillStart(xUpper, my - 1, originalRGB, fillRGB, width, height)
				}
			}
			prevRowLength = rowLength
		} while (prevRowLength != 0 && ++my < height)
	}

	private fun canPaint(x: Int, y: Int, originalRGB: Int): Boolean {
		return image.getRGB(x, y) == originalRGB
	}

	private fun setRGB(x: Int, y: Int, fillRGB: Int, image: BufferedImage) {
		image.setRGB(x, y, fillRGB)
		allPoints.add(Point(x, y))
	}

}