package de.rogallab.mobile.data


import de.rogallab.mobile.data.models.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImagesWebservice {
   @Multipart
   @POST("/images")
   suspend fun uploadImage(
      @Part file: MultipartBody.Part
   ): Response<ImageUploadResponse>
}
