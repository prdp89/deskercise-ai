package com.ai.app.move.deskercise.app

import android.content.Context
import com.google.gson.Gson
import com.ai.app.move.deskercise.data.User

class UserManager(context: Context, private val gson: Gson) {
    companion object {
        const val PREF_NAME = "pref-user"
        const val KEY_ACCESS_TOKEN = "key-access-token"
        const val KEY_REFRESH_TOKEN = "key-refresh-token"
        const val KEY_USER_INFO = "key-user-info"
    }

    private val userPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = userPref.edit()

    fun clear() {
        editor.clear().apply()
    }

    fun storeAccessToken(token: String) {
        editor.putString(KEY_ACCESS_TOKEN, token)
        editor.apply()
    }

    fun getAccessToken(): String = userPref.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    fun storeRefreshToken(token: String) {
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
    }

    fun getRefreshToken(): String = userPref.getString(KEY_REFRESH_TOKEN, "").orEmpty()

    fun storeUserInfo(user: User) {
        editor.putString(KEY_USER_INFO, gson.toJson(user))
        editor.apply()
    }

    fun getUserInfo(): User? {
        return try {
            val json = userPref.getString(KEY_USER_INFO, "")
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
