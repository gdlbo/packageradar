package ru.gdlbo.parcelradar.app.ui.components.status

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    maxRating: Int,
    colorActive: Color = Color.Yellow,
    colorInActive: Color = Color.Transparent,
    imageVector: ImageVector,
    rating: Float,
    extraInternalIconPadding: Dp = 0.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier,
    animateRatingChange: Boolean = false
) {
    require(maxRating > 0 && rating >= 0 && rating <= maxRating) {
        "Invalid rating parameters in RatingBar"
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(extraInternalIconPadding)
    ) {
        repeat(maxRating) { index ->
            val progress = if (index < rating.toInt()) {
                1f
            } else if (index == rating.toInt()) {
                rating - rating.toInt()
            } else {
                0f
            }

            val animatedProgress = animateFloatAsState(
                targetValue = progress,
                animationSpec = if (animateRatingChange) {
                    tween(durationMillis = 500)
                } else {
                    snap()
                }
            ).value

            val brush = Brush.horizontalGradient(
                0f to colorActive,
                animatedProgress to colorActive,
                animatedProgress to colorInActive,
                1f to colorInActive,
            )
            Icon(
                imageVector = imageVector,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcAtop)
                        }
                    },
                contentDescription = "Star Rate Icon - ${index + 1}",
            )
        }
    }
}