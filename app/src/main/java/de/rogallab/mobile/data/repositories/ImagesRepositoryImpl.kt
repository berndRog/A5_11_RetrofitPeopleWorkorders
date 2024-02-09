package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.ImagesWebservice
import de.rogallab.mobile.data.models.ImageDto
import de.rogallab.mobile.data.network.httpStatusMessage
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Image
import de.rogallab.mobile.domain.mapping.toImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
   private val _service: ImagesWebservice,
   private val _dispatcher: CoroutineDispatcher
): ImagesRepository {

   override suspend fun getById(id: UUID): ResultData<Image?> =
      withContext(_dispatcher) {
         try {
            val response: Response<ImageDto> = _service.getById(id)
            if (!response.isSuccessful) {
               return@withContext ResultData.Failure(
                  IOException("Response is not successful: ${httpStatusMessage(response.code())}")
               )
            }
            val imageDto = response.body()
            if (imageDto != null) {
               ResultData.Success(toImage(imageDto))
            } else {
               ResultData.Failure(IOException("response.body() is null"))
            }
         } catch (t: Throwable) {
            ResultData.Failure(t)
         }
      }

   override suspend fun delete(id: UUID): ResultData<Unit> =
      withContext(_dispatcher) {
         try {
            // delete the imageDto and the file with the remoteUriPath
            val response: Response<Unit> = _service.delete(id)
            if (response.isSuccessful)
               return@withContext ResultData.Success(Unit)
            else
               return@withContext ResultData.Failure(
                  IOException("${httpStatusMessage(response.code())}"))
         } catch (t: Throwable) {
            ResultData.Failure(t)
         }
      }


   override suspend fun existsFileName(fileName: String): ResultData<Boolean> =
      withContext(_dispatcher ) {
         try {
            val response: Response<Boolean> = _service.existsFileName(fileName)
            if (response.isSuccessful) {
               // Handle the successful response
               response.body()?.let{ it ->
                  return@withContext ResultData.Success(it)
               } ?: run    {
                  return@withContext ResultData.Failure(IOException("response.body() is null"))
               }
            } else {
               return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun post(
      localImagePath: String    // local file path
   ): ResultData<Image> =
      withContext(_dispatcher ) {
         try {
            createMultiPartBody(localImagePath).let { result ->
               if(result is ResultData.Failure) return@withContext result

               val body = (result as ResultData.Success).data
               val response: Response<ImageDto> = _service.upload(body)

               if (response.isSuccessful) {
                  // Handle the successful response
                  response.body()?.let{ it: ImageDto ->
                     return@withContext ResultData.Success(toImage(it))
                  } ?: run    {
                     return@withContext ResultData.Failure(IOException("response.body() is null"))
                  }
               } else {
                  return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))
               }
            }

         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun put(
      localImagePath: String,    // local file path
      remoteUriPath: String      // remote file path
   ): ResultData<Image> = withContext(_dispatcher ) {
         try {
            createMultiPartBody(localImagePath).let { result ->
               if(result is ResultData.Failure) return@withContext result

               val body = (result as ResultData.Success).data
               val response: Response<ImageDto> = _service.update(remoteUriPath, body)

               if (response.isSuccessful) {
                  // Handle the successful response
                  response.body()?.let{ it: ImageDto ->
                     return@withContext ResultData.Success(toImage(it))
                  } ?: run    {
                     return@withContext ResultData.Failure(IOException("response.body() is null"))
                  }
               } else {
                  return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))
               }
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   override suspend fun delete(
      fileName: String
   ): ResultData<Unit> =
      withContext(_dispatcher ) {
         try {
            val response: Response<ImageDto> = _service.delete(fileName)
            if (response.isSuccessful) {
               return@withContext ResultData.Success(Unit)
            } else {
               return@withContext ResultData.Failure(IOException("${httpStatusMessage(response.code())}"))
            }
         }
         catch (t: Throwable) {
            return@withContext ResultData.Failure(t)
         }
      }

   private fun createMultiPartBody(fromUrlPath:String): ResultData<MultipartBody.Part> {
      val file = File(fromUrlPath)
      if(!file.exists())
         return ResultData.Failure(IOException("file does not exist"))

      val mimeType = when(file.extension) {
         "jpeg", "jpg" -> "image/jpeg"
         "png" -> "image/png"
         "bmp" -> "image/bmp"
         "webp" -> "image/webp"
         "tiff", "tif" -> "image/tiff"
         "heif", "heic" -> "image/heif"
         else -> return ResultData.Failure(IOException("file extension not supported"))
      }

      val requestBody: RequestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
      val body: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.name, requestBody)
      return ResultData.Success(body)
   }

   companion object {
                             //12345678901234567890123
      private const val tag = "ok>ImagesRepositoryImpl"
   }

}