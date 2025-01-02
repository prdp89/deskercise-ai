package com.ai.app.move.deskercise.app

import android.content.Context

class AppStorage(context: Context) {
    companion object {
        const val PREF_NAME = "app-storage"
        const val KEY_GOOD_REPS = "key-good-reps"
        const val KEY_BAD_REPS = "key-bad-reps"
        const val KEY_SESSION_ID = "key-session-id"
    }

    private val userPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = userPref.edit()

    var cacheTotalPoint = 0

    fun storeGoodReps(value: Int) {
        editor.putInt(KEY_GOOD_REPS, value)
        editor.apply()
    }

    fun getGoodReps(): Int = userPref.getInt(KEY_GOOD_REPS, 0)

    fun storeBadReps(value: Int) {
        editor.putInt(KEY_BAD_REPS, value)
        editor.apply()
    }

    fun getBadReps(): Int = userPref.getInt(KEY_BAD_REPS, 0)

    fun storeSessionId(sessionId: Int) {
        editor.putInt(KEY_SESSION_ID, sessionId)
        editor.apply()
    }

    fun getSessionId(): Int = userPref.getInt(KEY_SESSION_ID, 0)
}
