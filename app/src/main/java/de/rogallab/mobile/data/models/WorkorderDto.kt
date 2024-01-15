package de.rogallab.mobile.data.models
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.maxValues
import de.rogallab.mobile.domain.utilities.toZuluString
import de.rogallab.mobile.domain.utilities.zonedDateTimeNow
import de.rogallab.mobile.domain.entities.WorkState
import java.util.UUID

@Entity(tableName = "workorders")
data class WorkorderDto(
   val title:String = "",
   val description: String = "",
   val created: String = toZuluString(zonedDateTimeNow()),
   var started: String = toZuluString(zonedDateTimeNow()),
   var completed: String = toZuluString(zonedDateTimeNow()),
   var duration: Long = 0L,
   var remark: String = "",
   var imagePath: String? = null,
   var state: WorkState = WorkState.Default,
   @PrimaryKey
   val id: UUID = UUID.randomUUID(),
   // foreign Key
   var personId: UUID? = null,
   @Embedded  // Workorder -> Address [0..1]
   var address: AddressDto? = null
){
   fun asString() : String = "${title.maxValues(20)} $created $started $completed ${state} $duration $remark.max(20) ${id.as8()}"
}