package com.app.yamamz.yamamzipscanner.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
@Immutable
data class Device(
    @PrimaryKey val ipAddress: String,
    val deviceName: String,
    val macAddress: String,
    val macVendor:String,
    val isActive: Boolean,
)





