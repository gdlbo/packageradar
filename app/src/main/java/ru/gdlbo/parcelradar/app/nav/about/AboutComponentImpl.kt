package ru.gdlbo.parcelradar.app.nav.about

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.ui.components.AppLogo
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutComponentImpl(aboutComponent: AboutComponent) {
    val context = LocalContext.current
    val handler = LocalUriHandler.current
    val versionName =
        context.packageManager.getPackageInfo(context.applicationInfo.packageName, 0).versionName
    val (baseUrl, privacyUrl) = if (Locale.getDefault().language == "ru") {
        "https://gdeposylka.ru" to "https://gdeposylka.ru/privacy"
    } else {
        "https://packageradar.com" to "https://packageradar.com/privacy"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { aboutComponent.popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo()
            Text(
                text = stringResource(R.string.version) + " $versionName",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.unofficial_app),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DeveloperCard(
                "gdlbo",
                R.string.gdlbo_card,
                "https://t.me/gdlbo",
                "https://desu.shikimori.one/system/users/x160/277870.png?"
            )
            Spacer(modifier = Modifier.height(16.dp))
            DeveloperCard(
                "recodius",
                R.string.recodius_card,
                "https://t.me/recodius",
                "https://desu.shikimori.one/system/users/x160/420638.png"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AboutSection(
                title = R.string.open_site,
                onClick = { handler.openUri(baseUrl) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AboutSection(
                title = R.string.source_code,
                onClick = { handler.openUri("https://github.com/gdlbo/packageradar") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AboutSection(
                title = R.string.privacy_policy,
                onClick = { handler.openUri(privacyUrl) }
            )
        }
    }
}

@Composable
fun DeveloperCard(name: String, @StringRes role: Int, profileUrl: String, imageUrl: String) {
    val handler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { handler.openUri(profileUrl) },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                placeholder = painterResource(R.drawable.placeholder_image),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(role), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun AboutSection(@StringRes title: Int, onClick: () -> Unit) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 16.dp)
    )
}