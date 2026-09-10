package com.tota.chamdiem.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val active: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Dương = cộng điểm, âm = trừ điểm. */
    val points: Int,
    val active: Boolean = true,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "score_entries",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("memberId"), Index("categoryId"), Index("dateEpochDay")],
)
data class ScoreEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val categoryId: Long?,
    /** Chụp lại tên loại tại thời điểm chấm. */
    val categoryName: String,
    /** Chụp lại số điểm tại thời điểm chấm. */
    val points: Int,
    /** Ngày áp dụng điểm = LocalDate.toEpochDay(). */
    val dateEpochDay: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
)

/** Cấu hình chung, luôn chỉ có một dòng id = 1. */
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val id: Int = 1,
    /** Điểm gốc mỗi thành viên có khi bắt đầu một tuần mới. */
    val weekBaseScore: Int = 100,
)

/** Kết quả tổng hợp điểm của một thành viên trong một khoảng thời gian. */
data class MemberScore(
    val memberId: Long,
    val memberName: String,
    val plusCount: Int,
    val minusCount: Int,
    val plusPoints: Int,
    val minusPoints: Int,
    val net: Int,
)

/** Tổng điểm của một thành viên trong một tuần cụ thể (weekIndex = số thứ tự tuần tính từ mốc epoch). */
data class WeekMemberAgg(
    val memberId: Long,
    val memberName: String,
    val weekIndex: Long,
    val plusCount: Int,
    val minusCount: Int,
    val plusPoints: Int,
    val minusPoints: Int,
    val net: Int,
)

/** Một lượt điểm kèm tên thành viên, dùng cho màn hình Lịch sử. */
data class EntryWithMember(
    val id: Long,
    val memberId: Long,
    val memberName: String,
    val categoryName: String,
    val points: Int,
    val dateEpochDay: Long,
    val note: String?,
    val createdAt: Long,
)
