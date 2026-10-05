package com.tik2wa.data.stickers

import com.tik2wa.domain.model.Sticker
import java.security.MessageDigest

object StickerDeduplicator {
    fun unique(stickers: List<Sticker>): List<Sticker> =
        stickers.distinctBy { it.contentHash.ifBlank { it.id } }

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}
