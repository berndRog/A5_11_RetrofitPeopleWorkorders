package de.rogallab.mobile.domain
import de.rogallab.mobile.domain.usecases.people.GetPeople
import de.rogallab.mobile.domain.usecases.people.ReadPeople

interface IPeopleUseCases {
   val readPeople: ReadPeople
   val getPeople: GetPeople
}