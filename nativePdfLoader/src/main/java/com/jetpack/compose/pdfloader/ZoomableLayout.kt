package com.jetpack.compose.pdfloader

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@Composable
fun ZoomableLayout(listBitmap: List<Bitmap>) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale.value * zoomChange).coerceIn(1f, 5f)

        coroutineScope.launch {
            scale.snapTo(newScale)

            if (newScale == 1f) {
                launch { offsetX.animateTo(0f, animationSpec = spring()) }
                launch { offsetY.animateTo(0f, animationSpec = spring()) }
            } else {
                launch { offsetX.snapTo(offsetX.value + offsetChange.x) }
                launch { offsetY.snapTo(offsetY.value + offsetChange.y) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                translationX = offsetX.value,
                translationY = offsetY.value,
                clip = true
            )
            .transformable(
                state = transformState,
                lockRotationOnZoomPan = true,
                canPan = { scale.value > 1f } // ← allow scroll when not zoomed
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            launch {
                                scale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                offsetY.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    }
                )
            }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(count = listBitmap.size) { i ->
                ZoomableImage(bitmap = listBitmap[i])
            }
        }
    }
}