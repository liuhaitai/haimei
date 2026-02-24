package com.haitao.haimei.ui.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haitao.haimei.R
import com.haitao.haimei.databinding.FragmentDiaryBinding
import kotlinx.coroutines.flow.collectLatest

class DiaryFragment : Fragment() {
    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var adapter: DiaryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DiaryListAdapter { entry ->
            val args = Bundle().apply {
                putString(DiaryEditFragment.ARG_ENTRY_ID, entry.id)
            }
            findNavController().navigate(R.id.diaryEditFragment, args)
        }

        binding.diaryList.layoutManager = LinearLayoutManager(requireContext())
        binding.diaryList.adapter = adapter

        binding.diaryFilterAll.isChecked = true
        binding.diaryMoodFilters.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            viewModel.setMoodFilter(moodFilterFromButtonId(checkedId))
        }

        binding.diarySearchEdit.addTextChangedListener {
            viewModel.setSearchQuery(it?.toString().orEmpty())
        }

        binding.diaryAddFab.setOnClickListener {
            findNavController().navigate(R.id.diaryEditFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.entries.collectLatest { entries ->
                adapter.submitList(entries)
                val isEmpty = entries.isEmpty()
                binding.diaryEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.diaryList.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
