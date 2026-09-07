package com.example.freshup.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Зелёная «свежая» палитра
val Green80 = Color(0xFFB5E8B0)
val Green60 = Color(0xFF81C784)
val Green40 = Color(0xFF2E7D32)
val Green20 = Color(0xFF1B5E20)

val DarkRed = Color(0xFFFFB4AB)
val LightRed = Color(0xFFBA1A1A)

val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5F6A5),
    onPrimaryContainer = Green20,
    secondary = Color(0xFF52634F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CD),
    onSecondaryContainer = Color(0xFF101F10),
    background = Color(0xFFF7FBF2),
    onBackground = Color(0xFF181D17),
    surface = Color(0xFFF7FBF2),
    onSurface = Color(0xFF181D17),
    surfaceVariant = Color(0xFFDEE5D8),
    onSurfaceVariant = Color(0xFF424940),
    error = LightRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val DarkColors = darkColorScheme(
    primary = Green60,
    onPrimary = Green20,
    primaryContainer = Color(0xFF2A4D2A),
    onPrimaryContainer = Color(0xFFA5F6A5),
    secondary = Color(0xFFB9CCB2),
    onSecondary = Color(0xFF243424),
    secondaryContainer = Color(0xFF3A4B39),
    onSecondaryContainer = Color(0xFFD5E8CD),
    background = Color(0xFF101410),
    onBackground = Color(0xFFE0E4DC),
    surface = Color(0xFF101410),
    onSurface = Color(0xFFE0E4DC),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD),
    error = DarkRed,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = DarkRed
)
