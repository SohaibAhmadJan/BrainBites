package com.example.brainbites.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.brainbites.ui.theme.DarkGreenPrimary

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import com.example.brainbites.ui.theme.BrainBitesTheme

/**
 * A clean, placeholder logo for BrainBites.
 * Draws a minimal brain representation inside a circular badge.
 */
@Composable
fun BrainBitesLogo(
    modifier: Modifier = Modifier.size(120.dp),
    color: Color = DarkGreenPrimary
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f

        // Draw circular badge outline
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Fill
        )

        // Draw a minimal brain shape (simplified as two overlapping ovals)
        val brainWidth = size.width * 0.45f
        val brainHeight = size.height * 0.35f

        // Left lobe
        drawOval(
            color = color,
            topLeft = Offset(center.x - brainWidth * 0.9f, center.y - brainHeight / 2),
            size = androidx.compose.ui.geometry.Size(brainWidth, brainHeight),
            style = Stroke(width = 3.dp.toPx())
        )

        // Right lobe
        drawOval(
            color = color,
            topLeft = Offset(center.x - brainWidth * 0.1f, center.y - brainHeight / 2),
            size = androidx.compose.ui.geometry.Size(brainWidth, brainHeight),
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner brain texture/details (simplified)
        drawLine(
            color = color,
            start = Offset(center.x, center.y - brainHeight * 0.3f),
            end = Offset(center.x, center.y + brainHeight * 0.3f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BrainBitesLogoPreview() {
    BrainBitesTheme {
        BrainBitesLogo(modifier = Modifier.padding(16.dp))
    }
}
