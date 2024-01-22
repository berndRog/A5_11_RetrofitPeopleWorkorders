package de.rogallab.mobile.ui.people

import de.rogallab.mobile.domain.entities.Person

data class PeopleUiState(
   val isLoading: Boolean = false,
   val isSuccessful: Boolean = false,
   val people: List<Person> = emptyList(),
   val failure: Throwable? = null
) {

   fun loading() = copy(
      isLoading = true,
      isSuccessful = false,
      people = emptyList<Person>(),
      failure = null
   )

   fun success(people: List<Person>) = copy(
      isLoading = false,
      isSuccessful = true,
      people = people,
      failure = null
   )

   fun failure(failure: Throwable) = copy(
      isLoading = false,
      isSuccessful = false,
      people = emptyList<Person>(),
      failure = failure
   )

}




