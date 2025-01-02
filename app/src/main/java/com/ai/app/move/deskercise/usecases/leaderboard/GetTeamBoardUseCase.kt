package com.ai.app.move.deskercise.usecases.leaderboard

import com.ai.app.move.deskercise.network.repositories.LeaderBoardRepository
import com.ai.app.move.deskercise.network.responses.TeamBoardResponse

class GetTeamBoardUseCase(private val leaderBoardRepository: LeaderBoardRepository) {
    suspend operator fun invoke(filter: String): List<TeamBoardResponse> {
        return leaderBoardRepository.getTeamBoard(filter)
            .sortedByDescending { it.score }
    }
}
