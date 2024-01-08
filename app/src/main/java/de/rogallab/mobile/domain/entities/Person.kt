package de.rogallab.mobile.domain.entities

import de.rogallab.mobile.domain.utilities.as8
import java.util.UUID

data class Person (
   var firstName: String = "",
   var lastName: String = "",
   var email: String? = null,
   var phone:String? = null,
   var imagePath: String? = null,
   val id: UUID = UUID.randomUUID(),
   // Relation Person --> Workorder [0..*]
   val workorders: MutableList<Workorder> = mutableListOf(),
   // Embedded relation Person -> Address[0..1]
   var address: Address? = null
) {
   fun asString() : String = "$firstName $lastName ${id.as8()}"
}