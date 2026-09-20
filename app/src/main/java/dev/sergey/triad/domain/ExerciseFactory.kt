package dev.sergey.triad.domain

class ExerciseFactory(
    private val distractorPool: (AppLanguage) -> List<String>,
) {
    fun forReview(
        concept: Concept,
        review: ReviewItem,
        nativeLang: AppLanguage,
        nowMillis: Long,
        slot: Int,
        quizLangs: List<AppLanguage> = listOf(review.targetLang),
    ): Exercise {
        val target = review.targetLang
        val targetText = concept.text(target).text
        val isBrandNew = review.state == FsrsCardState.New && review.reps == 0
        val mixSeed = nowMillis + slot.toLong() + concept.id.hashCode()
        val langs = quizLangs.ifEmpty { listOf(target) }
        return when {
            !isBrandNew &&
                concept.kind != ConceptKind.Word &&
                targetText.contains(" ") &&
                slot % 3 == 2 -> order(concept, nativeLang, target, mixSeed)
            else -> cloze(concept, nativeLang, target, mixSeed, langs)
        }
    }

    fun cloze(
        concept: Concept,
        nativeLang: AppLanguage,
        targetLang: AppLanguage,
        seed: Long,
        quizLangs: List<AppLanguage> = listOf(targetLang),
    ): Exercise.Cloze {
        val langs = quizLangs.ifEmpty { listOf(targetLang) }.distinct()
        val banks = langs.mapIndexed { index, lang ->
            bankFor(concept, lang, seed + index * 31L)
        }
        return Exercise.Cloze(concept, nativeLang, targetLang, banks)
    }

    fun order(
        concept: Concept,
        nativeLang: AppLanguage,
        targetLang: AppLanguage,
        seed: Long,
    ): Exercise.OrderChips {
        val correct = tokenize(concept.text(targetLang).text)
        val shuffled = correct.shuffled(java.util.Random(seed))
        val display = if (shuffled == correct && correct.size > 1) {
            correct.reversed()
        } else {
            shuffled
        }
        return Exercise.OrderChips(concept, nativeLang, targetLang, display, correct)
    }

    fun tone(
        concept: Concept,
        nativeLang: AppLanguage,
        targetLang: AppLanguage,
        seed: Long,
    ): Exercise.TonePick {
        val correct = concept.text(targetLang).tones.joinToString(" · ").ifBlank { "ngang" }
        val options = shuffleChoices(
            listOf(correct, "ngang", "sắc", "huyền", "hỏi", "ngã", "nặng").distinct().take(4),
            seed,
        )
        return Exercise.TonePick(concept, nativeLang, targetLang, options, correct)
    }

    private fun bankFor(concept: Concept, lang: AppLanguage, seed: Long): Exercise.ChoiceBank {
        val correct = concept.text(lang).text
        val others = distractorPool(lang)
            .filter { AnswerEvaluator.normalize(it, lang) != AnswerEvaluator.normalize(correct, lang) }
            .distinct()
            .shuffled(java.util.Random(seed))
            .take(3)
        val options = shuffleChoices((others + correct).distinct(), seed)
        return Exercise.ChoiceBank(lang, options, correct)
    }

    companion object {
        fun tokenize(text: String): List<String> =
            text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        fun shuffleChoices(choices: List<String>, seed: Long): List<String> {
            if (choices.size <= 1) return choices
            val shuffled = choices.shuffled(java.util.Random(seed))
            return if (shuffled == choices) {
                shuffled.drop(1) + shuffled.first()
            } else {
                shuffled
            }
        }
    }
}
