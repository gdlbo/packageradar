package ru.parcel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.parcel.app.R
import ru.parcel.app.nav.camera.BarcodeScannerScreen

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
    var showScanner by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            onBSStateChange(false)
        },
        sheetState = bottomSheetState,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.add_parcel),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = parcelName,
                        onValueChange = setParcelName,
                        label = { Text(stringResource(id = R.string.parcel_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = setTrackingNumber,
                        label = { Text(stringResource(id = R.string.tracking_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        trailingIcon = {
                            IconButton(onClick = {
                                showScanner = true
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_barcode_24),
                                    contentDescription = "Scan"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    onBSStateChange(false)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text(stringResource(id = R.string.dismiss))
                        }
                        TextButton(
                            onClick = {
                                addTracking(parcelName, trackingNumber)
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    onBSStateChange(false)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(),
                            enabled = listOf(
                                parcelName,
                                trackingNumber
                            ).all { it.isNotEmpty() && it.isNotBlank() }
                        ) {
                            Text(stringResource(id = R.string.add))
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )

    if (showScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                setTrackingNumber(barcode)
                showScanner = false
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            },
            onDismissRequest = {
                showScanner = false
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            }
        )
    }
}