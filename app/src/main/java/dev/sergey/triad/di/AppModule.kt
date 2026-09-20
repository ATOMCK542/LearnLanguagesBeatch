package dev.sergey.triad.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sergey.triad.data.db.TriadDatabase
import dev.sergey.triad.data.scheduler.FsrsScheduler
import dev.sergey.triad.data.translate.StubTranslationEngine
import dev.sergey.triad.data.translate.TranslationEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): TriadDatabase =
        Room.databaseBuilder(context, TriadDatabase::class.java, "triad.db")
            .addMigrations(TriadDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun catalogDao(db: TriadDatabase) = db.catalog()

    @Provides
    @Singleton
    fun scheduler(): FsrsScheduler = FsrsScheduler()

    @Provides
    @Singleton
    fun translationEngine(): TranslationEngine = StubTranslationEngine()
}
