package de.rogallab.mobile.ui.workorders

import de.rogallab.mobile.domain.entities.Workorder

data class WorkordersUiState(
   val isLoading: Boolean = false,
   val isSuccessful: Boolean = false,
   val workorders: List<Workorder> = emptyList()
)




