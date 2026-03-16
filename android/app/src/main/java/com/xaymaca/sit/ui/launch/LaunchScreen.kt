package com.xaymaca.sit.ui.launch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xaymaca.sit.ui.theme.Amber
import com.xaymaca.sit.ui.theme.Cobalt
import com.xaymaca.sit.ui.theme.Navy
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen(onComplete: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logo_alpha"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(2000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            drawPulseLogo(alpha = alpha)
        }
    }
}

private fun DrawScope.drawPulseLogo(alpha: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.width / 2f

    // Navy background circle
    drawCircle(
        color = Color(0xFF0A1628),
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Cobalt speech bubble shape
    // Body of bubble: rounded rectangle
    val bubbleLeft = centerX - radius * 0.55f
    val bubbleTop = centerY - radius * 0.50f
    val bubbleRight = centerX + radius * 0.55f
    val bubbleBottom = centerY + radius * 0.25f
    val bubbleCorner = radius * 0.18f

    val bubblePath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(
                    left = bubbleLeft,
                    top = bubbleTop,
                    right = bubbleRight,
                    bottom = bubbleBottom
                ),
                radiusX = bubbleCorner,
                radiusY = bubbleCorner
            )
        )
        // Tail pointing down-left
        moveTo(centerX - radius * 0.20f, bubbleBottom)
        lineTo(centerX - radius * 0.38f, bubbleBottom + radius * 0.30f)
        lineTo(centerX + radius * 0.05f, bubbleBottom)
        close()
    }
    drawPath(
        path = bubblePath,
        color = Cobalt.copy(alpha = alpha)
    )

    // Amber EKG / pulse wave inside the bubble
    val ekgPath = Path()
    val waveY = (bubbleTop + bubbleBottom) / 2f
    val waveLeft = bubbleLeft + radius * 0.12f
    val waveRight = bubbleRight - radius * 0.12f
    val waveWidth = waveRight - waveLeft
    val peakHeight = radius * 0.28f

    ekgPath.moveTo(waveLeft, waveY)
    // Flat line start
    ekgPath.lineTo(waveLeft + waveWidth * 0.25f, waveY)
    // Small upward blip
    ekgPath.lineTo(waveLeft + waveWidth * 0.32f, waveY - peakHeight * 0.3f)
    ekgPath.lineTo(waveLeft + waveWidth * 0.36f, waveY)
    // Main QRS spike
    ekgPath.lineTo(waveLeft + waveWidth * 0.44f, waveY - peakHeight)
    ekgPath.lineTo(waveLeft + waveWidth * 0.50f, waveY + peakHeight * 0.5f)
    ekgPath.lineTo(waveLeft + waveWidth * 0.56f, waveY)
    // T-wave hump
    ekgPath.cubicTo(
        waveLeft + waveWidth * 0.62f, waveY,
        waveLeft + waveWidth * 0.70f, waveY - peakHeight * 0.45f,
        waveLeft + waveWidth * 0.75f, waveY
    )
    // Flat line end
    ekgPath.lineTo(waveRight, waveY)

    drawPath(
        path = ekgPath,
        color = Amber.copy(alpha = alpha),
        style = Stroke(
            width = size.width * 0.040f,
            cap = StrokeCap.Round
        )
    )
}
