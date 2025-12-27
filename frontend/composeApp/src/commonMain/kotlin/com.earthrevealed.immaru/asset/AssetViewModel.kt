package com.earthrevealed.immaru.asset

import androidx.lifecycle.ViewModel
import com.earthrevealed.immaru.assets.FileAsset
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssetViewModel(
    configurationRepository: ConfigurationRepository
) : ViewModel() {
    private val configuration = configurationRepository.configuration

    fun contentUrlForAsset(asset: FileAsset): Flow<String?> {
        return configuration
            .map { config ->
                config.useActiveConfiguration?.let { activeName ->
                    config.serverConfigurations.find { it.name == activeName }?.url
                }
            }
            .map { baseUrl ->
                baseUrl?.let {
                    "${it}/api/collections/${asset.collectionId.value}/assets/${asset.id.value}/content"
                }
            }
    }
}
