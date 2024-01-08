package de.rogallab.mobile.data.seed

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.rogallab.mobile.R
import de.rogallab.mobile.data.io.deleteFileOnInternalStorage
import de.rogallab.mobile.data.io.writeImageToInternalStorage
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.Resource
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.entities.Workorder
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class Seed @Inject constructor(
   application: Application,
   private val _peopleRepository: IPeopleRepository,
   private val _workordersRepository: IWorkordersRepository,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) {

   private val _context: Context = application.applicationContext
   private val _resources: Resources = application.resources

   private val _firstNames = mutableListOf(
      "Arne", "Berta", "Cord", "Dagmar", "Ernst", "Frieda", "Günter", "Hanna",
      "Ingo", "Johanna", "Klaus", "Luise", "Martin", "Norbert", "Paula", "Otto",
      "Rosi", "Stefan", "Therese", "Uwe", "Veronika", "Walter", "Zwantje")
   private val _lastNames = mutableListOf(
      "Arndt", "Bauer", "Conrad", "Diehl", "Engel", "Fischer", "Grabe", "Hoffmann",
      "Imhof", "Jung", "Klein", "Lang", "Meier", "Neumann", "Peters", "Opitz",
      "Richter", "Schmidt", "Thormann", "Ulrich", "Vogel", "Wagner", "Zander")

   private val _emailProvider = mutableListOf("gmail.com", "icloud.com", "outlook.com", "yahoo.com",
      "t-online.de", "gmx.de", "freenet.de", "mailbox.org")

   private var _imagesUri = listOf<String>()


   fun initDatabase() {
      val coroutineScope = CoroutineScope(Job() + _dispatcher + _exceptionHandler)

      val job = coroutineScope.launch {
         val resource = _peopleRepository.count()
         var countPeople = 0;

         if(resource is Resource.Success) {
            countPeople = resource.data!!
         }

         if(countPeople == 0 && _workordersRepository.count() == 0) {
            _imagesUri = initializeImages()
            val people = initializePeople(_imagesUri)
            val workorders = initializeWorkorders()
            coroutineScope.async {
               _peopleRepository.addAll(people)
            }.await()
            coroutineScope.async {
               _workordersRepository.addAll(workorders)
            }.await()
         }
      }
      coroutineScope.launch {
         job.join()
      }
   }

   private fun initializeImages(): List<String> {
      // convert the drawables into image files
      val drawables = mutableListOf<Int>()
      drawables.add(0, R.drawable.man_1)
      drawables.add(1, R.drawable.man_2)
      drawables.add(2, R.drawable.man_3)
      drawables.add(3, R.drawable.man_4)
      drawables.add(4, R.drawable.man_5)
      drawables.add(5, R.drawable.woman_1)
      drawables.add(6, R.drawable.woman_2)
      drawables.add(7, R.drawable.woman_3)
      drawables.add(8, R.drawable.woman_4)
      drawables.add(9, R.drawable.woman_5)

      // Uri of images
      val imagesUri = mutableListOf<String>()
      drawables.forEach { drawable: Int ->
         val decodedBitmap = BitmapFactory.decodeResource(_resources, drawable)
         decodedBitmap?.let { bitmap: Bitmap ->
            writeImageToInternalStorage(_context, bitmap)?.let { uriPath: String? ->
               logDebug("ok>SaveImage          .", "Uri $uriPath")
               uriPath?.let {
                  imagesUri.add(uriPath)
               }
            }
         }
      }
      return imagesUri
   }

   fun disposeImages() {
      _imagesUri.forEach { uriPath ->
         logDebug("ok>disposeImages      .", "Uri $uriPath")
         deleteFileOnInternalStorage(uriPath)
      }
   }

   private fun initializePeople(imagesUri :List<String>): List<Person> {
      val people = mutableListOf<Person>()
      for (index in 0..<_firstNames.size) {
         val s = (index+1).toString().padStart(2,'0')
         val id = UUID.fromString(s+"000000-0000-0000-0000-000000000000");
         val firstName = _firstNames[index]
         val lastName = _lastNames[index]
         val email =
            "${firstName.lowercase(Locale.getDefault())}." +
               "${lastName.lowercase(Locale.getDefault())}@" +
               "${_emailProvider.random()}"
         val phone =
            "0${Random.nextInt(1234, 9999)} " +
               "${Random.nextInt(100, 999)}-" +
               "${Random.nextInt(10, 9999)}"

         val person = Person(firstName, lastName, email, phone, null, id)
         people.add(person)
      }
      val person = Person(
         firstName = "Erika",
         lastName = "Mustermann",
         email = "e.mustermann@t-online.de",
         phone = "0987 6543-210",
         id = UUID.fromString("10000000-0000-0000-0000-000000000000"))
      people.add(person)

      if(imagesUri.size == 10) {
         people[0].imagePath = _imagesUri[0]
         people[1].imagePath = _imagesUri[5]
         people[2].imagePath = _imagesUri[1]
         people[3].imagePath = _imagesUri[6]
         people[4].imagePath = _imagesUri[2]
         people[5].imagePath = _imagesUri[7]
         people[6].imagePath = _imagesUri[3]
         people[7].imagePath = _imagesUri[8]
         people[8].imagePath = _imagesUri[4]
         people[9].imagePath = _imagesUri[9]
      }
      return people
   }

   private fun initializeWorkorders(): List<Workorder> {
      val workorders = mutableListOf<Workorder>()
      workorders.add(
         Workorder(
            id = UUID.fromString("01000000-0000-0000-0000-000000000000"),
            title = "Rasenmähen, 500 m2",
            description = "Bahnhofstr. 1, 29556 Suderburg",
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("02000000-0000-0000-0000-000000000000"),
            title = "6 Büsche schneiden und entsorgen",
            description = "In den Twieten. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("03000000-0000-0000-0000-000000000000"),
            title = "1 Baum fällen und entsorgen",
            description = "Herbert-Meyer-Str. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("04000000-0000-0000-0000-000000000000"),
            title = "Rasenmähen 1200 m2",
            description = "Am Kindergarten. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("05000000-0000-0000-0000-000000000000"),
            title = "5 Büsche schneiden, Unkraut entfernen",
            description = "Lerchenweg 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("06000000-0000-0000-0000-000000000000"),
            title = "Tulpen und Narzissen pflanzen",
            description = "Spechtstr. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("07000000-0000-0000-0000-000000000000"),
            title = "Wegpflaster aufnehmen und neu verlegen, 30 m2",
            description = "Hauptstr. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("08000000-0000-0000-0000-000000000000"),
            title = "4 Baume schneiden, Unkraut entfernen",
            description = "Lindenstr. 1, 29556 Suderburg"
         )
      )
      workorders.add(
         Workorder(
            id = UUID.fromString("09000000-0000-0000-0000-000000000000"),
            title = "Rasenmähen 800 m2",
            description = "Burgstr. 1, 29556 Suderburg"
         )
      )
      return workorders
   }


}