package com.haitao.haimei.data.repository

import com.haitao.haimei.data.dao.ProfileDao
import com.haitao.haimei.data.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val dao: ProfileDao) {
    fun observeProfile(): Flow<ProfileEntity?> = dao.observeProfile()

    suspend fun upsert(profile: ProfileEntity) = dao.upsert(profile)

    suspend fun getProfile(): ProfileEntity? = dao.getProfile()
}

