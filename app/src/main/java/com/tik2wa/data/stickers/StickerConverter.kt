package com.tik2wa.data.stickers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

sealed interface ConversionResult {
    data class Success(val webp: ByteArray, val preview: Bitmap, val sourceWasAnimated: Boolean) : ConversionResult
    data class Invalid(val reason: String) : ConversionResult
}

/** Converts image bytes in memory; it never exposes intermediate files to the user. */
class StickerConverter(
    private val edgePx: Int = 512,
    private val maxBytes: Int = 100 * 1024
) {
    fun convert(source: ByteArray, declaredMime: String? = null): ConversionResult {
        if (source.isEmpty()) return ConversionResult.Invalid("La imagen está vacía.")
        val mime = declaredMime?.lowercase()
        if (mime == "image/gif" || mime == "image/webp" && isAnimatedWebp(source)) {
            return ConversionResult.Invalid("La conversión animada no está habilitada en esta versión.")
        }
        val decoded = BitmapFactory.decodeByteArray(source, 0, source.size)
            ?: return ConversionResult.Invalid("Formato de imagen no compatible.")
        if (decoded.width <= 0 || decoded.height <= 0) {
            decoded.recycle()
            return ConversionResult.Invalid("Dimensiones de imagen inválidas.")
        }
        val output = Bitmap.createBitmap(edgePx, edgePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.TRANSPARENT)
        val scale = minOf(edgePx.toFloat() / decoded.width, edgePx.toFloat() / decoded.height)
        val w = decoded.width * scale
        val h = decoded.height * scale
        val dst = RectF((edgePx - w) / 2f, (edgePx - h) / 2f, (edgePx + w) / 2f, (edgePx + h) / 2f)
        canvas.drawBitmap(decoded, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        decoded.recycle()

        var quality = 90
        var bytes: ByteArray
        do {
            val stream = java.io.ByteArrayOutputStream()
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
            output.compress(format, quality, stream)
            bytes = stream.toByteArray()
            if (bytes.size <= maxBytes) break
            quality -= 8
        } while (quality >= 42)
        if (bytes.size > maxBytes) {
            output.recycle()
            return ConversionResult.Invalid("No se pudo optimizar la imagen al tamaño permitido.")
        }
        return ConversionResult.Success(bytes, output, sourceWasAnimated = false)
    }

    private fun isAnimatedWebp(data: ByteArray): Boolean {
        val text = data.take(64).toByteArray().toString(Charsets.ISO_8859_1)
        return text.contains("ANIM") || text.contains("ANMF")
    }
}
