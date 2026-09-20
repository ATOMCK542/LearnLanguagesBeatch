package dev.sergey.triad.domain

import java.text.Normalizer

object AnswerEvaluator {
    fun normalize(raw: String, lang: AppLanguage): String {
        var s = raw.trim().lowercase()
        s = s.replace(Regex("\\s+"), " ")
        if (lang == AppLanguage.Vi) {
            s = Normalizer.normalize(s, Normalizer.Form.NFC)
        }
        return s
    }

    fun textMatches(expected: String, actual: String, lang: AppLanguage): Boolean =
        normalize(expected, lang) == normalize(actual, lang)

    fun chipsMatch(expected: List<String>, actual: List<String>, lang: AppLanguage): Boolean {
        if (expected.size != actual.size) return false
        return expected.zip(actual).all { (e, a) -> textMatches(e, a, lang) }
    }

    fun ratingForGame(correct: Boolean): Rating = if (correct) Rating.Good else Rating.Again
}
