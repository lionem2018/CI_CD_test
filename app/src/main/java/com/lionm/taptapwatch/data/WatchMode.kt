package com.lionm.taptapwatch.data

enum class WatchMode(
    val value: String,
    val id: String
) {
    STOP_WATCH("Stop Watch", "stop_watch"),
    TIMER("Timer", "timer"),
    CUSTOM("Custom", "custom");

    companion object {
        fun findWithId(id: String): WatchMode? = values().firstOrNull { it.id == id }
    }
}