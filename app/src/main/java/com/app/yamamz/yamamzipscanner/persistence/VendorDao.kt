package com.app.yamamz.yamamzipscanner.persistence

import androidx.room.Dao
import androidx.room.Query
import com.app.yamamz.yamamzipscanner.model.Vendor

@Dao
interface VendorDao {
    @Query("SELECT * FROM macvendor WHERE mac = :mac_")
    fun getVendor(mac_: String): Vendor?

    @Query("SELECT * FROM macvendor")
    suspend fun geVendorList(): List<Vendor>
}