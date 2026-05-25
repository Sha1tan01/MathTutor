package ru.mathtutor.app.presentation.theory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.mathtutor.app.databinding.FragmentTopicsBinding

@AndroidEntryPoint
class TopicsFragment : Fragment() {

    private var _binding: FragmentTopicsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TopicsViewModel by viewModels()
    private val args: TopicsFragmentArgs by navArgs()
    private lateinit var adapter: TopicsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSectionTitle.text = args.sectionTitle
        setupRecyclerView()
        observeState()
        viewModel.loadTopics(args.sectionId)
    }

    private fun setupRecyclerView() {
        adapter = TopicsAdapter { topic ->
            val action = TopicsFragmentDirections.actionTopicsFragmentToTopicFragment(
                topicId = topic.id,
                topicTitle = topic.title,
                sectionTitle = args.sectionTitle
            )
            findNavController().navigate(action)
        }
        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressIndicator.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    adapter.submitList(state.topics)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
