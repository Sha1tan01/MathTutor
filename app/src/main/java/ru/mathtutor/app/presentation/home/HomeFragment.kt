package ru.mathtutor.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.mathtutor.app.R
import ru.mathtutor.app.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        // Both buttons go to Sections (theory list)
        binding.btnLectures.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sectionsFragment)
        }
        binding.btnTasks.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sectionsFragment)
        }
        binding.btnAiChat.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(
                topicId = null, topicTitle = null, sectionTitle = null
            )
            findNavController().navigate(action)
        }
        binding.btnProgress.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_progressFragment)
        }
        binding.cardLastTopic.setOnClickListener {
            val state = viewModel.uiState.value
            val topicId = state.progress?.lastTopicId ?: return@setOnClickListener
            val topicTitle = state.progress.lastTopicTitle ?: return@setOnClickListener
            val sectionTitle = state.progress.lastSectionTitle ?: return@setOnClickListener
            val action = HomeFragmentDirections.actionHomeFragmentToTopicFragment(
                topicId = topicId,
                topicTitle = topicTitle,
                sectionTitle = sectionTitle
            )
            findNavController().navigate(action)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressIndicator.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    state.progress?.let { p ->
                        val percent = if (p.totalTopics > 0)
                            (p.completedTopics * 100 / p.totalTopics) else 0
                        binding.tvProgressPercent.text = "$percent%"
                        binding.progressBar.progress = percent
                        binding.tvProgressSubtitle.text =
                            "${p.completedTopics} из ${p.totalTopics} тем завершено"

                        if (p.lastTopicId != null) {
                            binding.cardLastTopic.visibility = View.VISIBLE
                            binding.tvLastTopicTitle.text = p.lastTopicTitle
                            binding.tvLastTopicSection.text = p.lastSectionTitle
                        } else {
                            binding.cardLastTopic.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
