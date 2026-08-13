package com.bittokazi.oauth2.auth.frontend.frontend.base.utils

object Utils {

    fun formatChangeLogHtml(changeLogs: List<String>): String {
        if (changeLogs.isEmpty()) return "<p class=\"mb-0\">No changelog available.</p>"

        val urlRegex = Regex("https?://[^\\s]+")

        fun toLink(url: String, label: String = "Link"): String {
            return "<a href=\"$url\" target=\"_blank\" rel=\"noopener noreferrer\">$label</a>"
        }

        fun formatInlineLink(text: String): String {
            val url = urlRegex.find(text)?.value ?: return text
            val prefix = text.substringBefore(url).trim()
            val label = when {
                url.contains("/pull/") -> "PR"
                url.contains("/compare/") -> "Full Changelog"
                else -> "Link"
            }

            return if (prefix.isEmpty()) {
                toLink(url, label)
            } else {
                "$prefix ${toLink(url, label)}"
            }
        }

        val htmlLines = changeLogs.map { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> "<h6 class=\"mt-3 mb-2 fw-bold\">${trimmed.removePrefix("## ").trim()}</h6>"
                trimmed.startsWith("* ") -> {
                    val item = trimmed.removePrefix("* ").trim()
                    val formattedItem = formatInlineLink(item)
                    "<li class=\"mb-1\">$formattedItem</li>"
                }
                trimmed.startsWith("**") && trimmed.contains("https://") -> {
                    val url = urlRegex.find(trimmed)?.value ?: ""
                    "<p class=\"mb-2\"><strong>Full Changelog:</strong> ${toLink(url, "Open Full Changelog")}</p>"
                }
                trimmed.startsWith("http") -> "<p class=\"mb-2\">${toLink(trimmed, "Open link")}</p>"
                trimmed.contains("https://") -> "<p class=\"mb-2\">${formatInlineLink(trimmed)}</p>"
                else -> "<p class=\"mb-2\">$trimmed</p>"
            }
        }

        val listStart = htmlLines.filter { it.startsWith("<li") }.joinToString("")
        val otherContent = htmlLines.filterNot { it.startsWith("<li") }.joinToString("")

        return "<div class=\"text-start\">$otherContent${if (listStart.isNotEmpty()) "<ul class=\"mb-2 ps-3\">$listStart</ul>" else ""}</div>"
    }
}
