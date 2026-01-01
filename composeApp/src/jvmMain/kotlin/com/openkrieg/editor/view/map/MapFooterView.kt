package com.openkrieg.editor.view.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openkrieg.editor.constant.ViewColor
import com.openkrieg.editor.viewmodel.MapViewModel

@Composable
fun MapFooterView(model: MapViewModel, modifier: Modifier) {
	Row(
		modifier = modifier.background(color = ViewColor.UI_FOOTER_DARK)
	) {
		Text(
			modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 4.dp),
			text = "Left click to select/deselect regions or territories.",
			fontSize = 12.sp,
			textAlign = TextAlign.Start,
			color = ViewColor.UI_TEXT_ON_DARK
		)
		Text(
			modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically).padding(horizontal = 4.dp),
			text = "Mouse: (${model.mousePosition.x}, ${model.mousePosition.y})  |   Size: ${model.mapImage().width}x${model.mapImage().height}",
			fontSize = 12.sp,
			textAlign = TextAlign.End,
			color = ViewColor.UI_TEXT_ON_DARK
		)
	}
}