package com.omkarsoft.arriveontimedelivery.helper

import java.text.SimpleDateFormat
import java.util.*

class DateFormatterHelper {
    companion object{
        fun format(pattern: String, timezone: TimeZone?, date: Date):String{
            val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
            timezone?.let{
                sdf.timeZone = timezone
            }
            return sdf.format(date)
        }

        fun formatString(newPattern:String, oldPattern:String, dateString:String):String{
            val parser = SimpleDateFormat(oldPattern, Locale("id", "ID"))
            val formatter = SimpleDateFormat(newPattern, Locale("id", "ID"))
            return formatter.format(parser.parse(dateString)!!)
        }
    }
}