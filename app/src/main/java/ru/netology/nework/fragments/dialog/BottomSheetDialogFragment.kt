package ru.netology.nework.fragments.dialog

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentBottomSheetDialogBinding
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.util.BundleKeys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomSheetDialogFragment : BottomSheetDialogFragment(R.layout.fragment_bottom_sheet_dialog) {

    companion object {
        const val TAG = "event_type_dialog"
        const val RESULT_KEY = "event_type_result"
    }

    private var _binding: FragmentBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottomSheetDialogBinding.bind(view)

        binding.textField.setEndIconOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .build()
            datePicker.addOnPositiveButtonClickListener { timestamp ->
                binding.dateTextField.setText(formatTimestamp(timestamp))
            }
            datePicker.show(parentFragmentManager, null)
        }

        binding.buttonConfirm.setOnClickListener {
            sendResultAndDismiss()
        }

        dialog?.setCancelable(true)
    }

    private fun sendResultAndDismiss() {
        val eventType = when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radioButtonOnline -> EventType.ONLINE
            R.id.radioButtonOffline -> EventType.OFFLINE
            else -> EventType.ONLINE
        }

        val dateText = binding.dateTextField.text?.toString()?.trim()
        val timestamp = dateText?.let { parseTimestamp(it) }

        val result = Bundle().apply {
            putString(BundleKeys.EVENT_TYPE, eventType.name)
            if (timestamp != null && timestamp > 0) {
                putLong(BundleKeys.EVENT_DATETIME, timestamp)
            }
        }

        parentFragmentManager.setFragmentResult(RESULT_KEY, result)

        dismissAllowingStateLoss()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun parseTimestamp(text: String): Long? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
            sdf.parse(text)?.time
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}