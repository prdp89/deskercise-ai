package com.ai.app.move.deskercise.ui.myPoints

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.AvailableReward
import com.ai.app.move.deskercise.usecases.reward.FetchAvailableRewardListUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MyPointsViewModel(
    private val fetchAvailableRewardListUseCase: FetchAvailableRewardListUseCase
) : BaseViewModel() {

    val availableRewardsLiveData = MutableLiveData<State<List<AvailableReward>>>()
    private var page = 1
    private var fetchRewardJob: Job? = null

    fun fetchAvailableReward(isForceUpdate: Boolean = true) {
        if (fetchRewardJob != null) {
            if (fetchRewardJob?.isCompleted != true) {
                return
            }
        }
        fetchRewardJob = viewModelScope.launch {
            availableRewardsLiveData.postValue(State.Starting())
            if (isForceUpdate) {
                page = 1
            }
            try {
                val items = fetchAvailableRewardListUseCase.invoke(page)
                page++
                availableRewardsLiveData.postValue(State.Success(items.availableRewards))
            } catch (e: Exception) {
                availableRewardsLiveData.postValue(State.Error(e))
            }
        }
        fetchRewardJob?.start()
    }

}