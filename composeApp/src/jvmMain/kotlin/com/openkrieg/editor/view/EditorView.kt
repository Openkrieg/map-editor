package com.openkrieg.editor.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.view.map.MapView
import com.openkrieg.editor.view.palette.PaletteView
import com.openkrieg.editor.viewmodel.EditorViewModel
import com.openkrieg.editor.viewmodel.internal.EditorType

class EditorView(val model: EditorViewModel) {

	@Composable
	fun build() {
//        val themeColors = if (isSystemInDarkTheme()) {
//            darkColors()
//        } else {
//            lightColors()
//        }
		MaterialTheme(colors = darkColors()) { // TODO: Support both dark and light themes
			mainView()
		}
	}

	@Composable
	fun mainView() {
		when (model.editorType) {
			EditorType.RKM_MAP -> MapView(model.mapViewModel, Modifier.fillMaxSize())
			EditorType.RKP_PALETTE -> PaletteView(model.paletteViewModel, Modifier.fillMaxSize())
			else -> DefaultEditorView()
		}
	}

	@Composable
	fun DefaultEditorView() {
		Box(
			modifier = Modifier.fillMaxSize().background(color = if (model.isDragAndDropping) ViewColor.UI_HOVER_HIGHLIGHT_DARK else ViewColor.UI_BACKGROUND_DARK)
		) {
			Text(
				"Drag a map file (.rkm) or palette file (.rkp) into this window in order to get started." + "\n\n" + "You can also go to File -> New... to get started.",
				fontStyle = FontStyle.Italic,
				textAlign = TextAlign.Center,
				modifier = Modifier.align(Alignment.Center),
				color = ViewColor.UI_TEXT_ON_DARK
			)
		}
	}

}