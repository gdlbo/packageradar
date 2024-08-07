package ru.parcel.app.ui.components.status

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.parcel.app.R
import ru.parcel.app.core.network.model.Courier

@Composable
fun CourierInfo(courier: Courier?, isDarkTheme: Boolean) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        CourierHeader()
        CourierDetails(courier = courier, context = context, isDarkTheme = isDarkTheme)
    }
}

@Composable
fun CourierHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_delivery_dining_24),
            contentDescription = "Courier Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.courier),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CourierDetails(courier: Courier?, context: Context, isDarkTheme: Boolean) {
    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            text = courier?.name ?: "N/A",
            style = MaterialTheme.typography.bodyLarge
        )
        courier?.reviewScore?.let {
            Spacer(Modifier.height(12.dp))
            CourierRating(
                rating = it.toFloat(),
                reviewCount = courier.reviewCount ?: 0,
                reviewUrl = courier.reviewUrl.toString(),
                context = context,
                isDarkTheme
            )
        }
    }
}

@Composable
fun CourierRating(
    rating: Float,
    reviewCount: Int,
    reviewUrl: String,
    context: Context,
    isDarkTheme: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.rating),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.45f else 0.9f),
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
                    colorInActive = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.45f else 0.9f),
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
    }
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(id = R.string.based_on_users, reviewCount),
        style = MaterialTheme.typography.bodyMedium
    )
}