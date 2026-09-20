package dev.sergey.triad.data.scheduler

import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.FsrsCardState
import dev.sergey.triad.domain.Rating
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsSchedulerTest {
    private val fsrs = FsrsScheduler()
    private val now = 1_700_000_000_000L

    @Test
    fun easyLongerThanGoodLongerThanHardLongerThanAgain() {
        val fresh = fsrs.newItem(1, "c1", AppLanguage.En, now)
        val again = fsrs.review(fresh, Rating.Again, now)
        val hard = fsrs.review(fresh, Rating.Hard, now)
        val good = fsrs.review(fresh, Rating.Good, now)
        val easy = fsrs.review(fresh, Rating.Easy, now)
        assertTrue(again.dueAt <= hard.dueAt)
        assertTrue(hard.dueAt <= good.dueAt)
        assertTrue(good.dueAt < easy.dueAt)
        assertTrue(good.stability > 0)
        assertTrue(easy.stability >= good.stability)
    }

    @Test
    fun againMarksRelearning() {
        val fresh = fsrs.newItem(1, "c1", AppLanguage.En, now)
        val again = fsrs.review(fresh, Rating.Again, now)
        assertTrue(again.state == FsrsCardState.Relearning)
        assertTrue(again.lapses == 1)
        assertTrue(again.reps == 1)
    }

    @Test
    fun secondGoodExtendsInterval() {
        val fresh = fsrs.newItem(1, "c1", AppLanguage.En, now)
        val first = fsrs.review(fresh, Rating.Good, now)
        val later = now + FsrsScheduler.DAY_MS * 2
        val second = fsrs.review(first, Rating.Good, later)
        assertTrue(second.scheduledDays >= first.scheduledDays)
        assertTrue(second.reps == 2)
    }

    @Test
    fun retrievabilityFallsWithTime() {
        val r0 = fsrs.retrievability(0.0, 2.4)
        val r10 = fsrs.retrievability(10.0, 2.4)
        assertTrue(r0 > r10)
        assertTrue(r0 <= 1.0)
    }

    @Test
    fun previewHasAllRatings() {
        val fresh = fsrs.newItem(2, "c2", AppLanguage.Vi, now)
        val preview = fsrs.preview(fresh, now)
        assertTrue(preview.keys.containsAll(Rating.entries.toSet()))
    }
}
