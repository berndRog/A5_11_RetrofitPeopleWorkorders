package de.rogallab.mobile.ui.workorders

import de.rogallab.mobile.domain.entities.Workorder
import javax.annotation.concurrent.Immutable

@Immutable
data class WorkorderUiState(
   val isLoading: Boolean = false,
   val isRefreshing: Boolean = false,
   val isSuccessful: Boolean = false,
   val workorders: List<Workorder> = emptyList(),
   val isError: Boolean = false,
) {
   fun init(): WorkorderUiState {
      return WorkorderUiState(
         isLoading = false,
         isRefreshing = false,
         isSuccessful = false,
         workorders = emptyList(),
      )
   }

   fun loading(): WorkorderUiState {
      return copy(
         isLoading = true,
         isRefreshing = false,
         isSuccessful = false,
         workorders = emptyList(),
      )
   }

   fun success(workorders: List<Workorder>): WorkorderUiState {
      return copy(
         isLoading = false,
         isRefreshing = false,
         isSuccessful = true,
         workorders = workorders,
      )
   }
}




