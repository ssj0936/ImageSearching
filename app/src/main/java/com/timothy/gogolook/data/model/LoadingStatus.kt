package com.timothy.gogolook.data.model

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData

sealed class LoadingStatus(
    val message: String? = null
){
    class Loading:LoadingStatus()
    class Error(message: String):LoadingStatus(message)
    class Success:LoadingStatus()
}

class LoadingStatusMutableLiveData:MutableLiveData<LoadingStatus>(){
    @SuppressLint("RestrictedApi")
    fun setLoading() {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.Loading()
            false -> this.postValue(LoadingStatus.Loading())
        }
    }

    @SuppressLint("RestrictedApi")
    fun setError(message: String) {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.Error(message)
            false -> this.postValue(LoadingStatus.Error(message))
        }
    }

    @SuppressLint("RestrictedApi")
    fun setSuccess() {
        when (ArchTaskExecutor.getInstance().isMainThread) {
            true -> this.value = LoadingStatus.Success()
            false -> this.postValue(LoadingStatus.Success())
        }
    }
}
