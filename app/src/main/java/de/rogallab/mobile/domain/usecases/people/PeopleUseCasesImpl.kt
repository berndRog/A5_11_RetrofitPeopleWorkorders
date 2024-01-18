package de.rogallab.mobile.domain.usecases.people

import de.rogallab.mobile.domain.IPeopleUseCases
import javax.inject.Inject

data class PeopleUseCasesImpl @Inject constructor(
   override val fetchPeople: FetchPeople,
) : IPeopleUseCases