package com.earthrevealed.immaru

import androidx.compose.runtime.Composable
import com.earthrevealed.immaru.koin.config.appModule
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

@Composable
fun ImmaruApp(platformSpecificModule: Module) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(platformSpecificModule, appModule)
        }
    ) {
        MainNavigation()
    }
}
