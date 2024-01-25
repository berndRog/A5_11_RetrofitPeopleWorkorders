package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.usecases.workorders.GetWorkorders
import de.rogallab.mobile.domain.usecases.workorders.SelectWorkorders

interface IWorkorderUseCases {
   val selectWorkorders: SelectWorkorders
   val getWorkorders: GetWorkorders
}