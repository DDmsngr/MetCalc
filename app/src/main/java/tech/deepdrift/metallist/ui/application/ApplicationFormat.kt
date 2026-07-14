package tech.deepdrift.metallist.ui.application

import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.ui.calc.CalcSnapshot

/**
 * Одна строка заявки (то, что видит поставщик).
 * Внутренние поля (плотность, масса, объём, цена) в заявку не попадают —
 * заявка составляется только для отгрузки.
 */
data class ApplicationRow(
    val no: Int,
    val material: String,   // марка (если задана) или общее название материала
    val shape: String,      // "Круг", "Плита"…
    val dimensions: String, // "⌀20 × L 1000" и т. п.
    val quantity: String,   // "5 шт"
)

object ApplicationFormat {

    fun buildRows(snapshots: List<CalcSnapshot>): List<ApplicationRow> =
        snapshots.mapIndexed { idx, s ->
            ApplicationRow(
                no = idx + 1,
                material = s.grade?.takeIf { it.isNotBlank() } ?: s.materialName,
                shape = shapeName(ProfileShape.valueOf(s.shape)),
                dimensions = dimensions(s),
                quantity = "${s.quantity} шт",
            )
        }

    private fun shapeName(shape: ProfileShape): String = when (shape) {
        ProfileShape.Round -> "Круг"
        ProfileShape.PipeRound -> "Труба круглая"
        ProfileShape.Hex -> "Шестигранник"
        ProfileShape.Plate -> "Плита"
        ProfileShape.BentChannel -> "Швеллер гнутый"
        ProfileShape.PipeRect -> "Труба прямоуг."
        ProfileShape.Angle -> "Уголок"
        ProfileShape.IBeam -> "Двутавр"
        ProfileShape.Channel -> "Швеллер"
        ProfileShape.Rebar -> "Арматура"
    }

    private fun dimensions(s: CalcSnapshot): String {
        val len = if (s.lengthMm > 0) " × L ${fmt(s.lengthMm)}" else ""
        val shape = ProfileShape.valueOf(s.shape)
        val isGost = s.mode == "Gost" && s.gostNumber != null

        // Стандарты сечения имеют собственное строковое представление.
        s.standard?.let { std ->
            val core = dimensionsByStandard(std, s.standardOption)
            if (core != null) return core + len
        }

        val core = when (shape) {
            ProfileShape.Round -> "⌀${fmt(s.d)}"
            ProfileShape.PipeRound -> "⌀${fmt(s.d)}/${fmt(s.d2)}"
            ProfileShape.Hex -> "S ${fmt(s.d)}"
            ProfileShape.Plate -> "${fmt(s.t)} × ${fmt(s.b)}"
            ProfileShape.BentChannel -> "${fmt(s.h)}×${fmt(s.b)}×${fmt(s.t)}"
            ProfileShape.PipeRect -> "${fmt(s.h)}×${fmt(s.b)}×${fmt(s.t)}"
            ProfileShape.Angle -> "${fmt(s.b)}×${fmt(s.t)}"
            ProfileShape.IBeam -> if (isGost) "№${s.gostNumber}" else "${fmt(s.h)}×${fmt(s.b)}"
            ProfileShape.Channel -> if (isGost) "№${s.gostNumber}" else "${fmt(s.h)}×${fmt(s.b)}"
            ProfileShape.Rebar -> if (isGost) "⌀${s.gostNumber}" else "⌀${fmt(s.d)}"
        }
        return core + len
    }

    private fun dimensionsByStandard(std: String, opt: String?): String? = when (std) {
        "GOST_3262" -> {
            // opt: "20|Normal"
            val (du, cls) = opt?.split("|", limit = 2)?.let {
                (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "")
            } ?: ("" to "")
            val clsLabel = when (cls) { "Light" -> "лёгкая"; "Heavy" -> "усиленная"; else -> "обычная" }
            "Ду$du ($clsLabel, ГОСТ 3262)"
        }
        "GOST_8568" -> {
            val kind = if (opt == "Lentil") "чечевица" else "ромб"
            "рифл. $kind (ГОСТ 8568)"
        }
        "GOST_24045" -> {
            val (profile, thick) = opt?.split("|", limit = 2)?.let {
                (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "")
            } ?: ("" to "")
            "$profile × $thick мм (ГОСТ 24045)"
        }
        else -> null
    }

    private fun fmt(v: Double): String {
        if (v == 0.0) return "—"
        // Целые — без запятой, иначе одна десятая
        return if (v == v.toLong().toDouble()) v.toLong().toString()
        else "%.1f".format(v)
    }

    /**
     * Плоская таблица в моноширинном формате — для копирования в буфер.
     * Ширины колонок подобраны так, чтобы влезало в WhatsApp без переноса на большинстве шрифтов.
     */
    fun asPlainText(rows: List<ApplicationRow>): String {
        val hdr = "№  %-16s %-14s %-24s %s".format("Материал", "Форма", "Размеры, мм", "Кол-во")
        val body = rows.joinToString("\n") { r ->
            "%-2d %-16s %-14s %-24s %s".format(
                r.no, r.material.take(16), r.shape.take(14), r.dimensions.take(24), r.quantity
            )
        }
        return "Заявка на металл\n\n$hdr\n${"-".repeat(hdr.length)}\n$body\n"
    }

    /** CSV с разделителем `;` — универсальный для русской локали Excel. */
    fun asCsv(rows: List<ApplicationRow>): String {
        val header = listOf("№", "Материал", "Форма", "Размеры (мм)", "Количество")
            .joinToString(";") { csvEscape(it) }
        val body = rows.joinToString("\n") { r ->
            listOf(r.no.toString(), r.material, r.shape, r.dimensions, r.quantity)
                .joinToString(";") { csvEscape(it) }
        }
        return "﻿$header\n$body\n" // BOM — чтобы Excel открывал в UTF-8
    }

    private fun csvEscape(v: String): String {
        val needQuote = v.contains(';') || v.contains('"') || v.contains('\n')
        val esc = v.replace("\"", "\"\"")
        return if (needQuote) "\"$esc\"" else esc
    }
}
