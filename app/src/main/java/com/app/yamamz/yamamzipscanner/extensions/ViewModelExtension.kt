package com.app.yamamz.yamamzipscanner.extensions


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

val ViewModel.viewModelIOContext: CoroutineContext
    inline get() = viewModelScope.coroutineContext + Dispatchers.IO