package com.tota.chamdiem.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tota.chamdiem.ChamDiemApp
import com.tota.chamdiem.data.Category
import com.tota.chamdiem.data.EntryWithMember
import com.tota.chamdiem.data.Member
import com.tota.chamdiem.data.MemberScore
import com.tota.chamdiem.data.ScoreEntry
import com.tota.chamdiem.data.Semester
import com.tota.chamdiem.data.Setting
import com.tota.chamdiem.util.DateUtils
import com.tota.chamdiem.util.Reporting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class ReportMode { WEEK, MONTH, SEMESTER }

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ChamDiemApp).repository

    private fun <T> StateFlow<T>.hot() = this
    private fun started() = SharingStarted.WhileSubscribed(5_000)

    val settings: StateFlow<Setting> =
        repo.settings.map { it ?: Setting() }.stateIn(viewModelScope, started(), Setting())

    val members: StateFlow<List<Member>> =
        repo.members.stateIn(viewModelScope, started(), emptyList())

    val activeMembers: StateFlow<List<Member>> =
        repo.activeMembers.stateIn(viewModelScope, started(), emptyList())

    val categories: StateFlow<List<Category>> =
        repo.categories.stateIn(viewModelScope, started(), emptyList())

    val activeCategories: StateFlow<List<Category>> =
        repo.activeCategories.stateIn(viewModelScope, started(), emptyList())

    val semesters: StateFlow<List<Semester>> =
        repo.semesters.stateIn(viewModelScope, started(), emptyList())

    // ---------------- Trang chủ ----------------

    private val _homeWeek = MutableStateFlow(LocalDate.now())
    val homeWeek: StateFlow<LocalDate> = _homeWeek

    val homeScores: StateFlow<List<MemberScore>> =
        _homeWeek.flatMapLatest { anchor ->
            val (s, e) = DateUtils.weekRange(anchor)
            repo.scores(s.toEpochDay(), e.toEpochDay())
        }.stateIn(viewModelScope, started(), emptyList())

    fun homePrevWeek() { _homeWeek.value = _homeWeek.value.minusWeeks(1) }
    fun homeNextWeek() { _homeWeek.value = _homeWeek.value.plusWeeks(1) }
    fun homeThisWeek() { _homeWeek.value = LocalDate.now() }

    fun quickLog(memberId: Long, category: Category) = viewModelScope.launch {
        repo.logEntry(listOf(memberId), category, LocalDate.now(), null)
    }

    // ---------------- Nhập điểm ----------------

    fun logEntry(memberIds: List<Long>, category: Category, date: LocalDate, note: String?) =
        viewModelScope.launch { repo.logEntry(memberIds, category, date, note) }

    // ---------------- Lịch sử ----------------

    private val _historyMonth = MutableStateFlow(YearMonth.now())
    val historyMonth: StateFlow<YearMonth> = _historyMonth

    private val _historyMemberId = MutableStateFlow<Long?>(null)
    val historyMemberId: StateFlow<Long?> = _historyMemberId

    val historyEntries: StateFlow<List<EntryWithMember>> =
        combine(_historyMonth, _historyMemberId) { m, id -> m to id }
            .flatMapLatest { (m, id) ->
                val (s, e) = DateUtils.monthRange(m)
                repo.entries(s.toEpochDay(), e.toEpochDay(), id)
            }.stateIn(viewModelScope, started(), emptyList())

    fun historyPrevMonth() { _historyMonth.value = _historyMonth.value.minusMonths(1) }
    fun historyNextMonth() { _historyMonth.value = _historyMonth.value.plusMonths(1) }
    fun historySetMember(id: Long?) { _historyMemberId.value = id }

    fun updateEntry(id: Long, points: Int, date: LocalDate, note: String?) = viewModelScope.launch {
        repo.updateEntryFields(id, points, date, note)
    }

    fun deleteEntry(id: Long) = viewModelScope.launch {
        repo.getEntry(id)?.let { repo.deleteEntry(it) }
    }

    // ---------------- Tổng hợp ----------------

    private val _reportMode = MutableStateFlow(ReportMode.WEEK)
    val reportMode: StateFlow<ReportMode> = _reportMode

    private val _reportWeek = MutableStateFlow(LocalDate.now())
    val reportWeek: StateFlow<LocalDate> = _reportWeek

    private val _reportMonth = MutableStateFlow(YearMonth.now())
    val reportMonth: StateFlow<YearMonth> = _reportMonth

    private val _reportSemesterId = MutableStateFlow<Long?>(null)
    val reportSemesterId: StateFlow<Long?> = _reportSemesterId

    fun setReportMode(m: ReportMode) { _reportMode.value = m }
    fun reportPrevWeek() { _reportWeek.value = _reportWeek.value.minusWeeks(1) }
    fun reportNextWeek() { _reportWeek.value = _reportWeek.value.plusWeeks(1) }
    fun reportPrevMonth() { _reportMonth.value = _reportMonth.value.minusMonths(1) }
    fun reportNextMonth() { _reportMonth.value = _reportMonth.value.plusMonths(1) }
    fun setReportSemester(id: Long?) { _reportSemesterId.value = id }

    val reportWeekScores: StateFlow<List<MemberScore>> =
        _reportWeek.flatMapLatest { anchor ->
            val (s, e) = DateUtils.weekRange(anchor)
            repo.scores(s.toEpochDay(), e.toEpochDay())
        }.stateIn(viewModelScope, started(), emptyList())

    /** Khoảng ngày đang áp dụng cho báo cáo tháng / kì (null nếu chưa chọn kì). */
    private val periodRange: StateFlow<Pair<Long, Long>?> =
        combine(_reportMode, _reportMonth, _reportSemesterId, semesters) { mode, month, semId, sems ->
            when (mode) {
                ReportMode.MONTH -> {
                    val (s, e) = DateUtils.monthRange(month)
                    s.toEpochDay() to e.toEpochDay()
                }
                ReportMode.SEMESTER -> {
                    val sem = sems.firstOrNull { it.id == semId } ?: sems.firstOrNull()
                    sem?.let { it.startEpochDay to it.endEpochDay }
                }
                ReportMode.WEEK -> null
            }
        }.stateIn(viewModelScope, started(), null)

    val reportPeriodRows: StateFlow<List<Reporting.PeriodRow>> =
        combine(periodRange, activeMembers, settings) { range, mems, setting ->
            Triple(range, mems, setting.weekBaseScore)
        }.flatMapLatest { (range, mems, base) ->
            if (range == null) {
                flowOf(emptyList())
            } else {
                repo.weekly(range.first, range.second).map { aggs ->
                    Reporting.periodRows(aggs, mems.map { it.id to it.name }, base)
                }
            }
        }.stateIn(viewModelScope, started(), emptyList())

    // ---------------- Thành viên ----------------

    fun addMember(name: String) = viewModelScope.launch { repo.addMember(name) }
    fun renameMember(m: Member, name: String) = viewModelScope.launch { repo.updateMember(m.copy(name = name.trim())) }
    fun setMemberActive(m: Member, active: Boolean) = viewModelScope.launch { repo.updateMember(m.copy(active = active)) }
    fun deleteMember(m: Member) = viewModelScope.launch { repo.deleteMember(m) }

    // ---------------- Loại điểm ----------------

    fun addCategory(name: String, points: Int) = viewModelScope.launch { repo.addCategory(name, points) }
    fun updateCategory(c: Category) = viewModelScope.launch { repo.updateCategory(c) }
    fun deleteCategory(c: Category) = viewModelScope.launch { repo.deleteCategory(c) }

    // ---------------- Học kì ----------------

    fun addSemester(name: String, start: LocalDate, end: LocalDate) =
        viewModelScope.launch { repo.addSemester(name, start.toEpochDay(), end.toEpochDay()) }

    fun updateSemester(s: Semester) = viewModelScope.launch { repo.updateSemester(s) }
    fun deleteSemester(s: Semester) = viewModelScope.launch { repo.deleteSemester(s) }

    // ---------------- Cài đặt ----------------

    fun setWeekBaseScore(value: Int) = viewModelScope.launch { repo.setWeekBaseScore(value) }

    suspend fun exportJson(): String = repo.exportJson()
    suspend fun importJson(text: String) = repo.importJson(text)
}
