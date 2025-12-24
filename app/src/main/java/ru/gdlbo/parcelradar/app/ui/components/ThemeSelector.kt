package ru.gdlbo.parcelradar.app.ui.components

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager

@Composable
fun ThemeSelector(
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by themeManager.isDarkTheme.collectAsState()
    val isDynamicColor by themeManager.isDynamicColor.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val themeOptions = remember {
            listOf(
                ThemeOption(DarkThemeEnum.Light.value, DarkThemeEnum.Light.id, Icons.Filled.LightMode),
                ThemeOption(DarkThemeEnum.Dark.value, DarkThemeEnum.Dark.id, Icons.Filled.DarkMode),
                ThemeOption(DarkThemeEnum.System.value, DarkThemeEnum.System.id, Icons.Filled.Settings)
            )
        }

        ThemeSection(
            title = stringResource(R.string.theme_label),
            options = themeOptions,
            selectedValue = isDarkTheme,
            onSelect = { themeManager.setDarkThemeValue(it as Boolean?) }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamicOptions = remember {
                listOf(
                    ThemeOption(DynamicThemeEnum.On.value, DynamicThemeEnum.On.id, Icons.Filled.Palette),
                    ThemeOption(DynamicThemeEnum.Off.value, DynamicThemeEnum.Off.id, Icons.Filled.Block),
                    ThemeOption(DynamicThemeEnum.System.value, DynamicThemeEnum.System.id, Icons.Filled.Settings)
                )
            }

            ThemeSection(
                title = stringResource(R.string.dynamic_color_label),
                options = dynamicOptions,
                selectedValue = isDynamicColor,
                onSelect = { themeManager.setDynamicColorValue(it as Boolean?) }
            )
        }
    }
}

@Composable
private fun ThemeSection(
    title: String,
    options: List<ThemeOption>,
    selectedValue: Any?,
    onSelect: (Any?) -> Unit
) {
    SettingCard(
        title = title,
        subtitle = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                ThemeSelectionCard(
                    option = option,
                    isSelected = option.value == selectedValue,
                    onClick = { onSelect(option.value) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "containerColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "borderWidth"
    )

    Surface(
        modifier = modifier
            .height(110.dp)
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 8.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(animationSpec = tween(150, delayMillis = 50)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(150)) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class ThemeOption(
    val value: Any?,
    val labelRes: Int,
    val icon: ImageVector
)

enum class DarkThemeEnum(val value: Boolean?, val id: Int) {
    Dark(true, R.string.dark_theme),
    Light(false, R.string.light_theme),
    System(null, R.string.system_default)
}

enum class DynamicThemeEnum(val value: Boolean?, val id: Int) {
    On(true, R.string.on_label),
    Off(false, R.string.off_label),
    System(null, R.string.system_default)
}
