package com.ai.app.move.deskercise.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.responses.ResetResponse
import com.ai.app.move.deskercise.usecases.ResetUseCase
import com.ai.app.move.deskercise.utils.PasswordValidator
import com.ai.app.move.deskercise.utils.SingleLiveEvent
import com.ai.app.move.deskercise.utils.passwordCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetViewModel(
    private val resetUseCase: ResetUseCase,
) : BaseViewModel() {

    private var email: String = ""
    private var confirmationCode: String = ""
    private var newPassword: String = ""
    private var confirmPassword: String = ""
    val resetLiveData = SingleLiveEvent<State<ResetResponse>>()
    val passwordValidatorLiveData = MutableLiveData<PasswordValidator>()
    val passwordIsMatchLiveData = MutableLiveData<Boolean>()

    fun updateEmail(value: String) {
        email = value
    }

    fun updateCode(value: String) {
        confirmationCode = value
    }

    fun updatePassword(value: String) {
        newPassword = value
        verifyPassword()
        passwordIsMatch()
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        passwordIsMatch()
    }

    fun reset() {
        if (!dataValidated()) {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    resetLiveData.postValue(State.Starting())
                    val result = resetUseCase(email, confirmationCode, newPassword)
                    Log.e("viewModelScope", "login: $result")
                    resetLiveData.postValue(State.Success(result))
                } catch (e: Exception) {
                    e.printStackTrace()
                    resetLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    /**
     * Updates the UI on whether the password check passes
     *
     * @see[CharSequence.passwordCheck]
     */
    private fun verifyPassword() {
        val pV = newPassword.passwordCheck()
        passwordValidatorLiveData.postValue(pV)
    }

    /**
     * Updates the UI on whether Password and Confirm Password are the same values
     */
    private fun passwordIsMatch() {
        val passwordIsMatch = newPassword == confirmPassword
        passwordIsMatchLiveData.postValue(passwordIsMatch)
    }

    private fun dataValidated(): Boolean {
        if (email.isEmpty()) {
            return false
        }

        return true
    }
}
