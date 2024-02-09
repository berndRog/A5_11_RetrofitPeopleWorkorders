package de.rogallab.mobile.ui.workorders

sealed class WorkorderUiEvent {
   object Title : WorkorderUiEvent()
   object Description : WorkorderUiEvent()
   object ImagePath : WorkorderUiEvent()
   object State : WorkorderUiEvent()
   object Created : WorkorderUiEvent()
   object Started : WorkorderUiEvent()
   object Completed : WorkorderUiEvent()
// object Duration : WorkorderUiEvent() is also calculated in Completed event
   object Remark : WorkorderUiEvent()
   object Id : WorkorderUiEvent()
   object PersonId : WorkorderUiEvent()
   object Person : WorkorderUiEvent()
}