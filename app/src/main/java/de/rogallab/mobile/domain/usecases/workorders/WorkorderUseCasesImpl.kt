package de.rogallab.mobile.domain.usecases.workorders

import de.rogallab.mobile.domain.IWorkorderUseCases
import javax.inject.Inject

data class WorkorderUseCasesImpl @Inject constructor(
   override val selectWorkorders: SelectWorkorders,
   override val getWorkorders: GetWorkorders,
) : IWorkorderUseCases