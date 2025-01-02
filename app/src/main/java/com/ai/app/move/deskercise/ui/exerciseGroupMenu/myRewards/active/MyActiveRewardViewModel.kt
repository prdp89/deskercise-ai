package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.active

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.RedeemRewards
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.past.RedeemRewardType
import com.ai.app.move.deskercise.usecases.reward.FetchRedeemRewardListUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MyActiveRewardViewModel(
    private val fetchRedeemRewardListUseCase: FetchRedeemRewardListUseCase,
) : BaseViewModel() {
    val redeemRewardsLiveData = MutableLiveData<State<List<RedeemRewards>>>()
    private var page = 1
    private var fetchRewardJob: Job? = null

    fun fetchRedeemReward() {
        if (fetchRewardJob != null) {
            if (fetchRewardJob?.isCompleted != true) {
                return
            }
        }
        fetchRewardJob = viewModelScope.launch {
            redeemRewardsLiveData.postValue(State.Starting())
            try {
                val items = fetchRedeemRewardListUseCase.invoke(page, RedeemRewardType.ACTIVE.name)
                page++
                redeemRewardsLiveData.postValue(State.Success(items))
            } catch (e: Exception) {
                redeemRewardsLiveData.postValue(State.Error(e))
            }
        }
    }
}