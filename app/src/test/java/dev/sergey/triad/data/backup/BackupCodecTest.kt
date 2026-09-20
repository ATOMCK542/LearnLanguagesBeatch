package dev.sergey.triad.data.backup

import dev.sergey.triad.data.db.ProfileEntity
import dev.sergey.triad.data.db.ReviewItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
    @Test
    fun exportCurrentProfileDoesNotLeakOther() {
        val a = ProfileEntity(1, "A", null, 1, 1, "en", "en", "vi", 10, true, 0, null, 3)
        val b = ProfileEntity(2, "B", null, 1, 1, "ru", "ru", "en", 10, true, 0, null, 9)
        val reviews = listOf(
            ReviewItemEntity(1, "c1", "vi", 1.0, 5.0, 10, null, 1, 0, "Review", 0.0, 1.0),
            ReviewItemEntity(2, "c2", "en", 1.0, 5.0, 10, null, 1, 0, "Review", 0.0, 1.0),
        )
        val payload = BackupCodec.fromEntities(listOf(a, b), reviews, emptyList(), emptyList(), emptyList(), 1)
        assertEquals(1, payload.profiles.size)
        assertEquals("A", payload.profiles.first().displayName)
        assertTrue(payload.reviews.all { it.profileId == 1L })
        assertTrue(payload.reviews.none { it.conceptId == "c2" })
    }

    @Test
    fun remapIdsOnImport() {
        val raw = BackupCodec.encode(
            BackupPayload(
                profiles = listOf(
                    ProfileEntityDto(1, "A", "x", 1, 1, "en", "en", "vi", 10, true, 0, null, 0),
                ),
                reviews = listOf(ReviewDto(1, "c1", "vi", 1.0, 5.0, 10, null, 1, 0, "Review", 0.0, 1.0)),
                userCards = emptyList(),
                primers = emptyList(),
                path = emptyList(),
            ),
        )
        val remapped = BackupCodec.remapIds(BackupCodec.decode(raw), 40)
        assertEquals(40, remapped.profiles.first().id)
        assertEquals(40, remapped.reviews.first().profileId)
    }
}
