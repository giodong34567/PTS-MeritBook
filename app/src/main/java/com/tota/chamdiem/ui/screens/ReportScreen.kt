package com.tota.chamdiem.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tota.chamdiem.data.MemberScore
import com.tota.chamdiem.ui.AppViewModel
import com.tota.chamdiem.ui.EmptyHint
import com.tota.chamdiem.ui.PeriodNav
import com.tota.chamdiem.ui.ReportMode
import com.tota.chamdiem.ui.collectAsStateLifecycleSafe
import com.tota.chamdiem.ui.theme.ScoreColors
import com.tota.chamdiem.util.DateUtils
import com.tota.chamdiem.util.Reporting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(vm: AppViewModel) {
    val context = LocalContext.current

    val mode by vm.reportMode.collectAsStateLifecycleSafe()
    val settings by vm.settings.collectAsStateLifecycleSafe()
    val week by vm.reportWeek.collectAsStateLifecycleSafe()
    val month by vm.reportMonth.collectAsStateLifecycleSafe()
    val semesters by vm.semesters.collectAsStateLifecycleSafe()
    val reportSemId by vm.reportSemesterId.collectAsStateLifecycleSafe()
    val weekScores by vm.reportWeekScores.collectAsStateLifecycleSafe()
    val periodRows by vm.reportPeriodRows.collectAsStateLifecycleSafe()

    val base = settings.weekBaseScore
    val activeSemester = semesters.firstOrNull { it.id == reportSemId } ?: semesters.firstOrNull()

    val title = when (mode) {
        ReportMode.WEEK -> "Tuần ${DateUtils.weekLabel(week)}"
        ReportMode.MONTH -> DateUtils.monthLabel(month)
        ReportMode.SEMESTER -> activeSemester?.name ?: "Học kì"
    }

    fun buildText(): String = when (mode) {
        ReportMode.WEEK -> Reporting.weekText(title, base, weekScores)
        else -> Reporting.periodText(title, base, periodRows)
    }

    fun buildCsv(): String = when (mode) {
        ReportMode.WEEK -> Reporting.weekCsv(title, base, weekScores)
        else -> Reporting.periodCsv(title, base, periodRows)
    }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(buildCsv().toByteArray()) }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Tổng hợp") }) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                val entries = listOf(ReportMode.WEEK to "Tuần", ReportMode.MONTH to "Tháng", ReportMode.SEMESTER to "Kì")
                entries.forEachIndexed { i, (m, label) ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { vm.setReportMode(m) },
                        shape = SegmentedButtonDefaults.itemShape(i, entries.size),
                    ) { Text(label) }
                }
            }

            when (mode) {
                ReportMode.WEEK -> PeriodNav(
                    label = DateUtils.weekLabel(week),
                    onPrev = vm::reportPrevWeek,
                    onNext = vm::reportNextWeek,
                )
                ReportMode.MONTH -> PeriodNav(
                    label = DateUtils.monthLabel(month),
                    onPrev = vm::reportPrevMonth,
                    onNext = vm::reportNextMonth,
                )
                ReportMode.SEMESTER -> SemesterPicker(
                    semesters = semesters.map { it.id to it.name },
                    selectedId = activeSemester?.id,
                    onSelect = { vm.setReportSemester(it) },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Báo cáo thi đua – $title")
                            putExtra(Intent.EXTRA_TEXT, buildText())
                        }
                        context.startActivity(Intent.createChooser(send, "Chia sẻ báo cáo"))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  Chia sẻ")
                }
                OutlinedButton(
                    onClick = {
                        val safe = title.replace(Regex("[^A-Za-z0-9]+"), "_")
                        saveCsvLauncher.launch("ChamDiem_$safe.csv")
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Text("  Lưu CSV")
                }
            }

            if (mode == ReportMode.SEMESTER && semesters.isEmpty()) {
                EmptyHint("Chưa khai báo học kì.\nVào Cài đặt → Học kì để thêm mốc ngày bắt đầu / kết thúc.")
            } else if (mode == ReportMode.WEEK) {
                WeekTable(weekScores, base)
            } else {
                PeriodTable(periodRows)
            }
        }
    }
}

@Composable
private fun WeekTable(scores: List<MemberScore>, base: Int) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            HeaderRow(listOf("#" to 0.12f, "Thành viên" to 0.44f, "▲" to 0.13f, "▼" to 0.13f, "Điểm" to 0.18f))
            HorizontalDivider()
            if (scores.isEmpty()) {
                EmptyHint("Chưa có dữ liệu.")
            } else {
                LazyColumn {
                    itemsIndexed(scores) { i, s ->
                        DataRow(
                            cells = listOf(
                                "${i + 1}" to 0.12f,
                                s.memberName to 0.44f,
                                "${s.plusCount}" to 0.13f,
                                "${s.minusCount}" to 0.13f,
                                "${base + s.net}" to 0.18f,
                            ),
                            emphasizeLastColor = if (s.net < 0) ScoreColors.negative else ScoreColors.positive,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodTable(rows: List<Reporting.PeriodRow>) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            val weeks = rows.firstOrNull()?.weeksScored ?: 0
            Text(
                "Số tuần đã chấm: $weeks",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
            HeaderRow(listOf("#" to 0.1f, "Thành viên" to 0.4f, "▲" to 0.12f, "▼" to 0.12f, "TB tuần" to 0.26f))
            HorizontalDivider()
            if (rows.isEmpty()) {
                EmptyHint("Chưa có dữ liệu trong kỳ này.")
            } else {
                LazyColumn {
                    itemsIndexed(rows) { i, r ->
                        DataRow(
                            cells = listOf(
                                "${i + 1}" to 0.1f,
                                r.memberName to 0.4f,
                                "${r.plusCount}" to 0.12f,
                                "${r.minusCount}" to 0.12f,
                                Reporting.fmt1(r.avgWeekScore) to 0.26f,
                            ),
                            emphasizeLastColor = if (r.totalDelta < 0) ScoreColors.negative else ScoreColors.positive,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(cells: List<Pair<String, Float>>) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        cells.forEach { (t, w) ->
            Text(
                t,
                modifier = Modifier.weight(w),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DataRow(cells: List<Pair<String, Float>>, emphasizeLastColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEachIndexed { idx, (t, w) ->
            val last = idx == cells.lastIndex
            Text(
                t,
                modifier = Modifier.weight(w),
                style = if (last) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (last) FontWeight.Bold else FontWeight.Normal,
                color = if (last) emphasizeLastColor else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SemesterPicker(semesters: List<Pair<Long, String>>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = semesters.firstOrNull { it.first == selectedId }?.second ?: "— Chọn học kì —"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Học kì") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            semesters.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}
