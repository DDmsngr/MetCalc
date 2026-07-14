package tech.deepdrift.metallist.ui.application

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экспорт заявки: генерация файлов и intent'ы для «Поделиться».
 * Все файлы кладём в `cacheDir/exports/`, чтобы система периодически чистила.
 */
object ApplicationExport {

    private const val AUTHORITY_SUFFIX = ".fileprovider"
    private fun authority(ctx: Context) = ctx.packageName + AUTHORITY_SUFFIX

    fun writeCsv(ctx: Context, rows: List<ApplicationRow>): File {
        val dir = File(ctx.cacheDir, "exports").apply { mkdirs() }
        val f = File(dir, "zayavka_${stamp()}.csv")
        f.writeText(ApplicationFormat.asCsv(rows), Charsets.UTF_8)
        return f
    }

    /**
     * Простой PDF: A4 портрет, шапка + таблица. Без внешних библиотек — только Android SDK.
     */
    fun writePdf(ctx: Context, rows: List<ApplicationRow>): File {
        val dir = File(ctx.cacheDir, "exports").apply { mkdirs() }
        val f = File(dir, "zayavka_${stamp()}.pdf")

        val pageWidth = 595   // A4 при 72 dpi
        val pageHeight = 842
        val margin = 40f

        val doc = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = doc.startPage(info)
        var canvas = page.canvas

        val title = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val small = Paint().apply { textSize = 10f; color = 0xFF666666.toInt() }
        val header = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val cell = Paint().apply { textSize = 11f }
        val line = Paint().apply { strokeWidth = 1f; color = 0xFF888888.toInt() }

        var y = margin + 20f
        canvas.drawText("Заявка на металл", margin, y, title)
        y += 20f
        canvas.drawText(dateHuman(), margin, y, small)
        y += 24f

        // Колонки: №(30), Материал(140), Форма(110), Размеры(180), Кол-во(остальное)
        val col1 = margin
        val col2 = margin + 30f
        val col3 = margin + 170f
        val col4 = margin + 280f
        val col5 = margin + 460f
        val rightEdge = pageWidth - margin

        // Шапка
        canvas.drawText("№", col1, y, header)
        canvas.drawText("Материал", col2, y, header)
        canvas.drawText("Форма", col3, y, header)
        canvas.drawText("Размеры, мм", col4, y, header)
        canvas.drawText("Кол-во", col5, y, header)
        y += 6f
        canvas.drawLine(margin, y, rightEdge, y, line)
        y += 14f

        for (r in rows) {
            if (y > pageHeight - margin - 20f) {
                doc.finishPage(page)
                page = doc.startPage(info); canvas = page.canvas
                y = margin + 20f
            }
            canvas.drawText(r.no.toString(), col1, y, cell)
            canvas.drawText(clip(r.material, 22), col2, y, cell)
            canvas.drawText(clip(r.shape, 18), col3, y, cell)
            canvas.drawText(clip(r.dimensions, 28), col4, y, cell)
            canvas.drawText(r.quantity, col5, y, cell)
            y += 18f
        }
        doc.finishPage(page)

        FileOutputStream(f).use { out -> doc.writeTo(out) }
        doc.close()
        return f
    }

    fun shareFile(ctx: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(ctx, authority(ctx), file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(Intent.createChooser(intent, "Отправить заявку").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun clip(s: String, max: Int): String =
        if (s.length <= max) s else s.take(max - 1) + "…"

    private fun stamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())

    private fun dateHuman(): String =
        SimpleDateFormat("dd MMMM yyyy · HH:mm", Locale("ru")).format(Date())
}
