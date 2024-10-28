package com.earthrevealed.immaru

import com.earthrevealed.immaru.ImmaruApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        setContent {
            ImmaruApp()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    ImmaruApp()
}