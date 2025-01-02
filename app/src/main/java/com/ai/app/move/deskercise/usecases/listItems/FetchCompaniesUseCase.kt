package com.ai.app.move.deskercise.usecases.listItems

import com.ai.app.move.deskercise.data.Company
import com.ai.app.move.deskercise.network.repositories.CompanyRepository

class FetchCompaniesUseCase(private val companyRepository: CompanyRepository) {
    suspend operator fun invoke(): List<Company> {
        // we can store User Info in UserManager to caching data.
        return companyRepository.getCompany()
    }
}
