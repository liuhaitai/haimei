package com.haitao.haimei.ui.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.haitao.haimei.R
import com.haitao.haimei.databinding.DialogProfileEditBinding
import com.haitao.haimei.databinding.FragmentProfileBinding
import com.haitao.haimei.util.LunarUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.ensureProfile()

        binding.profileEditButton.setOnClickListener {
            showEditDialog()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.profile.collectLatest { profile ->
                val name = profile?.name?.ifBlank { "-" } ?: "-"
                val birthdayText = profile?.birthday?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    date.toString()
                } ?: "-"
                val lunarText = viewModel.currentLunarDate(profile)?.let { lunar ->
                    com.haitao.haimei.util.LunarUtils.lunarDateText(lunar)
                } ?: "-"
                val zodiacName = viewModel.currentZodiacName(profile)
                val traits = profile?.zodiacTraits?.ifBlank { "-" } ?: "-"

                binding.profileName.text = getString(R.string.profile_name_value, name)
                binding.profileBirthday.text = getString(R.string.profile_birthday_value, birthdayText)
                binding.profileLunarBirthday.text = getString(R.string.profile_lunar_birthday_value, lunarText)
                binding.profileZodiac.text = getString(R.string.profile_zodiac_value, zodiacName)
                binding.profileTraits.text = getString(R.string.profile_traits_value, traits)
            }
        }
    }

    private fun showEditDialog() {
        val dialogBinding = DialogProfileEditBinding.inflate(layoutInflater)
        val profile = viewModel.profile.value

        dialogBinding.dialogNameInput.setText(profile?.name.orEmpty())
        val currentBirthdayText = profile?.birthday?.let { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            date.toString()
        } ?: "-"
        dialogBinding.dialogBirthdayText.text = currentBirthdayText

        dialogBinding.dialogBirthdayButton.setOnClickListener {
            showBirthdayPicker { millis ->
                dialogBinding.dialogBirthdayText.text = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
                dialogBinding.dialogBirthdayText.tag = millis
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_edit_entry)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.profile_done) { _, _ ->
                val name = dialogBinding.dialogNameInput.text?.toString().orEmpty()
                viewModel.updateName(name)
                val birthdayTag = dialogBinding.dialogBirthdayText.tag as? Long
                if (birthdayTag != null) {
                    viewModel.updateBirthday(birthdayTag)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showBirthdayPicker(onSelected: (Long) -> Unit) {
        val current = viewModel.profile.value?.birthday ?: System.currentTimeMillis()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("选择生日")
            .setSelection(current)
            .setTextInputFormat(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()))
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            onSelected(millis)
        }

        picker.show(childFragmentManager, "birthday_picker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
