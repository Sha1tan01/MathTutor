# МатТьютор — Android-приложение по высшей математике

Репетитор по высшей математике с ИИ-чатом, построенный на Kotlin + MVVM + Clean Architecture.

## Стек технологий

| Слой | Технология |
|------|-----------|
| UI | Fragments, ViewBinding, RecyclerView, Material 3 |
| Навигация | Navigation Component + Safe Args, ручное управление BottomNav |
| ViewModel | Hilt + StateFlow |
| База данных | Room (чат, прогресс) |
| Настройки | DataStore Preferences |
| Сеть | Retrofit 2 + OkHttp |
| ИИ | OpenAI GPT-4o-mini (легко заменяется на Gemini / Groq) | 
| Контент | JSON в assets (офлайн) |
| Рендеринг формул | WebView + MathJax 3 (LaTeX) |
| Рендеринг Markdown | Markwon (чат-сообщения) |
| DI | Hilt |

## Быстрый старт

### 1. Открыть в Android Studio

Распакуйте архив и откройте папку `MathTutor` в **Android Studio Hedgehog** или новее.

### 2. Добавить API-ключ

Создайте файл `local.properties` в корне проекта (рядом с `settings.gradle.kts`):

```properties
sdk.dir=C:\Users\yourname\AppData\Local\Android\Sdk
AI_API_KEY=sk-proj-ваш_ключ_openai
```

Получить ключ: https://platform.openai.com/api-keys

> Без ключа приложение работает полностью — теория, примеры, практика, прогресс. ИИ-чат вернёт ошибку сети.

### 3. Запустить

- Выберите устройство (эмулятор API 26+ или реальный телефон)
- Нажмите **Run ▶**

### 4. Формулы офлайн

LaTeX-формулы в теории рендерятся через MathJax, подгружаемый с CDN (`cdn.jsdelivr.net`). При первом открытии темы нужен интернет. После этого браузерный кеш работает офлайн.

---

## Структура проекта

```
MathTutor/
├── gradle/
│   ├── libs.versions.toml          # Version Catalog — все версии библиотек
│   └── wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts            # Зависимости модуля, BuildConfig
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/content/
│       │   ├── sections.json       # 12 разделов курса
│       │   └── topics.json         # Темы: теория, примеры, задачи
│       ├── res/
│       │   ├── navigation/nav_graph.xml
│       │   ├── layout/             # 9 layouts + 7 item layouts
│       │   ├── drawable/           # Фоны, иконки, кнопки
│       │   ├── values/             # colors, strings, themes, dimens
│       │   ├── mipmap-*/           # Иконки приложения (все плотности)
│       │   └── xml/network_security_config.xml
│       └── java/ru/mathtutor/app/
│           ├── MathTutorApp.kt     # @HiltAndroidApp
│           ├── MainActivity.kt     # Единственная Activity, BottomNav
│           ├── di/Modules.kt       # Hilt: DB, Network, Repository
│           ├── data/
│           │   ├── assets/ContentLoader.kt   # JSON → доменные модели
│           │   ├── local/dao/Daos.kt          # Room DAOs
│           │   ├── local/entity/Entities.kt   # Room сущности
│           │   ├── local/database/AppDatabase.kt
│           │   ├── remote/api/OpenAiApi.kt    # Retrofit → OpenAI
│           │   ├── remote/dto/Dtos.kt
│           │   └── repository/Repositories.kt # Реализации
│           ├── domain/
│           │   ├── model/Models.kt            # Section, Topic, ChatMessage…
│           │   ├── repository/Repositories.kt # Интерфейсы
│           │   └── usecase/UseCases.kt        # 10 Use Cases
│           └── presentation/
│               ├── common/
│               │   └── MathWebView.kt         # WebView + MathJax для LaTeX
│               ├── home/                      # Главный экран
│               ├── theory/                    # Разделы и подразделы
│               ├── topic/                     # Теория / Примеры / Практика
│               └── chat/                      # ИИ-чат с контекстом темы
```

---

## Навигация

Приложение использует единый `NavController` с тремя top-level экранами в нижней панели:

| Пункт меню | Фрагмент | Описание |
|-----------|---------|---------|
| Главная | `HomeFragment` | Прогресс, последняя тема, быстрый доступ |
| Теория | `SectionsFragment` | Список разделов → темы → содержимое |
| ИИ-чат | `ChatFragment` | Свободный режим без контекста |

При открытии чата из темы через кнопку «Спросить ИИ-репетитора» — чат получает контекст темы и открывается с баннером, скрывая нижнюю панель. Системная кнопка «Назад» возвращает к теме.

---

## Рендеринг LaTeX

Формулы в разделе «Теория» отображаются через `MathWebView` — кастомный `WebView` с MathJax 3:

- Блочные формулы: `$$формула$$` → отдельная строка с фоном
- Inline-формулы: `$формула$` → вровень с текстом

Markdown в теории (заголовки `##`, **жирный**, списки) конвертируется в HTML внутри `MathWebView` без внешних библиотек.

Markdown в сообщениях чата рендерится через **Markwon**.

---

## Добавление нового контента

Отредактируйте `app/src/main/assets/content/topics.json`:

```json
{
  "id": "my_topic",
  "sectionId": "math_analysis",
  "title": "Моя новая тема",
  "orderIndex": 9,
  "theory": "## Заголовок\n\nТекст с $формулой$ LaTeX\n\n$$\\int_a^b f(x)\\,dx = F(b) - F(a)$$",
  "examples": [
    {
      "id": "ex_1",
      "title": "Название примера",
      "steps": [
        { "stepNumber": 1, "description": "Описание шага", "formula": "$x^2 + y^2 = r^2$" },
        { "stepNumber": 2, "description": "Результат", "formula": null }
      ]
    }
  ],
  "practiceItems": [
    {
      "id": "p_1",
      "topicId": "my_topic",
      "question": "Вопрос с формулой $f(x) = x^2$?",
      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"],
      "correctIndex": 0,
      "explanation": "Объяснение правильного ответа"
    }
  ]
}
```

> Если тема есть в `topics.json`, но её `sectionId` не совпадает ни с одним разделом из `sections.json` — она не отобразится в списке.

---

## Замена AI-провайдера

### Groq (бесплатно, быстро)

```properties
# local.properties
AI_API_KEY=gsk_ваш_ключ
```

```kotlin
// build.gradle.kts — buildConfigField
buildConfigField("String", "AI_BASE_URL", "\"https://api.groq.com/openai/v1/\"")
```

В ` AiRepositoryImpl.kt` измените модель:
```kotlin
val model: String = "llama-3.1-8b-instant"
```

Groq полностью совместим с OpenAI API — больше ничего менять не нужно.

### Google Gemini

Требует изменения формата запроса в `OpenAiApi.kt` и `AiRepositoryImpl.kt`, так как Gemini использует другой REST API.

---

## Архитектура

```
UI Layer       HomeFragment · SectionsFragment · TopicsFragment
               TopicFragment · ChatFragment
                    ↕ StateFlow
ViewModel      HomeViewModel · SectionsViewModel · TopicsViewModel
               TopicViewModel · ChatViewModel
                    ↕ UseCase
Domain         GetSections · GetTopics · GetTopic · SaveProgress
               GetOverallProgress · SendChatMessage · ClearHistory…
                    ↕ Repository interface
Data           ContentRepositoryImpl  →  assets/JSON
               ProgressRepositoryImpl →  Room + DataStore
               ChatRepositoryImpl     →  Room
               AiRepositoryImpl       →  Retrofit → OpenAI
```

Все зависимости внедряются через **Hilt**. Domain-слой не зависит от Android — Use Cases тестируются изолированно.
