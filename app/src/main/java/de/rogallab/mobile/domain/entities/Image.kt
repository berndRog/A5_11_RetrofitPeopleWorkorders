package de.rogallab.mobile.domain.entities

import de.rogallab.mobile.domain.utilities.UUIDEmpty
import de.rogallab.mobile.domain.utilities.zonedDtMin
import java.time.ZonedDateTime
import java.util.UUID

data class Image(
   val remoteUriPath: String = "",
   val contentType: String = "",
   val updated: ZonedDateTime = zonedDtMin,
   val userId: UUID = UUIDEmpty,   // authorized user how owns picture
   val id: UUID = UUID.randomUUID()
)