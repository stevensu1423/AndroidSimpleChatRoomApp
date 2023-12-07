package com.steven.androidchatroom.util.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesConstant {
    const val PDS = "pds"
    val KEY_REMEMBER_ACCOUNT = booleanPreferencesKey("false")
    val KEY_ACCOUNT = stringPreferencesKey("userName")
    val KEY_PASSWORD = stringPreferencesKey("password")
}