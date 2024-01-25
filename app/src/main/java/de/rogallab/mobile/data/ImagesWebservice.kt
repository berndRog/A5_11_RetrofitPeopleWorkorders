package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.ImageDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.util.UUID

interface ImagesWebservice {
   @GET("workmanagerapi/v1/images/{id}")
   suspend fun getById(
      @Path("id") id: UUID,
   ): Response<ImageDto>

   @GET("workmanagerapi/v1/images/{fileName}")
   suspend fun existsFileName(
      @Path("fileName") fileName: String,
   ): Response<Boolean>

//   Endpint is used by Coil directly
//   @GET("workmanagerapi/v1/imageFiles/{fileName}")
//   suspend fun downlood(
//      @Path("fileName") fileName: String,
//   ): Response<File>

   @Multipart
   @POST("workmanagerapi/v1/imageFiles")
   suspend fun upload(
      @Part file: MultipartBody.Part
   ): Response<ImageDto>

   @Multipart
   @PUT("workmanagerapi/v1/imageFiles/{fileName}")
   suspend fun update(
      @Path("fileName") fileName: String,
      @Part file: MultipartBody.Part
   ): Response<ImageDto>
   
   @DELETE("workmanagerapi/v1/imageFiles/{fileName}")
   suspend fun delete(
      @Path("fileName") fileName: String,
   ): Response<ImageDto>
}
