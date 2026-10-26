@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.shoalapps.anglerfish.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.shoalapps.anglerfish.data.Settings

// Fallback palette: Anglerfish's colour, generated with Material's colour utilities.
// Used on Android 11 and below, or with dynamic colour turned off.
private val Light = lightColorScheme(
    primary = Color(0xFFA83716), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD2), onPrimaryContainer = Color(0xFF872000),
    secondary = Color(0xFF77574E), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD3C8), onSecondaryContainer = Color(0xFF5D4038),
    tertiary = Color(0xFF837342), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF837342), onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFFFFF8F6), onBackground = Color(0xFF231917),
    surface = Color(0xFFFFF8F6), onSurface = Color(0xFF231917), onSurfaceVariant = Color(0xFF53433F),
    surfaceContainerLow = Color(0xFFFFF1ED), surfaceContainer = Color(0xFFFCEAE5),
    surfaceContainerHigh = Color(0xFFF7E4E0), surfaceContainerHighest = Color(0xFFF1DFDA),
    outline = Color(0xFF83716C), outlineVariant = Color(0xFFD8C2BC), inversePrimary = Color(0xFFFFB5A1),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFFB5A1), onPrimary = Color(0xFF601400),
    primaryContainer = Color(0xFF872000), onPrimaryContainer = Color(0xFFFFDBD2),
    secondary = Color(0xFFE7BDB2), onSecondary = Color(0xFF442A22),
    secondaryContainer = Color(0xFF60423A), onSecondaryContainer = Color(0xFFFFDBD1),
    tertiary = Color(0xFFD9C58D), onTertiary = Color(0xFF3B2F05),
    tertiaryContainer = Color(0xFFA1905C), onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFF1A110F), onBackground = Color(0xFFF1DFDA),
    surface = Color(0xFF1A110F), onSurface = Color(0xFFF1DFDA), onSurfaceVariant = Color(0xFFD8C2BC),
    surfaceContainerLow = Color(0xFF231917), surfaceContainer = Color(0xFF271D1B),
    surfaceContainerHigh = Color(0xFF322825), surfaceContainerHighest = Color(0xFF3D3230),
    outline = Color(0xFFA08C87), outlineVariant = Color(0xFF53433F), inversePrimary = Color(0xFFA83716),
)

@Composable
fun PufferTheme(settings: Settings, content: @Composable () -> Unit) {
    val dark = when (settings.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val scheme = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> Dark
        else -> Light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialExpressiveTheme(
        colorScheme = scheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
