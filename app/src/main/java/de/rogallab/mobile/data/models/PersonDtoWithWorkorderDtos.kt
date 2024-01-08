package de.rogallab.mobile.data.models

import androidx.room.Embedded
import androidx.room.Relation


data class PersonDtoWithWorkorderDtos(
   @Embedded
   var personDto: PersonDto,
   @Relation(
      parentColumn = "id",
      entityColumn = "personId"
   )
   // WorkorderDto [0...*]
   var workorderDtos: List<WorkorderDto>
)
