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
    ): Exercise {
        val target = review.targetLang
        val targetText = concept.text(target).text
        val isBrandNew = review.state == FsrsCardState.New && review.reps == 0
        val mixSeed = nowMillis + slot.toLong() + concept.id.hashCode()
        return when {
            !isBrandNew &&
                target == AppLanguage.Vi &&
                concept.text(target).tones.isNotEmpty() &&
                slot % 5 == 4 -> tone(concept, nativeLang, target, mixSeed)
            !isBrandNew &&
                concept.kind != ConceptKind.Word &&
                targetText.contains(" ") &&
                slot % 3 == 2 -> order(concept, nativeLang, target, mixSeed)
            isBrandNew -> Exercise.Reveal(concept, nativeLang, target)
            else -> cloze(concept, nativeLang, target, mixSeed)
        }
    }

    fun reveal(concept: Concept, nativeLang: AppLanguage, targetLang: AppLanguage) =
        Exercise.Reveal(concept, nativeLang, targetLang)

    fun cloze(
        concept: Concept,
        nativeLang: AppLanguage,
        targetLang: AppLanguage,
        seed: Long,
    ): Exercise.Cloze {
        val correct = concept.text(targetLang).text
        val others = distractorPool(targetLang)
            .filter { AnswerEvaluator.normalize(it, targetLang) != AnswerEvaluator.normalize(correct, targetLang) }
            .distinct()
            .shuffled(java.util.Random(seed))
            .take(3)
        val options = shuffleChoices((others + correct).distinct(), seed)
        return Exercise.Cloze(concept, nativeLang, targetLang, options, correct)
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
