package com.haitao.haimei.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val birthday: Long,
    val zodiacOverrideEnabled: Boolean,
    val zodiacNameOverride: String?,
    val zodiacTraits: String,
    val lunarMonthIndex: Int?,
    val lunarDay: Int?,
    val lunarLeapMonth: Boolean?,
    val createdAt: Long,
    val updatedAt: Long
)
