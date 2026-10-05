package com.tik2wa.domain.auth

import kotlinx.coroutines.flow.Flow

data class AppAccount(val uid: String, val email: String?)

sealed interface AccountAuthResult {
    data class Success(val account: AppAccount) : AccountAuthResult
    data class Failure(val message: String) : AccountAuthResult
}

/** Application-account authentication; it is separate from TikTok and WhatsApp account access. */
interface AccountAuthRepository {
    val account: Flow<AppAccount?>
    suspend fun signIn(email: String, password: String): AccountAuthResult
    suspend fun register(email: String, password: String): AccountAuthResult
    suspend fun signOut()
}
