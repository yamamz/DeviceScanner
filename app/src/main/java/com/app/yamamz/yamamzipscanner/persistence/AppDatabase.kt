package com.app.yamamz.yamamzipscanner.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.model.Vendor


@Database(entities = [Device::class, Vendor::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun vendorDao(): VendorDao
}