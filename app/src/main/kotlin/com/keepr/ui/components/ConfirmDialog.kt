package com.keepr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceMid)
                .padding(24.dp)
        ) {
            Text(text = title, style = KeeprTypography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = KeeprTypography.bodyMedium.copy(color = TextSecondary)
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = cancelText,
                        style = KeeprTypography.labelLarge.copy(color = TextSecondary)
                    )
                }
                TextButton(onClick = onConfirm) {
                    Text(
                        text = confirmText,
                        style = KeeprTypography.labelLarge.copy(
                            color = if (destructive) ErrorColor else com.keepr.ui.theme.AccentPurple
                        )
                    )
                }
            }
        }
    }
}
