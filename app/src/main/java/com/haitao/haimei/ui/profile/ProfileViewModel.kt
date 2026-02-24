package com.haitao.haimei.ui.profile

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haitao.haimei.data.AppDatabase
import com.haitao.haimei.data.entity.ProfileEntity
import com.haitao.haimei.data.repository.ProfileRepository
import com.haitao.haimei.util.ZodiacTraits
import com.haitao.haimei.util.ZodiacUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(AppDatabase.getInstance(application).profileDao())

    val profile = repository.observeProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun ensureProfile() {
        viewModelScope.launch {
            val existing = repository.getProfile()
            if (existing == null) {
                val now = System.currentTimeMillis()
                val today = LocalDate.now()
                val zodiac = ZodiacUtils.zodiacName(today)
                val traits = ZodiacTraits.defaults[zodiac].orEmpty()
                val lunar = com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(today)
                repository.upsert(
                    ProfileEntity(
                        id = 1,
                        name = "",
                        birthday = now,
                        zodiacOverrideEnabled = false,
                        zodiacNameOverride = null,
                        zodiacTraits = traits,
                        lunarMonthIndex = lunar.monthIndex,
                        lunarDay = lunar.day,
                        lunarLeapMonth = lunar.isLeapMonth,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                val date = Instant.ofEpochMilli(existing.birthday).atZone(ZoneId.systemDefault()).toLocalDate()
                val lunar = com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(date)
                val needsUpdate = existing.lunarMonthIndex != lunar.monthIndex ||
                    existing.lunarDay != lunar.day ||
                    existing.lunarLeapMonth != lunar.isLeapMonth
                if (needsUpdate) {
                    repository.upsert(
                        existing.copy(
                            lunarMonthIndex = lunar.monthIndex,
                            lunarDay = lunar.day,
                            lunarLeapMonth = lunar.isLeapMonth,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun updateBirthday(birthdayMillis: Long) {
        updateProfile { current ->
            val date = Instant.ofEpochMilli(birthdayMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val zodiac = ZodiacUtils.zodiacName(date)
            val traits = ZodiacTraits.defaults[zodiac] ?: current.zodiacTraits
            val lunar = com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(date)
            current.copy(
                birthday = birthdayMillis,
                zodiacOverrideEnabled = false,
                zodiacNameOverride = null,
                zodiacTraits = traits,
                lunarMonthIndex = lunar.monthIndex,
                lunarDay = lunar.day,
                lunarLeapMonth = lunar.isLeapMonth,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun updateTraits(text: String) {
        updateProfile { it.copy(zodiacTraits = text, updatedAt = System.currentTimeMillis()) }
    }

    fun updateName(name: String) {
        updateProfile { it.copy(name = name, updatedAt = System.currentTimeMillis()) }
    }

    fun currentZodiacName(profile: ProfileEntity?): String {
        if (profile == null) return "-"
        return if (profile.zodiacOverrideEnabled && !profile.zodiacNameOverride.isNullOrBlank()) {
            profile.zodiacNameOverride
        } else {
            val date = Instant.ofEpochMilli(profile.birthday).atZone(ZoneId.systemDefault()).toLocalDate()
            ZodiacUtils.zodiacName(date)
        }
    }

    fun currentLunarDate(profile: ProfileEntity?): com.haitao.haimei.util.LunarUtils.LunarDate? {
        if (profile == null) return null
        val date = Instant.ofEpochMilli(profile.birthday)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(date)
    }

    fun setFixedLunarBirthday(monthIndex: Int, day: Int, isLeap: Boolean) {
        updateProfile { current ->
            current.copy(
                lunarMonthIndex = monthIndex,
                lunarDay = day,
                lunarLeapMonth = isLeap,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    private fun updateProfile(block: (ProfileEntity) -> ProfileEntity) {
        viewModelScope.launch {
            val current = repository.getProfile() ?: return@launch
            repository.upsert(block(current))
        }
    }
}
