package com.ai.app.move.deskercise.ui.rewards.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.usecases.reward.GetRewardDetailUseCase
import com.ai.app.move.deskercise.usecases.reward.RedemptionsRewardUseCase
import kotlinx.coroutines.launch

class RewardDetailViewModel(
    private val getRewardDetailUseCase: GetRewardDetailUseCase,
    private val redemptionsRewardUseCase: RedemptionsRewardUseCase
) : BaseViewModel() {

    val rewardDetailLiveData = MutableLiveData<State<RewardDetail>>()
    val redeemRewardLiveData = MutableLiveData<State<String>>()

    fun getRewardDetail(rewardId: Int) {
        viewModelScope.launch {
            rewardDetailLiveData.postValue(State.Starting())
            try {
                val item = getRewardDetailUseCase.invoke(rewardId)
                rewardDetailLiveData.postValue(State.Success(item))
            } catch (e: Exception) {
                rewardDetailLiveData.postValue(State.Error(e))
            }
        }
    }

    fun redeemReward(rewardId: Int) {
        viewModelScope.launch {
            redeemRewardLiveData.postValue(State.Starting())
            try {
                redemptionsRewardUseCase.invoke(rewardId)
                redeemRewardLiveData.postValue(State.Success("Succeed"))
            } catch (e: Exception) {
                redeemRewardLiveData.postValue(State.Error(e))
            }
        }
    }
}