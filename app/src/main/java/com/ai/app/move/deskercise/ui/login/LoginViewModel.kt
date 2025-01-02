package com.ai.app.move.deskercise.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.responses.LoginResponse
import com.ai.app.move.deskercise.usecases.LoginUseCase
import com.ai.app.move.deskercise.utils.isValidEmail
import com.ai.app.move.deskercise.utils.isValidPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginUseCase: LoginUseCase) : BaseViewModel() {

    private var email: String = ""
    private var password: String = ""

    val loginLiveData = MutableLiveData<State<LoginResponse>>()

    fun updateEmail(value: String) {
        email = value
    }

    fun updatePassword(value: String) {
        password = value
    }

    fun login() {
        if (!dataValidated()) {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    loginLiveData.postValue(State.Starting())
                    val result = loginUseCase(email, password)
                    Log.e("viewModelScope", "login: $result")
                    loginLiveData.postValue(State.Success(result))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loginLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    private fun dataValidated(): Boolean {
        if (email.isEmpty() || !email.isValidEmail()) {
            publishMessage(R.string.msg_email_format)
            return false
        }
        if (password.isEmpty() || !password.isValidPassword()) {
            publishMessage(R.string.msg_incorrect_password)
            return false
        }

        return true
    }
}
