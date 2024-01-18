package de.rogallab.mobile.ui.people

import de.rogallab.mobile.domain.entities.Person

data class PeopleUiState(
   val isLoading: Boolean = false,
   val isRefreshing: Boolean = false,
   val isSuccessful: Boolean = false,
   val people: List<Person> = emptyList(),
   val failure: Throwable? = null
) {

   fun loading(): PeopleUiState {
      return copy(
         isLoading = true,
         isRefreshing = false,
         isSuccessful = false,
         people = emptyList(),
         failure = null
      )
   }

   fun success(people: List<Person>): PeopleUiState {
      return copy(
         isLoading = false,
         isRefreshing = false,
         isSuccessful = true,
         people = people,
         failure = null
      )
   }
   fun failure(throwable: Throwable): PeopleUiState {
      return copy(
         isLoading = false,
         isRefreshing = false,
         isSuccessful = false,
         people = emptyList(),
         failure = throwable
      )
   }

}




