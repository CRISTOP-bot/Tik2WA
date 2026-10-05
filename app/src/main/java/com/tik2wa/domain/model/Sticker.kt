package com.tik2wa.domain.model

data class Sticker(
    val id: String,
    val title: String,
    val previewUrl: String?,
    val contentHash: String,
    val animated: Boolean = false,
    val syncState: SyncState = SyncState.Available
)

sealed interface SyncState {
    data object Available : SyncState
    data object Selected : SyncState
    data object Pending : SyncState
    data object Synced : SyncState
    data class Failed(val reason: String) : SyncState
}

enum class ProviderConnection { Connected, Disconnected, Unavailable }

data class ProviderStatus(val connection: ProviderConnection, val detail: String? = null)
