package com.lionm.taptapwatch.util

import android.content.Context
import kotlin.math.roundToInt

object ViewUtils {
    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).roundToInt()
    }
}