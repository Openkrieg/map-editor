package com.openkrieg.editor.view.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.openkrieg.editor.constant.ViewColor

@Composable
fun RkTextField(
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	value: String,
	label: String? = null,
	singleLine: Boolean = false,
	isError: Boolean = false,
	onValueChange: (String) -> Unit
) {
	val textSelectionColors = TextSelectionColors(
		handleColor = ViewColor.BROWN, backgroundColor = ViewColor.BROWN.copy(alpha = 0.4F)
	)
	CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
		TextField(
			modifier = modifier.clip(RoundedCornerShape(4.dp)),
			enabled = enabled,
			value = value,
			singleLine = singleLine,
			onValueChange = onValueChange,
			isError = isError,
			label = if (label.isNullOrBlank()) null else {
				{ Text(label) }
			},
			shape = RoundedCornerShape(4.dp),
			colors = TextFieldDefaults.textFieldColors(
				cursorColor = ViewColor.BROWN,
				focusedLabelColor = ViewColor.BROWN,
				unfocusedLabelColor = ViewColor.GREY.copy(alpha = 0.65F),
				backgroundColor = ViewColor.BEIGE,
				textColor = ViewColor.UI_TEXT_ON_LIGHT,
				errorCursorColor = ViewColor.ERROR_COLOR,
				errorLabelColor = ViewColor.ERROR_COLOR,
				errorLeadingIconColor = ViewColor.ERROR_COLOR,
				errorTrailingIconColor = ViewColor.ERROR_COLOR,
				focusedIndicatorColor = ViewColor.BROWN,
				unfocusedIndicatorColor = Color.Transparent,
				disabledIndicatorColor = Color.Transparent,
				errorIndicatorColor = Color.Transparent,
			))
	}

}