package de.rogallab.mobile.domain.utilities
import java.time.*
import java.time.format.DateTimeFormatter

val systemZoneId: ZoneId = ZoneId.systemDefault()

// zoned date Time at epoch = 0, i.e. 1970-01-01T00:00:00Z
val zonedDtMin = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())

val formatZDt:DateTimeFormatter = DateTimeFormatter.ofPattern("eee dd.MM.yyyy - HH:mm:ss:SSS z")
//val formatLongDayOfWeek:DateTimeFormatter = DateTimeFormatter.ofPattern("eeee")
val formatShortDayOfWeek:DateTimeFormatter = DateTimeFormatter.ofPattern("EE")
//val formatLongDate:DateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
//val formatMediumDate: DateTimeFormatter? = DateTimeFormatter.ofPattern("d. MMM yyyy")
val formatShortDate:DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val formatShortTime:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val formatTimeMin:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val formatTimeSec:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val formatTimeMs:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
//val formatISO:DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
val formatISO:DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
// Di., 3. Januar 2023, 10:45 Uhr

fun zonedDateTimeString(zdt: ZonedDateTime): String =
   "${zdt.format(formatShortDayOfWeek)} "+
   "${zdt.format(formatShortDate)} "+
   "${zdt.format(formatTimeSec)}"

fun zonedDateTimeNow(
   zoneId: ZoneId = systemZoneId
): ZonedDateTime = ZonedDateTime.now(zoneId)


//- LocalDateTime LocalDate, LocalTime <==> ZonedDateTime ------------
fun toLocalDateTime(zdt: ZonedDateTime): LocalDateTime =
   zdt.toLocalDateTime()
fun toLocalDate(zdt: ZonedDateTime): LocalDate =
   zdt.toLocalDate()
fun toLocalTime(zdt: ZonedDateTime): LocalTime =
   zdt.toLocalTime()
fun toLocalDateTime(date:LocalDate, time:LocalTime): LocalDateTime =
   time.atDate(date)

fun toZonedDateTime(
   ldt: LocalDateTime,
   zoneId: ZoneId = systemZoneId
): ZonedDateTime  =
   ldt.atZone( zoneId )

//- ZonedDateTime <==> Zulu-String, epoch --------------------------
fun toZonedDateTimeUTC(zdt: ZonedDateTime): ZonedDateTime =
   zdt.withZoneSameInstant(ZoneId.of("+0"))

// zonedDateTime -> zuluString
fun toZuluString(zdt: ZonedDateTime): String =
   toZonedDateTimeUTC(zdt).format(formatISO)
// zuluString --> zonedDateTime
fun toZonedDateTime(zulu: String): ZonedDateTime =
   ZonedDateTime.parse(zulu, formatISO)
                .withZoneSameInstant(systemZoneId)

fun toEpochMillis(
   zdt: ZonedDateTime
): Long =
   zdt.toInstant().toEpochMilli()

fun toZonedDateTime(
   epochMillis: Long,
   zoneId: ZoneId = systemZoneId
): ZonedDateTime =
   Instant.ofEpochMilli(epochMillis).atZone(zoneId)


/*
   Zulu Time (Coordinated Universal Time) == UTC(GMT) +0
   UTC	2022-06-27T13:22:58Z
   Zoned DateTime(germany)
   Timezone  (Europe/berlin)
*/

/* SHORT_IDS
   EST - -05:00
   HST - -10:00
   MST - -07:00
   ACT - Australia/Darwin
   AET - Australia/Sydney
   AGT - America/Argentina/Buenos_Aires
   ART - Africa/Cairo
   AST - America/Anchorage
   BET - America/Sao_Paulo
   BST - Asia/Dhaka
   CAT - Africa/Harare
   CNT - America/St_Johns
   CST - America/Chicago
   CTT - Asia/Shanghai
   EAT - Africa/Addis_Ababa
   ECT - Europe/Paris
   IET - America/Indiana/Indianapolis
   IST - Asia/Kolkata
   JST - Asia/Tokyo
   MIT - Pacific/Apia
   NET - Asia/Yerevan
   NST - Pacific/Auckland
   PLT - Asia/Karachi
   PNT - America/Phoenix
   PRT - America/Puerto_Rico
   PST - America/Los_Angeles
   SST - Pacific/Guadalcanal
   VST - Asia/Ho_Chi_Minh
 */
