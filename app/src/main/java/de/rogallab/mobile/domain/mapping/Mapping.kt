package de.rogallab.mobile.domain.mapping

import de.rogallab.mobile.data.models.AddressDto
import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.PersonDtoWithWorkorderDtos
import de.rogallab.mobile.data.models.WorkorderDto
import de.rogallab.mobile.domain.entities.Address
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

fun toPerson(personDto:PersonDto): Person = Person(
   firstName = personDto.firstName,
   lastName = personDto.lastName,
   email = personDto.email,
   phone = personDto.phone,
   imagePath = personDto.imagePath,
   id = personDto.id,
   workorders = mutableListOf(),
   address = personDto.address?.let{ toAddress(it) }
)
fun toPeople(peopleDto:List<PersonDto>): List<Person> =
   peopleDto.map { personDto -> toPerson(personDto) }

fun toPersonDto(person:Person): PersonDto = PersonDto(
   firstName = person.firstName,
   lastName = person.lastName,
   email = person.email,
   phone = person.phone,
   imagePath = person.imagePath,
   id = person.id,
   address = person.address?.let{ toAddressDto(it) }
)
fun toPeopleDto(people:List<Person>): List<PersonDto> =
   people.map { person -> toPersonDto(person) }

fun toWorkorder(workorderDto:WorkorderDto): Workorder = Workorder(
   title = workorderDto.title,
   description = workorderDto.description,
   created = toZonedDateTime(workorderDto.created),
   started = toZonedDateTime(workorderDto.started),
   completed = toZonedDateTime(workorderDto.completed),
   state = workorderDto.state,
   duration = Duration.ofMillis(workorderDto.duration),
   remark = workorderDto.remark,
   id = workorderDto.id,
   person = null,
   personId = workorderDto.personId,
   address = workorderDto.address?.let{ toAddress(it) }
)
fun toWorkorders(workorderDtos: List<WorkorderDto>): List<Workorder> =
   workorderDtos.map { workorderDto -> toWorkorder(workorderDto) }

fun Workorder.toWorkorderDto(): WorkorderDto = WorkorderDto(
   title = this.title,
   description = this.description,
   created = toZuluString(this.created),
   started = toZuluString(this.started),
   completed = toZuluString(this.completed),
   state = this.state,
   duration = this.duration.toMillis(),
   remark = this.remark,
   id = this.id,
   personId = this.personId,
   address = this.address?.let{ toAddressDto(it) }
)

fun toPerson(personDtoWithWorkorderDtos:PersonDtoWithWorkorderDtos): Person {
   val person = toPerson(personDtoWithWorkorderDtos.personDto)
   val workorders = personDtoWithWorkorderDtos.workorderDtos.map { it -> toWorkorder(it) }
   person.workorders.addAll(workorders)
   return person
}

