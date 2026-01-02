package com.openkrieg.editor.view.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.view.component.RkButton
import com.openkrieg.editor.view.component.RkTextField
import com.openkrieg.editor.viewmodel.MapViewModel

@Composable
fun MapSidebarView(model: MapViewModel, modifier: Modifier) {
	Column(
		modifier = modifier.background(color = ViewColor.UI_BACKGROUND_DARK)
	) {
		Spacer(modifier = Modifier.height(4.dp))

		RkTextField(
			modifier = Modifier.padding(horizontal = 10.dp), value = model.mapDisplayName, label = "Map Display Name", singleLine = true, onValueChange = {
				model.mapDisplayName = it
			})

		Spacer(modifier = Modifier.height(5.dp))

		RkTextField(
			modifier = Modifier.padding(horizontal = 10.dp), value = model.mapAuthorName, label = "Author", singleLine = true, onValueChange = {
				model.mapAuthorName = it
			})

		Spacer(modifier = Modifier.height(5.dp))

		AnimatedVisibility(visible = model.isSelectingTerritory || model.isSelectingRegion) {
			Column {
				Spacer(modifier = Modifier.height(5.dp))
				Divider(modifier = Modifier.padding(horizontal = 10.dp), color = Color.LightGray, thickness = 2.dp)
				Spacer(modifier = Modifier.height(5.dp))

				RkTextField(
					modifier = Modifier.padding(horizontal = 10.dp),
					enabled = model.isSelectingRegion || model.isSelectingTerritory,
					value = model.newTerritoryName,
					label = "Territory Name",
					singleLine = true,
					onValueChange = {
						model.newTerritoryName = it.replace("[^\\p{IsAlphabetic}\\p{IsDigit}]".toRegex(), "").toUpperCase(Locale.current)
					})

				Spacer(modifier = Modifier.height(5.dp))

				RkButton(
					onClick = {
						if (model.isSelectingTerritory && model.selectedTerritoriesAreSubmitted()) {
							model.renameSelectedTerritory(alsoSubmit = true)
						} else if (model.isSelectingTerritory) {
							model.submitSelectedNeighbors()
						} else if (model.isSelectingRegion) {
							model.submitSelectedRegions(false)
						}
					}, enabled = model.isSelectingTerritory || model.isSelectingRegion, colors = ButtonDefaults.buttonColors(
						backgroundColor = ViewColor.BLUE,
						contentColor = Color.White,
						disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
						disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
					)
				) {
					if (model.isSelectingTerritory && model.selectedTerritoriesAreSubmitted()) {
						Text("Update", fontSize = 14.sp)
					} else if (model.isSelectingTerritory) {
						Text("Submit", fontSize = 14.sp)
					} else {
						Text("Add", fontSize = 14.sp)
					}
				}

				Spacer(modifier = Modifier.height(5.dp))

				if (model.isSelectingRegion && !model.selectedRegionsHaveLabel) {
					RkButton(
						onClick = {
							model.submitSelectedRegions(true)
						}, enabled = !model.selectedRegionsHaveLabel, colors = ButtonDefaults.buttonColors(
							backgroundColor = ViewColor.ORANGE,
							contentColor = Color.White,
							disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
							disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
						)
					) {
						Text("Add & Label", fontSize = 14.sp)
					}

					Spacer(modifier = Modifier.height(5.dp))
				}

				if (model.isSelectingTerritory) {
					RkButton(
						onClick = {
							if (model.selectedTerritoryHasLabel) model.clearTerritoryLabel() else model.addTerritoryLabel()
						}, enabled = model.isSelectingTerritory, colors = ButtonDefaults.buttonColors(
							backgroundColor = ViewColor.ORANGE,
							contentColor = Color.White,
							disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
							disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
						)
					) {
						Text(if (model.selectedTerritoryHasLabel) "Clear Label" else "Add Label", fontSize = 14.sp)
					}

					Spacer(modifier = Modifier.height(5.dp))

					RkButton(
						onClick = {
							model.deleteSelectedTerritory()
						}, enabled = model.isSelectingTerritory, colors = ButtonDefaults.buttonColors(
							backgroundColor = ViewColor.RED,
							contentColor = Color.White,
							disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
							disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
						)
					) {
						Text("Delete", fontSize = 14.sp)
					}

				}

			}
		}

	}
}