package de.rogallab.mobile.domain.utilities


fun String.maxValues(n:Int): String {
   val end = kotlin.math.min(this.length, n)
   val result = this.substring(0, end)
   return result
}