package com.timothy.gogolook.data.model

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData

sealed class LoadingStatus{
    object Loading:LoadingStatus()
    class Error(val message: String?):LoadingStatus()
    object Success:LoadingStatus()
}

//class LoadingStatusMutableLiveData:MutableLiveData<LoadingStatus>(){
//    @SuppressLint("RestrictedApi")
//    fun setLoading() {
//        when (ArchTaskExecutor.getInstance().isMainThread) {
//            true -> this.value = LoadingStatus.Loading()
//            false -> this.postValue(LoadingStatus.Loading())
//        }
//    }
//
//    @SuppressLint("RestrictedApi")
//    fun setError(message: String) {
//        when (ArchTaskExecutor.getInstance().isMainThread) {
//            true -> this.value = LoadingStatus.Error(message)
//            false -> this.postValue(LoadingStatus.Error(message))
//        }
//    }
//
//    @SuppressLint("RestrictedApi")
//    fun setSuccess() {
//        when (ArchTaskExecutor.getInstance().isMainThread) {
//            true -> this.value = LoadingStatus.Success()
//            false -> this.postValue(LoadingStatus.Success())
//        }
//    }
//}
