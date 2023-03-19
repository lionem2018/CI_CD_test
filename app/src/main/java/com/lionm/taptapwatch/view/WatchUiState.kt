package com.lionm.taptapwatch.view

import com.lionm.taptapwatch.data.WatchMode
import com.lionm.taptapwatch.data.WatchState

data class WatchUiState(
    val watchMode: WatchMode = WatchMode.STOP_WATCH,
    val watchState: WatchState = WatchState.RESET
)