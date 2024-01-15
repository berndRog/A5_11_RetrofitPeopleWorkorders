package de.rogallab.mobile.ui.people

import de.rogallab.mobile.domain.entities.Person

data class PeopleListUiState(
   val people: List<Person> = emptyList(),
   val isLoading: Boolean = false,
   val isRefreshing: Boolean = false,
   val error: String? = "",
   var upHandler: Boolean = true,   // up   navigation = true: default operation
   var backHandler: Boolean = false // back navigation = true: abort operation
)




