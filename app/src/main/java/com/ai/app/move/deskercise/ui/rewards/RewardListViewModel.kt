package com.ai.app.move.deskercise.ui.rewards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.usecases.reward.FetchRewardListUseCase
import com.ai.app.move.deskercise.usecases.reward.RedeemRewardUseCase
import com.ai.app.move.deskercise.usecases.reward.RedemptionsRewardUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RewardListViewModel(
    private val fetchRewardListUseCase: FetchRewardListUseCase,
    private val redeemRewardUseCase: RedeemRewardUseCase,
    private val redemptionsRewardUseCase: RedemptionsRewardUseCase
) : BaseViewModel() {

    val rewardsLiveData = MutableLiveData<State<List<Reward>>>()
    val redeemRewardLiveData = MutableLiveData<State<Reward>>()
    private var page = 1
    private var fetchRewardJob: Job? = null

    fun fetchReward(clearData: Boolean = false) {
        if (fetchRewardJob != null) {
            if (fetchRewardJob?.isCompleted != true) {
                return
            }
        }
        fetchRewardJob = viewModelScope.launch {
            rewardsLiveData.postValue(State.Starting())
            try {
                val items = fetchRewardListUseCase.invoke(page)
                page++
                rewardsLiveData.postValue(State.Success(items))
            } catch (e: Exception) {
                rewardsLiveData.postValue(State.Error(e))
            }
        }
        fetchRewardJob?.start()
    }

    fun redeemReward(reward: Reward) {
        viewModelScope.launch {
            redeemRewardLiveData.postValue(State.Starting())
            try {
                redemptionsRewardUseCase.invoke(reward.id)
                redeemRewardLiveData.postValue(State.Success(reward))
            } catch (e: Exception) {
                redeemRewardLiveData.postValue(State.Error(e))
            }
        }
    }
}
