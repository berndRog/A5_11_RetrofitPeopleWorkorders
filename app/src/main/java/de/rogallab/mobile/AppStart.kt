package de.rogallab.mobile

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import de.rogallab.mobile.domain.utilities.logInfo
import okhttp3.Call

@HiltAndroidApp
class AppStart : Application(), ImageLoaderFactory {

   override fun onCreate() {
      super.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(tag, "onCreate() maxMemory $maxMemory kB")
   }

   // https://www.youtube.com/watch?v=qQVCtkg-O7w
   override fun newImageLoader(): ImageLoader {
      return ImageLoader(this).newBuilder()
         .memoryCachePolicy(CachePolicy.ENABLED)
         .memoryCache {
            MemoryCache.Builder(this)
               .maxSizePercent(0.1 )
               .strongReferencesEnabled(true)
               .build()
         }
         .diskCachePolicy(CachePolicy.ENABLED)
         .diskCache {
            DiskCache.Builder()
               .directory(cacheDir)
               .maxSizePercent(0.03)
               .build()
         }
         .logger(DebugLogger())
         .crossfade(true)
         .build()

   }

   companion object {
      //                       12345678901234567890123
      private const val tag = "ok>AppStart           ."
      const val isInfo = true
      const val isDebug = true
      const val isVerbose = true

      const val database_name:    String = "Workmanager.db"
      const val database_version: Int    = 1

      const val isWebservice = false
//    const val base_url: String = "http://10.0.2.2:5010/"
      const val base_url: String = "http://192.168.178.23:5010/"
      const val api_key:  String = ""
      const val bearer_token:  String = ""

   }
}