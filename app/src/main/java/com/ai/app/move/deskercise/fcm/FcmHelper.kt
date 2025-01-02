package com.ai.app.move.deskercise.fcm

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import kotlin.coroutines.suspendCoroutine

class FcmHelper {

    suspend fun getToken() =
        suspendCoroutine<String> { c ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("Fetching FCM registration token failed", task.exception)
                    c.resumeWith(Result.success(""))
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Timber.d(">> token=$token")
                c.resumeWith(Result.success(token))
            })
        }
}