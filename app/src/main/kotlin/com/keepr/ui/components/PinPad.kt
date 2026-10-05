package com.keepr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceRaised
import com.keepr.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun PinDots(
    pinLength: Int,
    maxLength: Int = 6,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(hasError, pinLength) {
        if (hasError) {
            for (i in 0..5) {
                shakeOffset.animateTo(
                    targetValue = if (i % 2 == 0) 10f else -10f,
                    animationSpec = tween(55)
                )
            }
            shakeOffset.animateTo(0f, tween(55))
        }
    }

    Row(
        modifier = modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val filled = index < pinLength
            val fillColor by animateColorAsState(
                targetValue = if (hasError) Color(0xFFFF6B6B) else AccentPurple,
                animationSpec = tween(120),
                label = "dot_fill_$index"
            )
            val scale by animateFloatAsState(
                targetValue = if (filled) 1f else 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "dot_scale_$index"
            )
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (filled) fillColor else Color.Transparent)
                    .border(
                        width = if (filled) 0.dp else 1.5.dp,
                        color = if (filled) Color.Transparent else BorderDefault,
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("extra", "0", "del")
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    when (key) {
                        "extra" -> Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) { extraAction?.invoke() }

                        "del" -> Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) { KeypadActionButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }}

                        else -> KeypadDigitButton(digit = key, onClick = { onDigit(key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(digit: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.87f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "digit_scale_$digit"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(76.dp)
            .clip(CircleShape)
            .background(SurfaceRaised)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                })
            }
            .semantics { contentDescription = "Key $digit" },
        contentAlignment = Alignment.Center
    ) {
        Text(text = digit, style = KeeprTypography.headlineMedium)
    }
}

@Composable
private fun KeypadActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "action_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(76.dp)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                })
            },
        contentAlignment = Alignment.Center
    ) { content() }
}
