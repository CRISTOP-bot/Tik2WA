package com.tik2wa.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.tik2wa.domain.auth.AccountAuthRepository
import com.tik2wa.domain.auth.AccountAuthResult
import com.tik2wa.domain.auth.AppAccount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAccountAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AccountAuthRepository {
    override val account: Flow<AppAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(user?.let { AppAccount(uid = it.uid, email = it.email) })
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.let { AppAccount(uid = it.uid, email = it.email) })
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): AccountAuthResult = performAuth {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await().user
    }

    override suspend fun register(email: String, password: String): AccountAuthResult = performAuth {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await().user
    }

    override suspend fun signOut() = firebaseAuth.signOut()

    private suspend fun performAuth(operation: suspend () -> com.google.firebase.auth.FirebaseUser?): AccountAuthResult =
        try {
            val user = operation()
            if (user == null) AccountAuthResult.Failure("Firebase no devolvió una cuenta válida.")
            else AccountAuthResult.Success(AppAccount(uid = user.uid, email = user.email))
        } catch (error: Exception) {
            AccountAuthResult.Failure(error.toUserMessage())
        }

    private fun Exception.toUserMessage(): String {
        val code = (this as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "Escribe un correo válido."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya tiene una cuenta. Inicia sesión."
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres."
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos."
            "ERROR_OPERATION_NOT_ALLOWED" -> "Activa el método Correo/contraseña en Firebase Authentication."
            "ERROR_NETWORK_REQUEST_FAILED" -> "No hay conexión. Inténtalo de nuevo."
            else -> "No se pudo autenticar. Revisa los datos y la configuración de Firebase."
        }
    }
}
