package ru.parcel.app.ui.components

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.parcel.app.R
import ru.parcel.app.di.theme.ThemeManager

@Composable
fun ThemeSelector(
    themeManager: ThemeManager
) {
    val isDarkTheme by remember { themeManager.isDarkTheme }
    val isDynamicColor by remember { themeManager.isDynamicColor }

    Column(
        modifier = Modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_label)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DarkThemeEnum.entries.forEach { darkState ->
                val selected = darkState.value == isDarkTheme
                SelectableCard(
                    selected = selected,
                    value = darkState.value,
                    onClick = { themeManager.setDarkThemeValue(it) },
                    text = stringResource(darkState.id),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.padding(6.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Text(
                text = stringResource(R.string.dynamic_color_label)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                DynamicThemeEnum.entries.forEach { dynamicState ->
                    val selected = dynamicState.value == isDynamicColor
                    SelectableCard(
                        selected = selected,
                        value = dynamicState.value,
                        onClick = { themeManager.setDynamicColorValue(it) },
                        text = stringResource(dynamicState.id),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableCard(
    selected: Boolean,
    value: Boolean?,
    onClick: (Boolean?) -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val cardColor = if (selected) {
        CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(
        colors = cardColor,
        modifier = modifier.clickable { onClick(value) }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

enum class DarkThemeEnum(val value: Boolean?, @StringRes val id: Int) {
    Dark(true, R.string.dark_theme),
    Light(false, R.string.light_theme),
    System(null, R.string.system_default)
}

enum class DynamicThemeEnum(val value: Boolean?, @StringRes val id: Int) {
    On(true, R.string.on_label),
    Off(false, R.string.off_label),
    System(null, R.string.system_default)
}