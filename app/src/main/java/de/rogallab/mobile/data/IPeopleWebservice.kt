package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.PersonDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface IPeopleWebservice {
   @GET("people")
   suspend fun getAll(
   ): Response<List<PersonDto>>

   @GET("people/{id}")
   suspend fun getById(
      @Path("id") id: UUID
   ): Response<PersonDto?>

   @POST("people")
   suspend fun post(
      @Body personDto: PersonDto
   ): Response<Unit>

   @PUT("people/{id")
   suspend fun put(
      @Path("id") id:UUID,
      @Body personDto: PersonDto
   ): Response<Unit>

   @DELETE("people/{id}")
   suspend fun delete(
      @Path("id") id: UUID
   ): Response<Unit>
}