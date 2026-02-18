package com.fitness.app.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<T>(initialState: T) : ViewModel() {
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    private val _uiStateLiveData = MutableLiveData<T>(initialState)
    val uiStateLiveData: LiveData<T> = _uiStateLiveData

    protected fun updateState(update: (T) -> T) {
        val newState = update(_uiState.value)
        _uiState.value = newState
        _uiStateLiveData.value = newState
    }
}
