package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.services.TeamService

interface TeamRepository {
    suspend fun getTeamMembers(id: Int): List<User>
}

class TeamRepositoryImpl(
    private val teamService: TeamService,
) : TeamRepository {
    override suspend fun getTeamMembers(id: Int): List<User> {
        return teamService.getTeamUsers(id).data ?: throw Exception("Unknown error")
    }
}
