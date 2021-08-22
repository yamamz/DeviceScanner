package com.app.yamamz.yamamzipscanner.di

import com.app.yamamz.yamamzipscanner.datasource.NetworkScannerLocalDataSourceImpl
import com.app.yamamz.yamamzipscanner.datasource.NetworkScannerRemoteDataSourceImpl
import com.app.yamamz.yamamzipscanner.repository.DeviceScannerRepositoryImpl
import com.app.yamamz.yamamzipscanner.repository.DeviceScannerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideHomeRepository(
        remoteDataSource: NetworkScannerRemoteDataSourceImpl,
        localDataSource: NetworkScannerLocalDataSourceImpl
    ): DeviceScannerRepository {
        return DeviceScannerRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource
        )
    }
}

