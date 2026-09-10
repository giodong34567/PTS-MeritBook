package com.tota.chamdiem.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** Tiện ích ngày tháng. Tuần luôn bắt đầu vào Thứ Hai. */
object DateUtils {

    private val dm = DateTimeFormatter.ofPattern("dd/MM")
    private val dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun today(): LocalDate = LocalDate.now()

    fun mondayOf(date: LocalDate): LocalDate =
        date.minusDays(((date.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())

    fun weekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val start = mondayOf(date)
        return start to start.plusDays(6)
    }

    fun monthRange(ym: YearMonth): Pair<LocalDate, LocalDate> =
        ym.atDay(1) to ym.atEndOfMonth()

    /** Số thứ tự tuần tính từ mốc epoch, khớp với biểu thức SQL ((dateEpochDay + 3) / 7). */
    fun weekIndexOf(date: LocalDate): Long = Math.floorDiv(date.toEpochDay() + 3, 7)

    fun mondayOfWeekIndex(weekIndex: Long): LocalDate =
        LocalDate.ofEpochDay(weekIndex * 7 - 3)

    fun epochDay(d: LocalDate): Long = d.toEpochDay()

    fun fromEpochDay(v: Long): LocalDate = LocalDate.ofEpochDay(v)

    fun fmtDM(d: LocalDate): String = d.format(dm)

    fun fmtDMY(d: LocalDate): String = d.format(dmy)

    fun weekLabel(date: LocalDate): String {
        val (s, e) = weekRange(date)
        return "${fmtDM(s)} – ${fmtDM(e)}/${e.year}"
    }

    fun monthLabel(ym: YearMonth): String = "Tháng ${ym.monthValue}/${ym.year}"

    private val weekdayNames = arrayOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")

    fun weekdayLabel(d: LocalDate): String = weekdayNames[(d.dayOfWeek.value - 1).coerceIn(0, 6)]
}
