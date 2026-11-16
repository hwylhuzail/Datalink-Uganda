
package com.datalink.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DataLinkColors = lightColorScheme(
    primary = Color(0xFF16C784),
    onPrimary = Color.White,

    secondary = Color(0xFF101B35),
    onSecondary = Color.White,

    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF101B35),

    surface = Color.White,
    onSurface = Color(0xFF101B35),

    error = Color(0xFFDC3545)
)

@Composable
fun DataLinkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DataLinkColors,
        typography = Typography(),
        content = content
    )
}
