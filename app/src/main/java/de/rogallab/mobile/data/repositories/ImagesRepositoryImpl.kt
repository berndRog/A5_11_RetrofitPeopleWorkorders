package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.ImagesWebservice
import de.rogallab.mobile.data.models.ImageUploadResponse
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
   private val service: ImagesWebservice,
   private val _dispatcher: CoroutineDispatcher
): ImagesRepository {

   override suspend fun upload(fromUrlPath: String): ResultData<ImageUploadResponse> =

      withContext(_dispatcher ) {
         try {
            val file = File(fromUrlPath)
            if(!file.exists()) return@withContext ResultData.Failure(
                  IOException("file does not exist"), tag)

            val mimeType = when(file.extension) {
               "jpeg", "jpg" -> "image/jpeg"
               "png" -> "image/png"
               "gif" -> "image/gif"
               "bmp" -> "image/bmp"
               "webp" -> "image/webp"
               "svg" -> "image/svg+xml"
               "tiff", "tif" -> "image/tiff"
               "heif", "heic" -> "image/heif"
               else -> return@withContext ResultData.Failure(
                  IOException("file extension not supported"), tag)
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response: Response<ImageUploadResponse> = service.uploadImage(body)
            if (response.isSuccessful) {
               // Handle the successful response
               response.body()?.let{ it: ImageUploadResponse ->
                  return@withContext ResultData.Success(it)
               } ?: run    {
                  return@withContext ResultData.Failure(
                     IOException("response body is null"), tag)
               }
            } else {
               return@withContext ResultData.Failure(
                  IOException("response is not successful " +
                  "${httpStatusMessage(response.code())}"), tag)
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t, tag)
         }
      }

   companion object {
                             //12345678901234567890123
      private const val tag = "ok>ImagesRepositoryImpl"
   }

}