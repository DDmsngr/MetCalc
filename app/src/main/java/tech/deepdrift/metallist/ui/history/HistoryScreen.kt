package tech.deepdrift.metallist.ui.history

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.data.db.HistoryEntity
import tech.deepdrift.metallist.data.repository.HistoryRepository
import tech.deepdrift.metallist.ui.calc.CalcSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: HistoryRepository,
) : ViewModel() {

    val items: StateFlow<List<HistoryEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selection = MutableStateFlow(SelectionState())
    val selection: StateFlow<SelectionState> = _selection.asStateFlow()

    fun startSelection() = _selection.update { it.copy(active = true) }
    fun stopSelection() = _selection.update { SelectionState() }
    fun toggle(id: Long) = _selection.update {
        val next = it.selectedIds.toMutableSet()
        if (!next.add(id)) next.remove(id)
        it.copy(active = true, selectedIds = next)
    }

    fun remove(item: HistoryEntity) = viewModelScope.launch { repo.remove(item) }
    fun clearAll() = viewModelScope.launch {
        repo.clearAll(); stopSelection()
    }
}

data class SelectionState(
    val active: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HistoryScreen(
    vm: HistoryViewModel = hiltViewModel(),
    onOpen: (historyId: Long, shapeName: String, kind: String) -> Unit,
    onCreateApplication: (Set<Long>) -> Unit,
) {
    val items by vm.items.collectAsState()
    val selection by vm.selection.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selection.active) {
                        Text("Выбрано: ${selection.selectedIds.size}")
                    } else {
                        Text(stringResource(R.string.section_history))
                    }
                },
                navigationIcon = {
                    if (selection.active) {
                        IconButton(onClick = vm::stopSelection) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (!selection.active && items.isNotEmpty()) {
                        TextButton(onClick = vm::startSelection) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Text("  Заявка")
                        }
                        IconButton(onClick = vm::clearAll) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Очистить историю")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (selection.active && selection.selectedIds.isNotEmpty()) {
                Surface(tonalElevation = 6.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = { onCreateApplication(selection.selectedIds) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Text(
                                "  Составить заявку (${selection.selectedIds.size})",
                            )
                        }
                    }
                }
            }
        },
    ) { pad ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it.id }) { entry ->
                    HistoryRow(
                        entry = entry,
                        selectionActive = selection.active,
                        selected = entry.id in selection.selectedIds,
                        onOpen = {
                            if (selection.active) {
                                vm.toggle(entry.id)
                            } else {
                                val shapeName = if (entry.kind == "metal") {
                                    runCatching {
                                        Json.decodeFromString(CalcSnapshot.serializer(), entry.payloadJson).shape
                                    }.getOrNull() ?: "Round"
                                } else "Round"
                                onOpen(entry.id, shapeName, entry.kind)
                            }
                        },
                        onLongPress = { vm.toggle(entry.id) },
                        onRemove = { vm.remove(entry) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    entry: HistoryEntity,
    selectionActive: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpen, onLongClick = onLongPress),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionActive) {
                Checkbox(checked = selected, onCheckedChange = { onLongPress() })
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Medium)
                Text(entry.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text(formatTs(entry.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!selectionActive) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                }
            }
        }
    }
}

private fun formatTs(ts: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(ts))
