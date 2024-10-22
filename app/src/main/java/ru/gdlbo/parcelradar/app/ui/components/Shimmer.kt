package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    transition: InfiniteTransition
) {
    val alpha = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(modifier = modifier.graphicsLayer(alpha = if (isLoading) alpha.value else 1f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondary)
        )
    }
}

@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    val transition =
        updateTransition(targetState = visible, label = "transition")
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alpha"
    ) { if (it) 1f else 0.5f }
    LaunchedEffect(key1 = Unit) {
        visible = true
    }
    this
        .clip(RoundedCornerShape(4.dp))
        .background(Color.LightGray.copy(alpha = alpha.value))
}

@Composable
fun ShimmerFeedCard(
    transition: Transition<Boolean>,
    isDarkTheme: Boolean,
    windowSizeClass: WindowWidthSizeClass
) {
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alpha"
    ) { if (it) 1f else 0f }

    val shimmerColor = if (isDarkTheme) Color.Gray else Color.LightGray
    val cardModifier = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp)

        WindowWidthSizeClass.Medium -> Modifier
            .fillMaxWidth(0.5f)
            .padding(start = 4.dp, end = 2.dp, bottom = 4.dp)

        WindowWidthSizeClass.Expanded -> Modifier
            .fillMaxWidth(0.33f)
            .padding(start = 4.dp, end = 2.dp, bottom = 4.dp)
    }

    Box(
        modifier = cardModifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerColor.copy(alpha = alpha.value))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerColor.copy(alpha = alpha.value))
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor.copy(alpha = alpha.value))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor.copy(alpha = alpha.value))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(150.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor.copy(alpha = alpha.value))
                )
            }
        }
    }
}