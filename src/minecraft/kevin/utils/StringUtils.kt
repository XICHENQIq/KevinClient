package kevin.utils

import java.util.*

object StringUtils {
    fun toCompleteString(args: Array<String>, start: Int): String {
        if (args.size <= start) return ""

        return java.lang.String.join(" ", *Arrays.copyOfRange(args, start, args.size))
    }

    fun replace(string: String, searchChars: String, replaceChars: String?): String {
        var replaceChars = replaceChars
        if (string.isEmpty() || searchChars.isEmpty() || searchChars == replaceChars) return string

        if (replaceChars == null) replaceChars = ""

        val stringLength = string.length
        val searchCharsLength = searchChars.length
        val stringBuilder = StringBuilder(string)

        for (i in 0 until stringLength) {
            val start = stringBuilder.indexOf(searchChars, i)

            if (start == -1) {
                if (i == 0) return string

                return stringBuilder.toString()
            }

            stringBuilder.replace(start, start + searchCharsLength, replaceChars)
        }

        return stringBuilder.toString()
    }
    fun extractContent(input: String,start: String,stop: String): String? {
        val pattern = "$start(.*?)$stop".toRegex()
        val matchResult = pattern.find(input)
        return matchResult?.groupValues?.get(1)
    }
}