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

fun toImage(imageDto: ImageDto): Image = Image(
   remoteUriPath = imageDto.remoteUriPath,
   contentType = imageDto.contentType,
   updated = toZonedDateTime(imageDto.updated),
   id = imageDto.id,
   userId = imageDto.userId,

)
fun toImageDto(image: Image): ImageDto = ImageDto(
   contentType = image.contentType,
   remoteUriPath = image.remoteUriPath,
   updated = toZuluString(image.updated),
   id = image.id,
   userId = image.userId,
)

fun toPerson(personDto:PersonDto): Person = Person(
   firstName = personDto.firstName,
   lastName = personDto.lastName,
   email = personDto.email,
   phone = personDto.phone,
   imagePath = personDto.imagePath,
   remoteUriPath = personDto.remoteUriPath,
   id = personDto.id,
   imageId = personDto.imageId,
   workorders = mutableListOf(),
   //address = personDto.address?.let{ toAddress(it) }
)

fun toPersonDto(person:Person): PersonDto = PersonDto(
   firstName = person.firstName,
   lastName = person.lastName,
   email = person.email,
   phone = person.phone,
   imagePath = person.imagePath,
   remoteUriPath = person.remoteUriPath,
   id = person.id,
   imageId = person.imageId,
)

fun toWorkorder(workorderDto:WorkorderDto): Workorder = Workorder(
   title = workorderDto.title,
   description = workorderDto.description,
   created = toZonedDateTime(workorderDto.created),
   started = toZonedDateTime(workorderDto.started),
   completed = toZonedDateTime(workorderDto.completed),
   state = workorderDto.state,
   duration = Duration.ofNanos(workorderDto.duration), // convert duration into nanos
   remark = workorderDto.remark,
   id = workorderDto.id,
   personId = workorderDto.personId,
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
   val person = toPerson(personDtoWithWorkorderDtos.personDto)
   val workorders = personDtoWithWorkorderDtos.workorderDtos.map { it -> toWorkorder(it) }
   person.workorders.addAll(workorders)
   return person
}

