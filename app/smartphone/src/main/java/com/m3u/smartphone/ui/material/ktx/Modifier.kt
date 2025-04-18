package com.m3u.smartphone.ui.material.ktx

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import com.m3u.smartphone.ui.material.model.LocalSpacing

@Composable
fun Modifier.textHorizontalLabel(
    color: Color = MaterialTheme.colorScheme.primary
): Modifier = composed {
    val spacing = LocalSpacing.current
    this
        .fillMaxWidth()
        .background(color)
        .padding(
            horizontal = spacing.medium,
            vertical = spacing.extraSmall
        )
}