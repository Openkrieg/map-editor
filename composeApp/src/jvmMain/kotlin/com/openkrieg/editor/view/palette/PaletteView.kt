package com.openkrieg.editor.view.palette

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.openkrieg.editor.viewmodel.PaletteViewModel

@OptIn(ExperimentalFoundationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun PaletteView(model: PaletteViewModel, modifier: Modifier) {
	Box(modifier = modifier) {
		Row(modifier = Modifier.fillMaxSize()) {
			PaletteSidebarView(model, Modifier.fillMaxHeight().weight(1.0f))
			PaletteMapPreview(model, Modifier.fillMaxHeight().weight(5.0f))
		}
	}
}