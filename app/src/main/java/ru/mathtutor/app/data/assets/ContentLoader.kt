package ru.mathtutor.app.data.assets

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.mathtutor.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val cachedSections: List<Section> by lazy { loadSections() }
    private val topicsMap: Map<String, List<Topic>> by lazy { loadAllTopics() }

    fun getSections(): List<Section> = cachedSections

    fun getTopics(sectionId: String): List<Topic> =
        topicsMap[sectionId] ?: emptyList()

    fun getTopic(topicId: String): Topic? =
        topicsMap.values.flatten().firstOrNull { it.id == topicId }

    private fun loadSections(): List<Section> = try {
        val json = context.assets.open("content/sections.json")
            .bufferedReader().readText()
        val type = object : TypeToken<List<SectionJson>>() {}.type
        val raw: List<SectionJson> = gson.fromJson(json, type)
        raw.map { it.toDomain() }
    } catch (e: Exception) {
        Log.e(TAG, "Не удалось загрузить разделы из assets/content/sections.json", e)
        emptyList()
    }

    private fun loadAllTopics(): Map<String, List<Topic>> = try {
        val json = context.assets.open("content/topics.json")
            .bufferedReader().readText()
        val type = object : TypeToken<List<TopicJson>>() {}.type
        val raw: List<TopicJson> = gson.fromJson(json, type)
        raw.map { it.toDomain() }.groupBy { it.sectionId }
    } catch (e: Exception) {
        Log.e(TAG, "Не удалось загрузить темы из assets/content/topics.json", e)
        emptyMap()
    }

    // ── JSON data classes ────────────────────────────────────────────────────

    private data class SectionJson(
        val id: String, val title: String, val icon: String,
        val description: String, val topicCount: Int, val colorTag: String
    ) {
        fun toDomain() = Section(id, title, icon, description, topicCount, colorTag)
    }

    private data class ExampleStepJson(
        val stepNumber: Int, val description: String, val formula: String?
    ) {
        fun toDomain() = ExampleStep(stepNumber, description, formula)
    }

    private data class ExampleJson(
        val id: String, val title: String, val steps: List<ExampleStepJson>
    ) {
        fun toDomain() = Example(id, title, steps.map { it.toDomain() })
    }

    private data class PracticeJson(
        val id: String, val topicId: String, val question: String,
        val options: List<String>, val correctIndex: Int, val explanation: String
    ) {
        fun toDomain() = PracticeItem(id, topicId, question, options, correctIndex, explanation)
    }

    private data class TopicJson(
        val id: String, val sectionId: String, val title: String,
        val orderIndex: Int, val theory: String,
        val examples: List<ExampleJson>, val practiceItems: List<PracticeJson>
    ) {
        fun toDomain() = Topic(
            id, sectionId, title, orderIndex, theory,
            examples.map { it.toDomain() }, practiceItems.map { it.toDomain() }
        )
    }

    companion object {
        private const val TAG = "ContentLoader"
    }
}
