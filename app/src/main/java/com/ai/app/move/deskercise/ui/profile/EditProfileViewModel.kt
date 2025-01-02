package com.ai.app.move.deskercise.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.Company
import com.ai.app.move.deskercise.data.Team
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.LogoutResponse
import com.ai.app.move.deskercise.usecases.DeleteFcmTokenToServerUseCase
import com.ai.app.move.deskercise.usecases.LogoutUseCase
import com.ai.app.move.deskercise.usecases.listItems.FetchCompaniesUseCase
import com.ai.app.move.deskercise.usecases.listItems.FetchTeamUseCase
import com.ai.app.move.deskercise.usecases.user.RequestDeleteAccountUseCase
import com.ai.app.move.deskercise.usecases.user.UpdateUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class EditProfileViewModel(
    private val fetchCompaniesUseCase: FetchCompaniesUseCase,
    private val fetchTeamUseCase: FetchTeamUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val requestDeleteAccountUseCase: RequestDeleteAccountUseCase,
    private val logoutUserCase: LogoutUseCase,
    private val deleteFcmTokenToServerUseCase: DeleteFcmTokenToServerUseCase,
    private val userManager: UserManager,
) :
    BaseViewModel() {
    val userLiveData = MutableLiveData<State<User>>()
    val updateUserLiveData = MutableLiveData<State<User>>()
    val companyLiveData = MutableLiveData<State<List<Company>>>()
    val teamLiveData = MutableLiveData<State<List<Team>>>()
    val deleteAccountLiveData = MutableLiveData<State<Boolean>>()
    val logoutLiveData = MutableLiveData<State<LogoutResponse?>>()

    lateinit var currentUser: User

    fun getCurrentUserInfo() {
        userManager.getUserInfo()?.let {
            currentUser = it
            userLiveData.postValue(State.Success(currentUser))
        } ?: kotlin.run {
            userLiveData.postValue(
                State.Error(
                    Exception("No User"),
                ),
            )
        }
    }

    fun updateUserProfile(value: User) {
        currentUser = value
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    logoutLiveData.postValue(State.Starting())
                    val deleteFcmToken = deleteFcmTokenToServerUseCase.invoke()
                    Timber.d(">>deleteFcmToken=$deleteFcmToken")
                    val result = logoutUserCase()
                    logoutLiveData.postValue(State.Success(result))
                } catch (e: Exception) {
                    e.printStackTrace()
                    logoutLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    fun getCompany() {
        if (currentUser.signUpCode.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                val companies = fetchCompaniesUseCase.invoke()
                companyLiveData.postValue(State.Success(companies))
            } catch (e: Exception) {
                e.printStackTrace()
                companyLiveData.postValue(State.Error(e))
            }
        }
    }

    fun sendProfile() {
        if (!validateUserData()) {
            return
        }
        viewModelScope.launch {
            try {
                updateUserLiveData.postValue(State.Starting())
                val user = updateUserProfileUseCase.invoke(currentUser)
                publishMessage(R.string.update_profile_successful)
                updateUserLiveData.postValue(State.Success(user))
            } catch (e: Exception) {
                e.printStackTrace()
                updateUserLiveData.postValue(State.Error(e))
            }
        }
    }

    private fun validateUserData(): Boolean {
        if (currentUser.name.isEmpty()) {
            publishMessage(R.string.msg_required_name)
            return false
        }

        if (currentUser.dob.isNullOrEmpty()) {
            publishMessage(R.string.msg_required_dob)
            return false
        }

        if (currentUser.phoneNumber.isNullOrEmpty()) {
            publishMessage(R.string.msg_required_phone_number)
            return false
        }

        return true
    }

    fun getTeam(companyId: Int) {
        if (currentUser.signUpCode.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                val teams = fetchTeamUseCase.invoke(companyId)
                teamLiveData.postValue(State.Success(teams))
            } catch (e: Exception) {
                teamLiveData.postValue(State.Error(e))
            }
        }
    }

    fun requestDeleteAccount() {
        viewModelScope.launch {
            try {
                deleteAccountLiveData.postValue(State.Starting())
                requestDeleteAccountUseCase.invoke()
                deleteAccountLiveData.postValue(State.Success(true))
            } catch (e: Exception) {
                Timber.e(e)
                deleteAccountLiveData.postValue(State.Error(e))
            }
        }
    }
}
