package com.haitao.haimei.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haitao.haimei.data.AppDatabase
import com.haitao.haimei.data.entity.DiaryEntryEntity
import com.haitao.haimei.data.repository.DiaryRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiaryRepository(AppDatabase.getInstance(application).diaryDao())

    private val searchQuery = MutableStateFlow("")
    private val moodFilter = MutableStateFlow(MoodFilter.ALL)

    val entries: StateFlow<List<DiaryEntryEntity>> = combine(
        repository.observeAll(),
        searchQuery,
        moodFilter
    ) { entries, query, filter ->
        entries.asSequence()
            .filter { matchesQuery(it, query) }
            .filter { matchesMood(it, filter) }
            .toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query.trim()
    }

    fun setMoodFilter(filter: MoodFilter) {
        moodFilter.value = filter
    }

    fun upsertEntry(
        id: String?,
        title: String,
        content: String,
        time: Long,
        mood: String?,
        tags: String?,
        imageUris: List<String>?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = id?.let { repository.getById(it) }
            val entry = DiaryEntryEntity(
                id = id ?: UUID.randomUUID().toString(),
                title = title,
                content = content,
                time = time,
                mood = mood,
                tags = tags,
                imageUris = imageUris?.joinToString("|")?.ifBlank { null },
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
            repository.upsert(entry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    suspend fun getEntry(id: String): DiaryEntryEntity? {
        return repository.getById(id)
    }

    private fun matchesQuery(entry: DiaryEntryEntity, query: String): Boolean {
        if (query.isBlank()) return true
        val lower = query.lowercase()
        return entry.title.lowercase().contains(lower) ||
            entry.content.lowercase().contains(lower) ||
            (entry.tags?.lowercase()?.contains(lower) == true)
    }

    private fun matchesMood(entry: DiaryEntryEntity, filter: MoodFilter): Boolean {
        if (filter == MoodFilter.ALL) return true
        return entry.mood == filter.code
    }
}
