package com.earthrevealed.immaru.components.searchbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Immutable
data class ChipData(
    val text: String,
)

@Composable
private fun InputBarChip(
    data: ChipData,
    onDeleteClick: () -> Unit
) {
    InputChip(
        modifier = Modifier,
        shape = RoundedCornerShape(50),
        selected = true,
        label = {
            Text(
                text = data.text,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        avatar = {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                Modifier.size(InputChipDefaults.IconSize)
            )
        },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove filter",
                Modifier.size(InputChipDefaults.IconSize)
            )
        },
        onClick = {
            onDeleteClick()
        },
    )
}

//@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipInputBar(
    modifier: Modifier = Modifier,
    searchTerms: List<ChipData> = emptyList(),
    onChipCreated: (ChipData) -> Unit,
    onChipRemoved: (ChipData) -> Unit,
) {
    var text by remember {
        mutableStateOf("")
    }

    FlowRow(
//        maxLines = 2,
//        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
//        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        searchTerms.forEachIndexed { index, item ->
            key(item.text) {
                InputBarChip(item, onDeleteClick = { onChipRemoved(item) })
            }
        }

        Box(
            modifier = Modifier.height(InputChipDefaults.Height + 16.dp)
                // This minimum width that TextField can have
                // if remaining space in same row is smaller it's moved to next line
                .widthIn(min = 80.dp)
                // TextField can grow as big as Composable width
                .weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
//                modifier = Modifier.focusRequester(focusRequester),
                value = text,
//                textStyle = TextStyle(
//                    fontSize = 20.sp
//                ),
//                cursorBrush = SolidColor(backgroundColor),
                singleLine = true,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (text.isNotEmpty()) {
                            onChipCreated(
                                ChipData(
                                    text = text
                                )
                            )
                            text = ""
                        }
                    }
                )
            )
        }
    }
}

@Preview
@Composable
fun ChipInputBarSample() {
    val chipDataSnapshotStateList = remember {
        mutableStateListOf<ChipData>().also { it.add(ChipData("Sample chip")) }
    }

    ChipInputBar(
        modifier = Modifier.fillMaxWidth(),//.padding(8.dp),
        searchTerms = chipDataSnapshotStateList,
        onChipCreated = {
            chipDataSnapshotStateList.add(it)
        },
        onChipRemoved = {
            chipDataSnapshotStateList.remove(it)
        },
    )
}
