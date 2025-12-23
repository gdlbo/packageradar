package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.camera.BarcodeScannerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingBottomSheet(
    addTracking: (String, String) -> Unit,
    onBSStateChange: (Boolean) -> Unit,
    showBottomSheet: Boolean,
    initialTrackingNumber: String? = null
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    if (showBottomSheet) {
        AddTrackingBottomSheet(
            onBSStateChange = onBSStateChange,
            bottomSheetState = bottomSheetState,
            coroutineScope = coroutineScope,
            addTracking = addTracking,
            initialTrackingNumber = initialTrackingNumber
        )
    }
}

@Composable
fun BarcodeScannerDialog(
    onBarcodeScanned: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .aspectRatio(1f)
                .background(Color.Black, RoundedCornerShape(16.dp))
        ) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    onBarcodeScanned(barcode)
                    onDismissRequest()
                },
                onClose = onDismissRequest
            )
        }
    }
}

@Composable
fun ParcelNameInput(
    parcelName: String,
    setParcelName: (String) -> Unit,
    parcelNameLabel: String,
    leadingIcon: ImageVector
) {
    val shape = RoundedCornerShape(16.dp)

    OutlinedTextField(
        value = parcelName,
        onValueChange = setParcelName,
        label = { Text(parcelNameLabel) },
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Parcel name input" },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingNumberInput(
    parcelName: String,
    trackingNumber: String,
    setTrackingNumber: (String) -> Unit,
    trackingNumberLabel: String,
    pasteLabel: String,
    scanLabel: String,
    leadingIcon: ImageVector,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    coroutineScope: CoroutineScope,
    bottomSheetState: SheetState,
    showScanner: MutableState<Boolean>,
    validate: (String) -> Boolean,
    addTracking: suspend (parcelName: String, trackingNumber: String) -> Unit,
    onBSStateChange: (Boolean) -> Unit,
    trackingError: String? = null
) {
    val shape = RoundedCornerShape(16.dp)

    OutlinedTextField(
        value = trackingNumber,
        onValueChange = {
            setTrackingNumber(it)
            if (trackingError != null) validate(it)
        },
        label = { Text(trackingNumberLabel) },
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Tracking number input" },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Row {
                IconButton(
                    onClick = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) setTrackingNumber(clip)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = pasteLabel,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    showScanner.value = true
                    coroutineScope.launch { bottomSheetState.hide() }
                }) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = scanLabel,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error
        ),
        isError = trackingError != null,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Ascii
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (validate(trackingNumber)) {
                    coroutineScope.launch {
                        addTracking(parcelName.trim(), trackingNumber.trim())
                        bottomSheetState.hide()
                        onBSStateChange(false)
                    }
                }
            }
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackingBottomSheet(
    addTracking: (String, String) -> Unit,
    onBSStateChange: (Boolean) -> Unit,
    bottomSheetState: SheetState,
    coroutineScope: CoroutineScope,
    initialTrackingNumber: String? = null
) {
    val (parcelName, setParcelName) = remember { mutableStateOf("") }
    val (trackingNumber, setTrackingNumber) = remember { mutableStateOf(initialTrackingNumber ?: "") }
    var trackingError by remember { mutableStateOf<String?>(null) }
    val showScanner = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val errorRequired = stringResource(id = R.string.error_required)
    val errorTooShort = stringResource(id = R.string.error_too_short)
    val pasteLabel = stringResource(id = R.string.paste)
    val scanLabel = stringResource(id = R.string.scan)

    val addParcelLabel = stringResource(id = R.string.add_parcel)
    val parcelNameLabel = stringResource(id = R.string.parcel_name)
    val trackingNumberLabel = stringResource(id = R.string.tracking_number)
    val carrierAutoInfo = stringResource(id = R.string.carrier_auto_info)
    val dismissLabel = stringResource(id = R.string.dismiss)
    val addLabel = stringResource(id = R.string.add)

    fun validate(tracking: String): Boolean {
        trackingError = when {
            tracking.isBlank() -> errorRequired
            tracking.length < 6 -> errorTooShort
            else -> null
        }
        return trackingError == null
    }

    ModalBottomSheet(
        onDismissRequest = { onBSStateChange(false) },
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .imePadding()
            ) {
                Text(
                    text = addParcelLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )

                ParcelNameInput(
                    parcelName,
                    setParcelName,
                    parcelNameLabel,
                    Icons.Outlined.Description
                )

                Spacer(modifier = Modifier.height(16.dp))

                TrackingNumberInput(
                    parcelName = parcelName,
                    trackingNumber = trackingNumber,
                    setTrackingNumber = { setTrackingNumber(it) },
                    trackingNumberLabel = trackingNumberLabel,
                    pasteLabel = pasteLabel,
                    scanLabel = scanLabel,
                    leadingIcon = Icons.Outlined.LocalShipping,
                    clipboardManager = clipboardManager,
                    coroutineScope = coroutineScope,
                    bottomSheetState = bottomSheetState,
                    showScanner = showScanner,
                    validate = ::validate,
                    addTracking = { name, number -> addTracking(name, number) },
                    onBSStateChange = onBSStateChange,
                    trackingError = trackingError
                )

                if (trackingError != null) {
                    Text(
                        text = trackingError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                            .semantics { contentDescription = "Tracking error" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = carrierAutoInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                onBSStateChange(false)
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
                        Text(dismissLabel, color = MaterialTheme.colorScheme.onSurface)
                    }

                    val enabled = listOf(parcelName, trackingNumber).all { it.isNotBlank() } && validate(trackingNumber)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                addTracking(parcelName.trim(), trackingNumber.trim())
                                bottomSheetState.hide()
                                onBSStateChange(false)
                            }
                        },
                        enabled = enabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(addLabel)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )

    if (showScanner.value) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                setTrackingNumber(barcode)
                showScanner.value = false
                coroutineScope.launch { bottomSheetState.show() }
            },
            onDismissRequest = {
                showScanner.value = false
                coroutineScope.launch { bottomSheetState.show() }
            }
        )
    }
}