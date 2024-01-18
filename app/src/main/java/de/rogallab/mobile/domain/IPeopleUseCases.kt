package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.usecases.people.FetchPeople

interface IPeopleUseCases {
   val fetchPeople: FetchPeople
}