package de.rogallab.mobile.data

import de.rogallab.mobile.data.models.WorkorderDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface IWorkordersWebservice {

   @GET("workmanagerapi/v1/workorders")
   suspend fun getAll(
   ): Response<List<WorkorderDto>>

   @GET("workmanagerapi/v1/workorders/{id}")
   suspend fun getById(
      @Path("id") id: UUID
   ): Response<WorkorderDto?>

   @POST("workmanagerapi/v1/workorders")
   suspend fun post(
      @Body workorderDto: WorkorderDto
   ): Response<Unit>

   @PUT("workmanagerapi/v1/workorders/{id}")
   suspend fun put(
      @Path("id") id:UUID,
      @Body workorderDto: WorkorderDto
   ): Response<Unit>

   @DELETE("workmanagerapi/v1/workorders/{id}")
   suspend fun delete(
      @Path("id") id: UUID
   ): Response<Unit>

}