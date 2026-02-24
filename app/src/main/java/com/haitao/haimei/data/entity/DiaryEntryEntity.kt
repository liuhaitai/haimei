package com.haitao.haimei.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val time: Long,
    val mood: String?,
    val tags: String?,
    val createdAt: Long,
    val updatedAt: Long
)

