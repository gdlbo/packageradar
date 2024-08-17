package ru.parcel.app.ui.components.status

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import ru.parcel.app.R
import ru.parcel.app.core.network.model.Courier
import java.util.Locale

@Composable
fun CourierRating(courier: Courier?, isDarkTheme: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_star_24),
            contentDescription = "Courier Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.courier_rating),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            courier?.reviewScore?.let {
                RatingDetails(
                    rating = it.toFloat(),
                    reviewCount = courier.reviewCount ?: 0,
                    reviewUrl = courier.reviewUrl.toString(),
                    context = context,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun RatingDetails(
    rating: Float,
    reviewCount: Int,
    reviewUrl: String,
    context: Context,
    isDarkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.45f else 1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(role = Role.Button, onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl))
                context.startActivity(intent)
            })
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingBar(
                maxRating = 5,
                colorInActive = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.45f else 0.5f),
                imageVector = Icons.Filled.Star,
                rating = rating,
                iconSize = 20.dp,
                animateRatingChange = true
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = rating.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White
                )
            )
        }
    }
    Text(
        text = stringResource(id = R.string.based_on_users, reviewCount),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun CourierName(courier: Courier?) {
    if (courier == null) return

    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    val name = courier.name
    val currentLanguage = Locale.getDefault().language
    val baseUrl =
        if (currentLanguage == "ru") "https://gdeposylka.ru" else "https://packageradar.com"
    val imageUrl = "$baseUrl/img/courier/${courier.slug}.svg"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_delivery_dining_24),
            contentDescription = "Courier Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.courier),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (name.isBlank()) {
                    stringResource(id = R.string.empty)
                } else {
                    name
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.width(12.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .size(Size.ORIGINAL)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Courier Image",
            contentScale = ContentScale.Crop,
            imageLoader = imageLoader,
            modifier = Modifier.size(40.dp)
        )
    }
}