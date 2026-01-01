package com.openkrieg.editor.view.palette

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.viewmodel.PaletteViewModel
import java.awt.Point
import java.awt.image.BufferedImage

// TODO: Allow users to use selected color to fill in territories (high priority)
// TODO: Allow users to use their own maps in the palette editor (low priority)

@Composable
fun PaletteMapPreview(model: PaletteViewModel, modifier: Modifier) {
	Box(modifier = modifier.background(color = ViewColor.UI_BACKGROUND_DARK)) {
		val map = model.paletteMap

		if (map != null) {
			MapViewport(
				modifier = Modifier.fillMaxSize().padding(20.dp).clip(RoundedCornerShape(4.dp)).clipToBounds(),
				baseLayer = model.paletteMapColoredBaseLayer(),
				textLayer = map.textLayer,
				model = model
			)
		} else {
			Box(
				modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
			) {
				Text("Loading Map...", color = Color.White)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun MapViewport(modifier: Modifier, baseLayer: BufferedImage, textLayer: BufferedImage, model: PaletteViewModel) {
	val stateVertical = rememberScrollState(0)
	val stateHorizontal = rememberScrollState(0)

	var pointerPos: Offset by remember { mutableStateOf(Offset(0f, 0f)) }

	val focusRequester = remember(::FocusRequester)
	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Box(modifier = modifier.background(color = ViewColor.UI_BACKGROUND_DARK)) {
		Box(
			modifier = Modifier.fillMaxSize().verticalScroll(stateVertical).horizontalScroll(stateHorizontal)
		) {
			Canvas(
				modifier = Modifier.width((baseLayer.width).dp).height((baseLayer.height).dp).align(Alignment.Center).focusable(true)
				.focusRequester(focusRequester).focusTarget().pointerMoveFilter(onMove = { pointerPos = it; false }, onExit = { pointerPos = Offset(0f, 0f); false })
				.combinedClickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = {
					focusRequester.requestFocus()
					model.interact()
				})
			) {
				drawIntoCanvas { canvas ->
					model.mousePosition = Point((pointerPos.x).toInt(), (pointerPos.y).toInt())
					canvas.drawImageRect(
						image = baseLayer.toComposeImageBitmap(), paint = Paint().apply { filterQuality = FilterQuality.High })
					canvas.drawImageRect(
						image = textLayer.toComposeImageBitmap(), paint = Paint().apply { filterQuality = FilterQuality.High })
				}
			}
		}
		VerticalScrollbar(
			modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd), adapter = rememberScrollbarAdapter(stateVertical)
		)
		HorizontalScrollbar(
			modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(end = 12.dp), adapter = rememberScrollbarAdapter(stateHorizontal)
		)
	}
}