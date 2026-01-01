package com.openkrieg.editor.view.palette

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.util.ColorUtil
import com.openkrieg.editor.view.component.RkButton
import com.openkrieg.editor.view.component.RkTextField
import com.openkrieg.editor.viewmodel.PaletteViewModel
import com.riskrieg.palette.RkpColor
import com.riskrieg.palette.RkpPalette

@Composable
fun PaletteSidebarView(model: PaletteViewModel, modifier: Modifier) {
	var selected by remember { mutableStateOf(false) }
	var selectedIndex by remember { mutableStateOf(-1) }

	val onClickListItem = { index: Int ->
		if (selected && selectedIndex == index) {
			// Deselect
			selected = false
			selectedIndex = -1
			model.deselectActiveColor()
		} else {
			// Select
			selectedIndex = index
			selected = true
			model.selectActiveColor(model.colorSet.toList()[index])
			model.newColorName = model.activeColorName()
			model.newColorHexString = model.activeColorHexString()
		}
	}

	val onMoveSelection = { movedUp: Boolean ->
		if (movedUp) {
			model.moveSelectedColorUp()
			selectedIndex -= 1
		} else {
			model.moveSelectedColorDown()
			selectedIndex += 1
		}
	}

	val onUpdate = {
		model.updateSelectedColor()
	}

	val onAdd = {
		// Add
		model.addNewColor()

		// Hack to update lazy list

		// Select
		selectedIndex = model.colorSet.size - 1
		selected = true
		model.selectActiveColor(model.colorSet.toList()[model.colorSet.size - 1])

		// Deselect
		selected = false
		selectedIndex = -1
		model.deselectActiveColor()
	}

	val onDelete = {
		model.removeSelectedColor()

		// Deselect
		selected = false
		selectedIndex = -1
		model.deselectActiveColor()
	}

	Box(modifier = modifier.background(color = ViewColor.UI_BACKGROUND_DARK)) {
		Column {
			ColorEditorView(
				modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 0.dp), model = model
			)
			AdjustmentButtonsView(
				modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 0.dp),
				selected = selected,
				index = selectedIndex,
				maxIndex = model.colorSet.size - 1,
				onMoveSelection = onMoveSelection,
				canUpdate = selected && model.isNewColorNameValid() && model.isNewColorHexStringValid() && (model.newColorName != model.activeColorName() || model.newColorHexString != model.activeColorHexString()) && !model.colorSetContainsHexColor(),
				canAdd = !selected && !model.colorSetContainsNewColor() && model.isNewColorNameValid() && model.isNewColorHexStringValid(),
				canDelete = selected,
				onUpdate = onUpdate,
				onAdd = onAdd,
				onDelete = onDelete
			)
			SelectableColorListView(
				modifier = Modifier.fillMaxHeight().padding(8.dp),
				list = model.colorSet.toList(),
				selected = { i -> selectedIndex == i && selected },
				onClickItem = onClickListItem
			)
		}
	}
}

@Composable
private fun ColorEditorView(
	modifier: Modifier, model: PaletteViewModel
) {
	Box(modifier = modifier) {
		Column {
			// TextField to enter palette name
			RkTextField(
				modifier = Modifier.padding(horizontal = 10.dp), value = model.paletteName, label = "Palette Name", singleLine = true, onValueChange = {
					model.paletteName = it
				})

			Spacer(modifier = Modifier.height(5.dp))

			// TextField to enter color name
			RkTextField(
				modifier = Modifier.padding(horizontal = 10.dp), value = model.newColorName, label = "Color Name", singleLine = true, onValueChange = {
					model.newColorName = it
				})

			Spacer(modifier = Modifier.height(5.dp))

			// TextField to enter color
			// TODO: Replace this with a color picker at some point
			RkTextField(
				modifier = Modifier.padding(horizontal = 10.dp), value = model.newColorHexString, label = "Color (Hex Code)", singleLine = true, onValueChange = {
					model.newColorHexString = it.uppercase().trim()
				})

			Spacer(modifier = Modifier.height(5.dp))
		}
	}
}

@Composable
private fun AdjustmentButtonsView(
	modifier: Modifier,
	selected: Boolean,
	index: Int,
	maxIndex: Int,
	onMoveSelection: (Boolean) -> Unit,
	canUpdate: Boolean,
	canAdd: Boolean,
	canDelete: Boolean,
	onUpdate: () -> Unit,
	onAdd: () -> Unit,
	onDelete: () -> Unit,
) {
	Box(modifier = modifier) {
		Column {
			RkButton(
				onClick = {
					if (canUpdate) { // Update
						onUpdate.invoke()
					} else if (canAdd) { // Add
						onAdd.invoke()
					} else if (canDelete) { // Delete
						onDelete.invoke()
					}
				},
				modifier = Modifier.padding(start = 0.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
				enabled = canUpdate || canAdd || canDelete,
				colors = ButtonDefaults.buttonColors(
					backgroundColor = if (canUpdate) { // Update
						ViewColor.ORANGE
					} else if (canAdd) { // Add
						ViewColor.GREEN
					} else if (canDelete) { // Delete
						ViewColor.RED
					} else {
						ViewColor.GREEN
					},
					contentColor = Color.White,
					disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
					disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
				)
			) {
				if (canUpdate) { // Update
					Text("Update", fontSize = 14.sp)
				} else if (canAdd) { // Add
					Text("Add", fontSize = 14.sp)
				} else if (canDelete) { // Delete
					Text("Delete", fontSize = 14.sp)
				} else {
					Text("Add", fontSize = 14.sp)
				}
			}

			Row {

				RkButton(
					onClick = {
						onMoveSelection.invoke(true)
					},
					modifier = Modifier.weight(1.0F).padding(start = 0.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
					enabled = selected && index > 0,
					colors = ButtonDefaults.buttonColors(
						backgroundColor = ViewColor.ORANGE,
						contentColor = Color.White,
						disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
						disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
					)
				) {
					Text("Up", fontSize = 14.sp)
				}

				RkButton(
					onClick = {
						onMoveSelection.invoke(false)
					},
					modifier = Modifier.weight(1.0F).padding(start = 2.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
					enabled = selected && index < maxIndex,
					colors = ButtonDefaults.buttonColors(
						backgroundColor = ViewColor.ORANGE,
						contentColor = Color.White,
						disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
						disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
					)
				) {
					Text("Down", fontSize = 14.sp)
				}

			}
		}
	}
}

@Composable
private fun SelectableColorListView(
	modifier: Modifier, list: List<RkpColor>, selected: (Int) -> Boolean, onClickItem: (Int) -> Unit
) {
	val listState = rememberLazyListState()

	Box(
		modifier = modifier.background(
			color = ViewColor.UI_BACKGROUND_DARK_ON_DARK, shape = RoundedCornerShape(4.dp)
		)
	) {
		LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 16.dp), listState) {
			items(list.size) { i ->
				val rkpColor = list[i]
				SelectableColorListItem(
					text = rkpColor.name, backgroundColor = Color(rkpColor.r, rkpColor.g, rkpColor.b), index = i, selected = selected(i), onClick = onClickItem
				)
				Spacer(modifier = Modifier.height(6.dp))
			}
		}
		VerticalScrollbar(
			modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 4.dp), adapter = rememberScrollbarAdapter(scrollState = listState)
		)
	}

}

@Composable
private fun SelectableColorListItem(
	text: String = "[ITEM]", backgroundColor: Color, index: Int, selected: Boolean, onClick: (Int) -> Unit
) {
	val borderStroke = if (selected) {
		BorderStroke(4.dp, ColorUtil.getTextLightOrDark(backgroundColor))
	} else {
		BorderStroke(0.dp, Color.Transparent)
	}
	Box( // TODO: Border color leaks out the corners
		modifier = Modifier.height(48.dp).fillMaxWidth().background(
				color = backgroundColor, shape = if (selected) RoundedCornerShape(6.dp) else RoundedCornerShape(4.dp)
			) // Hacky workaround for color leaking from corners
			.border(border = borderStroke, shape = RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp))
			.selectable(selected = selected, onClick = { onClick.invoke(index) }), contentAlignment = Alignment.Center
	) {
		Text(
			modifier = Modifier.padding(start = 10.dp, end = 10.dp),
			text = text,
			fontWeight = FontWeight.SemiBold,
			fontSize = 18.sp,
			textAlign = TextAlign.Center,
			color = if (selected) ColorUtil.getTextLightOrDark(backgroundColor) else Color(
				RkpPalette.DEFAULT_TEXT_COLOR.toAwtColor().red, RkpPalette.DEFAULT_TEXT_COLOR.toAwtColor().green, RkpPalette.DEFAULT_TEXT_COLOR.toAwtColor().blue
			)
		)
	}
}