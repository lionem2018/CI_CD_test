package com.lionm.taptapwatch.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.lionm.taptapwatch.data.WatchMode
import com.lionm.taptapwatch.databinding.ButtonGroupWatchModeBinding

class WatchModeRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): LinearLayout(context, attrs) {
    private val binding: ButtonGroupWatchModeBinding =
        ButtonGroupWatchModeBinding.inflate(LayoutInflater.from(context), this, true)

    private var onWatchModeChange: ((WatchMode?) -> Unit)? = null

    init {
        initView()
    }

    private fun initView() {
        binding.radioGroup.children.forEach { child ->
            setOnWatchModeChange(child)
        }
    }

    private fun setOnWatchModeChange(button: View) {
        button.setOnClickListener { view ->
            val stringViewId = resources.getResourceEntryName(view.id)
            val mode = WatchMode.findWithId(stringViewId)
            onWatchModeChange?.let { it(mode) }
        }
    }

    fun setOnWatchModeChange(listener: (WatchMode?) -> Unit) {
        this.onWatchModeChange = listener
    }
}