package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.Company
import com.ai.app.move.deskercise.data.Team
import com.ai.app.move.deskercise.network.services.CompanyService

interface CompanyRepository {
    suspend fun getCompany(): List<Company>

    suspend fun getTeam(id: Int): List<Team>
}

class CompanyRepositoryImpl(private val companyService: CompanyService) : CompanyRepository {

    override suspend fun getCompany(): List<Company> {
        return companyService.getCompany().data ?: throw Exception("Unknown error")
    }

    override suspend fun getTeam(id: Int): List<Team> {
        return companyService.getTeams(id).data ?: throw Exception("Unknown error")
    }
}
