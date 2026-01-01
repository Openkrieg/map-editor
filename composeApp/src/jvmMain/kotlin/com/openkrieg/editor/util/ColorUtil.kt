package com.openkrieg.editor.util

import androidx.compose.ui.graphics.Color
import com.openkrieg.editor.constant.ViewColor
import kotlin.math.pow

object ColorUtil {

	fun getTextLightOrDark(color: Color): Color {
		return getLightOrDark(color, ViewColor.UI_TEXT_ON_DARK, ViewColor.UI_TEXT_ON_LIGHT)
	}

	fun getLightOrDark(color: Color, light: Color, dark: Color): Color {
		return if (getLuminance(color) > 0.5) dark else light
	}

	fun getComplement(color: Color): Color {
		val r: Int = (color.red * 255).toInt()
		val g: Int = (color.green * 255).toInt()
		val b: Int = (color.blue * 255).toInt()
		val rp = 255 - r
		val gp = 255 - g
		val bp = 255 - b
		return Color(rp, gp, bp)
	}

	fun getContrastRatio(c1: Color, c2: Color): Double {
		val l1 = getLuminance(c1)
		val l2 = getLuminance(c2)
		return if (l1 > l2) (l1 + 0.05) / (l2 + 0.05) else (l2 + 0.05) / (l1 + 0.05)
	}

	fun getLuminance(color: Color): Double {
		val r: Int = (color.red * 255).toInt()
		val g: Int = (color.green * 255).toInt()
		val b: Int = (color.blue * 255).toInt()
		val rg: Double = if (r <= 10) r / 3294.0 else (r / 269.0 + 0.0513).pow(2.4)
		val gg: Double = if (g <= 10) g / 3294.0 else (g / 269.0 + 0.0513).pow(2.4)
		val bg: Double = if (b <= 10) b / 3294.0 else (b / 269.0 + 0.0513).pow(2.4)

		return (0.2126 * rg + 0.7152 * gg + 0.0722 * bg)
	}

}