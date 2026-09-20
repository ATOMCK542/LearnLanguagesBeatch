package dev.sergey.triad.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.FsrsCardState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ProfileIsolationTest {
    @Test
    fun deletingProfileRemovesOnlyItsReviews() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TriadDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val a = db.profiles().upsert(
            ProfileEntity(0, "A", null, 1, 1, "en", "en", "vi", 10, true, 0, null, 0),
        )
        val b = db.profiles().upsert(
            ProfileEntity(0, "B", null, 1, 1, "ru", "ru", "en", 10, true, 0, null, 0),
        )
        db.progress().upsertReview(
            ReviewItemEntity(a, "c1", AppLanguage.Vi.code, 1.0, 5.0, 1, null, 1, 0, FsrsCardState.Review.name, 0.0, 1.0),
        )
        db.progress().upsertReview(
            ReviewItemEntity(b, "c2", AppLanguage.En.code, 1.0, 5.0, 1, null, 1, 0, FsrsCardState.Review.name, 0.0, 1.0),
        )
        db.progress().upsertMastered(MasteredConceptEntity(a, "c1"))
        db.progress().upsertMastered(MasteredConceptEntity(b, "c2"))
        db.deleteProfileCascade(a)
        assertTrue(db.progress().reviews(a).isEmpty())
        assertTrue(db.progress().mastered(a).isEmpty())
        assertEquals(1, db.progress().reviews(b).size)
        assertEquals(1, db.progress().mastered(b).size)
        db.close()
    }
}
