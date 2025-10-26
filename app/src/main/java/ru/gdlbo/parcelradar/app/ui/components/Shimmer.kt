package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors

// Single reusable shimmer provider
@Composable
fun Shimmer(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    minAlpha: Float = 0.15f,
    maxAlpha: Float = 0.85f,
    durationMillis: Int = 1800,
    content: @Composable (alpha: Float) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val animatedAlpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    val appliedAlpha = if (isLoading) animatedAlpha else 1f

    Box(modifier = modifier.graphicsLayer(alpha = appliedAlpha)) {
        content(appliedAlpha)
    }
}

@Composable
fun Modifier.shimmerEffect(
    isLoading: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    baseColor: Color = ThemeColors.darkColor.background,
    minAlpha: Float = 0.15f,
    maxAlpha: Float = 0.85f,
    durationMillis: Int = 1800
): Modifier = composed {
    var animatedAlpha by remember { mutableFloatStateOf(if (isLoading) minAlpha else 1f) }
    val transition = rememberInfiniteTransition(label = "shimmerRectTransition")
    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerRectAlpha"
    )

    animatedAlpha = if (isLoading) alpha else 1f

    this
        .clip(shape)
        .background(baseColor.copy(alpha = animatedAlpha))
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val alpha = if (isLoading) 0.85f else 1f
    val baseColor = ThemeColors.darkColor.background

    Box(modifier = modifier.graphicsLayer(alpha = alpha)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(baseColor.copy(alpha = alpha))
        )
    }
}

@Composable
fun ShimmerFeedCard(
    isLoading: Boolean,
    windowSizeClass: WindowWidthSizeClass
) {
    val shimmerBase = ThemeColors.darkColor.background

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

    val transitionState = remember {
        MutableTransitionState(isLoading).apply { targetState = isLoading }
    }

    LaunchedEffect(isLoading) { transitionState.targetState = isLoading }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Shimmer(isLoading = isLoading, minAlpha = 0.15f, maxAlpha = 0.85f, durationMillis = 2500) { alpha ->
            Box(modifier = cardModifier) {
                Card(modifier = Modifier.fillMaxWidth()) {
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
                                    .background(shimmerBase.copy(alpha = alpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(shimmerBase.copy(alpha = alpha))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(120.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBase.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(100.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBase.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(150.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBase.copy(alpha = alpha))
                        )
                    }
                }
            }
        }
    }
}