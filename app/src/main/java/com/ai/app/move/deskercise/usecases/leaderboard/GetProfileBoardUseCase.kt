package com.ai.app.move.deskercise.usecases.leaderboard

import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.repositories.LeaderBoardRepository

class GetProfileBoardUseCase(private val leaderBoardRepository: LeaderBoardRepository) {
    suspend operator fun invoke(filter: String, type: String): User {
        return leaderBoardRepository.getProfileBoard(filter, type)
    }
}