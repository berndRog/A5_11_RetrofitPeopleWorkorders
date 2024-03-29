package de.rogallab.mobile.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rogallab.mobile.domain.utilities.UUIDEmpty
import java.util.UUID

@Entity(tableName = "images")
data class ImageDto(
   val remoteUriPath: String = "",
   val contentType: String = "",
   val updated: String = "",
   val userId: UUID = UUIDEmpty,
   @PrimaryKey
   val id: UUID = UUID.randomUUID()

)