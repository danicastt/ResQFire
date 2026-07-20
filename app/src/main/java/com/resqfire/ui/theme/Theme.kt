package com.resqfire.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FireRed = Color(0xFFD32F2F)
val FireOrange = Color(0xFFFF6F00)
val FireYellow = Color(0xFFFDD835)
val DarkGray = Color(0xFF212121)
val SmokeGray = Color(0xFF757575)

private val ColorScheme = darkColorScheme(
    primary = FireRed,
    secondary = FireOrange,
    tertiary = FireYellow,
    background = DarkGray,
    surface = Color(0xFF303030),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ResQFireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography(),
        content = content
    )
}
