package tech.deepdrift.metallist.ui.iso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.domain.iso.IsoCalculator
import tech.deepdrift.metallist.domain.iso.IsoDeviations
import tech.deepdrift.metallist.domain.iso.IsoResult
import tech.deepdrift.metallist.domain.iso.IsoTolerances
import javax.inject.Inject
import kotlin.math.abs

data class IsoUi(
    val size: String = "",
    val letter: String = "H",
    val grade: String = "IT7",
    val isHole: Boolean = true,
    val result: IsoResult? = null,
)

@HiltViewModel
class IsoViewModel @Inject constructor() : ViewModel() {
    private val _ui = MutableStateFlow(IsoUi())
    val ui: StateFlow<IsoUi> = _ui.asStateFlow()

    fun setSize(v: String) = _ui.update { it.copy(size = v) }
    fun setLetter(v: String) = _ui.update { it.copy(letter = v) }
    fun setGrade(v: String) = _ui.update { it.copy(grade = v) }
    fun setIsHole(v: Boolean) = _ui.update {
        val newLetter = if (v) it.letter.uppercase() else it.letter.lowercase()
        it.copy(isHole = v, letter = newLetter)
    }

    fun calculate() {
        val u = _ui.value
        val size = u.size.replace(',', '.').toDoubleOrNull() ?: return
        val res = IsoCalculator.compute(size, u.letter, u.grade)
        _ui.update { it.copy(result = res) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsoScreen(vm: IsoViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    val gradeList = IsoTolerances.grades.map { it.code }
    val letters = if (ui.isHole) IsoDeviations.holeLetters else IsoDeviations.shaftLetters

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(stringResource(R.string.iso_title))
                    Text(
                        stringResource(R.string.iso_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            })
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = ui.isHole,
                    onClick = { vm.setIsHole(true) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(stringResource(R.string.iso_hole)) }
                SegmentedButton(
                    selected = !ui.isHole,
                    onClick = { vm.setIsHole(false) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(stringResource(R.string.iso_shaft)) }
            }

            OutlinedTextField(
                value = ui.size,
                onValueChange = vm::setSize,
                label = { Text(stringResource(R.string.iso_nominal) + ", " + stringResource(R.string.unit_mm)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    LetterDropdown(letters = letters, selected = ui.letter, onPick = vm::setLetter)
                }
                Box(Modifier.weight(1f)) {
                    GradeDropdown(grades = gradeList, selected = ui.grade, onPick = vm::setGrade)
                }
            }

            Button(
                onClick = vm::calculate,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.calculate)) }

            ui.result?.let { r ->
                IsoResultCard(r)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LetterDropdown(letters: List<String>, selected: String, onPick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected, onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.iso_letter)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            letters.forEach { l ->
                DropdownMenuItem(text = { Text(l) }, onClick = { onPick(l); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeDropdown(grades: List<String>, selected: String, onPick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected, onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.iso_grade)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            grades.forEach { g ->
                DropdownMenuItem(text = { Text(g) }, onClick = { onPick(g); expanded = false })
            }
        }
    }
}

@Composable
private fun IsoResultCard(r: IsoResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Результат", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
            HorizontalDivider()
            ResultRow(stringResource(R.string.iso_tolerance), "%.3f мм".format(r.toleranceMicrons / 1000.0))
            ResultRow(stringResource(R.string.iso_size_avg), "%.4f мм".format(r.avgMm))
            ResultRow(stringResource(R.string.iso_upper), formatDev(r.esMicrons))
            ResultRow(stringResource(R.string.iso_lower), formatDev(r.eiMicrons))
            HorizontalDivider()
            ResultRow(stringResource(R.string.iso_max), "%.4f мм".format(r.upperMm))
            ResultRow(stringResource(R.string.iso_min), "%.4f мм".format(r.lowerMm))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun formatDev(microns: Double): String {
    val sign = if (microns >= 0) "+" else "−"
    return "$sign%.3f мм".format(abs(microns) / 1000.0)
}
