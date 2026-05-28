package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val backgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        surfaceColor.copy(alpha = 0.85f)
    }

    val borderColor = if (isDark) {
        primaryColor.copy(alpha = 0.18f)
    } else {
        primaryColor.copy(alpha = 0.22f)
    }

    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.large,
                clip = false,
                ambientColor = Color.Black.copy(0.08f),
                spotColor = Color.Black.copy(0.12f)
            )
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = if (isDark) 0.10f else 0.80f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.05f)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .padding(18.dp),
        content = content
    )
}

@Composable
fun GlassHighlightCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val backgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        surfaceColor.copy(alpha = 0.92f)
    }

    val borderColor = if (isDark) {
        secondaryColor.copy(alpha = 0.6f)
    } else {
        secondaryColor.copy(alpha = 0.4f)
    }

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.large,
                clip = false,
                ambientColor = Color.Black.copy(0.12f),
                spotColor = Color.Black.copy(0.16f)
            )
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = if (isDark) 0.15f else 0.85f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.2f)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .padding(20.dp),
        content = content
    )
}
