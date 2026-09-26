package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class UnlockGateTest {
    private val own = "dev.sergey.triad"
    private val quiz = "dev.sergey.triad.unlock.UnlockActivity"

    private fun concept(id: String) = Concept(
        id = id,
        kind = ConceptKind.Word,
        themeId = "cafe",
        tags = emptyList(),
        grammar = LocalizedText("", "", ""),
        texts = AppLanguage.all.associateWith { lang ->
            ConceptText(lang, "$id-${lang.code}", "", emptyMap())
        },
    )

    private fun review(
        profileId: Long,
        conceptId: String,
        lang: AppLanguage = AppLanguage.En,
        reps: Int = 2,
    ) = ReviewItem(
        profileId, conceptId, lang, 1.0, 5.0, 0, 1, reps, 0, FsrsCardState.Review, 1.0, 1.0,
    )

    private fun decide(
        snapshot: UnlockSnapshot = UnlockSnapshot(),
        keyguardLocked: Boolean = false,
        foregroundPackage: String? = "com.sec.android.app.launcher",
        foregroundClass: String? = "com.sec.android.app.launcher.Launcher",
        enabled: Boolean = true,
        hasPool: Boolean = true,
    ) = UnlockGate.decide(
        snapshot,
        keyguardLocked,
        foregroundPackage,
        foregroundClass,
        own,
        enabled,
        hasPool,
    )

    @Test
    fun eligibleKeepsSeenCardsForThisProfileOnly() {
        val concepts = listOf(concept("a"), concept("b"), concept("c"))
        val reviews = listOf(
            review(1, "a"),
            review(1, "b", reps = 0),
            review(1, "c", lang = AppLanguage.Vi),
            review(2, "a"),
            review(1, "missing"),
        )
        val pool = UnlockGate.eligible(1, listOf(AppLanguage.En), reviews, concepts)
        assertEquals(listOf("a"), pool.map { it.conceptId })
        assertTrue(pool.all { it.profileId == 1L && it.reps > 0 })
    }

    @Test
    fun emptyPoolDoesNotShow() {
        assertFalse(UnlockGate.shouldShow(enabled = true, pool = emptyList<ReviewItem>()))
        assertFalse(UnlockGate.shouldShow(enabled = false, pool = listOf(review(1, "a"))))
        assertTrue(UnlockGate.shouldShow(enabled = true, pool = listOf(review(1, "a"))))
    }

    @Test
    fun wrongAnswerDoesNotAdvanceAndNthCorrectPasses() {
        val start = UnlockProgress(required = 2)
        val missed = start.answer(false)
        assertEquals(0, missed.correct)
        assertFalse(missed.passed)
        val once = start.answer(true)
        assertEquals(1, once.correct)
        assertFalse(once.passed)
        val done = once.answer(true)
        assertTrue(done.passed)
        assertEquals(2, done.answer(true).correct)
    }

    @Test
    fun clampRequiredStaysInRange() {
        assertEquals(1, UnlockGate.clampRequired(0))
        assertEquals(10, UnlockGate.clampRequired(11))
        assertEquals(3, UnlockGate.clampRequired(3))
    }

    @Test
    fun pickSkipsTheSameConceptWhenAnotherExists() {
        val pool = listOf(review(1, "a"), review(1, "b"))
        val picked = (1..20).map { UnlockGate.pick(pool, "a", Random(it)) }
        assertTrue(picked.all { it?.conceptId == "b" })
        assertEquals("a", UnlockGate.pick(listOf(review(1, "a")), "a", Random(1))?.conceptId)
        assertNull(UnlockGate.pick(emptyList(), null, Random(1)))
    }

    @Test
    fun unlockShowsQuizUntilPassedThenLockResets() {
        val (shown, show) = decide(snapshot = UnlockSnapshot(locked = true, passed = false))
        assertEquals(UnlockAction.Show, show)
        assertFalse(shown.passed)

        val (left, again) = decide(
            snapshot = shown,
            foregroundPackage = "com.sec.android.app.launcher",
        )
        assertEquals(UnlockAction.Show, again)
        assertFalse(left.passed)

        val (front, stay) = decide(
            snapshot = left,
            foregroundPackage = own,
            foregroundClass = quiz,
        )
        assertEquals(UnlockAction.Ignore, stay)

        val (main, cover) = decide(
            snapshot = front,
            foregroundPackage = own,
            foregroundClass = "dev.sergey.triad.MainActivity",
        )
        assertEquals(UnlockAction.Show, cover)

        val passed = UnlockSnapshot(locked = false, passed = true)
        val (quiet, ignore) = decide(snapshot = passed)
        assertEquals(UnlockAction.Ignore, ignore)
        assertTrue(quiet.passed)

        val (reset, dismiss) = decide(snapshot = quiet, keyguardLocked = true)
        assertEquals(UnlockAction.Dismiss, dismiss)
        assertTrue(reset.locked)
        assertFalse(reset.passed)
    }

    @Test
    fun callDoesNotCoverAndQuizReturnsAfter() {
        assertTrue(UnlockGate.yieldsTo("com.samsung.android.incallui"))
        assertFalse(UnlockGate.yieldsTo("com.samsung.android.dialer"))
        assertFalse(UnlockGate.yieldsTo(null))

        val (during, hold) = decide(
            snapshot = UnlockSnapshot(locked = true, passed = false),
            foregroundPackage = "com.samsung.android.incallui",
            foregroundClass = "com.samsung.android.incallui.InCallActivity",
        )
        assertEquals(UnlockAction.Ignore, hold)
        assertFalse(during.locked)
        assertFalse(during.passed)

        val (_, show) = decide(snapshot = during, foregroundPackage = "com.sec.android.app.launcher")
        assertEquals(UnlockAction.Show, show)
    }

    @Test
    fun disabledOrEmptyPoolDismissesUntilNextLock() {
        val (off, dismiss) = decide(enabled = false)
        assertEquals(UnlockAction.Dismiss, dismiss)
        assertTrue(off.passed)
        assertFalse(off.locked)

        val (empty, dismissEmpty) = decide(hasPool = false)
        assertEquals(UnlockAction.Dismiss, dismissEmpty)
        assertTrue(empty.passed)
    }
}
