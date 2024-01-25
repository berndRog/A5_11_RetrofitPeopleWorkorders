package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.PersonDto
import de.rogallab.mobile.data.models.WorkorderDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface IPeopleWebservice {
   @GET("workmanagerapi/v1/people")
   suspend fun getAll(
   ): Response<List<PersonDto>>

   @GET("workmanagerapi/v1/people/{id}")
   suspend fun getById(
      @Path("id") id: UUID
   ): Response<PersonDto?>

   // get the workorders for a person with the given id
   @GET("workmanagerapi/v1/people/{personId}/workorders")
   suspend fun getByIdWithWorkorders(
      @Path("personId") personId: UUID
   ): Response<List<WorkorderDto>>

   @POST("workmanagerapi/v1/people")
   suspend fun post(
      @Body personDto: PersonDto
   ): Response<Unit>

   @PUT("workmanagerapi/v1/people/{id}")
   suspend fun put(
      @Path("id") id:UUID,
      @Body personDto: PersonDto
   ): Response<Unit>

   @DELETE("workmanagerapi/v1/people/{id}")
   suspend fun delete(
      @Path("id") id: UUID
   ): Response<Unit>
}