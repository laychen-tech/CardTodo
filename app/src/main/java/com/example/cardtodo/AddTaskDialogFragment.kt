package com.example.cardtodo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cardtodo.databinding.DialogAddTaskBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTaskDialogFragment(
    private val onTaskAdded: (String, String, Priority) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!
    private var selectedPriority = Priority.MEDIUM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.Theme_CardTodo_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 优先级按钮组
        updatePriorityUI()
        binding.btnPriorityHigh.setOnClickListener {
            selectedPriority = Priority.HIGH
            updatePriorityUI()
        }
        binding.btnPriorityMedium.setOnClickListener {
            selectedPriority = Priority.MEDIUM
            updatePriorityUI()
        }
        binding.btnPriorityLow.setOnClickListener {
            selectedPriority = Priority.LOW
            updatePriorityUI()
        }

        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isBlank()) {
                binding.etTitle.error = "标题不能为空"
                return@setOnClickListener
            }
            val description = binding.etDescription.text.toString().trim()
            onTaskAdded(title, description, selectedPriority)
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun updatePriorityUI() {
        val highSelected = selectedPriority == Priority.HIGH
        val medSelected = selectedPriority == Priority.MEDIUM
        val lowSelected = selectedPriority == Priority.LOW

        binding.btnPriorityHigh.isSelected = highSelected
        binding.btnPriorityMedium.isSelected = medSelected
        binding.btnPriorityLow.isSelected = lowSelected

        val highAlpha = if (highSelected) 1f else 0.45f
        val medAlpha = if (medSelected) 1f else 0.45f
        val lowAlpha = if (lowSelected) 1f else 0.45f

        binding.btnPriorityHigh.alpha = highAlpha
        binding.btnPriorityMedium.alpha = medAlpha
        binding.btnPriorityLow.alpha = lowAlpha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
