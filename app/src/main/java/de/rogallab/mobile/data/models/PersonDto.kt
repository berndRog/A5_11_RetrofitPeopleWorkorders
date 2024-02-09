package de.rogallab.mobile.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rogallab.mobile.domain.utilities.as8
import java.util.UUID

@Entity(tableName = "people")
data class PersonDto (
   val firstName: String = "",
   val lastName: String = "",
   var email: String? = null,
   var phone:String? = null,
   var imagePath: String? = null,
   val remoteUriPath: String? = null,
   @PrimaryKey
   val id: UUID = UUID.randomUUID(),
   // one to one relation
   var imageId: UUID? = null
) {
   fun asString() : String = "$firstName $lastName ${id.as8()}"
}