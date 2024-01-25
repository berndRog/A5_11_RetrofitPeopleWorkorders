package de.rogallab.mobile.ui.people

import de.rogallab.mobile.domain.entities.Person

data class PeopleUiState(
   val isLoading: Boolean = false,
   val isSuccessful: Boolean = false,
   val people: List<Person> = emptyList()
)




