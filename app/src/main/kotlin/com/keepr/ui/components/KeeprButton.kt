package com.keepr.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleDim
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.ErrorColor
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.TextPrimary
import com.keepr.ui.theme.TextSecondary

@Composable
fun KeeprPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentPurple,
            contentColor = TextPrimary,
            disabledContainerColor = AccentPurple.copy(alpha = 0.3f),
            disabledContentColor = TextPrimary.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            style = KeeprTypography.labelLarge
        )
    }
}

@Composable
fun KeeprSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AccentPurple,
            disabledContentColor = AccentPurple.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) AccentPurple.copy(alpha = 0.6f) else BorderDefault
        )
    ) {
        Text(
            text = text,
            style = KeeprTypography.labelLarge
        )
    }
}

@Composable
fun KeeprDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorColor.copy(alpha = 0.15f),
            contentColor = ErrorColor,
            disabledContainerColor = ErrorColor.copy(alpha = 0.05f),
            disabledContentColor = ErrorColor.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            style = KeeprTypography.labelLarge
        )
    }
}
