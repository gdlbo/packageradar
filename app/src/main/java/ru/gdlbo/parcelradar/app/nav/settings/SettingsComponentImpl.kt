package ru.gdlbo.parcelradar.app.nav.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.BuildConfig
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.api.entity.Profile
import ru.gdlbo.parcelradar.app.core.service.BackgroundService
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.ui.components.SettingCard
import ru.gdlbo.parcelradar.app.ui.components.ShimmerEffect
import ru.gdlbo.parcelradar.app.ui.components.ThemeSelector
import java.util.*

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComponentImpl(settingsComponent: SettingsComponent) {
    val coroutineScope = rememberCoroutineScope()
    val roomManager = remember { settingsComponent.roomManager }
    val themeManager = remember { settingsComponent.themeManager }
    val atm = remember { settingsComponent.atm }
    var notifyEmail by remember { mutableStateOf(false) }
    var notifyPush by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }
    var userEmailVerified by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var emailSent by remember { mutableStateOf(false) }
    var profile: Profile? by remember { mutableStateOf(null) }
    var isSwipeEnabled by remember { mutableStateOf(settingsComponent.settingsManager.isGestureSwipeEnabled) }
    var isPushNotificationsEnabled by remember { mutableStateOf(settingsComponent.settingsManager.arePushNotificationsEnabled) }
    var isUseLocalTime by remember { mutableStateOf(settingsComponent.settingsManager.isUseLocalTime) }
    val configuration = LocalConfiguration.current
    var isLoading by remember { mutableStateOf(true) }
    val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
    var isNotificationEnabledInSystem by remember {
        mutableStateOf(
            NotificationManagerCompat.from(
                ctx
            ).areNotificationsEnabled()
        )
    }
    var isPushServiceEnabled by remember {
        mutableStateOf(
            settingsComponent.isServiceEnabled(
                ctx,
                BackgroundService::class.java.name
            )
        )
    }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            powerManager.isIgnoringBatteryOptimizations(ctx.packageName)
        )
    }

    var isLinksHandlingEnabled by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        val settings = roomManager.loadNotifySettings()
        profile = roomManager.loadProfile()
        notifyEmail = settings.first
        notifyPush = settings.second
        userEmail = profile?.email.toString()
        userEmailVerified = profile?.isEmailConfirmed == true
        isLoading = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = ctx.getSystemService(DomainVerificationManager::class.java)
            val userState = manager.getDomainVerificationUserState(ctx.packageName)
            val hostToStateMap = userState?.hostToStateMap
            isLinksHandlingEnabled =
                hostToStateMap?.get("pochta.ru") == DomainVerificationUserState.DOMAIN_STATE_SELECTED ||
                        hostToStateMap?.get("pochta.ru") == DomainVerificationUserState.DOMAIN_STATE_VERIFIED
        }
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
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    UserProfileScreen(settingsComponent, isLoading, profile) { showDialog = it }
                }

                if (userEmailVerified.not()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        EmailConfirmation {
                            settingsComponent.approveEmail()
                            emailSent = true
                        }
                    }
                }

                SettingsSectionTitle(stringResource(R.string.notifications))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if ((!isNotificationEnabledInSystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) || !isIgnoringBatteryOptimizations) {
                        SettingCard(
                            title = stringResource(R.string.app_dozing_notifications_title),
                            subtitle = stringResource(R.string.app_dozing_notifications_description),
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text(stringResource(R.string.app_dozing_notifications_title)) },
                                supportingContent = { Text(stringResource(R.string.app_dozing_notifications_description)) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (!isNotificationEnabledInSystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val intent =
                                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(
                                                    Settings.EXTRA_APP_PACKAGE,
                                                    ctx.applicationInfo.packageName
                                                )
                                            }
                                        ctx.startActivity(intent)
                                    } else {
                                        val intent =
                                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = "package:${ctx.packageName}".toUri()
                                            }
                                        ctx.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }

                    SettingCard {
                        SwitchPreferenceItem(
                            label = stringResource(R.string.notification_in_app),
                            initialState = isPushNotificationsEnabled,
                            enabled = isNotificationEnabledInSystem
                        ) { newValue ->
                            coroutineScope.launch {
                                settingsComponent.settingsManager.arePushNotificationsEnabled =
                                    newValue
                                isPushNotificationsEnabled = newValue
                            }
                        }
                        SwitchPreferenceItem(
                            label = stringResource(R.string.notification_by_email),
                            initialState = notifyEmail,
                        ) { newValue ->
                            coroutineScope.launch {
                                settingsComponent.updateNotification(
                                    email = newValue,
                                    inapp = notifyPush
                                )
                                notifyEmail = newValue
                            }
                        }
                    }
                }

                SettingsSectionTitle(stringResource(R.string.system_title))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isLinksHandlingEnabled) {
                        SettingCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text(stringResource(R.string.enable_links_handling_title)) },
                                supportingContent = { Text(stringResource(R.string.enable_links_handling_text)) },
                                modifier = Modifier.clickable {
                                    val intent = Intent(
                                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                        Uri.parse("package:${ctx.packageName}")
                                    )
                                    ctx.startActivity(intent)
                                }
                            )
                        }
                    }

                    SettingCard {
                        SwitchPreferenceItem(
                            label = stringResource(R.string.swipe_gestures),
                            initialState = isSwipeEnabled,
                            summary = stringResource(R.string.swipe_gestures_summary)
                        ) { newValue ->
                            coroutineScope.launch {
                                settingsComponent.settingsManager.isGestureSwipeEnabled = newValue
                                isSwipeEnabled = newValue
                            }
                        }

                        SwitchPreferenceItem(
                            label = stringResource(R.string.use_local_time),
                            initialState = isUseLocalTime,
                            summary = stringResource(R.string.use_local_time_summary)
                        ) { newValue ->
                            coroutineScope.launch {
                                settingsComponent.settingsManager.isUseLocalTime = newValue
                                isUseLocalTime = newValue
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val locale = configuration.locales[0]
                            val currentLanguage =
                                locale.displayLanguage.replaceFirstChar { it.uppercase() }

                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text(stringResource(R.string.language)) },
                                trailingContent = {
                                    Text(
                                        text = currentLanguage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    val intent = Intent(
                                        "android.settings.APP_LOCALE_SETTINGS",
                                        ("package:" + ctx.applicationInfo.packageName).toUri()
                                    )
                                    ctx.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                ThemeSelector(themeManager = themeManager, modifier = Modifier.padding(0.dp))

                if (BuildConfig.DEBUG) {
                    SettingsSectionTitle("Debug")

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingCard {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text("Check in-app notifications") },
                                modifier = Modifier.clickable {
                                    settingsComponent.notificationCheck(ctx)
                                }
                            )

                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text("Notifications in system status") },
                                trailingContent = { Text(isNotificationEnabledInSystem.toString()) }
                            )

                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text("Is ignoring dozing") },
                                trailingContent = { Text(isIgnoringBatteryOptimizations.toString()) }
                            )

                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                headlineContent = { Text("Is service enabled") },
                                trailingContent = { Text(isPushServiceEnabled.toString()) }
                            )

                            SwitchPreferenceItem(
                                label = stringResource(R.string.notification_in_app),
                                initialState = notifyPush,
                                summary = "FCM pushes, unsupported tbh"
                            ) { newValue ->
                                coroutineScope.launch {
                                    settingsComponent.updateNotification(
                                        email = notifyEmail,
                                        inapp = newValue
                                    )
                                    notifyPush = newValue
                                }
                            }
                        }
                    }
                }

                SettingsSectionTitle(stringResource(R.string.about_app_title))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingCard {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            headlineContent = { Text(stringResource(R.string.about_app_title)) },
                            modifier = Modifier.clickable {
                                settingsComponent.navigateTo(RootComponent.TopLevelConfiguration.AboutScreenConfiguration)
                            }
                        )
                        DeleteAccountButton(ctx, atm)
                    }
                }
            }
        }
    }

    if (showDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDialog = false },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.logout_confirmation),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    showDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.no),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    showDialog = false
                                    settingsComponent.logout()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = stringResource(R.string.yes))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun UserProfileScreen(
    settingsComponent: SettingsComponent,
    isLoading: Boolean,
    profile: Profile?,
    updateDialogValue: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserCard(
            settingsComponent = settingsComponent,
            isLoading = isLoading,
            profile = profile,
            updateDialogValue = updateDialogValue
        )
        if (profile != null && BuildConfig.DEBUG) {
            DebugInfoCard(profile = profile)
        }
    }
}

@Composable
fun UserCard(
    settingsComponent: SettingsComponent,
    isLoading: Boolean,
    profile: Profile?,
    updateDialogValue: (Boolean) -> Unit
) {
    val isSystemLanguageRussian = Locale.getDefault().language == "ru"
    val country = if (isSystemLanguageRussian) profile?.countryNameRu else profile?.countryNameEn

    val gravatarUrl = remember(profile?.email) {
        settingsComponent.getGravatarUrl(profile?.email ?: "")
    }

    SettingCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerEffect(
                        modifier = Modifier
                            .height(20.dp)
                            .width(150.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .height(16.dp)
                            .width(100.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = gravatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.email ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!country.isNullOrEmpty()) {
                        Text(
                            text = country,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { updateDialogValue(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(R.string.logout)
                    )
                }
            }
        }
    }
}

@Composable
fun DebugInfoCard(profile: Profile?) {
    val debugInfo = listOfNotNull(
        profile?.id?.let { "User ID: $it" },
        profile?.isEmailConfirmed?.let { "Email Confirmed: $it" },
        profile?.appleConnected?.let { "Apple Connected: $it" },
        profile?.notifyEmail?.let { "Notify Email: $it" },
        profile?.notifyPush?.let { "Notify Push (FCM): $it" },
        profile?.countryCode?.let { "Country Code: $it" },
        profile?.countryNameRu?.let { "Country name (RU): $it" },
        profile?.countryNameEn?.let { "Country name (EN): $it" }
    )

    SettingCard(
        title = "Account info",
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            debugInfo.forEach { info ->
                Text(
                    text = info,
                    modifier = Modifier.padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

    ListItem(
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        headlineContent = {
            Text(
                text = stringResource(R.string.delete_account),
                color = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, deleteAccountUrl.toUri())
            ctx.startActivity(intent)
        }
    )
}

@Composable
private fun SwitchPreferenceItem(
    label: String,
    initialState: Boolean,
    summary: String? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        headlineContent = { Text(label) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = initialState,
                onCheckedChange = null,
                enabled = enabled
            )
        },
        modifier = Modifier.clickable(enabled = enabled) {
            onCheckedChange(!initialState)
        }
    )
}

@Composable
fun EmailConfirmation(onEmailSent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.confirm_email),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
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
                shape = RoundedCornerShape(12.dp),
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
