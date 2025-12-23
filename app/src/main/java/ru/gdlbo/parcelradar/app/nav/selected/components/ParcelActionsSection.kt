package ru.gdlbo.parcelradar.app.nav.selected.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.ui.components.status.Barcode

@Composable
fun ParcelActionsSection(
    tracking: Tracking,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit,
    popBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
                onClick = { showEditDialog = true },
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
                onClick = { showDeleteDialog = true },
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

    if (showEditDialog) {
        EditTitleDialog(
            title = tracking.title ?: "",
            tracking = tracking,
            onDismissRequest = { showEditDialog = false },
            focusRequester = focusRequester,
            focusManager = focusManager,
            updateItem = updateItem,
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            tracking,
            deleteItem = deleteItem,
            forceUpdateDB = forceUpdateDB,
            onDismissRequest = { showDeleteDialog = false },
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

@Composable
fun DeleteDialog(
    tracking: Tracking?,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    onDismissRequest: () -> Unit,
    popBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        title = { Text(text = stringResource(R.string.are_you_sure_remove)) },
        text = { Text(text = stringResource(R.string.are_you_sure_remove_subtitle)) },
        confirmButton = {
            TextButton(
                onClick = {
                    deleteItem(tracking)
                    onDismissRequest()
                    forceUpdateDB()
                    popBack()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.no))
            }
        }
    )
}

@Composable
fun EditTitleDialog(
    title: String,
    tracking: Tracking?,
    onDismissRequest: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    var titleState by remember { mutableStateOf(TextFieldValue(title)) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        titleState = TextFieldValue(
            text = titleState.text,
            selection = TextRange(titleState.text.length)
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.edit_title)) },
        text = {
            OutlinedTextField(
                value = titleState,
                onValueChange = { newText ->
                    titleState = TextFieldValue(
                        text = newText.text,
                        selection = TextRange(newText.text.length)
                    )
                },
                placeholder = { Text(text = stringResource(R.string.edit_title_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    updateItem(tracking, titleState.text)
                    onDismissRequest()
                },
                enabled = titleState.text.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
