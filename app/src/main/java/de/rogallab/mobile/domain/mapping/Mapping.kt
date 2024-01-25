package de.rogallab.mobile.domain.mapping

import de.rogallab.mobile.data.models.AddressDto
import de.rogallab.mobile.data.models.ImageDto
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.entities.Address
import de.rogallab.mobile.domain.entities.Image
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.toZonedDateTime
import de.rogallab.mobile.domain.utilities.toZuluString
import java.time.Duration

fun toAddress(addressDto: AddressDto): Address = Address(
   street = addressDto.street,
   number = addressDto.number,
   postal = addressDto.postal,
   city = addressDto.city
)
fun toAddressDto(address: Address): AddressDto = AddressDto(
   street = address.street,
   number = address.number,
   postal = address.postal,
   city = address.city
)

fun toImage(imageDto: ImageDto): Image = Image(
   contentType = imageDto.contentType,
   remoteUriPath = imageDto.remoteUriPath,
   userId = imageDto.userId,
   id = imageDto.id
)
fun toImageDto(image: Image): ImageDto = ImageDto(
   contentType = image.contentType,
   remoteUriPath = image.remoteUriPath,
   userId = image.userId,
   id = image.id
)

fun toPerson(personDto:PersonDto): Person = Person(
   firstName = personDto.firstName,
   lastName = personDto.lastName,
   email = personDto.email,
   phone = personDto.phone,
   imagePath = personDto.imagePath,
   id = personDto.id,
   workorders = mutableListOf(),
   //address = personDto.address?.let{ toAddress(it) }
)

fun toPersonDto(person:Person): PersonDto = PersonDto(
   firstName = person.firstName,
   lastName = person.lastName,
   email = person.email,
   phone = person.phone,
   imagePath = person.imagePath,
   id = person.id,
   //address = person.address?.let{ toAddressDto(it) }
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
   person = null,
   personId = workorderDto.personId,
   //address = workorderDto.address?.let{ toAddress(it) }
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
   //address = this.address?.let{ toAddressDto(it) }
)

fun toPerson(personDtoWithWorkorderDtos:PersonDtoWithWorkorderDtos): Person {
   val person = toPerson(personDtoWithWorkorderDtos.personDto)
   val workorders = personDtoWithWorkorderDtos.workorderDtos.map { it -> toWorkorder(it) }
   person.workorders.addAll(workorders)
   return person
}

