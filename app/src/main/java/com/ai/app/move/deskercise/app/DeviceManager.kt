package com.ai.app.move.deskercise.app

import android.content.Context

class DeviceManager(context: Context) {
    companion object {
        const val PREF_NAME = "pref-device"
        const val KEY_PREVIOUS_EMAIL = "key-previous-email"
    }

    private val devicePref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = devicePref.edit()

    fun storePreviousEmail(value: String) {
        editor.putString(KEY_PREVIOUS_EMAIL, value)
        editor.apply()
    }
    fun getPreviousEmail(): String = devicePref.getString(KEY_PREVIOUS_EMAIL, "").orEmpty()
}
