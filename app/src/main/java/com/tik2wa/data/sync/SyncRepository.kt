package com.tik2wa.data.sync

import android.content.SharedPreferences
import com.tik2wa.data.stickers.StickerDeduplicator
import com.tik2wa.domain.model.Sticker
import com.tik2wa.domain.provider.IntegrationResult
import com.tik2wa.domain.provider.TikTokStickerProvider
import com.tik2wa.domain.provider.WhatsAppStickerProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow

interface StickerRepository {
    fun syncedHashes(): Flow<Set<String>>
    suspend fun markSynced(hashes: Set<String>)
}

interface SyncRepository {
    suspend fun availableStickers(): IntegrationResult<List<Sticker>>
    suspend fun addToWhatsApp(stickers: List<Pair<Sticker, ByteArray>>): IntegrationResult<Set<String>>
}

class PreferencesStickerRepository(private val prefs: SharedPreferences) : StickerRepository {
    override fun syncedHashes(): Flow<Set<String>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(read()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    override suspend fun markSynced(hashes: Set<String>) {
        val merged = read() + hashes
        prefs.edit().putStringSet(KEY, merged).apply()
    }
    private fun read() = prefs.getStringSet(KEY, emptySet()).orEmpty().toSet()
    private companion object { const val KEY = "synced_hashes" }
}

class DefaultSyncRepository(
    private val tiktok: TikTokStickerProvider,
    private val whatsapp: WhatsAppStickerProvider,
    private val stickerRepository: StickerRepository
) : SyncRepository {
    override suspend fun availableStickers(): IntegrationResult<List<Sticker>> = when (val result = tiktok.favoriteStickers()) {
        is IntegrationResult.Success -> IntegrationResult.Success(StickerDeduplicator.unique(result.value))
        is IntegrationResult.Unavailable -> result
        is IntegrationResult.Failure -> result
    }

    override suspend fun addToWhatsApp(stickers: List<Pair<Sticker, ByteArray>>): IntegrationResult<Set<String>> {
        val pending = stickers.distinctBy { it.first.contentHash }
        if (pending.isEmpty()) return IntegrationResult.Success(emptySet())
        return when (val result = whatsapp.addStickerPack(pending.map { it.second })) {
            is IntegrationResult.Success -> {
                val hashes = pending.map { it.first.contentHash }.toSet()
                stickerRepository.markSynced(hashes)
                IntegrationResult.Success(hashes)
            }
            is IntegrationResult.Unavailable -> result
            is IntegrationResult.Failure -> result
        }
    }
}
