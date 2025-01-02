package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.base.BaseViewModel
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.Exercise
import com.ai.app.move.deskercise.network.responses.LogoutResponse
import com.ai.app.move.deskercise.network.responses.StartProgramResponse
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroupProgramModel
import com.ai.app.move.deskercise.usecases.DeleteFcmTokenToServerUseCase
import com.ai.app.move.deskercise.usecases.LogoutUseCase
import com.ai.app.move.deskercise.usecases.exercise.SendExerciseResultUseCase
import com.ai.app.move.deskercise.usecases.exercise.StartExerciseProgramUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class StartProgramViewModel(
    private val startExerciseUseCase: StartExerciseProgramUseCase,
    private val sendResultsUseCase: SendExerciseResultUseCase,
    private val logoutUserCase: LogoutUseCase,
    private val appStorage: AppStorage,
    private val deleteFcmTokenToServerUseCase: DeleteFcmTokenToServerUseCase,
) : BaseViewModel() {
    private var programId: Int = 0
    private var exerciseCount: Int = 0
    lateinit var programModel: ExerciseGroupProgramModel
    private var exercises = arrayListOf<Exercise>()
    private var job: Job? = null

    val startExerciseLiveData = MutableLiveData<State<StartProgramResponse?>>()
    val sendResultsLiveData = MutableLiveData<State<Int>>()
    val logoutLiveData = MutableLiveData<State<LogoutResponse?>>()
    val pointLiveData = MutableLiveData<Int>()
    private var isAddPoints = false

    fun updateGoodStep(value: Int) {
        if (value == 0) return
        val goodCondition = appStorage.getGoodReps()
        appStorage.cacheTotalPoint += goodCondition
        pointLiveData.postValue(appStorage.cacheTotalPoint)
    }

    fun updateBadStep(value: Int) {
        if (value == 0) return
        val badCondition = appStorage.getBadReps()
        appStorage.cacheTotalPoint += badCondition
        pointLiveData.postValue(appStorage.cacheTotalPoint)
    }

    fun updateExercises(value: List<Exercise>) {
        exercises.clear()
        exercises.addAll(value)
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

    fun updateProgramID(value: Int) {
        exerciseCount = value
        val programIDMap = mapOf(4 to 1, 7 to 2, 10 to 3)
        programId = programIDMap[value]!!
    }

    fun updateProgramModel(programModel: ExerciseGroupProgramModel) {
        this.programModel = programModel
    }

    fun startProgram(isRandom: Boolean = false) {
        if (job?.isActive == true) return

        appStorage.cacheTotalPoint = 0
        job = viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    startExerciseLiveData.postValue(State.Starting())
                    val result = startExerciseUseCase.invoke(programId, exerciseCount, isRandom)
                    startExerciseLiveData.postValue(State.Success(result))
                    isAddPoints = result.isAddedPoint
                    appStorage.cacheTotalPoint += result.score
                    pointLiveData.postValue(appStorage.cacheTotalPoint)
                } catch (e: Exception) {
                    e.printStackTrace()
                    startExerciseLiveData.postValue(State.Error(e))
                }
            }
        }
    }

    fun sendResults() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    sendResultsLiveData.postValue(State.Starting())
                    val result = sendResultsUseCase.invoke(programId, exercises)
                    sendResultsLiveData.postValue(State.Success(result))
                    appStorage.cacheTotalPoint = result
                } catch (e: Exception) {
                    e.printStackTrace()
                    sendResultsLiveData.postValue(State.Error(e))
                }
            }
        }
    }
}
