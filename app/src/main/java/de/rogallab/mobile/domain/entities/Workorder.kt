package de.rogallab.mobile.domain.entities
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.zonedDtMin
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

data class Workorder(
   var title:String = "",
   var description: String = "",
   var imagePath: String? = null,
   var state: WorkState = WorkState.Default,
   var created: ZonedDateTime = zonedDtMin,
   var started: ZonedDateTime = zonedDtMin,
   var completed: ZonedDateTime = zonedDtMin,
   var duration:Duration = Duration.ofMillis(0),
   var remark: String = "",
   val id: UUID = UUID.randomUUID(),
   // Workorder -> Person [0..1]
   var personId: UUID? = null,
   var person: Person? = null,
) {
   fun asString() : String = "${zonedDateTimeString(created)} $title ${id.as8()}"
}