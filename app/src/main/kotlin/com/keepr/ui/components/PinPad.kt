package com.keepr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleMuted
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.SurfaceRaised
import com.keepr.ui.theme.TextPrimary
import com.keepr.ui.theme.TextSecondary

@Composable
fun PinDots(
    pinLength: Int,
    maxLength: Int = 6,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val filled = index < pinLength
            val dotColor by animateColorAsState(
                targetValue = when {
                    hasError -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                    filled -> AccentPurple
                    else -> BorderDefault
                },
                animationSpec = tween(150),
                label = "dot_color"
            )
            val scale by animateFloatAsState(
                targetValue = if (filled) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "dot_scale"
            )
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(14.dp)
                    .background(
                        color = if (filled) dotColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = dotColor,
                        shape = CircleShape
                    )
                    .semantics { contentDescription = if (filled) "Filled" else "Empty" }
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    extraAction: @Composable (() -> Unit)? = null
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("extra", "0", "del")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "extra" -> Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            extraAction?.invoke()
                        }
                        "del" -> Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(72.dp)
                                    .semantics { contentDescription = "Delete" }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        else -> KeypadButton(
                            digit = key,
                            onClick = { onDigit(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    digit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .semantics { contentDescription = "Key $digit" },
            shape = CircleShape,
            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = SurfaceRaised,
                contentColor = TextPrimary
            )
        ) {
            Text(
                text = digit,
                style = KeeprTypography.headlineMedium.copy(color = TextPrimary)
            )
        }
    }
}
