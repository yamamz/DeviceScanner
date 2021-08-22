package com.app.yamamz.yamamzipscanner.repository

import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.model.Port
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface DeviceScannerRepository {
    suspend fun getCacheDevice(): List<Device>
    suspend fun getPing(ip: String): String
    suspend fun getExternalIp(): String
    fun searchDevicesOnNetworkRecurring(
        scope: CoroutineScope,
        ipString: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Device>>
    fun searchDevicesOnNetwork(
        ipString: String,
        scope: CoroutineScope,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Device>>

    fun searchOpenPorts(
        scope: CoroutineScope,
        ipString: String,
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<List<Port>>

    fun checkIfWifiConnected(
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<Boolean>

    fun getWirelessInfo(
        onStart: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ): Flow<String>
}