package de.rogallab.mobile.domain

import de.rogallab.mobile.data.models.ImageUploadResponse

interface ImagesRepository {
   // Webservice
   suspend fun upload(urlFrom:String): ResultData<ImageUploadResponse>
}