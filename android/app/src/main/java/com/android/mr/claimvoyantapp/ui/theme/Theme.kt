package com.android.mr.claimvoyantapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CanvasBackground = Color(0xFFF7F6F2)
val SolidBorder = Color(0xFF1A1A1A)
val SolidBaseInk = Color(0xFF1A1A1A)
val AccentedBlue = Color(0xFF2563EB)
val CardSurfaceWhite = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = SolidBaseInk,
    background = CanvasBackground,
    surface = CardSurfaceWhite,
    onPrimary = Color.White,
    onBackground = SolidBaseInk,
    onSurface = SolidBaseInk
)

@Composable
fun ClaimVoyantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
