package dev.sergey.triad.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.data.db.CatalogDao
import dev.sergey.triad.data.db.ConceptPartEntity
import dev.sergey.triad.data.db.Mappers.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalog: CatalogDao,
) {
    suspend fun importIfNeeded() {
        // Re-upsert bundled packs so IPA and copy updates reach existing installs.
        importAll()
    }

    suspend fun importAll() {
        val names = context.assets.list("content").orEmpty().filter { it.endsWith(".json") }
        val themes = mutableListOf<dev.sergey.triad.domain.Theme>()
        val concepts = mutableListOf<dev.sergey.triad.domain.Concept>()
        names.sorted().forEach { name ->
            val raw = context.assets.open("content/$name").bufferedReader().use { it.readText() }
            val pack = PackParser.parse(raw)
            themes += PackParser.themesOf(pack)
            concepts += PackParser.conceptsOf(pack)
        }
        catalog.upsertThemes(themes.distinctBy { it.id }.map { it.toEntity() })
        catalog.upsertConcepts(concepts.map { it.toEntity() })
        catalog.upsertTexts(concepts.flatMap { c -> c.texts.values.map { it.toEntity(c.id) } })
        catalog.clearParts()
        val parts = concepts.flatMap { concept ->
            concept.uses.map { wordId -> ConceptPartEntity(concept.id, wordId, concept.level) }
        }
        if (parts.isNotEmpty()) catalog.insertParts(parts)
    }
}
