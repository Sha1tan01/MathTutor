package ru.mathtutor.app.presentation.topic

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
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.mathtutor.app.databinding.FragmentTopicBinding

@AndroidEntryPoint
class TopicFragment : Fragment() {

    private var _binding: FragmentTopicBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TopicViewModel by viewModels()
    private val args: TopicFragmentArgs by navArgs()

    private lateinit var examplesAdapter: ExamplesAdapter
    private lateinit var practiceAdapter: PracticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTopicTitle.text = args.topicTitle
        binding.tvSectionSubtitle.text = args.sectionTitle

        setupTabs()
        setupAdapters()
        setupAiButton()
        observeState()

        viewModel.loadTopic(args.topicId, args.topicTitle, args.sectionTitle)
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = viewModel.selectTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupAdapters() {
        examplesAdapter = ExamplesAdapter(
            onAskAi = { content -> openChatWithContent(content) }
        )
        practiceAdapter = PracticeAdapter(
            onAnswer = { practiceId, selectedIndex ->
                val topic = viewModel.uiState.value.topic ?: return@PracticeAdapter
                viewModel.answerPractice(practiceId, selectedIndex, topic.id, topic.sectionId)
            },
            onAskAi = { content -> openChatWithContent(content) }
        )
        binding.rvExamples.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExamples.adapter = examplesAdapter

        binding.rvPractice.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPractice.adapter = practiceAdapter
    }

    private fun openChatWithContent(content: String) {
        val action = TopicFragmentDirections.actionTopicFragmentToChatFragment(
            topicId        = args.topicId,
            topicTitle     = args.topicTitle,
            sectionTitle   = args.sectionTitle,
            contextContent = content
        )
        findNavController().navigate(action)
    }

    private fun setupAiButton() {
        binding.btnAskAi.setOnClickListener {
            val action = TopicFragmentDirections.actionTopicFragmentToChatFragment(
                topicId        = args.topicId,
                topicTitle     = args.topicTitle,
                sectionTitle   = args.sectionTitle,
                contextContent = null
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

                    state.error?.let { error ->
                        binding.wvTheoryContent.setMarkdownLatex("**Ошибка:** $error")
                        showTab(0)
                        return@collect
                    }

                    state.topic?.let { topic ->
                        binding.wvTheoryContent.setMarkdownLatex(topic.theory)
                        examplesAdapter.submitList(topic.examples)
                        practiceAdapter.submitList(topic.practiceItems, state.answeredPractice)
                    }

                    showTab(state.selectedTabIndex)
                }
            }
        }
    }

    private fun showTab(index: Int) {
        binding.wvTheoryContent.visibility = if (index == 0) View.VISIBLE else View.GONE
        binding.rvExamples.visibility      = if (index == 1) View.VISIBLE else View.GONE
        binding.rvPractice.visibility      = if (index == 2) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
