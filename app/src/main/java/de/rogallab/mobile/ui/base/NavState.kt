package de.rogallab.mobile.ui.base

data class NavState(
   val route: String? = null,
   val clearBackStack: Boolean = true,
   // set navigation to handled
   val onNavRequestHandled: () -> Unit,
)