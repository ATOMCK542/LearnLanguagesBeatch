package dev.sergey.triad.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val email: String?,
    val createdAt: Long,
    val lastOpenedAt: Long,
    val uiLang: String,
    val nativeLang: String,
    val targetLangs: String,
    val newLimit: Int,
    val ttsEnabled: Boolean,
    val streakDays: Int,
    val lastStudyDay: String?,
    val reviewsDone: Int,
)

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val sortOrder: Int,
    val titleJson: String,
    val descriptionJson: String,
)

@Entity(tableName = "concepts")
data class ConceptEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val themeId: String,
    val tags: String,
    val grammarJson: String,
)

@Entity(tableName = "concept_texts", primaryKeys = ["conceptId", "lang"])
data class ConceptTextEntity(
    val conceptId: String,
    val lang: String,
    val text: String,
    val ipa: String,
    val hintsJson: String,
    val tones: String,
)

@Entity(
    tableName = "review_items",
    primaryKeys = ["profileId", "conceptId", "targetLang"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index("dueAt")],
)
data class ReviewItemEntity(
    val profileId: Long,
    val conceptId: String,
    val targetLang: String,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewAt: Long?,
    val reps: Int,
    val lapses: Int,
    val state: String,
    val elapsedDays: Double,
    val scheduledDays: Double,
)

@Entity(
    tableName = "user_cards",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class UserCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val conceptId: String,
)

@Entity(
    tableName = "primer_progress",
    primaryKeys = ["profileId", "primerId"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PrimerProgressEntity(
    val profileId: Long,
    val primerId: String,
    val completed: Boolean,
)

@Entity(
    tableName = "path_progress",
    primaryKeys = ["profileId", "themeId"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PathProgressEntity(
    val profileId: Long,
    val themeId: String,
    val unlocked: Boolean,
    val completedCount: Int,
)

@Entity(
    tableName = "mastered_concepts",
    primaryKeys = ["profileId", "conceptId"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class MasteredConceptEntity(
    val profileId: Long,
    val conceptId: String,
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY lastOpenedAt DESC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY lastOpenedAt DESC")
    suspend fun all(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun byId(id: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity): Long

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThemes(items: List<ThemeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConcepts(items: List<ConceptEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTexts(items: List<ConceptTextEntity>)

    @Query("SELECT * FROM themes ORDER BY sortOrder")
    suspend fun themes(): List<ThemeEntity>

    @Query("SELECT * FROM concepts")
    suspend fun concepts(): List<ConceptEntity>

    @Query("SELECT * FROM concept_texts")
    suspend fun texts(): List<ConceptTextEntity>

    @Query("SELECT COUNT(*) FROM concepts")
    suspend fun conceptCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserCard(card: UserCardEntity): Long

    @Query("SELECT * FROM user_cards WHERE profileId = :profileId")
    suspend fun userCards(profileId: Long): List<UserCardEntity>

    @Query("DELETE FROM user_cards WHERE profileId = :profileId AND id = :id")
    suspend fun deleteUserCard(profileId: Long, id: Long)
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(item: ReviewItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReviews(items: List<ReviewItemEntity>)

    @Query("SELECT * FROM review_items WHERE profileId = :profileId")
    suspend fun reviews(profileId: Long): List<ReviewItemEntity>

    @Query("SELECT * FROM review_items")
    suspend fun allReviews(): List<ReviewItemEntity>

    @Query("SELECT COUNT(*) FROM review_items WHERE profileId = :profileId AND dueAt <= :now")
    suspend fun dueCount(profileId: Long, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrimer(item: PrimerProgressEntity)

    @Query("SELECT * FROM primer_progress WHERE profileId = :profileId")
    suspend fun primers(profileId: Long): List<PrimerProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPath(item: PathProgressEntity)

    @Query("SELECT * FROM path_progress WHERE profileId = :profileId")
    suspend fun path(profileId: Long): List<PathProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMastered(item: MasteredConceptEntity)

    @Query("DELETE FROM mastered_concepts WHERE profileId = :profileId AND conceptId = :conceptId")
    suspend fun deleteMastered(profileId: Long, conceptId: String)

    @Query("SELECT * FROM mastered_concepts WHERE profileId = :profileId")
    suspend fun mastered(profileId: Long): List<MasteredConceptEntity>

    @Query("SELECT * FROM mastered_concepts")
    suspend fun allMastered(): List<MasteredConceptEntity>

    @Query("DELETE FROM review_items WHERE profileId = :profileId")
    suspend fun deleteReviews(profileId: Long)
}

@Database(
    entities = [
        ProfileEntity::class,
        ThemeEntity::class,
        ConceptEntity::class,
        ConceptTextEntity::class,
        ReviewItemEntity::class,
        UserCardEntity::class,
        PrimerProgressEntity::class,
        PathProgressEntity::class,
        MasteredConceptEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class TriadDatabase : RoomDatabase() {
    abstract fun profiles(): ProfileDao
    abstract fun catalog(): CatalogDao
    abstract fun progress(): ProgressDao

    @Transaction
    open suspend fun deleteProfileCascade(id: Long) {
        profiles().delete(id)
    }

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `mastered_concepts` (
                      `profileId` INTEGER NOT NULL,
                      `conceptId` TEXT NOT NULL,
                      PRIMARY KEY(`profileId`, `conceptId`),
                      FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_mastered_concepts_profileId` ON `mastered_concepts` (`profileId`)",
                )
            }
        }
    }
}
