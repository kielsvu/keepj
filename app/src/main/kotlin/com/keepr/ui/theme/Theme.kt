package com.keepr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KeeprColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = TextPrimary,
    primaryContainer = AccentPurpleContainer,
    onPrimaryContainer = AccentPurpleLight,
    secondary = AccentPurpleLight,
    onSecondary = Background,
    secondaryContainer = AccentPurpleMuted,
    onSecondaryContainer = AccentPurpleLight,
    tertiary = AccentPurpleDim,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceDeep,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceRaised,
    surfaceContainerLow = SurfaceDeep,
    surfaceContainerHigh = SurfaceOverlay,
    outline = BorderDefault,
    outlineVariant = BorderSubtle,
    error = ErrorColor,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorColor,
    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    scrim = Color(0xCC000000)
)

@Composable
fun KeeprTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KeeprColorScheme,
        typography = KeeprTypography,
        content = content
    )
}

object KeeprSpacing {
    const val xs = 4
    const val sm = 8
    const val md = 12
    const val lg = 16
    const val xl = 20
    const val xxl = 24
    const val xxxl = 32
    const val huge = 48
    const val massive = 64
}

object KeeprRadius {
    const val sm = 8
    const val md = 12
    const val lg = 16
    const val xl = 20
    const val full = 100
}
