package com.earthrevealed.immaru.koin.config

import com.earthrevealed.immaru.ImmaruHttpClientProvider
import com.earthrevealed.immaru.asset.AssetDetailsViewModel
import com.earthrevealed.immaru.asset.AssetViewModel
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.repositories.KtorAssetRepository
import com.earthrevealed.immaru.collections.CollectionDetailsViewModel
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.CollectionsViewModel
import com.earthrevealed.immaru.collections.repositories.KtorCollectionRepository
import com.earthrevealed.immaru.common.HttpClientProvider
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import com.earthrevealed.immaru.configuration.ConfigurationViewModel
import com.earthrevealed.immaru.configuration.datastore.DataStoreConfigurationRepository
import com.earthrevealed.immaru.lightbox.BrowseByDateViewViewModel
import com.earthrevealed.immaru.lightbox.LightboxViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::DataStoreConfigurationRepository) { bind<ConfigurationRepository>() }

    singleOf(::ImmaruHttpClientProvider) { bind<HttpClientProvider>() }

    singleOf(::KtorCollectionRepository) { bind<CollectionRepository>() }
    singleOf(::KtorAssetRepository) { bind<AssetRepository>() }

    viewModelOf(::ConfigurationViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModel { params ->
        CollectionDetailsViewModel(
            collectionRepository = get(),
            collectionId = params.getOrNull()
        )
    }
    viewModelOf(::LightboxViewModel)
    viewModelOf(::AssetDetailsViewModel)
    viewModelOf(::AssetViewModel)
    viewModelOf(::BrowseByDateViewViewModel)
}
