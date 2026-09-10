package com.tota.chamdiem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tota.chamdiem.data.Category
import com.tota.chamdiem.ui.AppViewModel
import com.tota.chamdiem.ui.EmptyHint
import com.tota.chamdiem.ui.SectionLabel
import com.tota.chamdiem.ui.collectAsStateLifecycleSafe
import com.tota.chamdiem.ui.theme.ScoreColors
import com.tota.chamdiem.util.DateUtils
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(vm: AppViewModel) {
    val members by vm.activeMembers.collectAsStateLifecycleSafe()
    val categories by vm.activeCategories.collectAsStateLifecycleSafe()

    val selectedMembers = remember { mutableStateListOf<Long>() }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val canSave = selectedMembers.isNotEmpty() && selectedCategory != null

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nhập điểm") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SectionLabel("Ngày")
            OutlinedButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Text("  ${DateUtils.weekdayLabel(date)}, ${DateUtils.fmtDMY(date)}")
            }

            SectionLabel("Thành viên (chọn một hoặc nhiều)")
            if (members.isEmpty()) {
                EmptyHint("Chưa có thành viên. Thêm ở Cài đặt → Thành viên.")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = {
                        selectedMembers.clear()
                        selectedMembers.addAll(members.map { it.id })
                    }) { Text("Chọn tất cả") }
                    TextButton(onClick = { selectedMembers.clear() }) { Text("Bỏ chọn") }
                }
                members.forEach { m ->
                    val checked = m.id in selectedMembers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                onValueChange = {
                                    if (it) selectedMembers.add(m.id) else selectedMembers.remove(m.id)
                                },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Text(m.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            SectionLabel("Loại điểm")
            if (categories.isEmpty()) {
                EmptyHint("Chưa có loại điểm. Thêm ở tab Cài đặt.")
            } else {
                CategoryGroup("Cộng điểm", categories.filter { it.points >= 0 }, selectedCategoryId, ScoreColors.positive) { selectedCategoryId = it }
                CategoryGroup("Trừ điểm", categories.filter { it.points < 0 }, selectedCategoryId, ScoreColors.negative) { selectedCategoryId = it }
            }

            SectionLabel("Ghi chú (không bắt buộc)")
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("VD: môn Toán, tiết 2…") },
                minLines = 1,
            )

            Button(
                onClick = {
                    val cat = selectedCategory ?: return@Button
                    val ids = selectedMembers.toList()
                    vm.logEntry(ids, cat, date, note)
                    val n = ids.size
                    selectedMembers.clear()
                    selectedCategoryId = null
                    note = ""
                    scope.launch {
                        snackbar.showMessage("Đã ghi ${cat.name} cho $n bạn (${if (cat.points > 0) "+" else ""}${cat.points})")
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("Lưu điểm")
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") } },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun CategoryGroup(
    title: String,
    cats: List<Category>,
    selectedId: Long?,
    accent: androidx.compose.ui.graphics.Color,
    onPick: (Long) -> Unit,
) {
    if (cats.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
    cats.forEach { cat ->
        val selected = cat.id == selectedId
        Surface(
            onClick = { onPick(cat.id) },
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (cat.points > 0) "+${cat.points}" else "${cat.points}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }
        }
    }
}

private suspend fun SnackbarHostState.showMessage(msg: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(msg)
}
