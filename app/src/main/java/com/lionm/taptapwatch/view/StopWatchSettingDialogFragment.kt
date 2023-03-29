package com.lionm.taptapwatch.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.lionm.taptapwatch.databinding.DialogStopWatchSettingBinding
import com.lionm.taptapwatch.databinding.DialogTimerSettingBinding

class StopWatchSettingDialogFragment : CommonSettingDialogFragment() {

    private var binding: DialogStopWatchSettingBinding? = null

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

        return hh * 3600000L + mm * 60000L + ss * 1000L
    }
}