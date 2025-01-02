package com.ai.app.move.deskercise.usecases.exercise

import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.Exercise
import com.ai.app.move.deskercise.network.repositories.ExerciseRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository
import timber.log.Timber

class SendExerciseResultUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository,
    private val appStorage: AppStorage,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(programId: Int, exercises: List<Exercise>): Int {
        val sessionId = appStorage.getSessionId()
        Timber.d(">> sessionId=$sessionId")
        val point = exerciseRepository.sendResult(programId, sessionId, exercises)
        val user = userRepository.getProfile()
        Timber.d(">> storeUser user=$user")
        userManager.storeUserInfo(user)
        return point
    }
}
