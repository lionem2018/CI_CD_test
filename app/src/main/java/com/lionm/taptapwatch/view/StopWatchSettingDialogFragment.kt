package com.lionm.taptapwatch.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.lionm.taptapwatch.databinding.DialogStopWatchSettingBinding

class StopWatchSettingDialogFragment : DialogFragment() {
    interface DialogEventListener {
        fun onClickPositiveButton(alarmTime: Long, checkRepetitive: Boolean)
        fun onClickNegativeButton()
    }

    private var binding: DialogStopWatchSettingBinding? = null
    private var listener: DialogEventListener? = null

    fun setDialogEventListener(listener: DialogEventListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            binding = DialogStopWatchSettingBinding.inflate(requireActivity().layoutInflater)
            initView()

            AlertDialog.Builder(it)
                .setView(binding?.root)
                .setTitle("Set Stop Watch Alarm")
                .setPositiveButton("Apply") { _, _ ->
                    listener?.onClickPositiveButton(calculateTime(), binding?.checkbox?.isChecked ?: false)
                }
                .setNegativeButton("Cancel") { _, _ ->
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initView() {
        binding?.pickerHh?.maxValue = 99
        binding?.pickerHh?.minValue = 0

        binding?.pickerMm?.maxValue = 59
        binding?.pickerMm?.minValue = 0

        binding?.pickerSs?.maxValue = 59
        binding?.pickerSs?.minValue = 0
    }

    private fun calculateTime(): Long {
        val hh = binding?.pickerHh?.value ?: 0
        val mm = binding?.pickerMm?.value ?: 0
        val ss = binding?.pickerSs?.value ?: 0

        return hh * 360L + mm * 60L + ss
    }
}