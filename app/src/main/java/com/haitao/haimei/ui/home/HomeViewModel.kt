package com.haitao.haimei.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haitao.haimei.data.AppDatabase
import com.haitao.haimei.data.entity.DiaryEntryEntity
import com.haitao.haimei.data.entity.ProfileEntity
import com.haitao.haimei.data.repository.DiaryRepository
import com.haitao.haimei.data.repository.ProfileRepository
import com.haitao.haimei.util.ZodiacTraits
import com.haitao.haimei.util.ZodiacUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val profileRepository = ProfileRepository(AppDatabase.getInstance(application).profileDao())
    private val diaryRepository = DiaryRepository(AppDatabase.getInstance(application).diaryDao())

    val profile = profileRepository.observeProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val latestDiary = diaryRepository.observeAll()
        .map { entries -> entries.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun ensureProfile() {
        viewModelScope.launch {
            val existing = profileRepository.getProfile()
            if (existing == null) {
                val now = System.currentTimeMillis()
                val today = LocalDate.now()
                val zodiac = ZodiacUtils.zodiacName(today)
                val traits = ZodiacTraits.defaults[zodiac].orEmpty()
                val lunar = com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(today)
                profileRepository.upsert(
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
            }
        }
    }

    fun birthdayCountdownText(profile: ProfileEntity?): String {
        return birthdayCountdownSolarText(profile)
    }

    fun birthdayCountdownSolarText(profile: ProfileEntity?): String {
        if (profile == null) return "公历生日：未设置"
        val zone = ZoneId.systemDefault()
        val birthday = Instant.ofEpochMilli(profile.birthday).atZone(zone).toLocalDate()
        val now = LocalDate.now(zone)
        var nextBirthday = birthday.withYear(now.year)
        if (!nextBirthday.isAfter(now)) {
            nextBirthday = nextBirthday.plusYears(1)
        }
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, nextBirthday)
        return if (days == 0L) {
            "公历生日：今天"
        } else {
            "公历生日：还有 $days 天"
        }
    }

    fun birthdayCountdownLunarText(profile: ProfileEntity?): String {
        if (profile == null) return "农历生日：未设置"
        val lunar = currentLunarDate(profile) ?: return "农历生日：未设置"
        val nowMillis = System.currentTimeMillis()
        val nextLunarMillis = com.haitao.haimei.util.LunarUtils.nextLunarBirthdayMillis(lunar, nowMillis)
        val now = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val next = Instant.ofEpochMilli(nextLunarMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, next)
        return if (days == 0L) {
            "农历生日：今天"
        } else {
            "农历生日：还有 $days 天"
        }
    }

    fun zodiacTitle(profile: ProfileEntity?): String {
        if (profile == null) return "星座"
        return if (profile.zodiacOverrideEnabled && !profile.zodiacNameOverride.isNullOrBlank()) {
            profile.zodiacNameOverride
        } else {
            val date = Instant.ofEpochMilli(profile.birthday).atZone(ZoneId.systemDefault()).toLocalDate()
            ZodiacUtils.zodiacName(date)
        }
    }

    fun zodiacSubtitle(profile: ProfileEntity?): String {
        return profile?.zodiacTraits?.ifBlank { "点击编辑星座特点" } ?: "点击完善她的资料"
    }

    fun latestDiarySummary(entry: DiaryEntryEntity?): String {
        if (entry == null) return "还没有日记，去写第一条吧"
        return entry.title.ifBlank { "(无标题)" }
    }

    fun birthdaySolarText(profile: ProfileEntity?): String {
        val text = profile?.birthday?.let { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            date.toString()
        } ?: "-"
        return "公历生日：$text"
    }

    fun birthdayLunarText(profile: ProfileEntity?): String {
        val lunar = currentLunarDate(profile) ?: return "农历生日：-"
        val text = com.haitao.haimei.util.LunarUtils.lunarDateText(lunar)
        return "农历生日：$text"
    }

    private fun currentLunarDate(profile: ProfileEntity?): com.haitao.haimei.util.LunarUtils.LunarDate? {
        if (profile == null) return null
        val date = Instant.ofEpochMilli(profile.birthday)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return com.haitao.haimei.util.LunarUtils.lunarDateFromLocalDate(date)
    }
}
