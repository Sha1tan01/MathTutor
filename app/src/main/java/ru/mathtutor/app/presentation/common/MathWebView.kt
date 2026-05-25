package ru.mathtutor.app.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
class MathWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    init {
        settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = false
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        webViewClient = WebViewClient()
        isScrollContainer = false
        setBackgroundColor(0x00000000) // transparent

        addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onHeightChanged(height: Int) {
                post {
                    val density = resources.displayMetrics.density
                    val newHeight = (height * density).toInt()
                    if (layoutParams != null && layoutParams.height != newHeight) {
                        layoutParams.height = newHeight
                        requestLayout()
                    }
                }
            }
        }, "AndroidBridge")
    }

    fun setMarkdownLatex(markdown: String) {
        val html = buildHtml(convertMarkdown(markdown))
        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    /** Renders a single LaTeX formula compactly — for example steps. */
    fun setFormula(latex: String) {
        // Strip outer $ or $$ delimiters if present
        val clean = latex.trim()
            .removePrefix("\$\$").removeSuffix("\$\$")
            .removePrefix("\$").removeSuffix("\$")
            .trim()

        val mathJaxExpr = "\\[$clean\\]"

        val html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: sans-serif;
    background: #EEF4FF;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
  }
  mjx-container { color: #0C447C; max-width: 100%; overflow-x: auto; }
</style>
<script>
  window.MathJax = {
    tex: {
      inlineMath: [['\\(','\\)']],
      displayMath: [['\\[','\\]']],
      packages: {'[+]': ['ams']}
    },
    startup: {
      ready() {
        MathJax.startup.defaultReady();
        MathJax.startup.promise.then(() => {
          const h = document.body.scrollHeight;
          window.ResizeObserver && new ResizeObserver(() => {
            AndroidBridge.onHeightChanged(document.body.scrollHeight);
          }).observe(document.body);
          AndroidBridge.onHeightChanged(h);
        });
      }
    }
  };
</script>
<script async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js"></script>
</head>
<body>$mathJaxExpr</body>
</html>""".trimIndent()

        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun convertMarkdown(markdown: String): String {
        var text = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        text = text
            .replace(Regex("\\$\\$([\\s\\S]+?)\\$\\$")) { "\\[${it.groupValues[1]}\\]" }
            .replace(Regex("\\$([^$\n]+?)\\$")) { "\\(${it.groupValues[1]}\\)" }
            .replace(Regex("(?m)^### (.+)$"), "<h3>$1</h3>")
            .replace(Regex("(?m)^## (.+)$"), "<h2>$1</h2>")
            .replace(Regex("(?m)^# (.+)$"), "<h1>$1</h1>")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
            .replace(Regex("(?m)^- (.+)$"), "<li>$1</li>")
            .replace(Regex("(?m)^\\d+\\. (.+)$"), "<li>$1</li>")
            .replace(Regex("\n\n+"), "</p><p>")
            .replace("\n", "<br>")
        return text
    }

    private fun buildHtml(text: String): String {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
  * { box-sizing: border-box; }
  body {
    font-family: sans-serif;
    font-size: 15px;
    line-height: 1.7;
    color: #1A1A2E;
    background: transparent;
    margin: 0;
    padding: 12px 16px 24px;
    word-wrap: break-word;
  }
  h1, h2 { color: #0C447C; margin: 20px 0 8px; font-size: 18px; }
  h3 { color: #0C447C; margin: 16px 0 6px; font-size: 16px; }
  li { margin: 4px 0 4px 16px; }
  strong { color: #1A1A2E; }
  .MathJax_Display {
    background: #EEF4FF;
    border-radius: 8px;
    padding: 10px;
    margin: 12px 0;
    overflow-x: auto;
  }
  .MathJax { color: #0C447C !important; }
  p { margin: 8px 0; }
  mjx-container { color: #0C447C; }
</style>
<script>
  window.MathJax = {
    tex: {
      inlineMath: [['\\(', '\\)']],
      displayMath: [['\\[', '\\]']],
      packages: {'[+]': ['ams']}
    },
    options: { skipHtmlTags: ['script','noscript','style','textarea','pre'] },
    startup: {
      ready() {
        MathJax.startup.defaultReady();
        MathJax.startup.promise.then(() => {
          // Tell Android WebView to resize after MathJax renders
          document.body.style.height = 'auto';
        });
      }
    }
  };
</script>
<script async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js"></script>
</head>
<body>
<p>$text</p>
</body>
</html>
        """.trimIndent()
    }
}
