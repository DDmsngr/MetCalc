package tech.deepdrift.metallist.ui.calc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.domain.calc.GostProfiles
import tech.deepdrift.metallist.domain.model.CalcDirection
import tech.deepdrift.metallist.domain.model.Material
import tech.deepdrift.metallist.domain.model.MaterialGrades
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeMode

@Composable
fun CalcScreen(
    shape: ProfileShape,
    historyId: Long,
    onBack: () -> Unit,
    vm: CalcViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()
    val materials by vm.materials.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
    ) {
        CompactHeader(title = stringResource(shape.readableRes()), onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MaterialSearchPicker(
                materials = materials,
                selected = ui.selectedMaterial,
                onPick = vm::onMaterialSelected,
            )
            GradePicker(
                material = ui.selectedMaterial,
                grade = ui.grade,
                onChange = vm::onGradeChange,
            )
            NumberField(
                label = stringResource(R.string.density) + ", " + stringResource(R.string.density_unit),
                value = ui.density, onChange = vm::onDensityChange,
            )

            DirectionRow(ui.direction, vm::onDirectionChange)

            if (shape.supportsGost) {
                ModeSwitcher(ui.mode, vm::onModeChange)
            }

            // Выпадашка «Стандарт» — только для форм, у которых есть подформаты
            if (StandardCatalog.hasStandardsFor(shape)) {
                StandardPicker(
                    shape = shape,
                    selectedStandard = ui.standard,
                    onChange = vm::onStandardChange,
                )
            }

            when {
                ui.mode == ShapeMode.Gost && shape.supportsGost ->
                    GostPicker(shape, ui.gostNumber, vm::onGostChange)
                ui.standard != null ->
                    StandardOptionInputs(
                        shape = shape,
                        standard = ui.standard!!,
                        option = ui.standardOption,
                        t = ui.t, b = ui.b,
                        onOption = vm::onStandardOptionChange,
                        onT = vm::onTChange,
                        onB = vm::onBChange,
                    )
                else -> ShapeInputs(shape, ui, vm)
            }

            if (ui.direction == CalcDirection.ByLength) {
                NumberField(
                    label = stringResource(R.string.length) + ", " + stringResource(R.string.unit_mm),
                    value = ui.length, onChange = vm::onLengthChange,
                )
            } else {
                NumberField(
                    label = stringResource(R.string.result_mass) + ", " + stringResource(R.string.unit_kg),
                    value = ui.mass, onChange = vm::onMassChange,
                )
            }
            NumberField(
                label = stringResource(R.string.quantity) + ", " + stringResource(R.string.quantity_unit),
                value = ui.quantity, onChange = vm::onQuantityChange, integer = true,
            )
            NumberField(
                label = stringResource(R.string.price_per_kg),
                value = ui.price, onChange = vm::onPriceChange,
            )

            Button(
                onClick = vm::calculate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.calculate))
            }

            ui.result?.let { r -> ResultCard(r, ui.direction) }
        }
    }
}

@Composable
private fun CompactHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSearchPicker(
    materials: List<Material>,
    selected: Material?,
    onPick: (Material) -> Unit,
) {
    var query by remember { mutableStateOf(selected?.name ?: "") }
    var expanded by remember { mutableStateOf(false) }

    // При смене выбранного материала снаружи (например, восстановление из истории) — синхронизируем.
    if (selected != null && !expanded && query != selected.name) {
        query = selected.name
    }

    val filtered = remember(materials, query) {
        if (query.isBlank()) materials
        else materials.filter { it.name.startsWith(query, ignoreCase = true) }
            .ifEmpty { materials.filter { it.name.contains(query, ignoreCase = true) } }
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(stringResource(R.string.material)) },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""; expanded = true
                    }) { Icon(Icons.Default.Clear, contentDescription = null) }
                } else {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (expanded && filtered.isNotEmpty()) {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .padding(top = 4.dp),
            ) {
                LazyColumn {
                    items(filtered, key = { it.id }) { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(m)
                                    query = m.name
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(m.name)
                            Text(
                                "%.2f".format(m.densityGCm3),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradePicker(
    material: Material?,
    grade: String,
    onChange: (String) -> Unit,
) {
    val allSuggestions = remember(material?.name) {
        MaterialGrades.suggestionsFor(material?.name)
    }
    var expanded by remember { mutableStateOf(false) }

    val filtered = remember(allSuggestions, grade) {
        if (grade.isBlank()) allSuggestions
        else allSuggestions.filter { it.contains(grade, ignoreCase = true) }
    }

    Column {
        OutlinedTextField(
            value = grade,
            onValueChange = {
                onChange(it)
                expanded = true
            },
            label = { Text(stringResource(R.string.grade)) },
            placeholder = { Text(stringResource(R.string.grade_hint)) },
            singleLine = true,
            trailingIcon = {
                if (grade.isNotEmpty()) {
                    IconButton(onClick = {
                        onChange("")
                        expanded = allSuggestions.isNotEmpty()
                    }) { Icon(Icons.Default.Clear, contentDescription = null) }
                } else if (allSuggestions.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (expanded && filtered.isNotEmpty()) {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(top = 4.dp),
            ) {
                LazyColumn {
                    items(filtered, key = { it }) { g ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChange(g)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(g)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionRow(direction: CalcDirection, onChange: (CalcDirection) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = direction == CalcDirection.ByLength,
                onClick = { onChange(CalcDirection.ByLength) },
            )
            Text(stringResource(R.string.mode_by_length))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = direction == CalcDirection.ByMass,
                onClick = { onChange(CalcDirection.ByMass) },
            )
            Text(stringResource(R.string.mode_by_mass))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSwitcher(mode: ShapeMode, onChange: (ShapeMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode == ShapeMode.Free,
            onClick = { onChange(ShapeMode.Free) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
        ) { Text(stringResource(R.string.mode_calc)) }
        SegmentedButton(
            selected = mode == ShapeMode.Gost,
            onClick = { onChange(ShapeMode.Gost) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
        ) { Text(stringResource(R.string.mode_gost)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GostPicker(shape: ProfileShape, current: String?, onPick: (String?) -> Unit) {
    val items = when (shape) {
        ProfileShape.IBeam -> GostProfiles.iBeams.map { it.number to "№${it.number} · ${it.massKgM} кг/м" }
        ProfileShape.Channel -> GostProfiles.channels.map { it.number to "№${it.number} · ${it.massKgM} кг/м" }
        ProfileShape.Angle -> GostProfiles.angles.map { it.number to "${it.number} · ${it.massKgM} кг/м" }
        ProfileShape.Rebar -> GostProfiles.rebars.map { it.number to "Ø${it.number} · ${it.massKgM} кг/м" }
        else -> emptyList()
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.first == current }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Типоразмер ГОСТ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { (key, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onPick(key); expanded = false
                })
            }
        }
    }
}

@Composable
private fun ShapeInputs(shape: ProfileShape, ui: CalcUiState, vm: CalcViewModel) {
    val mm = ", " + stringResource(R.string.unit_mm)
    when (shape) {
        ProfileShape.Round, ProfileShape.Rebar -> {
            NumberField("D" + mm, ui.d, vm::onDChange)
        }
        ProfileShape.PipeRound -> {
            NumberField("D наружн." + mm, ui.d, vm::onDChange)
            NumberField("d внутр." + mm, ui.d2, vm::onD2Change)
        }
        ProfileShape.Hex -> {
            NumberField("Размер под ключ S" + mm, ui.d, vm::onDChange)
        }
        ProfileShape.Plate -> {
            NumberField("Толщина h" + mm, ui.t, vm::onTChange)
            NumberField("Ширина B" + mm, ui.b, vm::onBChange)
        }
        ProfileShape.BentChannel -> {
            NumberField("Высота H" + mm, ui.h, vm::onHChange)
            NumberField("Полка B" + mm, ui.b, vm::onBChange)
            NumberField("Толщина t" + mm, ui.t, vm::onTChange)
        }
        ProfileShape.PipeRect -> {
            NumberField("Высота H" + mm, ui.h, vm::onHChange)
            NumberField("Ширина B" + mm, ui.b, vm::onBChange)
            NumberField("Толщина стенки t" + mm, ui.t, vm::onTChange)
        }
        ProfileShape.Angle -> {
            NumberField("Полка b" + mm, ui.b, vm::onBChange)
            NumberField("Толщина t" + mm, ui.t, vm::onTChange)
        }
        ProfileShape.IBeam, ProfileShape.Channel -> {
            NumberField("Высота h" + mm, ui.h, vm::onHChange)
            NumberField("Ширина полки b" + mm, ui.b, vm::onBChange)
            NumberField("Толщина стенки s" + mm, ui.s, vm::onSChange)
            NumberField("Толщина полки t" + mm, ui.t, vm::onTChange)
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    integer: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultCard(r: tech.deepdrift.metallist.domain.model.CalcResult, direction: CalcDirection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Результат", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
            HorizontalDivider()
            ResultRow(stringResource(R.string.result_mass), formatMass(r.totalMassKg))
            if (direction == CalcDirection.ByMass) {
                ResultRow(stringResource(R.string.result_length), formatLength(r.totalLengthMm))
            }
            ResultRow(stringResource(R.string.result_area), "%.2f мм²".format(r.crossSectionAreaMm2))
            ResultRow(stringResource(R.string.result_linear_mass), "%.4f кг/м".format(r.linearMassKgM))
            ResultRow(stringResource(R.string.result_volume), "%.2f см³".format(r.volumeMm3 / 1000.0))
            if (r.totalPrice > 0.0) {
                ResultRow(stringResource(R.string.result_price), "%.2f".format(r.totalPrice))
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun formatMass(kg: Double): String = when {
    kg >= 1000.0 -> "%.3f т".format(kg / 1000.0)
    kg >= 1.0 -> "%.3f кг".format(kg)
    else -> "%.1f г".format(kg * 1000.0)
}

private fun formatLength(mm: Double): String = when {
    mm >= 1000.0 -> "%.3f м".format(mm / 1000.0)
    else -> "%.1f мм".format(mm)
}

private fun ProfileShape.readableRes(): Int = when (this) {
    ProfileShape.Round -> R.string.shape_round
    ProfileShape.PipeRound -> R.string.shape_pipe_round
    ProfileShape.Hex -> R.string.shape_hex
    ProfileShape.Plate -> R.string.shape_plate
    ProfileShape.BentChannel -> R.string.shape_bent_channel
    ProfileShape.PipeRect -> R.string.shape_pipe_rect
    ProfileShape.Angle -> R.string.shape_angle
    ProfileShape.IBeam -> R.string.shape_i_beam
    ProfileShape.Channel -> R.string.shape_channel
    ProfileShape.Rebar -> R.string.shape_rebar
}
