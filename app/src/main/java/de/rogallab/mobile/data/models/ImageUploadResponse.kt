package de.rogallab.mobile.data.models

data class ImageUploadResponse(
   val success: Boolean = false  ,
   val imageUrl: String? = null,
   val message: String? = null
)