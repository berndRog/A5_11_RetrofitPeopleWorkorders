package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.usecases.people.GetPeople
import de.rogallab.mobile.domain.usecases.people.SelectPeople

interface IPeopleUseCases {
   val selectPeople: SelectPeople
   val getPeople: GetPeople
}