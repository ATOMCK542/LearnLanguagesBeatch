package dev.sergey.triad.domain

import kotlin.random.Random

enum class UnlockAction { Show, Dismiss, Ignore }

data class UnlockSnapshot(
    val locked: Boolean = true,
    val passed: Boolean = false,
)

data class UnlockProgress(
    val required: Int,
    val correct: Int = 0,
) {
    val passed: Boolean get() = correct >= required

    fun answer(correctAnswer: Boolean): UnlockProgress =
        if (correctAnswer && !passed) copy(correct = correct + 1) else this
}

object UnlockGate {
    const val MIN_CORRECT = 1
    const val MAX_CORRECT = 10
    const val DEFAULT_CORRECT = 3

    val callPackages: Set<String> = setOf(
        "com.samsung.android.incallui",
        "com.android.incallui",
        "com.android.server.telecom",
    )

    fun clampRequired(value: Int): Int = value.coerceIn(MIN_CORRECT, MAX_CORRECT)

    fun shouldShow(enabled: Boolean, pool: List<*>): Boolean = enabled && pool.isNotEmpty()

    fun yieldsTo(packageName: String?): Boolean =
        packageName != null && packageName in callPackages

    fun isQuizInFront(packageName: String?, className: String?, ownPackage: String): Boolean {
        if (packageName != ownPackage || className.isNullOrBlank()) return false
        return className == "dev.sergey.triad.unlock.UnlockActivity" || className.endsWith(".UnlockActivity")
    }

    fun eligible(
        profileId: Long,
        targetLangs: List<AppLanguage>,
        reviews: List<ReviewItem>,
        concepts: List<Concept>,
    ): List<ReviewItem> {
        val known = concepts.map { it.id }.toSet()
        val targets = targetLangs.toSet()
        return reviews.filter { review ->
            review.profileId == profileId &&
                review.reps > 0 &&
                review.targetLang in targets &&
                review.conceptId in known
        }
    }

    fun pick(pool: List<ReviewItem>, avoidConceptId: String?, random: Random): ReviewItem? {
        if (pool.isEmpty()) return null
        val preferred = if (avoidConceptId != null && pool.any { it.conceptId != avoidConceptId }) {
            pool.filter { it.conceptId != avoidConceptId }
        } else {
            pool
        }
        return preferred.random(random)
    }

    fun decide(
        snapshot: UnlockSnapshot,
        keyguardLocked: Boolean,
        foregroundPackage: String?,
        foregroundClass: String?,
        ownPackage: String,
        enabled: Boolean,
        hasPool: Boolean,
    ): Pair<UnlockSnapshot, UnlockAction> {
        if (keyguardLocked) {
            return UnlockSnapshot(locked = true, passed = false) to UnlockAction.Dismiss
        }
        if (!enabled || !hasPool) {
            return UnlockSnapshot(locked = false, passed = true) to UnlockAction.Dismiss
        }
        if (yieldsTo(foregroundPackage)) {
            val passed = snapshot.passed && !snapshot.locked
            return UnlockSnapshot(locked = false, passed = passed) to UnlockAction.Ignore
        }
        val justUnlocked = snapshot.locked
        if (justUnlocked) {
            return UnlockSnapshot(locked = false, passed = false) to UnlockAction.Show
        }
        if (snapshot.passed) {
            return UnlockSnapshot(locked = false, passed = true) to UnlockAction.Ignore
        }
        if (foregroundPackage.isNullOrBlank()) {
            return UnlockSnapshot(locked = false, passed = false) to UnlockAction.Ignore
        }
        if (isQuizInFront(foregroundPackage, foregroundClass, ownPackage)) {
            return UnlockSnapshot(locked = false, passed = false) to UnlockAction.Ignore
        }
        return UnlockSnapshot(locked = false, passed = false) to UnlockAction.Show
    }
}
