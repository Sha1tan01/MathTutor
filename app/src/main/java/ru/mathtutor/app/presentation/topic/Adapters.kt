package ru.mathtutor.app.presentation.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.mathtutor.app.R
import ru.mathtutor.app.databinding.ItemExampleBinding
import ru.mathtutor.app.databinding.ItemExampleStepBinding
import ru.mathtutor.app.databinding.ItemPracticeBinding
import ru.mathtutor.app.domain.model.Example
import ru.mathtutor.app.domain.model.ExampleStep
import ru.mathtutor.app.domain.model.PracticeItem

// ── Examples ──────────────────────────────────────────────────────────────────

class ExamplesAdapter(
    private val onAskAi: ((content: String) -> Unit)? = null
) : ListAdapter<Example, ExamplesAdapter.VH>(DiffCB()) {

    inner class VH(private val b: ItemExampleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(example: Example) {
            b.tvExampleTitle.text = example.title
            val stepsAdapter = ExampleStepsAdapter(onAskAi = { stepText ->
                val content = "Пример: «${example.title}»\n$stepText"
                onAskAi?.invoke(content)
            })
            b.rvSteps.layoutManager = LinearLayoutManager(b.root.context)
            b.rvSteps.adapter = stepsAdapter
            stepsAdapter.submitList(example.steps)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemExampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class DiffCB : DiffUtil.ItemCallback<Example>() {
        override fun areItemsTheSame(a: Example, b: Example) = a.id == b.id
        override fun areContentsTheSame(a: Example, b: Example) = a == b
    }
}

class ExampleStepsAdapter(
    private val onAskAi: ((stepText: String) -> Unit)? = null
) : ListAdapter<ExampleStep, ExampleStepsAdapter.VH>(DiffCB()) {

    inner class VH(private val b: ItemExampleStepBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(step: ExampleStep) {
            b.tvStepNumber.text = "Шаг ${step.stepNumber}"
            b.tvStepDescription.text = step.description
            if (step.formula != null) {
                b.wvStepFormula.visibility = View.VISIBLE
                b.wvStepFormula.setFormula(step.formula)
            } else {
                b.wvStepFormula.visibility = View.GONE
            }

            if (onAskAi != null) {
                b.btnAskAiExample.visibility = View.VISIBLE
                b.btnAskAiExample.setOnClickListener {
                    val text = "Шаг ${step.stepNumber}: ${step.description}" +
                        if (step.formula != null) "\nФормула: ${step.formula}" else ""
                    onAskAi.invoke(text)
                }
            } else {
                b.btnAskAiExample.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemExampleStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class DiffCB : DiffUtil.ItemCallback<ExampleStep>() {
        override fun areItemsTheSame(a: ExampleStep, b: ExampleStep) = a.stepNumber == b.stepNumber
        override fun areContentsTheSame(a: ExampleStep, b: ExampleStep) = a == b
    }
}

// ── Practice ──────────────────────────────────────────────────────────────────

data class PracticeItemUi(
    val item: PracticeItem,
    val selectedIndex: Int? = null
)

class PracticeAdapter(
    private val onAnswer: (practiceId: String, selectedIndex: Int) -> Unit,
    private val onAskAi: ((content: String) -> Unit)? = null
) : RecyclerView.Adapter<PracticeAdapter.VH>() {

    private var items: List<PracticeItemUi> = emptyList()

    fun submitList(practiceItems: List<PracticeItem>, answeredMap: Map<String, Int>) {
        items = practiceItems.map { p ->
            PracticeItemUi(p, answeredMap[p.id])
        }
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemPracticeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(ui: PracticeItemUi) {
            val item = ui.item
            b.wvQuestion.setMarkdownLatex(item.question)
            b.tvQuestionNumber.text = "Задание ${adapterPosition + 1} из ${itemCount}"

            val optionViews = listOf(b.option0, b.option1, b.option2, b.option3)

            optionViews.forEachIndexed { idx, tv ->
                if (idx < item.options.size) {
                    tv.visibility = View.VISIBLE
                    val label = listOf("a", "b", "c", "d")[idx]
                    tv.text = "$label) ${item.options[idx]}"
                    tv.setBackgroundResource(R.drawable.bg_option_default)
                    tv.isEnabled = ui.selectedIndex == null

                    if (ui.selectedIndex != null) {
                        when {
                            idx == item.correctIndex ->
                                tv.setBackgroundResource(R.drawable.bg_option_correct)
                            idx == ui.selectedIndex ->
                                tv.setBackgroundResource(R.drawable.bg_option_wrong)
                        }
                    }

                    tv.setOnClickListener {
                        if (ui.selectedIndex == null) onAnswer(item.id, idx)
                    }
                } else {
                    tv.visibility = View.GONE
                }
            }

            if (ui.selectedIndex != null) {
                b.wvExplanation.visibility = View.VISIBLE
                b.wvExplanation.setMarkdownLatex(item.explanation)

                // Показываем кнопку «Объясни решение» после ответа
                if (onAskAi != null) {
                    b.btnAskAiPractice.visibility = View.VISIBLE
                    b.btnAskAiPractice.setOnClickListener {
                        val content = "Вопрос: ${item.question}\n" +
                            "Варианты ответа: ${item.options.joinToString(", ")}\n" +
                            "Правильный ответ: ${item.options[item.correctIndex]}\n" +
                            "Объяснение: ${item.explanation}"
                        onAskAi.invoke(content)
                    }
                }
            } else {
                b.wvExplanation.visibility = View.GONE
                b.btnAskAiPractice.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemPracticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
