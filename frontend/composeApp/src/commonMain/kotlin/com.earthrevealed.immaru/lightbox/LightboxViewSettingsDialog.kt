package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

const val LIGHTBOX_VIEW_SETTINGS_CAPTION_SWITCH_TAG = "lightbox-view-settings-caption-switch"
const val LIGHTBOX_VIEW_SETTINGS_ZOOM_SLIDER_TAG = "lightbox-view-settings-zoom-slider"
const val LIGHTBOX_VIEW_SETTINGS_ZOOM_LABEL_TAG = "lightbox-view-settings-zoom-label"
const val ASSET_THUMBNAIL_CAPTION_TAG = "asset-thumbnail-caption"

@Composable
fun LightboxViewSettingsDialog(
    showAssetFilenameCaption: Boolean,
    thumbnailZoomPercent: Int,
    onShowAssetFilenameCaptionChange: (Boolean) -> Unit,
    onThumbnailZoomPercentChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var thumbnailZoomSlider by remember(thumbnailZoomPercent) {
        mutableStateOf(thumbnailZoomPercent.toFloat())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("View settings") },
        text = {
            Column {
                Text(text = "Show filename captions")
                Switch(
                    checked = showAssetFilenameCaption,
                    onCheckedChange = onShowAssetFilenameCaptionChange,
                    modifier = Modifier.testTag(LIGHTBOX_VIEW_SETTINGS_CAPTION_SWITCH_TAG),
                )

                Text(
                    text = "Thumbnail size: ${thumbnailZoomSlider.roundToInt()}%",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag(LIGHTBOX_VIEW_SETTINGS_ZOOM_LABEL_TAG)
                )
                Slider(
                    value = thumbnailZoomSlider,
                    onValueChange = { thumbnailZoomSlider = it },
                    valueRange = LightboxViewModel.MIN_THUMBNAIL_ZOOM_PERCENT.toFloat()..LightboxViewModel.MAX_THUMBNAIL_ZOOM_PERCENT.toFloat(),
                    onValueChangeFinished = {
                        onThumbnailZoomPercentChange(thumbnailZoomSlider.roundToInt())
                    },
                    modifier = Modifier.testTag(LIGHTBOX_VIEW_SETTINGS_ZOOM_SLIDER_TAG)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
