package de.rogallab.mobile.domain.utilities

fun getLocalOrRemoteImagePath(
   localImagePath: String?,
   remoteImagePath: String?
): String? {
   // a remote image, but no local image
   if (localImagePath == null && remoteImagePath != null) return remoteImagePath
   // a local image, but no remote image
   else if (localImagePath != null && remoteImagePath == null) return localImagePath
   // a new local image, but also a remote image to be updated
   else if (localImagePath != null && remoteImagePath != null) return localImagePath
   // no image at all
   else return null
}