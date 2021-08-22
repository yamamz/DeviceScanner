package com.app.yamamz.yamamzipscanner.datasource

import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.persistence.DeviceDao
import com.app.yamamz.yamamzipscanner.persistence.VendorDao
import javax.inject.Inject

interface NetworkScannerLocalDataSource {
     fun getVendor(mac:String):String?
    suspend fun getCacheDevice(): List<Device>
    suspend fun insertDeviceList(devices: List<Device>)

}

class NetworkScannerLocalDataSourceImpl @Inject constructor(private val deviceDao: DeviceDao,
                                                            private val vendorDao: VendorDao
) :NetworkScannerLocalDataSource {
    override  fun getVendor(mac: String): String? {
        return vendorDao.getVendor(mac.substring(0, 8)
            .uppercase())?.vendor
    }

    override  suspend fun getCacheDevice(): List<Device>{
        return try {
            deviceDao.getDeviceList()
        } catch (e:Exception) {
            emptyList()
        }
    }

    override suspend fun insertDeviceList(devices: List<Device>) {
        deviceDao.insertDeviceList(devices)
    }
}