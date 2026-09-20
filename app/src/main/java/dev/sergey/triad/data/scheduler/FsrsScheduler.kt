package dev.sergey.triad.data.scheduler

import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.FsrsCardState
import dev.sergey.triad.domain.Rating
import dev.sergey.triad.domain.ReviewItem
import dev.sergey.triad.domain.Scheduler
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

/** FSRS-4.5 (open-spaced-repetition spec), not the unofficial FSRS-Kotlin port. */
class FsrsScheduler(
    private val w: DoubleArray = DEFAULT_W.copyOf(),
    private val requestRetention: Double = 0.9,
    private val maximumIntervalDays: Int = 36500,
) : Scheduler {

    override fun newItem(
        profileId: Long,
        conceptId: String,
        targetLang: AppLanguage,
        now: Long,
    ): ReviewItem = ReviewItem(
        profileId = profileId,
        conceptId = conceptId,
        targetLang = targetLang,
        stability = 0.0,
        difficulty = 0.0,
        dueAt = now,
        lastReviewAt = null,
        reps = 0,
        lapses = 0,
        state = FsrsCardState.New,
        elapsedDays = 0.0,
        scheduledDays = 0.0,
    )

    override fun preview(item: ReviewItem, now: Long): Map<Rating, ReviewItem> =
        Rating.entries.associateWith { review(item, it, now) }

    override fun review(item: ReviewItem, rating: Rating, now: Long): ReviewItem {
        val elapsed = elapsedDays(item, now)
        val grade = rating.grade
        val next = if (item.state == FsrsCardState.New) {
            val s = initStability(grade)
            val d = initDifficulty(grade)
            applyInterval(item, s, d, rating, now, elapsed, newCard = true)
        } else {
            val r = retrievability(elapsed, max(item.stability, 0.1))
            val d = nextDifficulty(item.difficulty, grade)
            val s = nextStability(d, max(item.stability, 0.1), r, rating)
            applyInterval(item, s, d, rating, now, elapsed, newCard = false)
        }
        return next
    }

    fun retrievability(elapsedDays: Double, stability: Double): Double {
        val s = max(stability, 0.1)
        return (1 + FACTOR * elapsedDays / s).pow(DECAY)
    }

    fun nextIntervalDays(stability: Double): Int {
        val s = max(stability, 0.1)
        val interval = s / FACTOR * (requestRetention.pow(1 / DECAY) - 1)
        return min(maximumIntervalDays, max(1, round(interval).toInt()))
    }

    private fun applyInterval(
        item: ReviewItem,
        stability: Double,
        difficulty: Double,
        rating: Rating,
        now: Long,
        elapsed: Double,
        newCard: Boolean,
    ): ReviewItem {
        val days = when (rating) {
            Rating.Again -> if (newCard) 0 else 0
            else -> nextIntervalDays(stability)
        }
        val state = when (rating) {
            Rating.Again -> FsrsCardState.Relearning
            else -> FsrsCardState.Review
        }
        val dueDelay = if (days <= 0) TEN_MINUTES_MS else days * DAY_MS
        return item.copy(
            stability = stability,
            difficulty = difficulty.coerceIn(1.0, 10.0),
            dueAt = now + dueDelay,
            lastReviewAt = now,
            reps = item.reps + 1,
            lapses = item.lapses + if (rating == Rating.Again) 1 else 0,
            state = state,
            elapsedDays = elapsed,
            scheduledDays = days.toDouble(),
        )
    }

    private fun elapsedDays(item: ReviewItem, now: Long): Double {
        val last = item.lastReviewAt ?: return 0.0
        return ((now - last).coerceAtLeast(0) / DAY_MS.toDouble())
    }

    private fun initStability(grade: Int): Double = max(w[grade - 1], 0.1)

    private fun initDifficulty(grade: Int): Double =
        (w[4] - (grade - 3) * w[5]).coerceIn(1.0, 10.0)

    private fun nextDifficulty(difficulty: Double, grade: Int): Double {
        val next = difficulty - w[6] * (grade - 3)
        val reverted = w[7] * initDifficulty(Rating.Good.grade) + (1 - w[7]) * next
        return reverted.coerceIn(1.0, 10.0)
    }

    private fun nextStability(difficulty: Double, stability: Double, retrievability: Double, rating: Rating): Double {
        return if (rating == Rating.Again) {
            (stability * exp(w[11]) * difficulty.pow(-w[12]) * ((stability + 1).pow(w[13]) - 1) *
                exp((1 - retrievability) * w[14])).coerceAtLeast(0.1)
        } else {
            val hardPenalty = if (rating == Rating.Hard) w[15] else 1.0
            val easyBonus = if (rating == Rating.Easy) w[16] else 1.0
            val growth = exp(w[8]) * (11 - difficulty) * stability.pow(-w[9]) *
                (exp(w[10] * (1 - retrievability)) - 1) * hardPenalty * easyBonus
            (stability * (growth + 1)).coerceAtLeast(stability)
        }
    }

    private val Rating.grade: Int
        get() = when (this) {
            Rating.Again -> 1
            Rating.Hard -> 2
            Rating.Good -> 3
            Rating.Easy -> 4
        }

    companion object {
        val DEFAULT_W = doubleArrayOf(
            0.4, 0.6, 2.4, 5.8, 4.93, 0.94, 0.86, 0.01,
            1.49, 0.14, 0.94, 2.18, 0.05, 0.34, 1.26, 0.29, 2.61,
        )
        const val DECAY = -0.5
        val FACTOR = 19.0 / 81.0
        const val DAY_MS = 86_400_000L
        const val TEN_MINUTES_MS = 10 * 60 * 1000L
    }
}
