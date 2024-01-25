package de.rogallab.mobile.ui.workorders

import de.rogallab.mobile.domain.entities.Workorder
import javax.annotation.concurrent.Immutable

data class WorkorderUiState(
   val isLoading: Boolean = false,
   val isSuccessful: Boolean = false,
   val workorders: List<Workorder> = emptyList()
)




