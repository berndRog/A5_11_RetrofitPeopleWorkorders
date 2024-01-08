package de.rogallab.mobile.domain.entities

data class Address (
   val street: String,
   val number: String,
   val postal: String,
   val city:String
) {
   fun asString() : String = "$street $number, $postal $city"
}