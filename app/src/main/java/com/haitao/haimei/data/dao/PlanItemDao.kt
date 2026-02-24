package com.haitao.haimei.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haitao.haimei.data.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {
    @Query("SELECT * FROM plan_items ORDER BY isDone ASC, createdAt DESC")
    fun observeAll(): Flow<List<PlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PlanItemEntity)

    @Query("DELETE FROM plan_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

