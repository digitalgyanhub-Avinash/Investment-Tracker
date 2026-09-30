package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Renders the exact ambient liquid mesh gradient canvas
 * seen on the desktop/web version of Investment Tracker.
 */
@Composable
fun AmbientCanvasBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCCFBF1), // Luminous Mint / Aqua
                        Color(0xFFE0F2FE), // Luminous Sky / Ice
                        Color(0xFFEDE9FE), // Soft Lavender / Violet
                        Color(0xFFFCE7F3)  // Soft Rose / Peach
                    )
                )
            )
    ) {
        // Ambient chromatic orbs that diffuse through translucent frosted glass
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left emerald/teal orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x802DD4BF),
                        Color(0x4510B981),
                        Color(0x0010B981)
                    ),
                    center = Offset(w * 0.10f, h * 0.08f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.10f, h * 0.08f),
                radius = w * 0.75f
            )

            // Upper-right cyan/sky orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x7538BDF8),
                        Color(0x350284C7),
                        Color(0x000284C7)
                    ),
                    center = Offset(w * 0.90f, h * 0.22f),
                    radius = w * 0.70f
                ),
                center = Offset(w * 0.90f, h * 0.22f),
                radius = w * 0.70f
            )

            // Mid-left violet/purple orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x65A78BFA),
                        Color(0x307C3AED),
                        Color(0x007C3AED)
                    ),
                    center = Offset(w * 0.05f, h * 0.50f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.05f, h * 0.50f),
                radius = w * 0.65f
            )

            // Lower-right rose/fuchsia orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x65F472B6),
                        Color(0x30E11D48),
                        Color(0x00E11D48)
                    ),
                    center = Offset(w * 0.88f, h * 0.72f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.88f, h * 0.72f),
                radius = w * 0.75f
            )

            // Lower-left amber/orange glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55FBBF24),
                        Color(0x25F97316),
                        Color(0x00F97316)
                    ),
                    center = Offset(w * 0.20f, h * 0.88f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.20f, h * 0.88f),
                radius = w * 0.65f
            )
        }

        // Screen content rendered on top of canvas
        content()
    }
}
