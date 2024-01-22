package de.rogallab.mobile.ui.base

data class ErrorState(
   val errorParams: ErrorParams? = null,
   // set error to handled
   val onErrorHandled: () -> Unit,
)