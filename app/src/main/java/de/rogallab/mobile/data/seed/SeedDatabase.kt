package de.rogallab.mobile.data.seed

import android.app.Application
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class SeedDatabase @Inject constructor(
   private val _application: Application,
   private val _peopleRepository: IPeopleRepository,
   private val _workordersRepository: IWorkordersRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   private var _seed: Seed? = null

   fun initDatabase() {
      val coroutineScope = CoroutineScope(Job() + _dispatcher + _exceptionHandler)

      val job = coroutineScope.launch {
         var result: ResultData<Int> = _peopleRepository.count()
         var countPeople = 0
         if(result is ResultData.Success) {  countPeople = result.data  }

         result = _workordersRepository.count()
         var countWorkorders = 0
         if(result is ResultData.Success) {  countWorkorders = result.data  }

         if(countPeople == 0 && countWorkorders == 0) {
            _seed = Seed(_application)
            coroutineScope.async {
               _seed!!.people.forEach { person ->
                  _peopleRepository.add(person)
               }
            }.await()
            coroutineScope.async {
               _seed!!.workorders.forEach { workorder ->
                  _workordersRepository.add(workorder)
               }
            }.await()
         }
      }
      coroutineScope.launch {
         job.join()
      }
   }

   fun disposeImages() {
      _seed?.disposeImages()
   }
}