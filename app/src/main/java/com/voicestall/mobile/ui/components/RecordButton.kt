package com.voicestall.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voicestall.mobile.ui.theme.IdleGray
import com.voicestall.mobile.ui.theme.ProcessingAmber
import com.voicestall.mobile.ui.theme.RecordingRed

enum class RecordButtonState {
    IDLE, RECORDING, PROCESSING
}

@Composable
fun RecordButton(
    state: RecordButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            RecordButtonState.IDLE -> IdleGray
            RecordButtonState.RECORDING -> RecordingRed
            RecordButtonState.PROCESSING -> ProcessingAmber
        },
        label = "recordButtonColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == RecordButtonState.RECORDING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(80.dp)
            .scale(scale),
        shape = CircleShape,
        containerColor = backgroundColor,
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(
            imageVector = if (state == RecordButtonState.RECORDING) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = if (state == RecordButtonState.RECORDING) "Stop recording" else "Start recording",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
