package com.app.yamamz.yamamzipscanner.ui.home

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.app.yamamz.yamamzipscanner.model.Device
import com.app.yamamz.yamamzipscanner.repository.DeviceScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    var deviceScannerDeviceScannerRepository: DeviceScannerRepository
) : ViewModel() {
    @MainThread
    fun searDevices(ipString: String, scope: CoroutineScope) {
        viewModelScope.launch {
            if (ipString != "") {
                _isLoading.postValue(true)
                deviceScannerDeviceScannerRepository.searchDevicesOnNetwork(ipString = ipString,
                    scope = scope,
                    onStart = {
                        _isLoading.postValue(true)
                    },
                    onSuccess = {
                        _isLoading.postValue(false)
                        searDevicesRecurring(ipString = ipString, scope = scope)
                    },
                    onError = { _isLoading.postValue(false) }).collect {
                    _devices.postValue(it)
                    _isLoading.postValue(false)
                }
            }
        }
    }

    @MainThread
    fun searDevicesInBackground(ipString: String, scope: CoroutineScope) {
        viewModelScope.launch {
            if (ipString != "") {
                deviceScannerDeviceScannerRepository.searchDevicesOnNetwork(ipString = ipString,
                    scope = scope,
                    onStart = {
                    },
                    onSuccess = {
                    },
                    onError = { }).collect {
                    _devices.postValue(it)
                }
            }
        }
    }

    @MainThread
    fun searDevicesRecurring(ipString: String, scope: CoroutineScope) {
        viewModelScope.launch {
            if (ipString != "") {
                deviceScannerDeviceScannerRepository.searchDevicesOnNetworkRecurring(ipString = ipString,
                    scope = scope,
                    onStart = { },
                    onSuccess = { },
                    onError = { }).collect {
                    _devices.postValue(it)
                }
            }
        }
    }

    @MainThread
    fun checkIfWifiConnected() {
        viewModelScope.launch {
            deviceScannerDeviceScannerRepository.checkIfWifiConnected(
                onStart = { },
                onSuccess = { },
                onError = { }).collect {
                _isWifiConnected.postValue(it)
            }
        }
    }

    @MainThread
    fun getCacheDevices(ipString: String, scope: CoroutineScope) {

        viewModelScope.launch(Dispatchers.IO) {
            _devices.postValue(deviceScannerDeviceScannerRepository.getCacheDevice())
            searDevicesRecurring(ipString = ipString, scope = scope)
        }
    }

    @MainThread
    fun getWirelessInternalIpAddress() {
        viewModelScope.launch {
            deviceScannerDeviceScannerRepository.getWirelessInfo(
                onStart = { },
                onSuccess = { },
                onError = { }).collect {
                _wirelessInternalIpAddress.postValue(it)
            }
        }
    }


    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _devices: MutableLiveData<List<Device>> = MutableLiveData(mutableListOf())
    val devices: LiveData<List<Device>> get() = _devices

    private val _isWifiConnected: MutableLiveData<Boolean> = MutableLiveData(true)
    val isWifiConnected: LiveData<Boolean> get() = _isWifiConnected

    private val _wirelessInternalIpAddress: MutableLiveData<String> = MutableLiveData("")
    val wirelessInternalIpAddress: LiveData<String> get() = _wirelessInternalIpAddress


}