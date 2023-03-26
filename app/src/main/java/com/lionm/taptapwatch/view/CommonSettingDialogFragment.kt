package com.lionm.taptapwatch.view

import androidx.fragment.app.DialogFragment

abstract class CommonSettingDialogFragment : DialogFragment() {
    interface DialogEventListener {
        fun onClickPositiveButton(time: Long, checkRepetitive: Boolean)
        fun onClickNegativeButton()
    }

    protected var listener: DialogEventListener? = null

    fun setDialogEventListener(listener: DialogEventListener) {
        this.listener = listener
    }
}