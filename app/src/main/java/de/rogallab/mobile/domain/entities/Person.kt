package de.rogallab.mobile.domain.entities

import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.getLocalOrRemoteImagePath
import java.util.UUID

data class Person (
   val firstName: String = "",
   val lastName: String = "",
   val email: String? = null,
   val phone:String? = null,
   // local image path
   val imagePath: String? = null,
   val remoteUriPath: String? = null,
   val id: UUID = UUID.randomUUID(),
   // One-to-one relation Person --> Image[0..1]
   var imageId: UUID? = null,
   var image: Image? = null,
   // One-to-many Person --> Workorder [0..*]
   val workorders: MutableList<Workorder> = mutableListOf(),
) {
   fun asString() : String = "$firstName $lastName ${id.as8()}"

   fun getActualImagePath(): String? =
      getLocalOrRemoteImagePath(imagePath, remoteUriPath)

   fun addWorkorder(workorder: Workorder) {
      workorder.state = WorkState.Assigned
      workorder.person = this
      workorder.personId = this.id
      workorders.add(workorder)
   }

   fun removeWorkorder(workorder: Workorder) {
      workorders.remove(workorder)
      workorder.state = WorkState.Default
      workorder.person = null
      workorder.personId = null
   }

}