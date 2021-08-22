package com.app.yamamz.yamamzipscanner.ui.details

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.app.yamamz.yamamzipscanner.model.Port
import com.app.yamamz.yamamzipscanner.repository.DeviceScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
 var deviceScannerDeviceScannerRepository: DeviceScannerRepository
) : ViewModel() {

    @MainThread
    fun searchOpenPorts(ipString: String, scope: CoroutineScope) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            deviceScannerDeviceScannerRepository.searchOpenPorts(
                scope=scope,
                ipString = ipString,
                onStart = {
                    _isLoading.postValue(true)
                },
                onSuccess = {
                    _isLoading.postValue(false)
                },
                onError = { _isLoading.postValue(false) }).collect {
                _openPorts.value = it
                if (it.isNotEmpty()) {
                    _isLoading.postValue(false)
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
                _isWifiConnected.value = it
            }
        }
    }


    @MainThread
    fun pingIp(ip:String) {
        viewModelScope.launch(Dispatchers.IO) {
           _ping.postValue(deviceScannerDeviceScannerRepository.getPing(ip))
        }
    }

    @MainThread
    fun getExternalIp() {
        viewModelScope.launch(Dispatchers.IO) {
            _externalIp.postValue(deviceScannerDeviceScannerRepository.getExternalIp())
        }
    }

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _openPorts: MutableLiveData<List<Port>> = MutableLiveData(mutableListOf())
    val openPorts: LiveData<List<Port>> get() = _openPorts
    private val _isWifiConnected: MutableLiveData<Boolean> = MutableLiveData(true)
    val isWifiConnected: LiveData<Boolean> get() = _isWifiConnected
    private val _ping: MutableLiveData<String> = MutableLiveData("Pinging")
    val ping: LiveData<String> get() = _ping
    private val _externalIp: MutableLiveData<String> = MutableLiveData("Getting External Ip")
    val externalIp: LiveData<String> get() = _externalIp




}