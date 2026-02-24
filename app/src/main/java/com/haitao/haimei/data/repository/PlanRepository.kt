package com.haitao.haimei.data.repository

import com.haitao.haimei.data.dao.PlanItemDao
import com.haitao.haimei.data.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

class PlanRepository(private val dao: PlanItemDao) {
    fun observeAll(): Flow<List<PlanItemEntity>> = dao.observeAll()

    suspend fun upsert(item: PlanItemEntity) = dao.upsert(item)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}

