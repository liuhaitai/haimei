package com.haitao.haimei.ui.diary

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.haitao.haimei.R
import com.haitao.haimei.databinding.DialogDiaryImageBinding
import com.haitao.haimei.databinding.FragmentDiaryEditBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.WindowManager

class DiaryEditFragment : Fragment() {
    private var _binding: FragmentDiaryEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Array<String>>
    private val imageAdapter = DiaryImageAdapter { position, uriText ->
        showImagePreview(position, uriText)
    }
    private val selectedImages = mutableListOf<String>()

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

        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult
            val resolver = requireContext().contentResolver
            uris.forEach { uri ->
                try {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Ignore if persistable permission is not granted.
                }
            }
            selectedImages.clear()
            selectedImages.addAll(uris.map(Uri::toString))
            imageAdapter.submit(selectedImages)
        }

        binding.diaryEditImagesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.diaryEditImagesList.adapter = imageAdapter

        binding.diaryEditPickImages.setOnClickListener {
            pickImagesLauncher.launch(arrayOf("image/*"))
        }

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

                val images = entry.imageUris?.split("|")?.filter { it.isNotBlank() }.orEmpty()
                selectedImages.clear()
                selectedImages.addAll(images)
                imageAdapter.submit(selectedImages)
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
                tags = tags.ifBlank { null },
                imageUris = selectedImages.toList()
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

    private fun showImagePreview(position: Int, uriText: String) {
        val dialogBinding = DialogDiaryImageBinding.inflate(layoutInflater)
        dialogBinding.diaryFullImage.setImageURI(Uri.parse(uriText))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.diary_image_delete, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                imageAdapter.removeAt(position)
                selectedImages.clear()
                selectedImages.addAll(imageAdapter.getItems())
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_ENTRY_ID = "entryId"
    }
}
