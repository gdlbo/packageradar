package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass

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
    baseColor: Color? = null,
    minAlpha: Float = 0.15f,
    maxAlpha: Float = 0.85f,
    durationMillis: Int = 1800
): Modifier = composed {
    val color = baseColor ?: MaterialTheme.colorScheme.outlineVariant
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
        .background(color.copy(alpha = animatedAlpha))
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val alpha = if (isLoading) 0.85f else 1f
    val baseColor = MaterialTheme.colorScheme.outlineVariant

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
    val cardModifier = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)

        WindowWidthSizeClass.Medium -> Modifier
            .fillMaxWidth(0.5f)
            .padding(horizontal = 8.dp, vertical = 6.dp)

        WindowWidthSizeClass.Expanded -> Modifier
            .fillMaxWidth(0.33f)
            .padding(horizontal = 8.dp, vertical = 6.dp)
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
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .fillMaxWidth(0.6f)
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(80.dp)
                            .shimmerEffect(shape = RoundedCornerShape(16.dp))
                    )
                }

                // Main Content Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth(0.8f)
                                .shimmerEffect()
                        )
                    }
                }

                // Tracking Number Section
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = false)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(60.dp)
                                    .shimmerEffect()
                            )
                            Box(
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(120.dp)
                                    .shimmerEffect()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shimmerEffect(shape = RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
    }
}
