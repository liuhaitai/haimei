package com.haitao.haimei.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haitao.haimei.data.entity.PreferenceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceItemDao {
    @Query("SELECT * FROM preference_items WHERE type = :type ORDER BY `order` ASC")
    fun observeByType(type: String): Flow<List<PreferenceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PreferenceItemEntity)

    @Query("DELETE FROM preference_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

