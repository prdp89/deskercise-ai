package com.ai.app.move.deskercise.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.usecases.RenewAccessTokenUseCase
import com.ai.app.move.deskercise.usecases.exercise.FetchScoreConditionUseCase
import com.ai.app.move.deskercise.usecases.user.GetUserProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val renewAccessTokenUseCase: RenewAccessTokenUseCase,
    private val fetchScoreConditionUseCase: FetchScoreConditionUseCase,
    private val userManager: UserManager,
) : BaseViewModel() {

    val userLoggedLiveData = MutableLiveData<Boolean>()

    fun checkLogin() {
        CoroutineScope(Dispatchers.IO).launch {
            val token = userManager.getAccessToken()
            if (token.isEmpty()) { // user is not logged
                delay(2000) // wait 2 secs
                userLoggedLiveData.postValue(false)
                return@launch
            }
            try {
                getUserProfileUseCase.invoke()
                delay(500)
                fetchScoreConditionUseCase() // load score condition
                userLoggedLiveData.postValue(true)
            } catch (e: Exception) {
                renewAccessToken()
            }
        }
    }

    private fun renewAccessToken() {
        viewModelScope.launch {
            try {
                renewAccessTokenUseCase.invoke()
                getUserProfileUseCase.invoke()
                fetchScoreConditionUseCase() // load score condition
                userLoggedLiveData.postValue(true)
            } catch (e: Exception) {
                userLoggedLiveData.postValue(false)
            }
        }
    }
}
