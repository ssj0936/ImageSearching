package com.timothy.gogolook.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ViewModelState

abstract class BaseViewModel<State:ViewModelState>:ViewModel(){
    private val initState by lazy { initState() }

    private val _uiState:MutableStateFlow<State> = MutableStateFlow(initState)
    val uiState = _uiState.asStateFlow()
    val currentState:State get() = _uiState.value

    fun setState(reduce:State.()->State){
        val newState = _uiState.value.reduce()
        _uiState.value = newState
    }

    abstract fun initState():State
}