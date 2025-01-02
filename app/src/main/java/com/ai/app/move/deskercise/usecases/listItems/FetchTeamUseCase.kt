package com.ai.app.move.deskercise.usecases.listItems

import com.ai.app.move.deskercise.data.Team
import com.ai.app.move.deskercise.network.repositories.CompanyRepository

class FetchTeamUseCase(private val companyRepository: CompanyRepository) {

    suspend operator fun invoke(id: Int): List<Team> {
        return companyRepository.getTeam(id)
    }
}
