package com.tik2wa.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Encrypted token storage for future authorized providers. No credentials are requested in this build. */
class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "secure_sessions",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun put(key: String, value: String) { preferences.edit().putString(key, value).apply() }
    fun get(key: String): String? = preferences.getString(key, null)
    fun remove(key: String) { preferences.edit().remove(key).apply() }
    fun clear() { preferences.edit().clear().apply() }
}
