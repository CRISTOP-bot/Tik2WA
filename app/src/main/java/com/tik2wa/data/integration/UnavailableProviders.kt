package com.tik2wa.data.integration

import com.tik2wa.domain.model.ProviderConnection
import com.tik2wa.domain.model.ProviderStatus
import com.tik2wa.domain.model.Sticker
import com.tik2wa.domain.provider.AuthProvider
import com.tik2wa.domain.provider.IntegrationResult
import com.tik2wa.domain.provider.TikTokStickerProvider
import com.tik2wa.domain.provider.WhatsAppStickerProvider

/** Explicitly disabled adapters: this build does not pretend to access either account. */
class UnavailableTikTokProvider : TikTokStickerProvider, AuthProvider {
    private val reason = "TikTok no ofrece una API pública oficial para leer los stickers guardados en Favoritos."
    override suspend fun status() = ProviderStatus(ProviderConnection.Unavailable, reason)
    override suspend fun favoriteStickers(): IntegrationResult<List<Sticker>> = IntegrationResult.Unavailable(reason)
    override suspend fun signIn(): IntegrationResult<Unit> = IntegrationResult.Unavailable(
        "No hay un flujo oficial autorizado disponible para iniciar sesión y leer Favoritos."
    )
    override suspend fun signOut() = Unit
}

class UnavailableWhatsAppProvider : WhatsAppStickerProvider, AuthProvider {
    private val reason = "WhatsApp admite packs mediante su mecanismo oficial de stickers, pero no permite vincular una cuenta para importar stickers en silencio."
    override suspend fun status() = ProviderStatus(ProviderConnection.Unavailable, reason)
    override suspend fun addStickerPack(stickers: List<ByteArray>): IntegrationResult<Unit> =
        IntegrationResult.Unavailable(reason)
    override suspend fun signIn(): IntegrationResult<Unit> = IntegrationResult.Unavailable(reason)
    override suspend fun signOut() = Unit
}
