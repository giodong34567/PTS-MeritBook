package com.tota.chamdiem.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tota.chamdiem.data.Category
import com.tota.chamdiem.data.Semester
import com.tota.chamdiem.ui.AppViewModel
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
fun SettingsScreen(vm: AppViewModel, onOpenMembers: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by vm.settings.collectAsStateLifecycleSafe()
    val categories by vm.categories.collectAsStateLifecycleSafe()
    val semesters by vm.semesters.collectAsStateLifecycleSafe()

    var baseText by remember(settings.weekBaseScore) { mutableStateOf(settings.weekBaseScore.toString()) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var addingCategory by remember { mutableStateOf(false) }
    var deletingCategory by remember { mutableStateOf<Category?>(null) }
    var editingSemester by remember { mutableStateOf<Semester?>(null) }
    var addingSemester by remember { mutableStateOf(false) }
    var deletingSemester by remember { mutableStateOf<Semester?>(null) }
    var importConfirm by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) scope.launch {
            val json = vm.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            message = "Đã lưu bản sao lưu."
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) importConfirm = text
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Cài đặt") }) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // ---- Thành viên ----
            ElevatedCard(onClick = onOpenMembers, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thành viên trong tổ", style = MaterialTheme.typography.titleMedium)
                        Text("Thêm, đổi tên, ẩn/hiện thành viên", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }

            // ---- Điểm gốc ----
            SectionLabel("Điểm gốc mỗi tuần")
            Text(
                "Mỗi thành viên bắt đầu tuần mới với số điểm này; sau đó cộng thành tích và trừ vi phạm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Điểm gốc") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(140.dp),
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    enabled = baseText.toIntOrNull()?.let { it in 1..9999 } == true &&
                        baseText.toIntOrNull() != settings.weekBaseScore,
                    onClick = { baseText.toIntOrNull()?.let { vm.setWeekBaseScore(it) } },
                ) { Text("Lưu") }
            }

            // ---- Loại điểm ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                SectionLabel("Loại điểm")
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { addingCategory = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null); Text(" Thêm")
                }
            }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    categories.forEachIndexed { i, c ->
                        if (i > 0) HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (c.points > 0) "+${c.points}" else "${c.points}",
                                modifier = Modifier.width(44.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (c.points < 0) ScoreColors.negative else ScoreColors.positive,
                            )
                            Text(
                                c.name,
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (c.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Switch(
                                checked = c.active,
                                onCheckedChange = { vm.updateCategory(c.copy(active = it)) },
                            )
                            IconButton(onClick = { editingCategory = c }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Sửa")
                            }
                            IconButton(onClick = { deletingCategory = c }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Xoá")
                            }
                        }
                    }
                }
            }

            // ---- Học kì ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                SectionLabel("Học kì")
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { addingSemester = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null); Text(" Thêm")
                }
            }
            if (semesters.isEmpty()) {
                Text(
                    "Chưa có học kì nào. Thêm mốc ngày để tổng hợp điểm theo kì (VD: HK1 05/09/2025 – 15/01/2026).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        semesters.forEachIndexed { i, s ->
                            if (i > 0) HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                                    Text(s.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${DateUtils.fmtDMY(DateUtils.fromEpochDay(s.startEpochDay))} – ${DateUtils.fmtDMY(DateUtils.fromEpochDay(s.endEpochDay))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { editingSemester = s }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Sửa")
                                }
                                IconButton(onClick = { deletingSemester = s }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Xoá")
                                }
                            }
                        }
                    }
                }
            }

            // ---- Sao lưu ----
            SectionLabel("Sao lưu & phục hồi")
            Text(
                "Toàn bộ dữ liệu nằm trong máy. Nên xuất file .json định kỳ để phòng mất máy hoặc khi đổi điện thoại.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val stamp = LocalDate.now().toString()
                        exportLauncher.launch("ChamDiemTo_backup_$stamp.json")
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Xuất dữ liệu") }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier.weight(1f),
                ) { Text("Nhập dữ liệu") }
            }

            SectionLabel("Giới thiệu")
            Text("Chấm Điểm Tổ • phiên bản 1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    // ----- Dialogs -----

    if (addingCategory) {
        CategoryDialog(
            title = "Thêm loại điểm",
            initialName = "",
            initialPoints = 1,
            onDismiss = { addingCategory = false },
            onConfirm = { name, pts -> vm.addCategory(name, pts); addingCategory = false },
        )
    }
    editingCategory?.let { c ->
        CategoryDialog(
            title = "Sửa loại điểm",
            initialName = c.name,
            initialPoints = c.points,
            onDismiss = { editingCategory = null },
            onConfirm = { name, pts -> vm.updateCategory(c.copy(name = name, points = pts)); editingCategory = null },
        )
    }
    deletingCategory?.let { c ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Xoá loại \"${c.name}\"?") },
            text = { Text("Các lượt điểm đã chấm theo loại này vẫn được giữ nguyên (đã lưu tên và số điểm lúc chấm).") },
            confirmButton = { TextButton(onClick = { vm.deleteCategory(c); deletingCategory = null }) { Text("Xoá") } },
            dismissButton = { TextButton(onClick = { deletingCategory = null }) { Text("Huỷ") } },
        )
    }

    if (addingSemester) {
        SemesterDialog(
            title = "Thêm học kì",
            initialName = "",
            initialStart = LocalDate.now(),
            initialEnd = LocalDate.now().plusMonths(4),
            onDismiss = { addingSemester = false },
            onConfirm = { n, s, e -> vm.addSemester(n, s, e); addingSemester = false },
        )
    }
    editingSemester?.let { sem ->
        SemesterDialog(
            title = "Sửa học kì",
            initialName = sem.name,
            initialStart = DateUtils.fromEpochDay(sem.startEpochDay),
            initialEnd = DateUtils.fromEpochDay(sem.endEpochDay),
            onDismiss = { editingSemester = null },
            onConfirm = { n, s, e ->
                vm.updateSemester(sem.copy(name = n, startEpochDay = s.toEpochDay(), endEpochDay = e.toEpochDay()))
                editingSemester = null
            },
        )
    }
    deletingSemester?.let { sem ->
        AlertDialog(
            onDismissRequest = { deletingSemester = null },
            title = { Text("Xoá học kì \"${sem.name}\"?") },
            text = { Text("Chỉ xoá mốc thời gian này, không xoá lượt điểm.") },
            confirmButton = { TextButton(onClick = { vm.deleteSemester(sem); deletingSemester = null }) { Text("Xoá") } },
            dismissButton = { TextButton(onClick = { deletingSemester = null }) { Text("Huỷ") } },
        )
    }

    importConfirm?.let { text ->
        AlertDialog(
            onDismissRequest = { importConfirm = null },
            title = { Text("Nhập dữ liệu?") },
            text = { Text("Toàn bộ dữ liệu hiện tại sẽ bị thay thế bằng nội dung trong file sao lưu. Không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            vm.importJson(text)
                            message = "Đã phục hồi dữ liệu từ file sao lưu."
                        } catch (e: Exception) {
                            message = "File không hợp lệ: ${e.message}"
                        }
                    }
                    importConfirm = null
                }) { Text("Nhập & thay thế") }
            },
            dismissButton = { TextButton(onClick = { importConfirm = null }) { Text("Huỷ") } },
        )
    }

    message?.let { m ->
        AlertDialog(
            onDismissRequest = { message = null },
            confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } },
            text = { Text(m) },
        )
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialName: String,
    initialPoints: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var isMinus by remember { mutableStateOf(initialPoints < 0) }
    var magnitude by remember { mutableStateOf(kotlin.math.abs(initialPoints).toString()) }
    val mag = magnitude.toIntOrNull()
    val valid = name.isNotBlank() && mag != null && mag > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên loại điểm") },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isMinus) "Trừ điểm" else "Cộng điểm", modifier = Modifier.weight(1f))
                    Switch(checked = isMinus, onCheckedChange = { isMinus = it })
                }
                OutlinedTextField(
                    value = magnitude,
                    onValueChange = { magnitude = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Số điểm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val signed = if (isMinus) -mag!! else mag!!
                onConfirm(name.trim(), signed)
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SemesterDialog(
    title: String,
    initialName: String,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, LocalDate) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var start by remember { mutableStateOf(initialStart) }
    var end by remember { mutableStateOf(initialEnd) }
    var picking by remember { mutableStateOf(0) } // 0 none, 1 start, 2 end
    val valid = name.isNotBlank() && !end.isBefore(start)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên học kì") },
                    placeholder = { Text("VD: HK1 (2025–2026)") },
                    singleLine = true,
                )
                OutlinedButton(onClick = { picking = 1 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Bắt đầu: ${DateUtils.fmtDMY(start)}")
                }
                OutlinedButton(onClick = { picking = 2 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Kết thúc: ${DateUtils.fmtDMY(end)}")
                }
                if (!valid && name.isNotBlank()) {
                    Text("Ngày kết thúc phải sau ngày bắt đầu.", style = MaterialTheme.typography.bodySmall, color = ScoreColors.negative)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(name.trim(), start, end) }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )

    if (picking != 0) {
        val current = if (picking == 1) start else end
        val state = rememberDatePickerState(
            initialSelectedDateMillis = current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { picking = 0 },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val d = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        if (picking == 1) start = d else end = d
                    }
                    picking = 0
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { picking = 0 }) { Text("Huỷ") } },
        ) { DatePicker(state = state) }
    }
}
