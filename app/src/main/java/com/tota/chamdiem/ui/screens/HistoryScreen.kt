package com.tota.chamdiem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.tota.chamdiem.data.EntryWithMember
import com.tota.chamdiem.ui.AppViewModel
import com.tota.chamdiem.ui.EmptyHint
import com.tota.chamdiem.ui.PeriodNav
import com.tota.chamdiem.ui.collectAsStateLifecycleSafe
import com.tota.chamdiem.ui.theme.ScoreColors
import com.tota.chamdiem.util.DateUtils
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: AppViewModel) {
    val month by vm.historyMonth.collectAsStateLifecycleSafe()
    val memberId by vm.historyMemberId.collectAsStateLifecycleSafe()
    val entries by vm.historyEntries.collectAsStateLifecycleSafe()
    val members by vm.members.collectAsStateLifecycleSafe()

    var editing by remember { mutableStateOf<EntryWithMember?>(null) }
    var deleting by remember { mutableStateOf<EntryWithMember?>(null) }
    var filterMenu by remember { mutableStateOf(false) }

    val monthLabel = "Tháng ${month.monthValue}/${month.year}"
    val selectedMemberName = members.firstOrNull { it.id == memberId }?.name

    Scaffold(topBar = { TopAppBar(title = { Text("Lịch sử") }) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PeriodNav(
                label = monthLabel,
                onPrev = vm::historyPrevMonth,
                onNext = vm::historyNextMonth,
                modifier = Modifier.padding(top = 8.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    FilterChip(
                        selected = memberId != null,
                        onClick = { filterMenu = true },
                        label = { Text(selectedMemberName ?: "Tất cả thành viên") },
                    )
                    DropdownMenu(expanded = filterMenu, onDismissRequest = { filterMenu = false }) {
                        DropdownMenuItem(text = { Text("Tất cả thành viên") }, onClick = {
                            vm.historySetMember(null); filterMenu = false
                        })
                        members.forEach { m ->
                            DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                vm.historySetMember(m.id); filterMenu = false
                            })
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                Text("${entries.size} lượt", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (entries.isEmpty()) {
                EmptyHint("Chưa có lượt điểm nào trong $monthLabel.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                    items(entries, key = { it.id }) { e ->
                        EntryCard(e, onClick = { editing = e }, onLongDelete = { deleting = e })
                    }
                }
            }
        }
    }

    editing?.let { e ->
        EditEntryDialog(
            entry = e,
            onDismiss = { editing = null },
            onSave = { points, date, note ->
                vm.updateEntry(e.id, points, date, note)
                editing = null
            },
            onDelete = { editing = null; deleting = e },
        )
    }

    deleting?.let { e ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xoá lượt điểm?") },
            text = { Text("${e.memberName} — ${e.categoryName} (${if (e.points > 0) "+" else ""}${e.points}), ${DateUtils.fmtDMY(DateUtils.fromEpochDay(e.dateEpochDay))}") },
            confirmButton = { TextButton(onClick = { vm.deleteEntry(e.id); deleting = null }) { Text("Xoá") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Huỷ") } },
        )
    }
}

@Composable
private fun EntryCard(e: EntryWithMember, onClick: () -> Unit, onLongDelete: () -> Unit) {
    val date = DateUtils.fromEpochDay(e.dateEpochDay)
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(e.memberName, style = MaterialTheme.typography.titleMedium)
                Text(e.categoryName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${DateUtils.weekdayLabel(date)}, ${DateUtils.fmtDMY(date)}" + (e.note?.let { "  •  $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (e.points > 0) "+${e.points}" else "${e.points}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (e.points < 0) ScoreColors.negative else ScoreColors.positive,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryDialog(
    entry: EntryWithMember,
    onDismiss: () -> Unit,
    onSave: (points: Int, date: java.time.LocalDate, note: String?) -> Unit,
    onDelete: () -> Unit,
) {
    var pointsText by remember { mutableStateOf(entry.points.toString()) }
    var note by remember { mutableStateOf(entry.note ?: "") }
    val date = remember { DateUtils.fromEpochDay(entry.dateEpochDay) }
    val points = pointsText.trim().toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa lượt điểm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${entry.memberName} — ${entry.categoryName}", style = MaterialTheme.typography.bodyMedium)
                Text("Ngày: ${DateUtils.fmtDMY(date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = { pointsText = it },
                    label = { Text("Số điểm (âm = trừ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                )
                TextButton(onClick = onDelete) { Text("Xoá lượt này", color = ScoreColors.negative) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = points != null && points != 0,
                onClick = { onSave(points!!, date, note) },
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )
}
