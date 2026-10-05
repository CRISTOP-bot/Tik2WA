package com.tik2wa.domain

import com.tik2wa.data.stickers.StickerDeduplicator
import com.tik2wa.domain.model.Sticker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StickerDeduplicatorTest {
    @Test fun removesRepeatedContentHashAndKeepsFirstSticker() {
        val items = listOf(
            Sticker("one", "One", null, "same"),
            Sticker("two", "Duplicate", null, "same"),
            Sticker("three", "Other", null, "different")
        )
        val result = StickerDeduplicator.unique(items)
        assertEquals(listOf("one", "three"), result.map { it.id })
    }

    @Test fun computesStableSha256() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            StickerDeduplicator.sha256("abc".toByteArray())
        )
        assertNotEquals(StickerDeduplicator.sha256("a".toByteArray()), StickerDeduplicator.sha256("b".toByteArray()))
    }
}
