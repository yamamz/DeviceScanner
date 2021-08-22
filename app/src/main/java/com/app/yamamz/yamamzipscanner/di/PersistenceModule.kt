package com.app.yamamz.yamamzipscanner.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.app.yamamz.yamamzipscanner.persistence.AppDatabase
import com.app.yamamz.yamamzipscanner.persistence.DeviceDao
import com.app.yamamz.yamamzipscanner.persistence.VendorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room
            .databaseBuilder(
                application,
                AppDatabase::class.java,
                "app.db"
            )
            .createFromAsset("vendor.db")
            .fallbackToDestructiveMigration()
            .build()

    }

    @Provides
    @Singleton
    fun provideVendorDao(appDatabase: AppDatabase): VendorDao {
        return appDatabase.vendorDao()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(appDatabase: AppDatabase): DeviceDao {
        return appDatabase.deviceDao()
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Empty implementation, because the schema isn't changing.
    }
}