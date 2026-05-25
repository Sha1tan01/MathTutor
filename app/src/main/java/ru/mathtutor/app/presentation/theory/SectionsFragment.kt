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
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.mathtutor.app.databinding.FragmentSectionsBinding

@AndroidEntryPoint
class SectionsFragment : Fragment() {

    private var _binding: FragmentSectionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SectionsViewModel by viewModels()
    private lateinit var adapter: SectionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = SectionsAdapter { section ->
            val action = SectionsFragmentDirections
                .actionSectionsFragmentToTopicsFragment(
                    sectionId = section.id,
                    sectionTitle = section.title
                )
            findNavController().navigate(action)
        }
        binding.rvSections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSections.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressIndicator.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    adapter.submitList(state.sections)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
