package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.usecases.workorders.FetchWorkorders

interface IWorkorderUseCases {
   val fetchWorkorders: FetchWorkorders
}