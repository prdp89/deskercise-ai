package com.ai.app.move.deskercise.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.responses.VerifyResponse
import com.ai.app.move.deskercise.usecases.ResentOptCodeUseCase
import com.ai.app.move.deskercise.usecases.VerifyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class VerifyViewModel (
    private val verifyUseCase: VerifyUseCase,
    private val resendOptUpUseCase: ResentOptCodeUseCase,
) : BaseViewModel() {

    private var email: String = ""
    private var confirmationCode: String = ""
    private var resendCountDownTime = 0L
    val verifyLiveData = MutableLiveData<State<VerifyResponse>>()
    val resendLiveData = MutableLiveData<State<Any?>>()
    val resendTimerLiveData = MutableLiveData<Long>()
    private var countDownJob: Job? = null

    /**
     * Start the countdown timer for resend code.
     * This is to prevent user from sending too many requests to resend code.
     */
    fun startCountDown() {
        // Stop any existing countdowns
        stopCountDown()

        countDownJob = viewModelScope.launch {
            val totalSeconds = TimeUnit.MINUTES.toSeconds(1)
            val tickSeconds = 1
            for (second in totalSeconds downTo tickSeconds) {
                resendCountDownTime = second

                // Update the UI displaying the timer
                resendTimerLiveData.postValue(resendCountDownTime)
                delay(1000)
            }
            resendTimerLiveData.postValue(0L)
        }
        countDownJob?.start()
    }

    /**
     * Stop any existing countdowns.
     *
     * @see startCountDown
     */
    private fun stopCountDown() {
        countDownJob?.cancel()
    }

    fun updateEmail(value: String) {
        email = value
    }

    fun updateConfirmationCode(value: String) {
        confirmationCode = value
    }

    /**
     * Check if user inputs clear validation
     */
    private fun verifyDataValidated(): Boolean {
        if (confirmationCode.isEmpty()) {
            publishMessage(R.string.msg_required_code)
            return false
        }

        if (confirmationCode.length < 6) {
            publishMessage(R.string.msg_code_format)
            return false
        }
        return true
    }

    /**
     * Interacts with the authentication repository to
     * send/resend confirmation code
     */
    fun resend() {
        viewModelScope.launch {
            try {
                resendLiveData.postValue(State.Starting())
                val result = resendOptUpUseCase.invoke(email)
                resendLiveData.postValue(State.Success(result))
                startCountDown()
            } catch (e: Exception) {
                e.printStackTrace()
                resendLiveData.postValue(State.Error(e))
            }
        }
    }

    /**
     * Interacts with the authentication repository to
     * confirm that an email has been verified
     */
    fun verify() {
        if (!verifyDataValidated()) return

        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    verifyLiveData.postValue(State.Starting())
                    val result = verifyUseCase(email, confirmationCode)
                    verifyLiveData.postValue(State.Success(result))
                    stopCountDown()
                } catch (e: Exception) {
                    e.printStackTrace()
                    verifyLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCountDown()
    }
}