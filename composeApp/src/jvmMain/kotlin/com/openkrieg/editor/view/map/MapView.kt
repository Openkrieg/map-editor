package com.openkrieg.editor.view.map

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import java.awt.Point
import kotlin.math.pow

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MapView(model: MapViewModel, modifier: Modifier) {
	Column(modifier = Modifier.background(color = ViewColor.UI_BACKGROUND_DARK)) {
		Row(modifier = Modifier.weight(1f)) {
			MapSidebarView(model, modifier = Modifier.fillMaxHeight().width(180.dp))
			Column(Modifier.weight(1f)) {
				MapViewport(model, modifier)
			}
		}
		MapFooterView(
			model, Modifier.fillMaxWidth().height(25.dp)
		)
	}
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun MapViewport(model: MapViewModel, modifier: Modifier) {
	val stateVertical = rememberScrollState(0)
	val stateHorizontal = rememberScrollState(0)
	val scope = rememberCoroutineScope()

	var pointerPos: Offset by remember { mutableStateOf(Offset(0f, 0f)) }
	var scale: Float by remember { mutableStateOf(1.0f) }
	var canZoom: Boolean by remember { mutableStateOf(false) }

	val maxScale = 3.0f
	val minScale = 0.5f

	val rawBaseLayer = model.mapImage()
	val rawTextLayer = model.textImage()

	val baseBitmap = remember(rawBaseLayer) { rawBaseLayer.toComposeImageBitmap() }
	val textBitmap = remember(rawTextLayer) { rawTextLayer.toComposeImageBitmap() }

	val highQualityPaint = remember { Paint().apply { filterQuality = FilterQuality.High } }

	val currentMapX = (pointerPos.x / scale).toInt()
	val currentMapY = (pointerPos.y / scale).toInt()

	SideEffect {
		model.mousePosition = Point(currentMapX, currentMapY)
	}

	val focusRequester = remember(::FocusRequester)
	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Box(modifier = modifier.background(color = ViewColor.UI_BACKGROUND_DARK)) {
		Box(
			modifier = Modifier.fillMaxSize().verticalScroll(stateVertical).horizontalScroll(stateHorizontal)
		) {
			Canvas(
				modifier = Modifier.width((rawBaseLayer.width * scale).dp).height((rawBaseLayer.height * scale).dp).align(Alignment.Center).focusable(true)
				.focusRequester(focusRequester).focusTarget().pointerInput(Unit) {
					awaitPointerEventScope {
						while (true) {
							val event = awaitPointerEvent()
							val changes = event.changes.first()
							val position = changes.position

							if (event.type == PointerEventType.Move) {
								pointerPos = position
							}

							if (event.type == PointerEventType.Scroll && canZoom) {
								val scrollDelta = changes.scrollDelta.y

								val zoomFactor = 1.05f.pow(-scrollDelta)
								val oldScale = scale
								val newScale = (oldScale * zoomFactor).coerceIn(minScale, maxScale)

								if (newScale != oldScale) {
									val actualFactor = newScale / oldScale
									val xShift = position.x * (actualFactor - 1)
									val yShift = position.y * (actualFactor - 1)

									scale = newScale

									scope.launch {
										stateHorizontal.dispatchRawDelta(xShift)
										stateVertical.dispatchRawDelta(yShift)
									}
								}
								changes.consume()
							}
						}
					}
				}.onKeyEvent { keyEvent ->
					canZoom = keyEvent.isCtrlPressed
					if (keyEvent.isCtrlPressed && keyEvent.key == Key.Zero) {
						scale = 1.0f
					}
					false
				}.combinedClickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = {
					focusRequester.requestFocus()
					model.interact()
				})
			) {
				drawIntoCanvas { canvas ->
					canvas.scale(scale, scale, 0f, 0f)

					canvas.drawImageRect(image = baseBitmap, paint = highQualityPaint)
					canvas.drawImageRect(image = textBitmap, paint = highQualityPaint)
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