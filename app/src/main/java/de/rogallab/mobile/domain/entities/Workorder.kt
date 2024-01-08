package de.rogallab.mobile.domain.entities
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.domain.utilities.zonedDateTimeString
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

data class Workorder(
   var title:String = "",
   var description: String = "",
   var created: ZonedDateTime = zonedDateTimeNow(),
   var started: ZonedDateTime = zonedDateTimeNow(),
   var completed: ZonedDateTime = zonedDateTimeNow(),
   var duration: Duration = Duration.ofMillis(0),
   var remark: String = "",
   var imagePath: String? = null,
   var state: WorkState = WorkState.Default,
   val id: UUID = UUID.randomUUID(),
   // Workorder -> Person [0..1]
   var person: Person? = null,
   var personId: UUID? = null,
   // Workorder -> Addrss [0..1]  embedded
   var address: Address? = null
) {
   fun asString() : String = "${zonedDateTimeString(created)} $title ${id.as8()}"
}