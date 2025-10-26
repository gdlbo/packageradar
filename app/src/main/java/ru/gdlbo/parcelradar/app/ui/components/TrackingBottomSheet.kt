package ru.gdlbo.parcelradar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
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
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    if (showBottomSheet) {
        AddTrackingBottomSheet(
            onBSStateChange = onBSStateChange,
            bottomSheetState = bottomSheetState,
            coroutineScope = coroutineScope,
            addTracking = addTracking
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
                .background(Color.Black)
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
    leadingIconPainter: Painter
) {
    val shape = RoundedCornerShape(12.dp)

    OutlinedTextField(
        value = parcelName,
        onValueChange = setParcelName,
        label = { Text(parcelNameLabel) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Parcel name input" },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(
                painter = leadingIconPainter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = shape,
        colors = TextFieldDefaults.colors().copy(
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
    leadingIconPainter: Painter,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    coroutineScope: CoroutineScope,
    bottomSheetState: SheetState,
    showScanner: MutableState<Boolean>,
    validate: (String) -> Boolean,
    addTracking: suspend (parcelName: String, trackingNumber: String) -> Unit,
    onBSStateChange: (Boolean) -> Unit,
    trackingError: String? = null
) {
    val shape = RoundedCornerShape(12.dp)

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
                painter = leadingIconPainter,
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
                        painter = painterResource(R.drawable.baseline_content_paste_24),
                        contentDescription = pasteLabel
                    )
                }
                IconButton(onClick = {
                    showScanner.value = true
                    coroutineScope.launch { bottomSheetState.hide() }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_camera_24),
                        contentDescription = scanLabel
                    )
                }
            }
        },
        shape = shape,
        colors = TextFieldDefaults.colors().copy(
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
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
    coroutineScope: CoroutineScope
) {
    val (parcelName, setParcelName) = remember { mutableStateOf("") }
    val (trackingNumber, setTrackingNumber) = remember { mutableStateOf("") }
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
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 10.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .imePadding()
            ) {
                Text(
                    text = addParcelLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                ParcelNameInput(parcelName, setParcelName, parcelNameLabel, painterResource(R.drawable.baseline_label_24))

                Spacer(modifier = Modifier.height(6.dp))

                TrackingNumberInput(
                    parcelName = parcelName,
                    trackingNumber = trackingNumber,
                    setTrackingNumber = { setTrackingNumber(it) },
                    trackingNumberLabel = trackingNumberLabel,
                    pasteLabel = pasteLabel,
                    scanLabel = scanLabel,
                    leadingIconPainter = painterResource(R.drawable.archive_24),
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
                            .padding(start = 8.dp, top = 6.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                onBSStateChange(false)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dismissLabel)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(addLabel)
                    }
                }
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