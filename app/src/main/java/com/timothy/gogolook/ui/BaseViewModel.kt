package com.timothy.gogolook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ViewModelState
interface ViewModelEvent
interface ViewModelEffect

abstract class BaseViewModel<State:ViewModelState, Event:ViewModelEvent, Effect:ViewModelEffect>:ViewModel(){
    private val initState by lazy { initState() }

    //state
    private val _uiState:MutableStateFlow<State> = MutableStateFlow(initState)
    val uiState = _uiState.asStateFlow()
    val currentState:State get() = _uiState.value

    //effect
    private val _uiEvent:MutableSharedFlow<Event> = MutableSharedFlow()
    val uiEvent = _uiEvent.asSharedFlow()

    //effect
    private val _uiEffect:MutableSharedFlow<Effect> = MutableSharedFlow()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiEvent.collect{handleEvent(it)}
        }
    }

    fun setEffect(effect: Effect) = viewModelScope.launch {
        _uiEffect.emit(effect)
    }

    fun setState(reduce:State.()->State){
        val newState = _uiState.value.reduce()
        _uiState.value = newState
    }

    abstract fun initState():State
    abstract fun handleEvent(event: Event)
}