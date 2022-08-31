package org.batteryparkdev.genomicgraphcore.common

/*
Represents a collection of common utility functions used
throughout the application
 */
fun String.formatNeo4jPropertyValue(): String =
    when (this.toIntOrNull()) {
        null -> "\"$this\""
        else -> this
    }

fun String.parseToNeo4jStringList(sep: Char = '|'): String {
    if (this.isNotEmpty()) {
        val list = this.parseOnDelimiter(sep).map { symbol -> symbol.formatNeo4jPropertyValue() }
        return "[".plus(list.joinToString(separator = ",")).plus("]")
    }
    return "[]"
}

fun String.parseValidInteger(): Int =
    when (this.toIntOrNull()) {
        null -> 0
        else -> this.toInt()
    }

/*
  Function to convert a floating point String into a Float
  returns 0.0 if the String is not in a floating point format
   */
fun parseValidFloatFromString(fs: String): Float =
    when (Regex("[-+]?[0-9]*\\.?[0-9]+").matches(fs)) {
        true -> fs.toFloat()
        else -> 0.0F
    }

fun String.isValid(): Boolean = this.isBlank().not()

fun String.parseOnSemicolon(): List<String> = this.parseOnDelimiter(';')

fun String.parseOnColon(): List<String> = this.parseOnDelimiter(':')

fun String.parseOnComma(): List<String> = this.parseOnDelimiter(',')

fun String.parseOnTab(): List<String> = this.parseOnDelimiter('\t')

fun String.parseOnPipe(): List<String> = this.parseOnDelimiter('|')

/*
Double quotes (i.e. ") inside a text field cause Cypher
processing errors
*/
fun String.removeInternalQuotes(): String = this.replace("\"", "'")

fun String.YNtoBoolean(): Boolean = this.lowercase() == "y"

fun String.isNumeric(): Boolean = this.all { it in '0'..'9' }

fun String.parseOnDelimiter(delimiter: Char): List<String> =
    when (this.isValid()) {
        false -> emptyList()
        true -> this.replace("\"", "").split(delimiter).map { it.trim() }
    }

fun String.splitPairOnEquals(): Pair<String, String>? {
    val list = this.parseOnDelimiter('=')
    return when (list.size == 2) {
        true -> list[0] to list[1]
        false -> null
    }
}
fun String.convertNumericToBoolean(): Boolean = this == "1"

fun String.isHumanSpeciesId() = this.trim() == "9606"

fun Int.toBoolean(): Boolean = this == 1

fun reduceListToDelimitedString(list: List<String>, delimiter: String): String =
    list.joinToString { delimiter }

fun reduceIntListToCommaDelimitedString(ints: List<Int>): String =
    ints.joinToString(separator = ",")

fun reduceListToPipeDelimitedString(list: List<String>) = reduceListToDelimitedString(list, "|")

/*
Function to return last element in a List
Needed to support Java clients
 */
fun <T> getLastListElement(list: List<T>): T = list.last()

fun parseDoubleString(ds: String): Double = ds.replace(',', '.').toDouble()


fun parseValidDoubleFromString(fs: String): Double =
    when (Regex("[-+]?[0-9]*\\.?[0-9]+").matches(fs)) {
        true -> fs.toDouble()
        else -> 0.0
    }
fun String.parseValidDouble(): Double =
    when (Regex("[-+]?[0-9]*\\.?[0-9]+").matches(this)) {
    true -> this.toDouble()
    else -> 0.0
}

fun String.parseValidFloat(): Float =
    when (Regex("[-+]?[0-9]*\\.?[0-9]+").matches(this)) {
        true -> this.toFloat()
        else -> 0.0F
    }

fun formatIntList(intList: String): String =
    when (intList.isNotEmpty()) {
        true -> "[".plus(
            intList.replace("|", ",")
                .replace("\"", "")
        ).plus(']')

        false -> "[0]"
    }
