package com.keepr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.BorderFocus
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextPrimary
import com.keepr.ui.theme.TextTertiary

@Composable
fun KeeprTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    error: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    readOnly: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = KeeprTypography.labelMedium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceMid, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = when {
                        error != null -> ErrorColor.copy(alpha = 0.6f)
                        focused -> BorderFocus.copy(alpha = 0.7f)
                        else -> BorderDefault
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .let { if (trailingContent != null) it.padding(end = 40.dp) else it }
                    .onFocusChanged { focused = it.isFocused },
                textStyle = KeeprTypography.bodyLarge.copy(color = TextPrimary),
                cursorBrush = SolidColor(BorderFocus),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                maxLines = maxLines,
                minLines = minLines,
                readOnly = readOnly,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = KeeprTypography.bodyLarge.copy(color = TextTertiary)
                        )
                    }
                    inner()
                }
            )

            if (trailingContent != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    trailingContent()
                }
            }
        }

        AnimatedVisibility(visible = error != null) {
            Text(
                text = error ?: "",
                style = KeeprTypography.bodySmall.copy(color = ErrorColor),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
