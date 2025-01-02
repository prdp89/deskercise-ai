package com.ai.app.move.deskercise.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.responses.SignUpResponse
import com.ai.app.move.deskercise.usecases.SignUpUseCase
import com.ai.app.move.deskercise.utils.PasswordValidator
import com.ai.app.move.deskercise.utils.SingleLiveEvent
import com.ai.app.move.deskercise.utils.isValidEmail
import com.ai.app.move.deskercise.utils.isValidPassword
import com.ai.app.move.deskercise.utils.passwordCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val signUpUseCase: SignUpUseCase,
) : BaseViewModel() {

    private var email: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""
    private var name: String = ""
    private var signUpCode: String = ""
    private var isAcceptTermAndCondition = false
    val signUpLiveData = SingleLiveEvent<State<SignUpResponse>>()
    val passwordValidatorLiveData = MutableLiveData<PasswordValidator>()
    val passwordIsMatchLiveData = MutableLiveData<Boolean>()
    val snackBarLiveData = SingleLiveEvent<Int>()

    fun updateEmail(value: String) {
        email = value
    }

    fun updatePassword(value: String) {
        password = value
        verifyPassword()
        passwordIsMatch()
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        passwordIsMatch()
    }

    /**
     * Updates the UI on whether the password check passes
     *
     * @see[CharSequence.passwordCheck]
     */
    private fun verifyPassword() {
        val pV = password.passwordCheck()
        passwordValidatorLiveData.postValue(pV)
    }

    /**
     * Updates the UI on whether Password and Confirm Password are the same values
     */
    private fun passwordIsMatch() {
        val passwordIsMatch = password == confirmPassword
        passwordIsMatchLiveData.postValue(passwordIsMatch)
    }

    fun updateName(value: String) {
        name = value
    }

    fun updateSignUpCode(value: String) {
        signUpCode = value
    }

    fun updateAcceptTerm(isAccept: Boolean) {
        isAcceptTermAndCondition = isAccept
    }

    private fun postSnackBar(@StringRes id: Int) {
        snackBarLiveData.postValue(id)
    }

    private fun dataValidated(): Boolean {
        if (email.isEmpty()) {
            postSnackBar(R.string.msg_required_email)
            return false
        }

        if (!email.isValidEmail()) {
            postSnackBar(R.string.msg_email_format)
            return false
        }
        if (password.isEmpty()) {
            postSnackBar(R.string.msg_required_password)
            return false
        }

        if (!password.isValidPassword()) {
            postSnackBar(R.string.msg_invalid_password)
            return false
        }

        if (password != confirmPassword) {
            postSnackBar(R.string.msg_incorrect_password)
            return false
        }

        if (name.isEmpty()) {
            postSnackBar(R.string.msg_required_name)
            return false
        }
        if (signUpCode.isEmpty()) {
            postSnackBar(R.string.msg_required_company_code)
            return false
        }

        if (!isAcceptTermAndCondition) {
            postSnackBar(R.string.msg_required_check_term_and_condition)
            return false
        }
        return true
    }

    fun signup() {
        if (!dataValidated()) {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    signUpLiveData.postValue(State.Starting())
                    val result = signUpUseCase(email, password, name, signUpCode)
                    signUpLiveData.postValue(State.Success(result))
                } catch (e: Exception) {
                    e.printStackTrace()
                    signUpLiveData.postValue(State.Error(e))
                }
            }
        }
    }
}
