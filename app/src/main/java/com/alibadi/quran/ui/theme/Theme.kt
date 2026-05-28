package com.alibadi.quran.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark
)

fun scaleTypography(base: Typography, factor: Float): Typography {
    return Typography(
        displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * factor, lineHeight = base.displayLarge.lineHeight * factor),
        displayMedium = base.displayMedium.copy(fontSize = base.displayMedium.fontSize * factor, lineHeight = base.displayMedium.lineHeight * factor),
        displaySmall = base.displaySmall.copy(fontSize = base.displaySmall.fontSize * factor, lineHeight = base.displaySmall.lineHeight * factor),
        
        headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * factor, lineHeight = base.headlineLarge.lineHeight * factor),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * factor, lineHeight = base.headlineMedium.lineHeight * factor),
        headlineSmall = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize * factor, lineHeight = base.headlineSmall.lineHeight * factor),
        
        titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * factor, lineHeight = base.titleLarge.lineHeight * factor),
        titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * factor, lineHeight = base.titleMedium.lineHeight * factor),
        titleSmall = base.titleSmall.copy(fontSize = base.titleSmall.fontSize * factor, lineHeight = base.titleSmall.lineHeight * factor),
        
        bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * factor, lineHeight = base.bodyLarge.lineHeight * factor),
        bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * factor, lineHeight = base.bodyMedium.lineHeight * factor),
        bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * factor, lineHeight = base.bodySmall.lineHeight * factor),
        
        labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * factor, lineHeight = base.labelLarge.lineHeight * factor),
        labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * factor, lineHeight = base.labelMedium.lineHeight * factor),
        labelSmall = base.labelSmall.copy(fontSize = base.labelSmall.fontSize * factor, lineHeight = base.labelSmall.lineHeight * factor),
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    fontScale: String = "NORMAL",
    content: @Composable () -> Unit,
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = remember(darkTheme, amoled, baseColorScheme) {
        if (darkTheme && amoled) {
            baseColorScheme.copy(
                background = Color.Black,
                surface = Color(0xFF0C0F0E),
                surfaceVariant = Color(0xFF131917),
                onBackground = Color(0xFFE8F5F1),
                onSurface = Color(0xFFE8F5F1)
            )
        } else {
            baseColorScheme
        }
    }

    val scaleFactor = when (fontScale) {
        "LARGE" -> 1.15f
        "XLARGE" -> 1.30f
        else -> 1.0f
    }

    val scaledTypography = remember(fontScale) {
        if (scaleFactor == 1.0f) Typography else scaleTypography(Typography, scaleFactor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        shapes = Shapes,
        content = content
    )
}
