package com.lionm.taptapwatch.view

import androidx.lifecycle.ViewModel
import com.lionm.taptapwatch.data.WatchMode
import com.lionm.taptapwatch.data.WatchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private var _watchUiState = MutableStateFlow(WatchUiState())
    val watchUiState: StateFlow<WatchUiState> = _watchUiState.asStateFlow()

    fun changeWatchMode(newWatchMode: WatchMode?) {
        newWatchMode ?: return

        _watchUiState.update { currentState ->
            currentState.copy(
                watchMode = newWatchMode,
                watchState = WatchState.RESET
            )
        }
    }

    fun changeTimerState(state: WatchState) {
        _watchUiState.update { currentState ->
            currentState.copy(
                watchState = state
            )
        }
    }
}