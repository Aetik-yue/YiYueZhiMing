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
    val daysLeft: String
        get() {
            val today = LocalDate.now()
            val thisYear = date.withYear(today.year)
            val next = if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
            val days = ChronoUnit.DAYS.between(today, next)
            return when (days) {
                0L -> "就是今天"
                else -> "还有${days}天"
            }
        }
}
