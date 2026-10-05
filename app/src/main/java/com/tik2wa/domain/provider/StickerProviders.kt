package com.tik2wa.domain.provider

import com.tik2wa.domain.model.ProviderStatus
import com.tik2wa.domain.model.Sticker

sealed interface IntegrationResult<out T> {
    data class Success<T>(val value: T) : IntegrationResult<T>
    data class Unavailable(val explanation: String) : IntegrationResult<Nothing>
    data class Failure(val safeMessage: String) : IntegrationResult<Nothing>
}

interface AuthProvider {
    suspend fun status(): ProviderStatus
    suspend fun signIn(): IntegrationResult<Unit>
    suspend fun signOut()
}

interface TikTokStickerProvider {
    suspend fun status(): ProviderStatus
    suspend fun favoriteStickers(): IntegrationResult<List<Sticker>>
}

interface WhatsAppStickerProvider {
    suspend fun status(): ProviderStatus
    suspend fun addStickerPack(stickers: List<ByteArray>): IntegrationResult<Unit>
}
