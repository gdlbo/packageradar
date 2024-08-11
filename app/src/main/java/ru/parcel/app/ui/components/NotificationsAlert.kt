package ru.parcel.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import ru.parcel.app.R
import ru.parcel.app.di.prefs.SettingsManager

@Composable
fun CheckAndEnablePushNotificationsDialog() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }

    val context = LocalContext.current
    val settingsManager = SettingsManager()
    val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val openDialog = remember { mutableStateOf(!areNotificationsEnabled) }

    if (openDialog.value && settingsManager.areNotificationDialogSkipped.not()) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text = stringResource(R.string.enable_push_notifications_title)) },
            text = { Text(text = stringResource(R.string.enable_push_notifications_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.applicationInfo.packageName
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = stringResource(R.string.enable))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        settingsManager.areNotificationDialogSkipped = true
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@SuppressLint("BatteryLife")
@Composable
fun CheckAndDisableBatteryOptimizationDialog() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return
    }

    val context = LocalContext.current
    val settingsManager = SettingsManager()
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    val openDialog = remember { mutableStateOf(!isIgnoringBatteryOptimizations) }

    if (openDialog.value && settingsManager.areOptimizationDialogSkipped.not()) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text = stringResource(R.string.disable_battery_optimization_title)) },
            text = { Text(text = stringResource(R.string.disable_battery_optimization_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:" + context.applicationInfo.packageName)
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = stringResource(R.string.disable))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        settingsManager.areOptimizationDialogSkipped = true
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}