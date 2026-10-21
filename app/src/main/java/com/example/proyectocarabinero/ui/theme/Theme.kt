package com.example.proyectocarabinero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF455A64),
    tertiary = Color(0xFFF9A825)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFB0BEC5),
    tertiary = Color(0xFFFFD54F)
)

@Composable
fun ProyectoCarabineroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = Typography,
        content = content
    )
}