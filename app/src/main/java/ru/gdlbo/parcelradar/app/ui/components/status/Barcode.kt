package ru.gdlbo.parcelradar.app.ui.components.status

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val bottomSheetState = rememberModalBottomSheetState()
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

    ModalBottomSheet(
        onDismissRequest = { updateShowBarcodeBottomSheet(false) },
        sheetState = bottomSheetState,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = trackingNumber,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Image(
                    bitmap = barcodeBitmap,
                    contentDescription = trackingNumber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                            updateShowBarcodeBottomSheet(false)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )
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