package de.rogallab.mobile.domain.entities

import de.rogallab.mobile.domain.utilities.UUIDEmpty
import java.util.UUID

data class Image(
   val contentType: String = "",
   val remoteUriPath: String = "",
   val userId: UUID = UUIDEmpty,
   val id: UUID = UUID.randomUUID()
)