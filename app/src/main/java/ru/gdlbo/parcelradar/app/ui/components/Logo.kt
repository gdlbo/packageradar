package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R

@Composable
fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.ic_logo_unofficial),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier
            .size(width = 175.dp, height = 200.dp)
            .padding(bottom = 32.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}