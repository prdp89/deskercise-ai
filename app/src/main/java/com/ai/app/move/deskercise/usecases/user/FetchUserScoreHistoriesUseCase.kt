package com.ai.app.move.deskercise.usecases.user

import com.ai.app.move.deskercise.data.Score
import com.ai.app.move.deskercise.network.repositories.UserRepository

class FetchUserScoreHistoriesUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): List<Score> {
        // we can store User Info in UserManager to caching data.
        return userRepository.getScoreHistories().take(10)
    }
}
