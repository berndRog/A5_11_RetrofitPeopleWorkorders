package de.rogallab.mobile.domain

import androidx.room.Query
import de.rogallab.mobile.domain.entities.Image
import retrofit2.http.DELETE
import java.io.File
import java.util.UUID

interface ImagesRepository {
   // @GET("/images/{id}")
   suspend fun getById(id: UUID): ResultData<Image?>

   //@GET("/images/{fileName}")
   suspend fun existsFileName(fileName: String): ResultData<Boolean>

   //@DELETE("/images/{id}")
   suspend fun delete(id: UUID): ResultData<Unit>

   // @POST("/imageFiles")
   suspend fun post(localImagePath: String): ResultData<Image>

   // @PUT("/imageFiles/{fileName}")
   suspend fun put(fileName: String, localImagePath: String): ResultData<Image>

   @DELETE("/imageFiles/{fileName}")
   suspend fun delete(fileName: String): ResultData<Unit>

}


