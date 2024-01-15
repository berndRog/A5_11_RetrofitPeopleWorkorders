package de.rogallab.mobile.data.database
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import de.rogallab.mobile.domain.utilities.formatISO
import de.rogallab.mobile.domain.utilities.systemZoneId
import de.rogallab.mobile.domain.utilities.toZonedDateTimeUTC
import java.time.ZonedDateTime
import java.util.UUID

@ProvidedTypeConverter
object ZonedDateTimeConverters {
   @TypeConverter
   fun stringToZonedDateTime(utcTimeStamp: String): ZonedDateTime =
      ZonedDateTime.parse(utcTimeStamp, formatISO)
         .withZoneSameInstant(systemZoneId)

   @TypeConverter
   fun zonedDateTimeToString(zdt: ZonedDateTime): String =
      toZonedDateTimeUTC(zdt).format(formatISO)

}


@ProvidedTypeConverter
object UUIDConverter {
   @TypeConverter
   fun fromUUID(uuid: UUID): String = uuid.toString()

   @TypeConverter
   fun toUUID(suuid: String): UUID = UUID.fromString(suuid)
}

/*
object ZonedDateTimeConverters {

   @TypeConverter
   @JvmStatic
   fun toZonedDateTime(zulu: String?): ZonedDateTime? =
      zulu?.let {
         ZonedDateTime.parse(zulu, formatISO).withZoneSameInstant(systemZoneId)
      }

   @TypeConverter
   @JvmStatic
   fun fromZonedDateTime(zdt: ZonedDateTime?): String?  =
       zdt?.let {
          toZonedDateTimeUTC(zdt).format(formatISO)
       }


   @TypeConverter
   @JvmStatic
   fun toDuration(span: Long): Duration  =
      span.toDuration(DurationUnit.NANOSECONDS)

   @TypeConverter
   @JvmStatic
   fun fromDuration(duration: Duration): Long  =
         duration.toLong(DurationUnit.NANOSECONDS)


}
*/