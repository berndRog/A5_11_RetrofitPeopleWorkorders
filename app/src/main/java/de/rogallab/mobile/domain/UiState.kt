package de.rogallab.mobile.domain

sealed class UiState<out T>(
   var upHandler: Boolean = true,   // up   navigation = true: default operation
   var backHandler: Boolean = false // back navigation = true: abort operation
) {
   data object Empty                       : UiState<Nothing>(true, false)   // Singleton
   data object Loading                     : UiState<Nothing>( true, false)  // Singleton
   data class  Success<out T>(val data: T?): UiState<T>(true, false)
   data class  Error(
      val message: String,
      val up: Boolean = false,
      val back: Boolean = true
   )                                       : UiState<Nothing>(up, back)
}




