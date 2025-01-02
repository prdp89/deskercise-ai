package com.ai.app.move.deskercise.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.responses.ForgotResponse
import com.ai.app.move.deskercise.usecases.ForgetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgetViewModel(private val forgetUseCase: ForgetUseCase) : BaseViewModel() {

    private var email: String = ""
    val forgetLiveData = MutableLiveData<State<ForgotResponse>>()

    fun updateEmail(value: String) {
        email = value
    }

    fun forget() {
        if (!dataValidated()) {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    forgetLiveData.postValue(State.Starting())
                    val result = forgetUseCase(email)
                    // Log.e("viewModelScope", "login: $result")
                    forgetLiveData.postValue(State.Success(result))
                } catch (e: Exception) {
                    e.printStackTrace()
                    forgetLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    private fun dataValidated(): Boolean {
        if (email.isEmpty()) {
            return false
        }
        return true
    }
}