package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R

import java.util.*

@Composable
fun AppLogo() {
    val logoId = if (Locale.getDefault().language != "ru") {
        R.drawable.logo_gp_en
    } else {
        R.drawable.logo_gp
    }

    Image(
        painter = painterResource(id = logoId),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier
            .size(width = 300.dp, height = 100.dp)
            .padding(bottom = 32.dp)
    )
}