package de.rogallab.mobile.domain
import de.rogallab.mobile.domain.usecases.people.GetPeople
import de.rogallab.mobile.domain.usecases.people.ReadPeople
import de.rogallab.mobile.domain.usecases.people.WorkorderAdd
import de.rogallab.mobile.domain.usecases.people.WorkorderRemove

interface IPeopleUseCases {
   val readPeople: ReadPeople
   val workorderAdd: WorkorderAdd        // add a workorder to a person
   val workorderRemove: WorkorderRemove

   val getPeople: GetPeople
}