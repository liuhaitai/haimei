package com.haitao.haimei.data.repository

import com.haitao.haimei.data.dao.DiaryDao
import com.haitao.haimei.data.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {
    fun observeAll(): Flow<List<DiaryEntryEntity>> = dao.observeAll()

    fun searchByTitleOrTags(query: String): Flow<List<DiaryEntryEntity>> = dao.searchByTitleOrTags(query)

    suspend fun getById(id: String): DiaryEntryEntity? = dao.getById(id)

    suspend fun upsert(entry: DiaryEntryEntity) = dao.upsert(entry)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}

