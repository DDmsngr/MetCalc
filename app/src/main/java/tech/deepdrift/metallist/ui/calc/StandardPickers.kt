package tech.deepdrift.metallist.ui.calc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.deepdrift.metallist.domain.calc.GostPipes3262
import tech.deepdrift.metallist.domain.calc.GostProfiledSheets
import tech.deepdrift.metallist.domain.calc.Pipe3262Class
import tech.deepdrift.metallist.domain.calc.RiflePattern
import tech.deepdrift.metallist.domain.model.ProfileShape

/**
 * Каталог поддерживаемых стандартов по формам.
 * (Ключ стандарта, отображаемое имя)
 */
object StandardCatalog {

    fun forShape(shape: ProfileShape): List<Pair<String, String>> = when (shape) {
        ProfileShape.PipeRound -> listOf("GOST_3262" to "ГОСТ 3262-75 (ВГП)")
        ProfileShape.Plate -> listOf(
            "GOST_8568" to "ГОСТ 8568-77 (рифлёный)",
            "GOST_24045" to "ГОСТ 24045-94 (профнастил)",
        )
        else -> emptyList()
    }

    fun hasStandardsFor(shape: ProfileShape): Boolean = forShape(shape).isNotEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardPicker(
    shape: ProfileShape,
    selectedStandard: String?,
    onChange: (String?) -> Unit,
) {
    val items = listOf<Pair<String?, String>>(null to "Произвольный") +
        StandardCatalog.forShape(shape).map { it.first to it.second }
    val currentLabel = items.firstOrNull { it.first == selectedStandard }?.second ?: "Произвольный"
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Стандарт") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { (key, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onChange(key); expanded = false
                })
            }
        }
    }
}

@Composable
fun StandardOptionInputs(
    shape: ProfileShape,
    standard: String,
    option: String?,
    t: String,
    b: String,
    onOption: (String?) -> Unit,
    onT: (String) -> Unit,
    onB: (String) -> Unit,
) {
    when (standard) {
        "GOST_3262" -> Gost3262Inputs(option, onOption)
        "GOST_8568" -> Gost8568Inputs(option, t, b, onOption, onT, onB)
        "GOST_24045" -> Gost24045Inputs(option, onOption)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Gost3262Inputs(option: String?, onOption: (String?) -> Unit) {
    val parsed = option?.split("|", limit = 2)
    val currentDu = parsed?.getOrNull(0)?.toIntOrNull()
    val currentClass = runCatching { parsed?.getOrNull(1)?.let { Pipe3262Class.valueOf(it) } }.getOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SimpleDropdown(
            label = "Ду (условный проход)",
            value = GostPipes3262.byDu(currentDu ?: 0)?.label ?: "",
            items = GostPipes3262.all.map { it.du.toString() to it.label },
            onPick = { newDu ->
                val cls = currentClass ?: Pipe3262Class.Normal
                onOption("$newDu|${cls.name}")
            },
        )
        SimpleDropdown(
            label = "Класс",
            value = currentClass?.label ?: "",
            items = Pipe3262Class.entries.map { it.name to it.label },
            onPick = { newCls ->
                val du = currentDu ?: return@SimpleDropdown
                onOption("$du|$newCls")
            },
        )
        currentDu?.let { GostPipes3262.byDu(it) }?.let { pipe ->
            val cls = currentClass ?: Pipe3262Class.Normal
            Text(
                "⌀ ${pipe.dOuter} мм × стенка ${pipe.thickness(cls)} мм",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Gost8568Inputs(
    option: String?,
    t: String, b: String,
    onOption: (String?) -> Unit,
    onT: (String) -> Unit, onB: (String) -> Unit,
) {
    val current = runCatching { option?.let { RiflePattern.valueOf(it) } }.getOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SimpleDropdown(
            label = "Тип рифления",
            value = current?.label ?: "",
            items = RiflePattern.entries.map { it.name to it.label },
            onPick = { onOption(it) },
        )
        androidx.compose.material3.OutlinedTextField(
            value = t, onValueChange = onT,
            label = { Text("Толщина гладкой основы, мм") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        androidx.compose.material3.OutlinedTextField(
            value = b, onValueChange = onB,
            label = { Text("Ширина, мм") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Gost24045Inputs(option: String?, onOption: (String?) -> Unit) {
    val parsed = option?.split("|", limit = 2)
    val currentProfile = parsed?.getOrNull(0)?.let { GostProfiledSheets.byName(it) }
    val currentThick = parsed?.getOrNull(1)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SimpleDropdown(
            label = "Профиль",
            value = currentProfile?.name ?: "",
            items = GostProfiledSheets.profiles.map { it.name to "${it.name} (ширина ${it.widthMm} мм)" },
            onPick = { newProf ->
                val old = currentThick
                onOption(if (old != null) "$newProf|$old" else newProf)
            },
        )
        currentProfile?.let { profile ->
            SimpleDropdown(
                label = "Толщина стали, мм",
                value = currentThick ?: "",
                items = profile.thicknessOptions.map {
                    it.thicknessMm.toString() to "${it.thicknessMm} мм · ${it.massKgM2} кг/м²"
                },
                onPick = { newThick ->
                    onOption("${profile.name}|$newThick")
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    items: List<Pair<String, String>>, // (key, label)
    onPick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { (key, lab) ->
                DropdownMenuItem(text = { Text(lab) }, onClick = {
                    onPick(key); expanded = false
                })
            }
        }
    }
}
