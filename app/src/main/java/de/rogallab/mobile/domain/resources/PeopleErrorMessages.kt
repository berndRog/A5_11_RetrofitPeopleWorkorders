package de.rogallab.mobile.domain.resources

import de.rogallab.mobile.R
import de.rogallab.mobile.domain.resources.ResourceProvider

class PeopleErrorMessages(
   resourceProvider: ResourceProvider
) {
   val firstNameTooShort = resourceProvider.getString(R.string.errorFirstNameTooShort) ?: ""
   val lastNameTooShort =  resourceProvider.getString(R.string.errorLastNameTooShort) ?: ""
   val firstNameTooLong = resourceProvider.getString(R.string.errorFirstNameTooLong) ?: ""
   val lastNameTooLong = resourceProvider.getString(R.string.errorLastNameTooShort) ?:""
   val emailInValid = resourceProvider.getString(R.string.errorEmail) ?: ""
   val phoneInValid = resourceProvider.getString(R.string.errorPhone) ?: ""
}