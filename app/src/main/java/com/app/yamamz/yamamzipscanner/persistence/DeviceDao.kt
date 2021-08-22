package com.app.yamamz.yamamzipscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.yamamz.yamamzipscanner.model.Device


@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceList(devices: List<Device>)

    @Query("SELECT * FROM Device WHERE ipAddress = :id_")
    fun getDevice(id_: Long): LiveData<Device>

    @Query("SELECT * FROM Device")
    suspend fun getDeviceList(): List<Device>
}