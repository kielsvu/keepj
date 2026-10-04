package com.keepr.ui.screens.generator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.KeeprPrimaryButton
import com.keepr.ui.theme.AccentPurple
import com.keepr.ui.theme.AccentPurpleContainer
import com.keepr.ui.theme.AccentPurpleLight
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.BorderDefault
import com.keepr.ui.theme.KeeprTypography
import com.keepr.ui.theme.SurfaceMid
import com.keepr.ui.theme.TextSecondary
import com.keepr.ui.theme.TextTertiary
import com.keepr.utils.ClipboardUtils
import com.keepr.utils.PasswordStrength
import androidx.compose.ui.graphics.Color

@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    onBack: () -> Unit,
    onUsePassword: ((String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextSecondary)
            }
            Text(
                text = "Password generator",
                style = KeeprTypography.titleLarge,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceMid)
                    .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = state.generatedPassword,
                    transitionSpec = {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                    },
                    label = "password_anim"
                ) { password ->
                    Text(
                        text = password,
                        style = KeeprTypography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                StrengthBar(strength = state.strength)

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = { viewModel.regenerate() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPurpleContainer)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate", tint = AccentPurpleLight)
                    }
                    IconButton(
                        onClick = {
                            ClipboardUtils.copyToClipboard(context, "Password", state.generatedPassword)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPurpleContainer)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = AccentPurpleLight)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceMid)
                    .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Length", style = KeeprTypography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        "${state.options.length}",
                        style = KeeprTypography.bodyMedium.copy(color = AccentPurpleLight)
                    )
                }
                Slider(
                    value = state.options.length.toFloat(),
                    onValueChange = { viewModel.onLength(it.toInt()) },
                    valueRange = 8f..64f,
                    steps = 55,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPurple,
                        activeTrackColor = AccentPurple,
                        inactiveTrackColor = AccentPurpleContainer
                    )
                )
            }

            listOf(
                Triple("Uppercase", state.options.uppercase) { viewModel.onUppercase(!state.options.uppercase) },
                Triple("Lowercase", state.options.lowercase) { viewModel.onLowercase(!state.options.lowercase) },
                Triple("Numbers", state.options.numbers) { viewModel.onNumbers(!state.options.numbers) },
                Triple("Symbols", state.options.symbols) { viewModel.onSymbols(!state.options.symbols) }
            ).forEach { (label, checked, toggle) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceMid)
                        .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = KeeprTypography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = checked,
                        onCheckedChange = { toggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = AccentPurpleContainer
                        )
                    )
                }
            }

            if (onUsePassword != null) {
                Spacer(Modifier.height(4.dp))
                KeeprPrimaryButton(
                    text = "Use this password",
                    onClick = { onUsePassword(state.generatedPassword) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StrengthBar(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.VERY_WEAK -> com.keepr.ui.theme.StrengthVeryWeak
        PasswordStrength.WEAK -> com.keepr.ui.theme.StrengthWeak
        PasswordStrength.FAIR -> com.keepr.ui.theme.StrengthFair
        PasswordStrength.STRONG -> com.keepr.ui.theme.StrengthStrong
        PasswordStrength.VERY_STRONG -> com.keepr.ui.theme.StrengthVeryStrong
    }
    val filled = strength.score + 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < filled) color else BorderDefault)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = strength.label,
            style = KeeprTypography.labelSmall.copy(color = color)
        )
    }
}
