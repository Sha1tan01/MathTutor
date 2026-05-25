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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.mathtutor.app.databinding.FragmentProgressBinding
import ru.mathtutor.app.domain.model.TopicStatus

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProgress()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val p = state.progress ?: return@collect

                    val percent = if (p.totalTopics > 0)
                        (p.completedTopics * 100 / p.totalTopics) else 0

                    binding.tvCompletedCount.text = p.completedTopics.toString()
                    binding.tvTotalCount.text = p.totalTopics.toString()
                    binding.tvPercent.text = "$percent%"
                    binding.progressBarOverall.progress = percent
                }
            }
        }

        // Also observe detailed progress from ProgressRepository
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progressListState.collect { list ->
                    val done       = list.count { it.status == TopicStatus.DONE }
                    val inProgress = list.count { it.status == TopicStatus.IN_PROGRESS }
                    val notStarted = list.count { it.status == TopicStatus.NOT_STARTED }

                    val correctAnswers = list.sumOf { it.practiceCorrect }
                    val totalAnswers   = list.sumOf { it.practiceTotal }

                    binding.tvDoneCount.text        = done.toString()
                    binding.tvInProgressCount.text  = inProgress.toString()
                    binding.tvNotStartedCount.text  = notStarted.toString()
                    binding.tvCorrectAnswers.text   = correctAnswers.toString()
                    binding.tvTotalAnswers.text      = totalAnswers.toString()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
