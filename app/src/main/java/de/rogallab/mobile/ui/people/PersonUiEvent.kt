package de.rogallab.mobile.ui.people

sealed class PersonUiEvent {
   object Id : PersonUiEvent()
   object FirstName : PersonUiEvent()
   object LastName : PersonUiEvent()
   object Email : PersonUiEvent()
   object Phone : PersonUiEvent()
   object ImagePath : PersonUiEvent()
   object RemoteUriPath : PersonUiEvent()
}