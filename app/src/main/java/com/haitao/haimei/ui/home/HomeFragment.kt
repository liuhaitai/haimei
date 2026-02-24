package com.haitao.haimei.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.haitao.haimei.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.ensureProfile()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.profile.collectLatest { profile ->
                binding.homeName.text = profile?.name?.ifBlank { "她的昵称" } ?: "她的昵称"
                binding.birthdayCountdownSolar.text = viewModel.birthdayCountdownSolarText(profile)
                binding.birthdayCountdownLunar.text = viewModel.birthdayCountdownLunarText(profile)
                binding.birthdaySolar.text = viewModel.birthdaySolarText(profile)
                binding.birthdayLunar.text = viewModel.birthdayLunarText(profile)
                binding.zodiacTitle.text = viewModel.zodiacTitle(profile)
                binding.zodiacSubtitle.text = viewModel.zodiacSubtitle(profile)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.latestDiary.collectLatest { entry ->
                binding.diarySubtitle.text = viewModel.latestDiarySummary(entry)
            }
        }

        binding.addDiaryFab.setOnClickListener {
            // TODO: Navigate to diary editor when available.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
