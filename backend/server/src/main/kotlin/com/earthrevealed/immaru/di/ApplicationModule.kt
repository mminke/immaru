package com.earthrevealed.immaru.di

import com.earthrevealed.immaru.Configuration
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.library.Library
import com.earthrevealed.immaru.assets.repositories.r2dbc.R2dbcAssetRepository
import com.earthrevealed.immaru.collections.CollectionRepository
import com.earthrevealed.immaru.collections.repositories.r2dbc.R2dbcCollectionRepository
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.koin.dsl.module

val applicationModule = module {
    single<ConnectionFactory> {
        ConnectionFactories.get(Configuration.immaru.database.r2dbc.url)
    }
    single {
        Library(Configuration.immaru.library.path)
    }
    single<CollectionRepository> {
        R2dbcCollectionRepository(get())
    }
    single<AssetRepository> {
        R2dbcAssetRepository(get(), get())
    }
}
