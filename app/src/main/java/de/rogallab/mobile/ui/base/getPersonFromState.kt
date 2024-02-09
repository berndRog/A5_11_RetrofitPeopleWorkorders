package de.rogallab.mobile.ui.base

import de.rogallab.mobile.domain.entities.Person

fun getPersonFromState(personStateValue: Person): Person =
   Person(
      firstName = personStateValue.firstName,
      lastName = personStateValue.lastName,
      email = personStateValue.email,
      phone = personStateValue.phone,
      imagePath = personStateValue.imagePath,
      remoteUriPath = personStateValue.remoteUriPath,
      id = personStateValue.id,
      imageId = personStateValue.imageId,
   )
