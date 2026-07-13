package tech.deepdrift.metallist.ui.calc

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tech.deepdrift.metallist.data.db.HistoryEntity
import tech.deepdrift.metallist.data.repository.HistoryRepository
import tech.deepdrift.metallist.data.repository.MaterialsRepository
import tech.deepdrift.metallist.domain.calc.MetalCalculator
import tech.deepdrift.metallist.domain.model.CalcDirection
import tech.deepdrift.metallist.domain.model.CalcRequest
import tech.deepdrift.metallist.domain.model.CalcResult
import tech.deepdrift.metallist.domain.model.Material
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeMode
import tech.deepdrift.metallist.domain.model.ShapeParams
import javax.inject.Inject

@Serializable
data class CalcSnapshot(
    val shape: String,
    val mode: String,
    val direction: String,
    val d: Double = 0.0, val d2: Double = 0.0,
    val h: Double = 0.0, val b: Double = 0.0,
    val t: Double = 0.0, val s: Double = 0.0,
    val gostNumber: String? = null,
    val materialName: String,
    val density: Double,
    val lengthMm: Double = 0.0,
    val massKg: Double = 0.0,
    val quantity: Int = 1,
    val pricePerKg: Double = 0.0,
)

data class CalcUiState(
    val shape: ProfileShape,
    val mode: ShapeMode = ShapeMode.Free,
    val direction: CalcDirection = CalcDirection.ByLength,
    val d: String = "", val d2: String = "",
    val h: String = "", val b: String = "",
    val t: String = "", val s: String = "",
    val gostNumber: String? = null,
    val length: String = "",
    val mass: String = "",
    val quantity: String = "1",
    val price: String = "0",
    val selectedMaterial: Material? = null,
    val density: String = "7.85",
    val result: CalcResult? = null,
)

@HiltViewModel
class CalcViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val materialsRepo: MaterialsRepository,
    private val historyRepo: HistoryRepository,
) : ViewModel() {

    private val shapeName: String = savedState["shape"] ?: ProfileShape.Round.name
    private val historyId: Long = savedState["historyId"]?.toString()?.toLongOrNull() ?: 0L

    val materials: StateFlow<List<Material>> = materialsRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ui = MutableStateFlow(CalcUiState(shape = ProfileShape.valueOf(shapeName)))
    val ui: StateFlow<CalcUiState> = _ui.asStateFlow()

    init {
        // При наличии historyId — заранее пробуем восстановить.
        if (historyId != 0L) viewModelScope.launch {
            historyRepo.getById(historyId)?.let { entity ->
                if (entity.kind == "metal") restoreSnapshot(entity.payloadJson)
            }
        }
    }

    fun onMaterialSelected(m: Material) = _ui.update {
        it.copy(selectedMaterial = m, density = m.densityGCm3.toString())
    }

    fun onDensityChange(v: String) = _ui.update { it.copy(density = v) }
    fun onModeChange(m: ShapeMode) = _ui.update { it.copy(mode = m) }
    fun onDirectionChange(d: CalcDirection) = _ui.update { it.copy(direction = d) }
    fun onDChange(v: String) = _ui.update { it.copy(d = v) }
    fun onD2Change(v: String) = _ui.update { it.copy(d2 = v) }
    fun onHChange(v: String) = _ui.update { it.copy(h = v) }
    fun onBChange(v: String) = _ui.update { it.copy(b = v) }
    fun onTChange(v: String) = _ui.update { it.copy(t = v) }
    fun onSChange(v: String) = _ui.update { it.copy(s = v) }
    fun onGostChange(v: String?) = _ui.update { it.copy(gostNumber = v) }
    fun onLengthChange(v: String) = _ui.update { it.copy(length = v) }
    fun onMassChange(v: String) = _ui.update { it.copy(mass = v) }
    fun onQuantityChange(v: String) = _ui.update { it.copy(quantity = v) }
    fun onPriceChange(v: String) = _ui.update { it.copy(price = v) }

    fun calculate() {
        val u = _ui.value
        val request = CalcRequest(
            shape = u.shape,
            mode = u.mode,
            params = ShapeParams(
                d = u.d.parseDouble(),
                d2 = u.d2.parseDouble(),
                h = u.h.parseDouble(),
                b = u.b.parseDouble(),
                t = u.t.parseDouble(),
                s = u.s.parseDouble(),
                gostNumber = u.gostNumber,
            ),
            densityGCm3 = u.density.parseDouble(7.85),
            direction = u.direction,
            lengthMm = u.length.parseDouble(),
            massKg = u.mass.parseDouble(),
            quantity = u.quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            pricePerKg = u.price.parseDouble(),
        )
        val result = MetalCalculator.calculate(request)
        _ui.update { it.copy(result = result) }
        viewModelScope.launch { saveToHistory(request, result) }
    }

    private suspend fun saveToHistory(req: CalcRequest, res: CalcResult) {
        val snap = CalcSnapshot(
            shape = req.shape.name,
            mode = req.mode.name,
            direction = req.direction.name,
            d = req.params.d, d2 = req.params.d2,
            h = req.params.h, b = req.params.b,
            t = req.params.t, s = req.params.s,
            gostNumber = req.params.gostNumber,
            materialName = _ui.value.selectedMaterial?.name ?: "—",
            density = req.densityGCm3,
            lengthMm = req.lengthMm,
            massKg = req.massKg,
            quantity = req.quantity,
            pricePerKg = req.pricePerKg,
        )
        val json = Json.encodeToString(CalcSnapshot.serializer(), snap)
        val title = "${req.shape.readable()} · ${snap.materialName}"
        val summary = "Масса ${"%.3f".format(res.totalMassKg)} кг · Длина ${"%.0f".format(res.totalLengthMm)} мм"
        historyRepo.add(
            HistoryEntity(
                timestamp = System.currentTimeMillis(),
                kind = "metal",
                title = title,
                summary = summary,
                payloadJson = json,
            )
        )
    }

    private fun restoreSnapshot(json: String) {
        val snap = runCatching { Json.decodeFromString(CalcSnapshot.serializer(), json) }.getOrNull() ?: return
        _ui.update {
            it.copy(
                shape = ProfileShape.valueOf(snap.shape),
                mode = ShapeMode.valueOf(snap.mode),
                direction = CalcDirection.valueOf(snap.direction),
                d = snap.d.orBlank(), d2 = snap.d2.orBlank(),
                h = snap.h.orBlank(), b = snap.b.orBlank(),
                t = snap.t.orBlank(), s = snap.s.orBlank(),
                gostNumber = snap.gostNumber,
                length = snap.lengthMm.orBlank(),
                mass = snap.massKg.orBlank(),
                quantity = snap.quantity.toString(),
                price = snap.pricePerKg.toString(),
                density = snap.density.toString(),
            )
        }
    }
}

private fun String.parseDouble(fallback: Double = 0.0): Double =
    replace(',', '.').toDoubleOrNull() ?: fallback

private fun Double.orBlank(): String = if (this == 0.0) "" else this.toString()

private fun ProfileShape.readable(): String = when (this) {
    ProfileShape.Round -> "Круг"
    ProfileShape.PipeRound -> "Труба круглая"
    ProfileShape.Square -> "Квадрат"
    ProfileShape.Hex -> "Шестигранник"
    ProfileShape.Sheet -> "Лист"
    ProfileShape.BentChannel -> "Швеллер гнутый"
    ProfileShape.PipeRect -> "Труба прямоугольная"
    ProfileShape.Angle -> "Уголок"
    ProfileShape.IBeam -> "Двутавр"
    ProfileShape.Channel -> "Швеллер"
    ProfileShape.Rebar -> "Арматура"
}
