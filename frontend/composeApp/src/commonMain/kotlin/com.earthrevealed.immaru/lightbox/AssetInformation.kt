package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.assets.Asset
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.common.Table


@Composable
fun AssetInformationTable(asset: Asset?) {
    if (asset == null) {
        Text("Nothing selected")
    } else {
        val properties = mutableListOf(
            "Name" to asset.name,
        ).apply {
            if (asset is FileAsset) {
                add("Original filename" to asset.originalFilename)
                add("Media type" to asset.mediaType.toString())
            }
        }

        Column(modifier = Modifier.padding(5.dp)) {
            Text("Information")

            Table(
                properties.size,
                2,
                modifier = Modifier.border(1.dp, Color.LightGray).fillMaxWidth()
            ) { row, column ->
                if (column == 0) {
                    Text(
                        properties[row].first,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        properties[row].second,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun AssetInformation(asset: Asset?) {
    if (asset == null) {
        Text("Nothing selected")
    } else {
        val properties = mutableListOf(
            "Name" to asset.name,
        ).apply {
            if (asset is FileAsset) {
                add("Original filename" to asset.originalFilename)
                add("Media type" to asset.mediaType.toString())
            }
        }

        Column(modifier = Modifier.padding(5.dp)) {
            Text("Information")

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
                    .padding(5.dp)
            ) {
                items(properties) { item ->
                    Row {
                        Text(
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth(0.3f),
                            text = item.first,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            item.second, modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth(1f)
                        )
                    }
                }
            }
        }
    }
}