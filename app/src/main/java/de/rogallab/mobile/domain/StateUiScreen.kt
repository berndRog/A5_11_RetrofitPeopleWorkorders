package de.rogallab.mobile.domain

data class StateUiScreen<T>(
   val data: T? = null,
   val isLoading: Boolean = false,
   val error: String? = "",
   var upHandler: Boolean = true,   // up   navigation = true: default operation
   var backHandler: Boolean = false // back navigation = true: abort operation
)




