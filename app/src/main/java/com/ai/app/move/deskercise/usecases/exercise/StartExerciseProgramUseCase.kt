package com.ai.app.move.deskercise.usecases.exercise

import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.network.repositories.ExerciseRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository
import com.ai.app.move.deskercise.network.responses.StartProgramResponse
import timber.log.Timber

class StartExerciseProgramUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository,
    private val appStorage: AppStorage,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(programId: Int, exerciseCount: Int, isRandom: Boolean): StartProgramResponse {
        val response = exerciseRepository.startProgram(programId, exerciseCount, isRandom)
        Timber.d(">> response=$response")
        appStorage.storeSessionId(response.id)
        val user = userRepository.getProfile()
        Timber.d(">> storeUser user=$user")
        userManager.storeUserInfo(user)
        return response
    }
}
