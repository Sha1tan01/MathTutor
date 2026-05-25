package ru.mathtutor.app.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import ru.mathtutor.app.databinding.FragmentChatBinding

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init(args.topicId, args.topicTitle, args.sectionTitle, args.contextContent)

        setupToolbar()
        setupContextBanner()
        setupRecyclerView()
        setupInput()
        observeState()
    }

    private fun setupToolbar() {
        // Show back button only when opened from a topic (has context)
        binding.btnBack.visibility =
            if (args.topicId != null) View.VISIBLE else View.GONE
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnClear.setOnClickListener { viewModel.clearChat() }
    }

    private fun setupContextBanner() {
        if (args.topicTitle != null) {
            binding.contextBanner.visibility = View.VISIBLE
            binding.tvContextText.text = buildString {
                append(args.topicTitle)
                if (args.sectionTitle != null) append(" · ${args.sectionTitle}")
                if (args.contextContent != null) append(" · Конкретное задание")
            }
        } else {
            binding.contextBanner.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.rvMessages.layoutManager =
            LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.etMessage.setText("")
        viewModel.send(text)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.messages) {
                        if (state.messages.isNotEmpty()) {
                            binding.rvMessages.scrollToPosition(state.messages.size - 1)
                        }
                    }
                    binding.typingIndicator.visibility =
                        if (state.isTyping) View.VISIBLE else View.GONE
                    binding.btnSend.isEnabled = !state.isTyping

                    state.error?.let { error ->
                        android.widget.Toast.makeText(
                            requireContext(),
                            error,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
