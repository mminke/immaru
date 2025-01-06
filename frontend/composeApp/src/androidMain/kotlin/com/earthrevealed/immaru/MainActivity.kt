package com.earthrevealed.immaru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import createAndroidDataStore
import io.github.vinceglb.filekit.core.FileKit
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        setContent {
            ImmaruApp(
                module {
                    single { createAndroidDataStore(this@MainActivity) }
                }
            )
        }
    }
}
