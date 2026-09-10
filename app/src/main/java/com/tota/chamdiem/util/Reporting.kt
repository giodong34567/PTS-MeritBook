package com.tota.chamdiem.util

import com.tota.chamdiem.data.MemberScore
import com.tota.chamdiem.data.WeekMemberAgg
import java.util.Locale

/** Tính toán và xuất báo cáo tổng hợp. */
object Reporting {

    /** Một dòng báo cáo theo tháng / học kì (gộp nhiều tuần). */
    data class PeriodRow(
        val memberId: Long,
        val memberName: String,
        val weeksScored: Int,
        val plusCount: Int,
        val minusCount: Int,
        val bonusPoints: Int,
        val penaltyPoints: Int,
        val avgWeekScore: Double,
        val totalDelta: Int,
    )

    fun periodRows(
        aggs: List<WeekMemberAgg>,
        members: List<Pair<Long, String>>,
        baseScore: Int,
    ): List<PeriodRow> {
        val weeks = aggs.map { it.weekIndex }.toSortedSet()
        val weeksScored = weeks.size
        return members.map { (id, name) ->
            val mine = aggs.filter { it.memberId == id }
            val netByWeek = mine.associate { it.weekIndex to it.net }
            val avg = if (weeksScored == 0) {
                baseScore.toDouble()
            } else {
                weeks.sumOf { (baseScore + (netByWeek[it] ?: 0)).toDouble() } / weeksScored
            }
            PeriodRow(
                memberId = id,
                memberName = name,
                weeksScored = weeksScored,
                plusCount = mine.sumOf { it.plusCount },
                minusCount = mine.sumOf { it.minusCount },
                bonusPoints = mine.sumOf { it.plusPoints },
                penaltyPoints = mine.sumOf { it.minusPoints },
                avgWeekScore = avg,
                totalDelta = mine.sumOf { it.net },
            )
        }.sortedWith(compareByDescending<PeriodRow> { it.avgWeekScore }.thenBy { it.memberName })
    }

    fun fmt1(v: Double): String = String.format(Locale.US, "%.1f", v)

    private fun csvCell(s: String): String =
        if (s.contains(',') || s.contains('"') || s.contains('\n')) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else {
            s
        }

    // ----- Báo cáo TUẦN -----

    fun weekCsv(title: String, baseScore: Int, scores: List<MemberScore>): String {
        val sb = StringBuilder()
        sb.append(title).append('\n')
        sb.append("Điểm gốc,").append(baseScore).append('\n')
        sb.append("Hạng,Thành viên,Lượt cộng,Lượt trừ,Điểm cộng,Điểm trừ,Điểm thi đua\n")
        scores.forEachIndexed { i, s ->
            sb.append(i + 1).append(',')
                .append(csvCell(s.memberName)).append(',')
                .append(s.plusCount).append(',')
                .append(s.minusCount).append(',')
                .append(s.plusPoints).append(',')
                .append(s.minusPoints).append(',')
                .append(baseScore + s.net).append('\n')
        }
        return sb.toString()
    }

    fun weekText(title: String, baseScore: Int, scores: List<MemberScore>): String {
        val sb = StringBuilder()
        sb.append("BÁO CÁO THI ĐUA – ").append(title).append('\n')
        sb.append("Điểm gốc mỗi tuần: ").append(baseScore).append('\n')
        sb.append("──────────────────────────────\n")
        scores.forEachIndexed { i, s ->
            sb.append(i + 1).append(". ").append(s.memberName)
                .append("  =  ").append(baseScore + s.net).append(" điểm")
                .append("   (▲").append(s.plusCount).append("/+").append(s.plusPoints)
                .append("  ▼").append(s.minusCount).append('/').append(s.minusPoints).append(")\n")
        }
        return sb.toString()
    }

    // ----- Báo cáo THÁNG / KÌ -----

    fun periodCsv(title: String, baseScore: Int, rows: List<PeriodRow>): String {
        val sb = StringBuilder()
        sb.append(title).append('\n')
        sb.append("Điểm gốc,").append(baseScore).append('\n')
        sb.append("Hạng,Thành viên,Số tuần,Lượt cộng,Lượt trừ,Điểm cộng,Điểm trừ,TB điểm tuần,Tổng chênh lệch\n")
        rows.forEachIndexed { i, r ->
            sb.append(i + 1).append(',')
                .append(csvCell(r.memberName)).append(',')
                .append(r.weeksScored).append(',')
                .append(r.plusCount).append(',')
                .append(r.minusCount).append(',')
                .append(r.bonusPoints).append(',')
                .append(r.penaltyPoints).append(',')
                .append(fmt1(r.avgWeekScore)).append(',')
                .append(if (r.totalDelta > 0) "+${r.totalDelta}" else r.totalDelta.toString())
                .append('\n')
        }
        return sb.toString()
    }

    fun periodText(title: String, baseScore: Int, rows: List<PeriodRow>): String {
        val weeks = rows.firstOrNull()?.weeksScored ?: 0
        val sb = StringBuilder()
        sb.append("BÁO CÁO THI ĐUA – ").append(title).append('\n')
        sb.append("Điểm gốc: ").append(baseScore).append("   Số tuần đã chấm: ").append(weeks).append('\n')
        sb.append("──────────────────────────────\n")
        rows.forEachIndexed { i, r ->
            sb.append(i + 1).append(". ").append(r.memberName)
                .append("  =  ").append(fmt1(r.avgWeekScore)).append(" điểm/tuần")
                .append("   (▲").append(r.plusCount).append("  ▼").append(r.minusCount)
                .append("  chênh ").append(if (r.totalDelta > 0) "+${r.totalDelta}" else r.totalDelta.toString())
                .append(")\n")
        }
        return sb.toString()
    }
}
