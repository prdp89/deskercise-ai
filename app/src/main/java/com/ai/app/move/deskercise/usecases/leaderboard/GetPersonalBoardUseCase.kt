package com.ai.app.move.deskercise.usecases.leaderboard

import com.ai.app.move.deskercise.network.repositories.LeaderBoardRepository
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse

class GetPersonalBoardUseCase(private val leaderBoardRepository: LeaderBoardRepository) {
    suspend operator fun invoke(filter: String): List<PersonalBoardResponse> {
        return leaderBoardRepository.getPersonalBoard(filter)
    }
}
