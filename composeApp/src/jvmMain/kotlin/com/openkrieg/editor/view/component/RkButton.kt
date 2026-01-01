package com.openkrieg.editor.view.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.openkrieg.editor.constant.ViewColor

@Composable
fun RkButton(
	onClick: () -> Unit, modifier: Modifier = Modifier.padding(horizontal = 10.dp), enabled: Boolean = true, colors: ButtonColors = ButtonDefaults.buttonColors(
		backgroundColor = ViewColor.BROWN,
		contentColor = Color.White,
		disabledBackgroundColor = ViewColor.UI_BUTTON_DISABLED,
		disabledContentColor = ViewColor.UI_TEXT_ON_DARK_DISABLED.copy(alpha = ContentAlpha.disabled)
	), content: @Composable RowScope.() -> Unit
) {
	Button(
		onClick = onClick, modifier = modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(4.dp), enabled = enabled, colors = colors, content = content
	)
}