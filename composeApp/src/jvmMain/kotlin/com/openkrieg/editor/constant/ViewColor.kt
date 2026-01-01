package com.openkrieg.editor.constant

import androidx.compose.ui.graphics.Color
import com.riskrieg.palette.RkpPalette


object ViewColor {

	var UI_BACKGROUND_DARK = Color(48, 48, 48)
	var UI_BACKGROUND_LIGHT = Color(255, 255, 255)

	var UI_BACKGROUND_DARK_ON_DARK = Color(69, 69, 69)

	var UI_HOVER_HIGHLIGHT_DARK = Color(20, 20, 20, 0xB4)
	var UI_HOVER_HIGHLIGHT_LIGHT = Color(0, 0, 0, 0xB4)

	var UI_FOOTER_DARK = Color(32, 32, 32)
	var UI_FOOTER_LIGHT = Color(255, 255, 255)

	var UI_TEXT_ON_DARK = Color(255, 255, 255)
	var UI_TEXT_ON_LIGHT = Color(0, 0, 0)

	var UI_TEXT_ON_DARK_DISABLED = Color(128, 128, 128)

	var UI_BUTTON_DISABLED = Color(60, 60, 60)

	val BROWN = Color(RkpPalette.DEFAULT_BORDER_COLOR.toAwtColor().rgb)
	val BEIGE = Color(RkpPalette.DEFAULT_TERRITORY_COLOR.toAwtColor().rgb)
	val GREY = Color(54, 54, 54)

	//    val ORANGE = Color(180, 112, 54)

	val RED = Color(190, 54, 54)
	val GREEN = Color(54, 190, 54)
	val BLUE = Color(54, 112, 180)
	val ORANGE = Color(190, 98, 54)

	val SUCCESS_COLOR = Color(54, 190, 54)
	val ERROR_COLOR = Color(190, 54, 54)

}