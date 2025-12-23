package ru.gdlbo.parcelradar.app.nav.selected.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.ui.components.status.Barcode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelActionsSection(
    tracking: Tracking,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit,
    popBack: () -> Unit
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Barcode button
            FilledTonalButton(
                onClick = { showBarcodeDialog = true },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_barcode_24),
                    contentDescription = "Barcode"
                )
            }

            // Edit button
            FilledTonalButton(
                onClick = { showEditSheet = true },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Outlined.Create, contentDescription = "Edit")
            }

            // Remove button
            FilledTonalButton(
                onClick = { showDeleteSheet = true },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove")
            }
        }
    }

    if (showEditSheet) {
        EditTitleBottomSheet(
            title = tracking.title ?: "",
            tracking = tracking,
            onDismissRequest = { showEditSheet = false },
            updateItem = updateItem,
        )
    }

    if (showDeleteSheet) {
        DeleteBottomSheet(
            tracking,
            deleteItem = deleteItem,
            forceUpdateDB = forceUpdateDB,
            onDismissRequest = { showDeleteSheet = false },
            popBack = popBack
        )
    }

    if (showBarcodeDialog) {
        Barcode(
            tracking.trackingNumberCurrent ?: tracking.trackingNumber,
            true,
            updateShowBarcodeBottomSheet = { showBarcodeDialog = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBottomSheet(
    tracking: Tracking?,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    onDismissRequest: () -> Unit,
    popBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.are_you_sure_remove),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.are_you_sure_remove_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Text(stringResource(R.string.no), color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        deleteItem(tracking)
                        onDismissRequest()
                        forceUpdateDB()
                        popBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.yes))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTitleBottomSheet(
    title: String,
    tracking: Tracking?,
    onDismissRequest: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var titleState by remember { mutableStateOf(TextFieldValue(title)) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleState = TextFieldValue(
            text = titleState.text,
            selection = TextRange(titleState.text.length)
        )
        try {
            delay(400)
            focusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        contentWindowInsets = { WindowInsets.ime }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .imePadding()
        ) {
            Text(
                text = stringResource(R.string.edit_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = titleState,
                onValueChange = { newText ->
                    titleState = newText
                },
                label = { Text(stringResource(R.string.parcel_name)) },
                placeholder = { Text(stringResource(R.string.edit_title_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        updateItem(tracking, titleState.text)
                        onDismissRequest()
                    },
                    enabled = titleState.text.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}