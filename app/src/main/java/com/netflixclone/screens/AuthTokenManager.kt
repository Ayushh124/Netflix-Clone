package com.netflixclone.helpers

import android.content.Context
import android.content.SharedPreferences

//class AuthTokenManager(context: Context) {
//    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//
//    fun saveToken(token: String) {
//        prefs.edit().putString("auth_token", token).apply()
//    }
//
//    fun saveSubscriptionStatus(subscribed: Boolean) {
//        prefs.edit().putBoolean("is_subscribed", subscribed).apply()
//    }
//
//    fun getToken(): String? {
//        return prefs.getString("auth_token", null)
//    }
//
//    fun isUserSubscribed(): Boolean {
//        return prefs.getBoolean("is_subscribed", false)
//    }
//
//    fun clearSession() {
//        prefs.edit().clear().apply()
//    }
//}