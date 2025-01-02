package com.ai.app.move.deskercise.ui.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.Score
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.ScoreConditionResponse
import com.ai.app.move.deskercise.usecases.exercise.FetchScoreConditionUseCase
import com.ai.app.move.deskercise.usecases.team.FetchTeamUsersUseCase
import com.ai.app.move.deskercise.usecases.user.FetchUserScoreHistoriesUseCase
import com.ai.app.move.deskercise.usecases.user.GetUserProfileUseCase
import com.ai.app.move.deskercise.usecases.user.UpdateUserAvatarUseCase
import kotlinx.coroutines.launch
import timber.log.Timber

class MyProfileViewModel(
    private val fetchUserScoreHistoriesUseCase: FetchUserScoreHistoriesUseCase,
    private val userManager: UserManager,
    private val fetchTeamUsersUseCase: FetchTeamUsersUseCase,
    private val fetchScoreConditionUseCase: FetchScoreConditionUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) :
    BaseViewModel() {

    val historiesLiveData = MutableLiveData<State<List<Score>>>()
    val teamUsersLiveData = MutableLiveData<State<List<User>>>()
    val userInfoLiveData = MutableLiveData<State<User>>()
    val scoreConditionLiveData = MutableLiveData<State<ScoreConditionResponse>>()
    val updateAvatarLiveData = MutableLiveData<State<User>>()

    fun changeAvatar(file: Uri) {
        Timber.d(">> file $file")
        viewModelScope.launch {
            updateAvatarLiveData.postValue(State.Starting())
            try {
                val user = updateUserAvatarUseCase.invoke(file)
                updateAvatarLiveData.postValue(State.Success(user))
            } catch (e: Exception) {
                updateAvatarLiveData.postValue(State.Error(e))
            }
        }
    }
    fun getScoreCondition() {
        viewModelScope.launch {
            scoreConditionLiveData.postValue(State.Starting())
            try {
                val condition = fetchScoreConditionUseCase.invoke()
                scoreConditionLiveData.postValue(State.Success(condition))
            } catch (e: Exception) {
                scoreConditionLiveData.postValue(State.Error(e))
            }
        }
    }

    fun getUserHistories() {
        viewModelScope.launch {
            historiesLiveData.postValue(State.Starting())
            try {
                val scores = fetchUserScoreHistoriesUseCase.invoke()
                historiesLiveData.postValue(State.Success(scores))
            } catch (e: Exception) {
                historiesLiveData.postValue(State.Error(e))
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            try {
                val user = userManager.getUserInfo()
                user?.let { userInfoLiveData.postValue(State.Success(it)) }
                val cloudUser = getUserProfileUseCase.invoke()
                userInfoLiveData.postValue(State.Success(cloudUser))
            } catch (e: Exception) {
                e.printStackTrace()
                userInfoLiveData.postValue(State.Error(e))
            }
        }
    }

    fun getTeamUsers() {
        viewModelScope.launch {
            teamUsersLiveData.postValue(State.Starting())
            try {
                val items = fetchTeamUsersUseCase.invoke()
                teamUsersLiveData.postValue(State.Success(items))
            } catch (e: Exception) {
                teamUsersLiveData.postValue(State.Error(e))
            }
        }
    }
}
