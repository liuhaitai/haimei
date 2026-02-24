package com.haitao.haimei.data.repository

import com.haitao.haimei.data.dao.PreferenceItemDao
import com.haitao.haimei.data.entity.PreferenceItemEntity
import kotlinx.coroutines.flow.Flow

class PreferenceRepository(private val dao: PreferenceItemDao) {
    fun observeByType(type: String): Flow<List<PreferenceItemEntity>> = dao.observeByType(type)

    suspend fun upsert(item: PreferenceItemEntity) = dao.upsert(item)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}

