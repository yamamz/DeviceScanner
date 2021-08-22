package com.app.yamamz.yamamzipscanner.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.app.yamamz.yamamzipscanner.datasource.NetworkScannerLocalDataSource
import com.app.yamamz.yamamzipscanner.datasource.NetworkScannerRemoteDataSource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.model.Port

class DeviceScannerRepositoryImpl @Inject constructor(
    private val localDataSource: NetworkScannerLocalDataSource,
    private val remoteDataSource: NetworkScannerRemoteDataSource
) : DeviceScannerRepository {

    @WorkerThread
    override suspend fun getCacheDevice(): List<Device> {
        return localDataSource.getCacheDevice()
    }

    @WorkerThread
    override suspend fun getPing(ip: String): String {
        return try {
            remoteDataSource.getPing(ip)
        } catch (e: Exception) {
            "Unreachable"
        }
    }

    @WorkerThread
    override suspend fun getExternalIp(): String {
        return try {
            remoteDataSource.getExternalIp()
        } catch (e: Exception) {
            "Unreachable"
        }
    }

    @WorkerThread
    override fun searchDevicesOnNetworkRecurring(
        scope: CoroutineScope,
        ipString: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Device>> = flow {
        while (true) {
            try {
                val devices =
                    remoteDataSource.getDevicesOnNetwork(ipString = ipString, scope = scope)
                val transformDevices = transformDeviceWithMacVendor(devices)
                localDataSource.insertDeviceList(transformDevices)
                val saveDevices = localDataSource.getCacheDevice()
                val mapDeviceIsOnline =
                    transformDeviceOnActive(saveDevices = saveDevices, devices = devices)
                emit(mapDeviceIsOnline)
            } catch (e: Exception) {
                onError(e.message);
            }
            delay(60000);
        }


    }.onStart { onStart() }
        .onCompletion {
            onSuccess()
        }
        .flowOn(Dispatchers.IO)

    @WorkerThread
    override fun searchDevicesOnNetwork(
        ipString: String,
        scope: CoroutineScope,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Device>> = flow {
        try {
            val devices = remoteDataSource.getDevicesOnNetwork(ipString = ipString, scope = scope)
            val transformDevices = transformDeviceWithMacVendor(devices)
            localDataSource.insertDeviceList(transformDevices)
            val saveDevices = localDataSource.getCacheDevice()
            val mapDeviceIsOnline =
                transformDeviceOnActive(saveDevices = saveDevices, devices = devices)
            emit(mapDeviceIsOnline)
        } catch (e: Exception) {
            onError(e.message);
        }

    }.onStart {
        onStart()
    }.onCompletion {
        onSuccess()
    }.flowOn(Dispatchers.IO)

    private fun transformDeviceWithMacVendor(devices: List<Device>): List<Device> {
        return devices.map {
            var vendor = localDataSource.getVendor(
                it.macAddress.substring(0, 8)
                    .uppercase()
            )
            if (vendor == null) {
                vendor = "No vendor found"
            }
            Device(
                it.ipAddress,
                it.deviceName,
                it.macAddress,
                vendor,
                true,
            )
        }.toList()
    }

    private fun transformDeviceOnActive(
        saveDevices: List<Device>,
        devices: List<Device>
    ): List<Device> {
        return saveDevices.map {
            val isContain = contains(devices, it.ipAddress)
            Log.e("IsContains", "$isContain")
            if (isContain) {
                Device(
                    it.ipAddress,
                    it.deviceName,
                    it.macAddress,
                    it.macVendor,
                    true,
                )
            } else {
                Device(
                    it.ipAddress,
                    it.deviceName,
                    it.macAddress,
                    it.macVendor,
                    false,
                )
            }
        }.toList()
    }


    @WorkerThread
    override fun searchOpenPorts(
        scope: CoroutineScope,
        ipString: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Port>> = flow {
        try {
            val openPorts = remoteDataSource.getOpenPort(ipString = ipString, scope = scope)
            emit(openPorts)
        } catch (e: Exception) {
            onError(e.message);
        }

    }.onStart {
        onStart()
    }.onCompletion {
        onSuccess()
    }.flowOn(Dispatchers.IO)

    private fun contains(list: List<Device>, ipString: String): Boolean {
        if (list.isEmpty()) return true
        for (item in list) {
            if (item.ipAddress == ipString) {
                return true
            }
        }
        return false
    }

    @WorkerThread
    override fun checkIfWifiConnected(
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<Boolean> = flow {

        try {
            while (true) {
                val isWifiConnected = remoteDataSource.checkWifiConnected()
                emit(isWifiConnected)
                delay(2000)
            }
        } catch (e: Exception) {
            onError(e.message);
        }

    }.onStart {
        onStart()
    }.onCompletion {
        onSuccess()
    }.flowOn(Dispatchers.IO)

    @WorkerThread
    override fun getWirelessInfo(
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<String> = flow {
        try {
            emit(remoteDataSource.getWifiInternalIpAddress())
        } catch (e: Exception) {
            onError(e.message);
        }
    }.onStart {
        onStart()
    }.onCompletion {
        onSuccess()
    }.flowOn(Dispatchers.IO)

}