package com.tota.chamdiem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.tota.chamdiem.data.Category
import com.tota.chamdiem.data.Member
import com.tota.chamdiem.data.MemberScore
import com.tota.chamdiem.ui.AppViewModel
import com.tota.chamdiem.ui.EmptyHint
import com.tota.chamdiem.ui.PeriodNav
import com.tota.chamdiem.ui.collectAsStateLifecycleSafe
import com.tota.chamdiem.ui.theme.ScoreColors
import com.tota.chamdiem.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, onOpenAdd: () -> Unit) {
    val week by vm.homeWeek.collectAsStateLifecycleSafe()
    val scores by vm.homeScores.collectAsStateLifecycleSafe()
    val categories by vm.activeCategories.collectAsStateLifecycleSafe()
    val settings by vm.settings.collectAsStateLifecycleSafe()
    val members by vm.activeMembers.collectAsStateLifecycleSafe()

    var sheetFor by remember { mutableStateOf<MemberScore?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalPlus = scores.sumOf { it.plusCount }
    val totalMinus = scores.sumOf { it.minusCount }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chấm Điểm Tổ") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Nhập điểm")
            }
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PeriodNav(
                label = "Tuần ${DateUtils.weekLabel(week)}",
                onPrev = vm::homePrevWeek,
                onNext = vm::homeNextWeek,
                todayLabel = "Về tuần này",
                onToday = vm::homeThisWeek,
                modifier = Modifier.padding(top = 8.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Lượt cộng", "▲ $totalPlus", ScoreColors.positive, Modifier.weight(1f))
                StatBox("Lượt trừ", "▼ $totalMinus", ScoreColors.negative, Modifier.weight(1f))
                StatBox("Điểm gốc", "${settings.weekBaseScore}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }

            if (members.isEmpty()) {
                EmptyHint("Chưa có thành viên nào.\nVào tab Cài đặt → Thành viên để thêm các bạn trong tổ.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp),
                ) {
                    items(scores, key = { it.memberId }) { s ->
                        MemberRow(
                            rank = scores.indexOf(s) + 1,
                            score = s,
                            base = settings.weekBaseScore,
                            onClick = { sheetFor = s },
                        )
                    }
                }
            }
        }
    }

    val target = sheetFor
    if (target != null) {
        ModalBottomSheet(onDismissRequest = { sheetFor = null }, sheetState = sheetState) {
            QuickLogSheet(
                memberName = target.memberName,
                categories = categories,
                onPick = { cat ->
                    vm.quickLog(target.memberId, cat)
                    sheetFor = null
                },
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MemberRow(rank: Int, score: MemberScore, base: Int, onClick: () -> Unit) {
    val weekScore = base + score.net
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$rank", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(score.memberName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "▲ ${score.plusCount} (+${score.plusPoints})   ▼ ${score.minusCount} (${score.minusPoints})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "$weekScore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (score.net < 0) ScoreColors.negative else ScoreColors.positive,
            )
        }
    }
}

@Composable
private fun QuickLogSheet(memberName: String, categories: List<Category>, onPick: (Category) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(memberName, style = MaterialTheme.typography.titleLarge)
        Text("Chọn loại điểm để ghi cho hôm nay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        val plus = categories.filter { it.points >= 0 }
        val minus = categories.filter { it.points < 0 }

        if (plus.isNotEmpty()) {
            Text("Cộng điểm", style = MaterialTheme.typography.labelLarge, color = ScoreColors.positive, modifier = Modifier.padding(top = 8.dp))
            plus.forEach { CategoryPickRow(it, onPick) }
        }
        if (minus.isNotEmpty()) {
            Text("Trừ điểm", style = MaterialTheme.typography.labelLarge, color = ScoreColors.negative, modifier = Modifier.padding(top = 8.dp))
            minus.forEach { CategoryPickRow(it, onPick) }
        }
        if (categories.isEmpty()) {
            EmptyHint("Chưa có loại điểm. Thêm ở tab Cài đặt.")
        }
    }
}

@Composable
private fun CategoryPickRow(cat: Category, onPick: (Category) -> Unit) {
    Surface(
        onClick = { onPick(cat) },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
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
                color = if (cat.points < 0) ScoreColors.negative else ScoreColors.positive,
            )
        }
    }
}
