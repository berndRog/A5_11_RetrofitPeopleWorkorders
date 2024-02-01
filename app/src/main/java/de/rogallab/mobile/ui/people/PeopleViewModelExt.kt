package de.rogallab.mobile.ui.people

import android.util.Patterns
import android.util.Patterns.PHONE

fun PeopleViewModel.validateAndNavigate(
   isInput:Boolean,
   charMin: Int = 2,
   charMax: Int = 16
) {
   // firstName or lastName too short
   if (personStateValue.firstName.isEmpty() || personStateValue.firstName.length < charMin) {
      val message = errorMessages.firstNameTooShort
      showOnError(message)
      return
   }
   else if (personStateValue.lastName.isEmpty() || personStateValue.lastName.length < charMin) {
      val message = errorMessages.lastNameTooShort
      showOnError(message)
      return
   }
   // firstName or lastName too long
   else if (personStateValue.firstName.length > charMax) {
      val message = errorMessages.firstNameTooLong
      showOnError(message)
      return
   }
   else if (personStateValue.lastName.length > charMax) {
      val message = errorMessages.lastNameTooLong
      showOnError(message)
      return
   }
   else if(personStateValue.email != null &&
           ! Patterns.EMAIL_ADDRESS.matcher(personStateValue.email!!).matches()) {
      val message = errorMessages.emailInValid
      this.showOnError(message)
      return
   }
   else if(personStateValue.phone != null &&
           ! PHONE.matcher(personStateValue.phone!!).matches()) {
      val message: String = errorMessages.phoneInValid
      this.showOnError(message)
      return
   }
   else {
      if (isInput) this.add()
      else         this.update()
   }
}