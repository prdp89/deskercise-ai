package com.ai.app.move.deskercise.usecases.team

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.repositories.TeamRepository

class FetchTeamUsersUseCase(
    private val teamRepository: TeamRepository,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(): List<User> {
        val user = userManager.getUserInfo() ?: throw Exception("No User")
        val team = user.team ?: throw Exception("No Team")
        return teamRepository.getTeamMembers(team.id).take(10)
    }
}
