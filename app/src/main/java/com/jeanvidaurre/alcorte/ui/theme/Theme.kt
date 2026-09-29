package com.jeanvidaurre.alcorte.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    background = Color(0xFF0D1210),      // Negro carbón profundo elegante
    surface = Color(0xFF161F1A),         // Tarjetas con tono pizarra oscuro sutil
    onBackground = Color(0xFFE2E8E4),    // Texto principal blanco suave
    onSurface = Color(0xFFE2E8E4),
    outline = Color(0xFF26332D)          // Bordes sutiles oscuros profesionales
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    background = BackgroundCream,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderSoft
)

@Composable
fun AlCorteTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}