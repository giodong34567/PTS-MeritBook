package com.tota.chamdiem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.tota.chamdiem.data.Member
import com.tota.chamdiem.ui.AppViewModel
import com.tota.chamdiem.ui.EmptyHint
import com.tota.chamdiem.ui.collectAsStateLifecycleSafe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(vm: AppViewModel, onBack: () -> Unit) {
    val members by vm.members.collectAsStateLifecycleSafe()

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Member?>(null) }
    var deleting by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thành viên trong tổ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm thành viên")
            }
        },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 16.dp)) {
            if (members.isEmpty()) {
                EmptyHint("Chưa có thành viên. Nhấn nút + để thêm các bạn trong tổ.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                ) {
                    items(members, key = { it.id }) { m ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        m.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        if (m.active) "Đang trong tổ" else "Đã rời tổ (ẩn khỏi bảng điểm)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(checked = m.active, onCheckedChange = { vm.setMemberActive(m, it) })
                                IconButton(onClick = { editing = m }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Đổi tên")
                                }
                                IconButton(onClick = { deleting = m }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Xoá")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        NameDialog(
            title = "Thêm thành viên",
            initial = "",
            onDismiss = { showAdd = false },
            onConfirm = { vm.addMember(it); showAdd = false },
        )
    }
    editing?.let { m ->
        NameDialog(
            title = "Đổi tên",
            initial = m.name,
            onDismiss = { editing = null },
            onConfirm = { vm.renameMember(m, it); editing = null },
        )
    }
    deleting?.let { m ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xoá ${m.name}?") },
            text = { Text("Toàn bộ lịch sử điểm của bạn này cũng sẽ bị xoá. Nếu chỉ muốn tạm ẩn, hãy tắt công tắc \"Đang trong tổ\" thay vì xoá.") },
            confirmButton = { TextButton(onClick = { vm.deleteMember(m); deleting = null }) { Text("Xoá hẳn") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Huỷ") } },
        )
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Họ và tên") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.trim()) }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )
}
