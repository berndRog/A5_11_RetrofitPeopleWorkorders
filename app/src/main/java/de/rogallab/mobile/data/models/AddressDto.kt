package de.rogallab.mobile.data.models

import androidx.room.Entity

@Entity(tableName = "addresses")
data class AddressDto (
   val street: String,
   val number: String,
   val postal: String,
   val city:String,
) {
   fun asString() : String = "$street $number, $postal $city"
}