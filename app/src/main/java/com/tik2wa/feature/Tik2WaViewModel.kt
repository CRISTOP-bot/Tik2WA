package com.tik2wa.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tik2wa.data.integration.UnavailableTikTokProvider
import com.tik2wa.data.integration.UnavailableWhatsAppProvider
import com.tik2wa.data.auth.FirebaseAccountAuthRepository
import com.tik2wa.domain.auth.AccountAuthRepository
import com.tik2wa.domain.auth.AccountAuthResult
import com.tik2wa.data.stickers.StickerDeduplicator
import com.tik2wa.domain.model.Sticker
import com.tik2wa.domain.provider.IntegrationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab(val label: String) { Home("Inicio"), Stickers("Stickers"), Sync("Sincronizar"), Profile("Perfil") }

data class Tik2WaUiState(
    val tab: MainTab = MainTab.Home,
    val settingsOpen: Boolean = false,
    val feedback: String? = null,
    val syncStep: Int = -1,
    val syncProgress: Float = 0f,
    val autoSync: Boolean = false,
    val notifications: Boolean = true,
    val darkAppearance: Boolean = true,
    val stickers: List<Sticker> = emptyList(),
    val selectedStickerIds: Set<String> = emptySet(),
    val accountEmail: String? = null,
    val authLoading: Boolean = false,
    val authMessage: String? = null
)

/** Owns screen state and orchestrates provider calls; composables remain presentation-only. */
class Tik2WaViewModel : ViewModel() {
    private val tiktok = UnavailableTikTokProvider()
    private val whatsapp = UnavailableWhatsAppProvider()
    private val accountAuth: AccountAuthRepository = FirebaseAccountAuthRepository()
    private val mutableUiState = MutableStateFlow(Tik2WaUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountAuth.account.collect { account ->
                mutableUiState.update { it.copy(accountEmail = account?.email) }
            }
        }
    }

    fun navigate(tab: MainTab) = mutableUiState.update { it.copy(tab = tab, settingsOpen = false) }
    fun toggleSettings() = mutableUiState.update { it.copy(settingsOpen = !it.settingsOpen) }
    fun openSettings() = mutableUiState.update { it.copy(settingsOpen = true) }
    fun dismissFeedback() = mutableUiState.update { it.copy(feedback = null) }
    fun setAutoSync(value: Boolean) = mutableUiState.update { it.copy(autoSync = value) }
    fun setNotifications(value: Boolean) = mutableUiState.update { it.copy(notifications = value) }
    fun setDarkAppearance(value: Boolean) = mutableUiState.update { it.copy(darkAppearance = value) }
    fun logout() = show("No hay sesiones conectadas que cerrar.")

    fun authenticate(email: String, password: String, createAccount: Boolean) = viewModelScope.launch {
        mutableUiState.update { it.copy(authLoading = true, authMessage = null) }
        val result = if (createAccount) accountAuth.register(email, password) else accountAuth.signIn(email, password)
        mutableUiState.update { state ->
            when (result) {
                is AccountAuthResult.Success -> state.copy(
                    authLoading = false,
                    accountEmail = result.account.email,
                    authMessage = if (createAccount) "Cuenta creada y sesión iniciada." else "Sesión iniciada."
                )
                is AccountAuthResult.Failure -> state.copy(authLoading = false, authMessage = result.message)
            }
        }
    }

    fun signOutAppAccount() = viewModelScope.launch {
        accountAuth.signOut()
        mutableUiState.update { it.copy(accountEmail = null, authMessage = "Sesión cerrada.") }
    }

    fun connectTikTok() = viewModelScope.launch {
        show(unavailableMessage(tiktok.signIn()))
    }

    fun connectWhatsApp() = viewModelScope.launch {
        show(unavailableMessage(whatsapp.signIn()))
    }

    fun refreshStickers() = viewModelScope.launch {
        val result = tiktok.favoriteStickers()
        if (result is IntegrationResult.Success) {
            val unique = StickerDeduplicator.unique(result.value)
            mutableUiState.update { it.copy(stickers = unique, selectedStickerIds = emptySet()) }
        }
        val message = when (result) {
            is IntegrationResult.Unavailable -> result.explanation
            is IntegrationResult.Failure -> result.safeMessage
            is IntegrationResult.Success -> if (result.value.isEmpty()) "No encontramos stickers disponibles." else "Stickers actualizados."
        }
        show(message)
    }

    fun toggleSticker(id: String) = mutableUiState.update { state ->
        val selected = state.selectedStickerIds
        state.copy(selectedStickerIds = if (id in selected) selected - id else selected + id)
    }

    fun selectAllStickers() = mutableUiState.update { state ->
        state.copy(selectedStickerIds = state.stickers.filter { it.syncState !is com.tik2wa.domain.model.SyncState.Synced }.map { it.id }.toSet())
    }

    fun addSticker() = show("La conexión oficial todavía no está disponible; no se transfirió ningún sticker.")
    fun addAllStickers() = show("La conexión oficial todavía no está disponible; no se transfirió ningún sticker.")

    fun playSyncPreview() = viewModelScope.launch {
        mutableUiState.update { it.copy(syncStep = 0, syncProgress = 0f) }
        for (step in 0..3) {
            mutableUiState.update { it.copy(syncStep = step, syncProgress = (step + 1) / 4f) }
            delay(520)
        }
        mutableUiState.update { it.copy(syncStep = 4) }
        show("La animación terminó; no se transfirieron stickers porque las integraciones oficiales no están disponibles.")
    }

    private fun unavailableMessage(result: IntegrationResult<Unit>): String = when (result) {
        is IntegrationResult.Unavailable -> result.explanation
        is IntegrationResult.Failure -> result.safeMessage
        is IntegrationResult.Success -> "Conexión completada."
    }

    private fun show(message: String) = mutableUiState.update { it.copy(feedback = message) }
}
