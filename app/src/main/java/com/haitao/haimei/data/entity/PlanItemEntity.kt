package com.haitao.haimei.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val priority: Int,
    val isDone: Boolean,
    val createdAt: Long,
    val doneAt: Long?,
    val updatedAt: Long
)

