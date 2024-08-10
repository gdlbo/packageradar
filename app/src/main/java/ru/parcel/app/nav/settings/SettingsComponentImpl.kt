package ru.parcel.app.nav.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.parcel.app.R
import ru.parcel.app.di.prefs.AccessTokenManager
import ru.parcel.app.nav.RootComponent
import ru.parcel.app.ui.components.ShimmerEffect
import ru.parcel.app.ui.components.ThemeSelector
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComponentImpl(settingsComponent: SettingsComponent) {
    val coroutineScope = rememberCoroutineScope()
    val roomManager = remember { settingsComponent.roomManager }
    val themeManager = remember { settingsComponent.themeManager }
    val atm = remember { settingsComponent.atm }
    var notifyEmail by remember { mutableStateOf(false) }
    var notifyPush by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userEmailVerified by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var emailSent by remember { mutableStateOf(false) }
    var isSwipeEnabled by remember { mutableStateOf(settingsComponent.isGestureSwipeEnabled) }
    val configuration = LocalConfiguration.current
    var isLoading by remember { mutableStateOf(true) }
    val transition = rememberInfiniteTransition(label = "shimmerTransition")

    LaunchedEffect(Unit) {
        val settings = roomManager.loadNotifySettings()
        val profile = roomManager.loadProfile()
        notifyEmail = settings.first
        notifyPush = settings.second
        userEmail = profile?.email.toString()
        userEmailVerified = profile?.isEmailConfirmed == true
        userId = ctx.getString(R.string.user_id_label, profile?.id.toString())
        isLoading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { settingsComponent.popBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                UserCard(isLoading, userId, userEmail, { showDialog = it }, transition)

                if (userEmailVerified.not()) {
                    EmailConfirmation {
                        settingsComponent.approveEmail()
                        emailSent = true
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                SwitchPreferenceItem(
                    label = stringResource(R.string.notification_in_app),
                    initialState = notifyPush,
                    summary = "Currently unstable",
                ) { newValue ->
                    coroutineScope.launch {
                        settingsComponent.updateNotification(
                            email = notifyEmail,
                            inapp = newValue
                        )
                        notifyPush = newValue
                    }
                }

                SwitchPreferenceItem(
                    label = stringResource(R.string.notification_by_email),
                    initialState = notifyEmail,
                ) { newValue ->
                    coroutineScope.launch {
                        settingsComponent.updateNotification(email = newValue, inapp = notifyPush)
                        notifyEmail = newValue
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            settingsComponent.notificaionCheck(ctx)
                        }
                        .padding(12.dp),
                ) {
                    Text(
                        text = "Check in-app notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.system_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                SwitchPreferenceItem(
                    label = stringResource(R.string.swipe_gestures),
                    initialState = isSwipeEnabled,
                ) { newValue ->
                    coroutineScope.launch {
                        settingsComponent.isGestureSwipeEnabled = newValue
                        isSwipeEnabled = newValue
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val locale = configuration.locales[0]
                    val currentLanguage =
                        locale.displayLanguage.replaceFirstChar { it.uppercase() }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clickable {
                                val intent = Intent(
                                    "android.settings.APP_LOCALE_SETTINGS",
                                    Uri.parse("package:" + ctx.applicationInfo.packageName)
                                )
                                ctx.startActivity(intent)
                            },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentLanguage,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.themes_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                ThemeSelector(
                    themeManager = themeManager
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            settingsComponent.navigateTo(RootComponent.TopLevelConfiguration.AboutScreenConfiguration)
                        }
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_app_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }

                DeleteAccountButton(ctx, atm)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(R.string.logout)) },
            text = { Text(text = stringResource(R.string.logout_confirmation)) },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        settingsComponent.logout()
                    }
                }) {
                    Text(text = stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.no))
                }
            }
        )
    }

    if (emailSent) {
        LaunchedEffect(snackbarHostState) {
            val message = ctx.getString(R.string.approval_email_sent, userEmail)
            snackbarHostState.showSnackbar(message)
            emailSent = false
        }
    }
}

@Composable
fun UserCard(
    isLoading: Boolean,
    userId: String,
    userEmail: String,
    updateDialogValue: (Boolean) -> Unit,
    transition: InfiniteTransition
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (isLoading) {
                    ShimmerEffect(
                        transition = transition,
                        modifier = Modifier
                            .height(20.dp)
                            .width(120.dp)
                            .padding(vertical = 4.dp)
                    )
                    ShimmerEffect(
                        transition = transition,
                        modifier = Modifier
                            .height(20.dp)
                            .width(180.dp)
                            .padding(vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = userId,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = userEmail,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { updateDialogValue(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun DeleteAccountButton(ctx: Context, atm: AccessTokenManager) {
    val accessToken = atm.getAccessToken()
    val currentLanguage = Locale.getDefault().language

    val (baseUrl, addr) = if (currentLanguage == "ru") {
        "https://gdeposylka.ru/api/a1/go" to "https://gdeposylka.ru/auth/remove"
    } else {
        "https://packageradar.com/api/a1/go" to "https://packageradar.com/auth/remove"
    }

    val deleteAccountUrl = "$baseUrl?token=$accessToken&addr=$addr"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deleteAccountUrl))
                ctx.startActivity(intent)
            }
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.delete_account),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun SwitchPreferenceItem(
    label: String,
    initialState: Boolean,
    summary: String? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = initialState,
                onCheckedChange = { newValue ->
                    if (enabled) {
                        onCheckedChange(newValue)
                    }
                },
                enabled = enabled
            )
        }
    }
}

@Composable
fun EmailConfirmation(onEmailSent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.confirm_email),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.confirm_email_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onEmailSent,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.send_email),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}