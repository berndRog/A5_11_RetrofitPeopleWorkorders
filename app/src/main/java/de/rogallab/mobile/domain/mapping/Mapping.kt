package de.rogallab.mobile.domain.mapping

import de.rogallab.mobile.data.models.ImageDto
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.entities.Image
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.toZonedDateTime
import de.rogallab.mobile.domain.utilities.toZuluString
import java.time.Duration

fun ImageDto.toImage(): Image = Image(
   id = id,
   remoteUriPath = remoteUriPath,
   contentType = contentType,
   updated = toZonedDateTime(updated),
   userId = userId,
)
fun Image.toImageDto(): ImageDto = ImageDto(
   id = id,
   contentType = contentType,
   remoteUriPath = remoteUriPath,
   updated = toZuluString(updated),
   userId = userId,
)

fun PersonDto.toPerson(): Person = Person(
   id = id,
   firstName = firstName,
   lastName = lastName,
   email = email,
   phone = phone,
   imagePath = imagePath,
   remoteUriPath = remoteUriPath,
   imageId = imageId,
   workorders = mutableListOf(),
)

fun Person.toPersonDto(): PersonDto = PersonDto(
   id = id,
   firstName = firstName,
   lastName = lastName,
   email = email,
   phone = phone,
   imagePath = imagePath,
   remoteUriPath = remoteUriPath,
   imageId = imageId,
)

fun WorkorderDto.toWorkorder(): Workorder = Workorder(
   id = id,
   title = title,
   description = description,
   created = toZonedDateTime(created),
   started = toZonedDateTime(started),
   completed = toZonedDateTime(completed),
   state = state,
   duration = Duration.ofNanos(duration), // convert duration into nanos
   remark = remark,
   personId = personId,
)

fun toWorkorderDto(workorder: Workorder): WorkorderDto = WorkorderDto(
   title = workorder.title,
   description = workorder.description,
   created = toZuluString(workorder.created),
   started = toZuluString(workorder.started),
   completed = toZuluString(workorder.completed),
   state = workorder.state,
   duration = workorder.duration.toNanos(),          // convert duration into nanos
   remark = workorder.remark,
   id = workorder.id,
   personId = workorder.personId,
)

fun toPerson(personDtoWithWorkorderDtos:PersonDtoWithWorkorderDtos): Person {
   val person = personDtoWithWorkorderDtos.personDto.toPerson()
   val workorders = personDtoWithWorkorderDtos.workorderDtos.map { it: WorkorderDto -> it.toWorkorder() }
   person.workorders.addAll(workorders)
   return person
}