package ru.gdlbo.parcelradar.app.ui.components.status

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Barcode(
    trackingNumber: String,
    showBarcodeBottomSheet: Boolean,
    updateShowBarcodeBottomSheet: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val barcodeBitmap = generateBarcode(trackingNumber, 800, 400)
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(showBarcodeBottomSheet) {
        val window = (context as Activity).window
        val layoutParams = window.attributes

        if (showBarcodeBottomSheet) {
            layoutParams.screenBrightness = 1.0f
        } else {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }

        window.attributes = layoutParams

        onDispose {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = layoutParams
        }
    }

    if (showBarcodeBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { updateShowBarcodeBottomSheet(false) },
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
                        text = trackingNumber,
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
                        Image(
                            bitmap = barcodeBitmap,
                            contentDescription = stringResource(R.string.barcode_content_description, trackingNumber),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
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
                                    updateShowBarcodeBottomSheet(false)
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
                                text = stringResource(R.string.close),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        context.getString(R.string.share_tracking_number_text, trackingNumber)
                                    )
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        context.getString(R.string.share_tracking_number_chooser)
                                    )
                                )
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
                            Text(text = stringResource(R.string.share))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        )
    }
}

fun generateBarcode(trackingNumber: String, width: Int, height: Int): ImageBitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        trackingNumber,
        BarcodeFormat.CODE_128,
        width,
        height
    )
    val bitmap = createBitmap(width, height)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap[x, y] =
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return bitmap.asImageBitmap()
}