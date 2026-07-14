package tech.deepdrift.metallist.ui.application

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.deepdrift.metallist.data.repository.HistoryRepository
import tech.deepdrift.metallist.ui.calc.CalcSnapshot
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val historyRepo: HistoryRepository,
) : ViewModel() {

    private val idsArg: String = savedState.get<String>("ids") ?: ""
    private val ids: List<Long> = idsArg.split(",").mapNotNull { it.toLongOrNull() }

    private val _rows = MutableStateFlow<List<ApplicationRow>>(emptyList())
    val rows: StateFlow<List<ApplicationRow>> = _rows.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val snapshots = ids.mapNotNull { id ->
            val entity = historyRepo.getById(id) ?: return@mapNotNull null
            if (entity.kind != "metal") return@mapNotNull null
            runCatching {
                Json.decodeFromString(CalcSnapshot.serializer(), entity.payloadJson)
            }.getOrNull()
        }
        _rows.value = ApplicationFormat.buildRows(snapshots)
    }
}

@Composable
fun ApplicationScreen(
    onBack: () -> Unit,
    vm: ApplicationViewModel = hiltViewModel(),
) {
    val rows by vm.rows.collectAsState()
    val ctx = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Header(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Заявка на металл",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Позиций: ${rows.size}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )

            TablePreview(rows)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { copyToClipboard(ctx, rows) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Копировать")
                }
                OutlinedButton(
                    onClick = { sharePdf(ctx, rows) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("PDF")
                }
                OutlinedButton(
                    onClick = { shareCsv(ctx, rows) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("CSV")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    }
}

@Composable
private fun TablePreview(rows: List<ApplicationRow>) {
    if (rows.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Нет позиций", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                HeaderCell("№", 0.08f)
                HeaderCell("Материал", 0.30f)
                HeaderCell("Форма", 0.22f)
                HeaderCell("Размеры", 0.28f)
                HeaderCell("Кол-во", 0.12f)
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            rows.forEach { r ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Cell(r.no.toString(), 0.08f)
                    Cell(r.material, 0.30f)
                    Cell(r.shape, 0.22f)
                    Cell(r.dimensions, 0.28f)
                    Cell(r.quantity, 0.12f)
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(weight).padding(end = 4.dp),
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Cell(text: String, weight: Float) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(weight).padding(end = 4.dp),
    )
}

private fun copyToClipboard(ctx: Context, rows: List<ApplicationRow>) {
    if (rows.isEmpty()) return
    val text = ApplicationFormat.asPlainText(rows)
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Заявка", text))
    Toast.makeText(ctx, "Заявка скопирована", Toast.LENGTH_SHORT).show()
}

private fun sharePdf(ctx: Context, rows: List<ApplicationRow>) {
    if (rows.isEmpty()) return
    val file = ApplicationExport.writePdf(ctx, rows)
    ApplicationExport.shareFile(ctx, file, "application/pdf")
}

private fun shareCsv(ctx: Context, rows: List<ApplicationRow>) {
    if (rows.isEmpty()) return
    val file = ApplicationExport.writeCsv(ctx, rows)
    ApplicationExport.shareFile(ctx, file, "text/csv")
}
