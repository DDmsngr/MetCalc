package tech.deepdrift.metallist.ui.calc

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.domain.calc.GostProfiles
import tech.deepdrift.metallist.domain.model.CalcDirection
import tech.deepdrift.metallist.domain.model.Material
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeMode
import tech.deepdrift.metallist.ui.common.ShapeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcScreen(
    shape: ProfileShape,
    historyId: Long,
    onBack: () -> Unit,
    vm: CalcViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()
    val materials by vm.materials.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shape.readableRes().let { stringResource(it) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
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
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ShapeIcon(shape = shape, color = MaterialTheme.colorScheme.onSurface, sizeDp = 120)
            }

            MaterialPicker(
                materials = materials,
                selected = ui.selectedMaterial,
                density = ui.density,
                onPick = vm::onMaterialSelected,
                onDensity = vm::onDensityChange,
            )

            DirectionRow(ui.direction, vm::onDirectionChange)

            if (shape.supportsGost) {
                ModeSwitcher(ui.mode, vm::onModeChange)
            }

            if (ui.mode == ShapeMode.Gost && shape.supportsGost) {
                GostPicker(shape, ui.gostNumber, vm::onGostChange)
            } else {
                ShapeInputs(shape, ui, vm)
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

            ui.result?.let { r ->
                ResultCard(r, ui.direction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialPicker(
    materials: List<Material>,
    selected: Material?,
    density: String,
    onPick: (Material) -> Unit,
    onDensity: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selected?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.material)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                materials.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name) },
                        onClick = {
                            onPick(m); expanded = false
                        },
                    )
                }
            }
        }
        NumberField(
            label = stringResource(R.string.density) + ", " + stringResource(R.string.density_unit),
            value = density, onChange = onDensity,
        )
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
        androidx.compose.material3.ExposedDropdownMenu(
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
        ProfileShape.Square -> {
            NumberField("Сторона a" + mm, ui.d, vm::onDChange)
        }
        ProfileShape.Hex -> {
            NumberField("Размер под ключ S" + mm, ui.d, vm::onDChange)
        }
        ProfileShape.Sheet -> {
            NumberField("Толщина t" + mm, ui.t, vm::onTChange)
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
    ProfileShape.Square -> R.string.shape_square
    ProfileShape.Hex -> R.string.shape_hex
    ProfileShape.Sheet -> R.string.shape_sheet
    ProfileShape.BentChannel -> R.string.shape_bent_channel
    ProfileShape.PipeRect -> R.string.shape_pipe_rect
    ProfileShape.Angle -> R.string.shape_angle
    ProfileShape.IBeam -> R.string.shape_i_beam
    ProfileShape.Channel -> R.string.shape_channel
    ProfileShape.Rebar -> R.string.shape_rebar
}
