package ru.mathtutor.app.data.assets

import android.content.Context
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
        hardcodedSections()
    }

    private fun loadAllTopics(): Map<String, List<Topic>> = try {
        val json = context.assets.open("content/topics.json")
            .bufferedReader().readText()
        val type = object : TypeToken<List<TopicJson>>() {}.type
        val raw: List<TopicJson> = gson.fromJson(json, type)
        raw.map { it.toDomain() }.groupBy { it.sectionId }
    } catch (e: Exception) {
        hardcodedTopics()
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

    // ── Hardcoded fallback data ───────────────────────────────────────────────

    private fun hardcodedSections() = listOf(
        Section("math_analysis",   "Математический анализ",       "∫", "Пределы, производные, интегралы",           8,  "blue"),
        Section("linear_algebra",  "Линейная алгебра",             "Σ", "Матрицы, векторы, детерминанты",            7,  "teal"),
        Section("diff_equations",  "Дифференциальные уравнения",   "λ", "ОДУ, ДУЧП, методы решения",                 6,  "purple"),
        Section("analytic_geom",   "Аналитическая геометрия",      "π", "Прямые, кривые, поверхности",               5,  "green"),
        Section("probability",     "Теория вероятностей",          "∂", "Случайные события, распределения",          5,  "orange"),
        Section("statistics",      "Математическая статистика",    "μ", "Оценки, гипотезы, регрессия",               4,  "red"),
        Section("discrete_math",   "Дискретная математика",        "∀", "Графы, комбинаторика, логика",              5,  "blue"),
        Section("series",          "Числовые ряды",                "∞", "Сходимость, разложение функций",            3,  "teal"),
        Section("multivariable",   "Функции многих переменных",    "∇", "Частные производные, экстремумы",           4,  "purple"),
        Section("multiple_int",    "Кратные интегралы",            "⊗", "Двойные, тройные интегралы",                3,  "green"),
        Section("operational",     "Операционное исчисление",      "℃", "Преобразование Лапласа, Фурье",             4,  "orange"),
        Section("complex",         "Комплексный анализ",           "ℝ", "Функции комплексного переменного",          3,  "red")
    )

    private fun hardcodedTopics(): Map<String, List<Topic>> = mapOf(
        "linear_algebra" to listOf(
            Topic(
                id = "matrices_intro",
                sectionId = "linear_algebra",
                title = "Введение в матрицы",
                orderIndex = 1,
                theory = """## Матрица
**Матрица** — прямоугольная таблица чисел, упорядоченных по строкам и столбцам.

Матрица размера ${'$'}m \times n${'$'} содержит ${'$'}m${'$'} строк и ${'$'}n${'$'} столбцов:

${'$'}${'$'}A = \begin{pmatrix} a_{11} & a_{12} & \cdots & a_{1n} \\ a_{21} & a_{22} & \cdots & a_{2n} \\ \vdots & \vdots & \ddots & \vdots \\ a_{m1} & a_{m2} & \cdots & a_{mn} \end{pmatrix}${'$'}${'$'}

### Виды матриц
- **Квадратная**: ${'$'}m = n${'$'}
- **Нулевая**: все элементы равны нулю
- **Единичная** ${'$'}E${'$'}: ${'$'}e_{ij} = 1${'$'} при ${'$'}i = j${'$'}, иначе ${'$'}0${'$'}
- **Диагональная**: все элементы вне главной диагонали равны нулю
""",
                examples = listOf(
                    Example(
                        id = "ex_matrix_1",
                        title = "Запись матрицы",
                        steps = listOf(
                            ExampleStep(1, "Запишем матрицу 2×3", null),
                            ExampleStep(2, "Обозначаем строки и столбцы", "\$A = \\begin{pmatrix} 1 & 2 & 3 \\\\ 4 & 5 & 6 \\end{pmatrix}\$"),
                            ExampleStep(3, "Элемент a₁₂ = 2 (строка 1, столбец 2)", null)
                        )
                    )
                ),
                practiceItems = listOf(
                    PracticeItem(
                        id = "p_matrix_1",
                        topicId = "matrices_intro",
                        question = "Матрица A имеет размер 3×4. Сколько в ней элементов?",
                        options = listOf("7", "12", "9", "16"),
                        correctIndex = 1,
                        explanation = "3 строки × 4 столбца = 12 элементов"
                    ),
                    PracticeItem(
                        id = "p_matrix_2",
                        topicId = "matrices_intro",
                        question = "Какая матрица называется квадратной?",
                        options = listOf(
                            "Матрица с одной строкой",
                            "Матрица, у которой число строк равно числу столбцов",
                            "Матрица с нулевыми элементами",
                            "Матрица размером 2×4"
                        ),
                        correctIndex = 1,
                        explanation = "Квадратная матрица — матрица, у которой m = n"
                    )
                )
            ),
            Topic(
                id = "matrices_sum",
                sectionId = "linear_algebra",
                title = "Сложение матриц",
                orderIndex = 2,
                theory = """## Сложение матриц
Две матрицы можно складывать только если они одного размера ${'$'}m \times n${'$'}.

Результат — матрица того же размера, где каждый элемент равен сумме соответствующих элементов:

${'$'}${'$'}C = A + B \Leftrightarrow c_{ij} = a_{ij} + b_{ij}${'$'}${'$'}

### Свойства сложения
1. **Коммутативность**: ${'$'}A + B = B + A${'$'}
2. **Ассоциативность**: ${'$'}(A + B) + C = A + (B + C)${'$'}
3. **Нейтральный элемент**: ${'$'}A + O = A${'$'}, где ${'$'}O${'$'} — нулевая матрица
4. **Противоположная матрица**: ${'$'}A + (-A) = O${'$'}
""",
                examples = listOf(
                    Example(
                        id = "ex_sum_1",
                        title = "Сложение матриц 2×2",
                        steps = listOf(
                            ExampleStep(1, "Даны матрицы A и B одного размера", "\$A = \\begin{pmatrix} 1 & 2 \\\\ 3 & 4 \\end{pmatrix}, \\quad B = \\begin{pmatrix} 5 & 6 \\\\ 7 & 8 \\end{pmatrix}\$"),
                            ExampleStep(2, "Складываем соответствующие элементы", "\$A + B = \\begin{pmatrix} 1+5 & 2+6 \\\\ 3+7 & 4+8 \\end{pmatrix}\$"),
                            ExampleStep(3, "Результат", "\$A + B = \\begin{pmatrix} 6 & 8 \\\\ 10 & 12 \\end{pmatrix}\$")
                        )
                    )
                ),
                practiceItems = listOf(
                    PracticeItem(
                        id = "p_sum_1",
                        topicId = "matrices_sum",
                        question = "Можно ли сложить матрицу 2×3 с матрицей 3×2?",
                        options = listOf("Да", "Нет", "Только если они квадратные", "Только нулевые"),
                        correctIndex = 1,
                        explanation = "Сложение возможно только для матриц одинакового размера m×n"
                    )
                )
            ),
            Topic(
                id = "matrices_mul",
                sectionId = "linear_algebra",
                title = "Умножение матриц",
                orderIndex = 3,
                theory = """## Умножение матриц
Произведение ${'$'}A \cdot B${'$'} определено, если **число столбцов A равно числу строк B**.

Если ${'$'}A${'$'} — матрица ${'$'}m \times k${'$'}, ${'$'}B${'$'} — матрица ${'$'}k \times n${'$'}, результат ${'$'}C = AB${'$'} — матрица ${'$'}m \times n${'$'}:

${'$'}${'$'}c_{ij} = \sum_{t=1}^{k} a_{it} \cdot b_{tj}${'$'}${'$'}

### Важные свойства
- **Не коммутативно**: ${'$'}AB \neq BA${'$'} в общем случае
- **Ассоциативно**: ${'$'}(AB)C = A(BC)${'$'}
- **Дистрибутивно**: ${'$'}A(B+C) = AB + AC${'$'}
""",
                examples = listOf(
                    Example(
                        id = "ex_mul_1",
                        title = "Умножение матриц 2×2",
                        steps = listOf(
                            ExampleStep(1, "Даны матрицы", "\$A = \\begin{pmatrix} 1 & 2 \\\\ 3 & 4 \\end{pmatrix}, \\quad B = \\begin{pmatrix} 2 & 0 \\\\ 1 & 3 \\end{pmatrix}\$"),
                            ExampleStep(2, "c₁₁ = 1·2 + 2·1 = 4", null),
                            ExampleStep(3, "c₁₂ = 1·0 + 2·3 = 6", null),
                            ExampleStep(4, "c₂₁ = 3·2 + 4·1 = 10", null),
                            ExampleStep(5, "c₂₂ = 3·0 + 4·3 = 12", null),
                            ExampleStep(6, "Результат", "\$AB = \\begin{pmatrix} 4 & 6 \\\\ 10 & 12 \\end{pmatrix}\$")
                        )
                    )
                ),
                practiceItems = listOf(
                    PracticeItem(
                        id = "p_mul_1",
                        topicId = "matrices_mul",
                        question = "Матрица A имеет размер 3×2, матрица B — 2×4. Каков размер произведения AB?",
                        options = listOf("2×2", "3×4", "4×3", "6×8"),
                        correctIndex = 1,
                        explanation = "При умножении (3×2)·(2×4) получаем матрицу 3×4"
                    )
                )
            )
        ),
        "math_analysis" to listOf(
            Topic(
                id = "limits_intro",
                sectionId = "math_analysis",
                title = "Пределы функций",
                orderIndex = 1,
                theory = """## Предел функции
Говорят, что функция ${'$'}f(x)${'$'} имеет предел ${'$'}L${'$'} при ${'$'}x \to a${'$'}, если значения ${'$'}f(x)${'$'} сколь угодно близко подходят к ${'$'}L${'$'} при приближении ${'$'}x${'$'} к ${'$'}a${'$'}:

${'$'}${'$'}\lim_{x \to a} f(x) = L${'$'}${'$'}

### Основные теоремы
- **Теорема о сжатой переменной**: если ${'$'}g(x) \leq f(x) \leq h(x)${'$'} и ${'$'}\lim g = \lim h = L${'$'}, то ${'$'}\lim f = L${'$'}
- **Первый замечательный предел**: ${'$'}\lim_{x \to 0} \frac{\sin x}{x} = 1${'$'}
- **Второй замечательный предел**: ${'$'}\lim_{x \to \infty} \left(1 + \frac{1}{x}\right)^x = e${'$'}
""",
                examples = listOf(
                    Example(
                        id = "ex_limit_1",
                        title = "Вычисление простого предела",
                        steps = listOf(
                            ExampleStep(1, "Найти предел при x → 2", "\$\\lim_{x \\to 2} (3x^2 - x + 1)\$"),
                            ExampleStep(2, "Для полиномов — подставляем значение напрямую", "\$= 3 \\cdot 4 - 2 + 1 = 11\$")
                        )
                    )
                ),
                practiceItems = listOf(
                    PracticeItem(
                        id = "p_limit_1",
                        topicId = "limits_intro",
                        question = "Чему равен lim(x→3) (x² - 9) / (x - 3)?",
                        options = listOf("0", "3", "6", "Не существует"),
                        correctIndex = 2,
                        explanation = "(x²-9)/(x-3) = (x-3)(x+3)/(x-3) = x+3 → 3+3 = 6"
                    )
                )
            ),
            Topic(
                id = "definite_integral",
                sectionId = "math_analysis",
                title = "Определённый интеграл",
                orderIndex = 7,
                theory = """## Определённый интеграл
**Определённый интеграл** функции ${'$'}f(x)${'$'} на отрезке ${'$'}[a, b]${'$'} — предел интегральных сумм Римана:

${'$'}${'$'}\int_a^b f(x)\,dx = \lim_{\lambda \to 0} \sum_{i=1}^n f(\xi_i)\,\Delta x_i${'$'}${'$'}

### Формула Ньютона — Лейбница
${'$'}${'$'}\int_a^b f(x)\,dx = F(b) - F(a)${'$'}${'$'}

где ${'$'}F(x)${'$'} — любая первообразная функции ${'$'}f(x)${'$'}.

### Основные свойства
- **Линейность**: ${'$'}\int_a^b [\alpha f + \beta g]\,dx = \alpha\int_a^b f\,dx + \beta\int_a^b g\,dx${'$'}
- **Аддитивность**: ${'$'}\int_a^b f\,dx = \int_a^c f\,dx + \int_c^b f\,dx${'$'}
- **Геометрический смысл**: площадь фигуры под графиком ${'$'}f(x)${'$'} над осью ${'$'}Ox${'$'}
""",
                examples = listOf(
                    Example(
                        id = "ex_int_1",
                        title = "Вычисление определённого интеграла",
                        steps = listOf(
                            ExampleStep(1, "Вычислить", "\$\\int_0^2 x^2\\,dx\$"),
                            ExampleStep(2, "Находим первообразную", "\$F(x) = \\frac{x^3}{3}\$"),
                            ExampleStep(3, "Применяем формулу Ньютона–Лейбница", "\$F(2) - F(0) = \\frac{8}{3} - 0 = \\frac{8}{3}\$")
                        )
                    )
                ),
                practiceItems = listOf(
                    PracticeItem(
                        id = "p_int_1",
                        topicId = "definite_integral",
                        question = "Вычислите: ∫₀¹ (2x + 1) dx",
                        options = listOf("1", "2", "3", "4"),
                        correctIndex = 1,
                        explanation = "∫(2x+1)dx = x²+x, при x=1: 1+1=2, при x=0: 0. Ответ: 2"
                    )
                )
            )
        )
    )
}
