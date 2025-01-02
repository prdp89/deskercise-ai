package com.ai.app.move.deskercise.ui.leaderboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse
import com.ai.app.move.deskercise.network.responses.TeamBoardResponse
import com.ai.app.move.deskercise.usecases.leaderboard.GetPersonalBoardUseCase
import com.ai.app.move.deskercise.usecases.leaderboard.GetProfileBoardUseCase
import com.ai.app.move.deskercise.usecases.leaderboard.GetTeamBoardUseCase
import kotlinx.coroutines.launch

class LeaderBoardViewModel(
    private val getPersonalBoardUseCase: GetPersonalBoardUseCase,
    private val getTeamBoardUseCase: GetTeamBoardUseCase,
    private val getProfileBoardUseCase: GetProfileBoardUseCase
) : BaseViewModel() {
    private var filterValue = ""
    private var boardOption = ""
    val personalBoardLiveData = MutableLiveData<State<List<PersonalBoardResponse>>>()
    val teamBoardLiveData = MutableLiveData<State<List<TeamBoardResponse>>>()
    val profileBoardLiveData = MutableLiveData<State<User>>()

    fun updateFilterValue(filter: String) {
        this.filterValue = filter
    }

    fun updateBoardOption(option: String) {
        this.boardOption = option
    }

    fun requestUpdateData() {
        when (boardOption) {
            "Individual" -> {
                getPersonalBoard()
                getProfileBoard("personal")
            }
            "Team" -> {
                getTeamBoard()
                getProfileBoard("team")
            }
        }
    }

    private fun getPersonalBoard() {
        viewModelScope.launch {
            try {
                personalBoardLiveData.postValue(State.Starting())
                val users = getPersonalBoardUseCase.invoke(filterValue)
                personalBoardLiveData.postValue(State.Success(users))
            } catch (e: Exception) {
                personalBoardLiveData.postValue(State.Error(e))
            }
        }
    }

    private fun getTeamBoard() {
        viewModelScope.launch {
            try {
                teamBoardLiveData.postValue(State.Starting())
                val teams = getTeamBoardUseCase.invoke(filterValue)
                teamBoardLiveData.postValue(State.Success(teams))
            } catch (e: Exception) {
                teamBoardLiveData.postValue(State.Error(e))
            }
        }
    }

    private fun getProfileBoard(typeProfile: String) {
        viewModelScope.launch {
            try {
                profileBoardLiveData.postValue(State.Starting())
                val user = getProfileBoardUseCase.invoke(filterValue, typeProfile)
                profileBoardLiveData.postValue(State.Success(user))
            } catch (e: Exception) {
                profileBoardLiveData.postValue(State.Error(e))
            }
        }
    }
}
