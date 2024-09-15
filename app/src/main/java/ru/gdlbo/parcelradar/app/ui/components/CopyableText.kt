package ru.gdlbo.parcelradar.app.ui.components

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.buildAnnotatedString
import ru.gdlbo.parcelradar.app.R

@Composable
fun CopyableText(prefix: String, text: String) {
    val annotatedString = buildAnnotatedString {
        append(prefix)
        append(text)
        addStringAnnotation(
            tag = "Copyable",
            annotation = "Copyable",
            start = prefix.length,
            end = prefix.length + text.length
        )
    }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.clickable {
            val selection = annotatedString.getStringAnnotations(
                tag = "Copyable",
                start = 0,
                end = annotatedString.length
            ).firstOrNull()
            if (selection != null) {
                val clipData = ClipData.newPlainText(
                    "Tracking Number",
                    annotatedString.text.substring(selection.start, selection.end)
                )
                clipboardManager.setClip(clipData.toClipEntry())
                Toast.makeText(
                    context,
                    context.getString(R.string.copy_to_clipboard),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}