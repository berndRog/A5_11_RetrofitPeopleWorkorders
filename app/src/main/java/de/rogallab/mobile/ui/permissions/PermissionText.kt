package de.rogallab.mobile.ui.permissions
import android.content.Context

class PermissionCamera : IPermissionText {
   override fun getDescription(
      isPermanentlyDeclined: Boolean
   ): String =
      when (isPermanentlyDeclined) {
         true -> "Sie haben den Zugriff auf die Kamera mehrfach abgelehnt. " +
            "Daher können Sie diese Berechtigung nur noch über die App Einstellungen ändern."
         false -> "App erfordert den Zugriff auf die Kamera, um ein Foto aufzunehmen."
      }
}

class PermissionCoarseLocation : IPermissionText {
   override fun getDescription(
      isPermanentlyDeclined: Boolean
   ): String =
      when (isPermanentlyDeclined) {
         false -> "Sie haben den Zugriff auf die ungefähre Ortsbestimmung mehrfach abgelehnt. " +
            "Die Berechtigung kann nur noch über die App Einstellungen geändert werden."
         true -> "App erfordert den Zugriff auf die ungefähre Ortsbestimmung."
      }
}

class PermissionFineLocation : IPermissionText {
   override fun getDescription(
      isPermanentlyDeclined: Boolean
   ): String =
      when (isPermanentlyDeclined) {
         false -> "Sie haben den Zugriff auf die genaue Ortsbestimmung mehrfach abgelehnt. " +
            "Die Berechtigung kann nur noch über die App Einstellungen geändert werden."
         true -> "App erfordert die den Zugriff auf die genaue Ortsbestimmung"
      }
}


class PermissionTextNotFound : IPermissionText {
   override fun getDescription(
      isPermanentlyDeclined: Boolean
   ): String = "Permission text not found"
 }

class PermissionRecordAudio : IPermissionText {
   override fun getDescription(
      isPermanentlyDeclined: Boolean
   ): String =
      when (isPermanentlyDeclined) {
         false -> "Sie haben den Zugriff auf das Mirofon mehrfach abgelehnt. " +
            "Die Berechtigung kann nur noch über die App Einstellungen geändert werden."
         true -> "App erfordert die den Zugriff auf das Mikrofon"

      }
}

