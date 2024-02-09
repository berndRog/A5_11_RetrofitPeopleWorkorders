package de.rogallab.mobile.data.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import de.rogallab.mobile.domain.utilities.logError
import java.io.File
import java.util.*

fun readImageFromInternalStorage(uri: Uri): Bitmap? {
   uri.toFile().apply {
      return BitmapFactory.decodeFile(this.absolutePath)
   }
}

fun exitsFileOnInternalStorage(fileName: String): Boolean =
   File(fileName).exists()

fun getFileEpochMillis(fileName: String): Long? =
   File(fileName).lastModified()

fun writeImageToInternalStorage(context: Context, bitmap: Bitmap): String? {
// .../app_images/...
   val images: File = context.getDir("images", Context.MODE_PRIVATE)
   val fileName = "${UUID.randomUUID()}.jpg"

   var uriPath: String? = null
   File(images, fileName).apply {
      this.outputStream().use { out ->
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
         out.flush()
      }
      Uri.fromFile(this)?.let {
         uriPath = it.path
      }
   }
   return uriPath
}

fun deleteFileOnInternalStorage(fileName:String) {
   try {
      File(fileName).apply {
         this.absoluteFile.delete()
      }
   } catch(e:Exception ) {
      logError("ok>deleteFileOnInternalStorage","Error deleting file + ${e.localizedMessage}")
   }
}