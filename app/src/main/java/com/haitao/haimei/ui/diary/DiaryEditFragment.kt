package com.haitao.haimei.ui.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.haitao.haimei.R
import com.haitao.haimei.databinding.FragmentDiaryEditBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class DiaryEditFragment : Fragment() {
    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val entryId = arguments?.getString(ARG_ENTRY_ID)

        binding.diaryEditMoodGroup.check(R.id.diary_edit_mood_all)
        binding.diaryEditTimeText.text = formatter.format(LocalDateTime.now())
        binding.diaryEditTimeText.tag = System.currentTimeMillis()

        binding.diaryEditTimeButton.setOnClickListener {
            showDateTimePicker { millis ->
                binding.diaryEditTimeText.text = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(formatter)
                binding.diaryEditTimeText.tag = millis
            }
        }

        binding.diaryEditDeleteButton.visibility = View.GONE
        binding.diaryEditTitleLabel.text = getString(R.string.diary_edit_add_title)

        if (!entryId.isNullOrBlank()) {
            binding.diaryEditDeleteButton.visibility = View.VISIBLE
            binding.diaryEditTitleLabel.text = getString(R.string.diary_edit_edit_title)
            viewLifecycleOwner.lifecycleScope.launch {
                val entry = viewModel.getEntry(entryId)
                if (entry == null) return@launch
                binding.diaryEditTitle.setText(entry.title)
                binding.diaryEditContent.setText(entry.content)
                binding.diaryEditTags.setText(entry.tags.orEmpty())
                binding.diaryEditTimeText.text = Instant.ofEpochMilli(entry.time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(formatter)
                binding.diaryEditTimeText.tag = entry.time
                binding.diaryEditMoodGroup.check(moodButtonIdFromCode(entry.mood))
            }
        }

        binding.diaryEditSaveButton.setOnClickListener {
            val title = binding.diaryEditTitle.text?.toString().orEmpty()
            val content = binding.diaryEditContent.text?.toString().orEmpty()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), R.string.diary_edit_content_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tags = binding.diaryEditTags.text?.toString()?.trim().orEmpty()
            val timeMillis = binding.diaryEditTimeText.tag as? Long ?: System.currentTimeMillis()
            val mood = moodFilterFromButtonId(binding.diaryEditMoodGroup.checkedButtonId).code

            viewModel.upsertEntry(
                id = entryId,
                title = title,
                content = content,
                time = timeMillis,
                mood = mood,
                tags = tags.ifBlank { null }
            )
            findNavController().popBackStack()
        }

        binding.diaryEditDeleteButton.setOnClickListener {
            if (entryId.isNullOrBlank()) {
                findNavController().popBackStack()
                return@setOnClickListener
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.diary_edit_delete)
                .setMessage(R.string.diary_edit_delete_confirm)
                .setPositiveButton(R.string.diary_edit_delete) { _, _ ->
                    viewModel.deleteEntry(entryId)
                    findNavController().popBackStack()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun showDateTimePicker(onSelected: (Long) -> Unit) {
        val current = binding.diaryEditTimeText.tag as? Long ?: System.currentTimeMillis()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.diary_edit_pick_time))
            .setSelection(current)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            onSelected(selection)
        }

        picker.show(childFragmentManager, "diary_time_picker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_ENTRY_ID = "entryId"
    }
}
