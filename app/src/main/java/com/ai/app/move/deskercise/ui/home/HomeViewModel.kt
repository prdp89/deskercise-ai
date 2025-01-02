package com.ai.app.move.deskercise.ui.home

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.usecases.UpdateFcmTokenToServerUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val updateFcmTokenToServerUseCase: UpdateFcmTokenToServerUseCase,
) : BaseViewModel() {

    fun updateFcmToken() {
        viewModelScope.launch {
            try {
                updateFcmTokenToServerUseCase.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}