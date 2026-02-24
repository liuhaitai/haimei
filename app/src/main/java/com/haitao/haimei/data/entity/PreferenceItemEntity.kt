package com.haitao.haimei.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preference_items")
data class PreferenceItemEntity(
    @PrimaryKey val id: String,
    val type: String,
    val value: String,
    val note: String?,
    val order: Int,
    val createdAt: Long,
    val updatedAt: Long
)

