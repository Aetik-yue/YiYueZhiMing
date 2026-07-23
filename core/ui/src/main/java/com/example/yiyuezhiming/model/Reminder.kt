package com.example.yiyuezhiming.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reminder(
    val id: Long = 0,
    val title: String,
    val type: String,
    val date: LocalDate,
    val isEnabled: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    val dateText: String get() = "%02d / %02d".format(date.monthValue, date.dayOfMonth)
    val monthText: String get() = "${date.monthValue}月"
    val dayText: String get() = "%02d".format(date.dayOfMonth)

    /** 距下一次到来的天数（数值，供排序与色彩分级共用） */
    val daysLeftValue: Long
        get() {
            val today = LocalDate.now()
            val thisYear = date.withYear(today.year)
            val next = if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
            return ChronoUnit.DAYS.between(today, next)
        }

    val daysLeft: String
        get() = when (daysLeftValue) {
            0L -> "就是今天"
            else -> "还有${daysLeftValue}天"
        }
}
