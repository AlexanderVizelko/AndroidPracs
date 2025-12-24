package com.example.hw_3.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.hw_3.data.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile_preferences")

class ProfileManager(private val context: Context) {
    companion object {
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
        private val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri")
        private val RESUME_URL_KEY = stringPreferencesKey("resume_url")
        private val POSITION_KEY = stringPreferencesKey("position")
    }

    val profile: Flow<Profile> = context.profileDataStore.data.map { preferences ->
        Profile(
            fullName = preferences[FULL_NAME_KEY] ?: "",
            avatarUri = preferences[AVATAR_URI_KEY],
            resumeUrl = preferences[RESUME_URL_KEY] ?: "",
            position = preferences[POSITION_KEY] ?: ""
        )
    }

    suspend fun saveProfile(profile: Profile) {
        context.profileDataStore.edit { preferences ->
            preferences[FULL_NAME_KEY] = profile.fullName
            if (profile.avatarUri != null) {
                preferences[AVATAR_URI_KEY] = profile.avatarUri
            } else {
                preferences.remove(AVATAR_URI_KEY)
            }
            preferences[RESUME_URL_KEY] = profile.resumeUrl
            preferences[POSITION_KEY] = profile.position
        }
    }

    suspend fun getProfile(): Profile {
        val preferences = context.profileDataStore.data.first()
        return Profile(
            fullName = preferences[FULL_NAME_KEY] ?: "",
            avatarUri = preferences[AVATAR_URI_KEY],
            resumeUrl = preferences[RESUME_URL_KEY] ?: "",
            position = preferences[POSITION_KEY] ?: ""
        )
    }
}

