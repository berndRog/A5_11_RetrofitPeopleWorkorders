package de.rogallab.mobile.ui.base

fun isNameTooShort(name: String, charMin: Int): Boolean =
   name.isEmpty() || name.length < charMin

fun isNameTooLong(name: String, charMax: Int): Boolean =
   name.length > charMax


fun validateName(
   name: String,
   charMin: Int,
   charMax: Int,
   errorTooShort: String,
   errorTooLong: String
): Pair<Boolean, String> {

   if (isNameTooShort(name, charMin)) {
      return Pair(true, errorTooShort)
   } else if (isNameTooLong(name, charMax)) {
      return Pair(true, errorTooLong)
   } else {
      return Pair(false, "")
   }
}

fun validateNameTooShort(
   name: String,
   charMin: Int,
   errorTooShort: String,
): Pair<Boolean, String> {
   if (isNameTooShort(name, charMin)) {
      return Pair(true, errorTooShort)
   } else {
      return Pair(false, "")
   }
}

fun validateNameTooLong(
   name: String,
   charMax: Int,
   errorTooLong: String,
): Pair<Boolean, String> {
   if (isNameTooLong(name, charMax)) {
      return Pair(true, errorTooLong)
   } else {
      return Pair(false, "")
   }
}


fun validateEmail(email: String?, errorEmail: String): Pair<Boolean, String> {
   email?.let {
      when (android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
         true -> return Pair(false, "") // email ok
         false -> return Pair(true, errorEmail) // email with an error
      }
   } ?: return Pair(false, "")
}

fun validatePhone(phone: String?, errorPhone: String): Pair<Boolean, String> {
   phone?.let {
      when (android.util.Patterns.PHONE.matcher(it).matches()) {
         true -> return Pair(false,"")   // email ok
         false -> return Pair(true,errorPhone)   // email with an error
      }
   } ?: return Pair(false, "")
}
