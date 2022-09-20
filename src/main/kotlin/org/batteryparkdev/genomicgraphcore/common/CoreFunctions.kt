package org.batteryparkdev.genomicgraphcore.common

import java.util.*

/*
Represents a collection of common utility functions used
throughout the application
 */
fun String.formatNeo4jPropertyValue(): String {
    val tmp = this.removeSurrounding("\"")
  return  when (tmp.toIntOrNull()) {
        null -> "\"$tmp\""
        else -> tmp
    }
}

/*
Specialized function that encapsulates all members of a delimited String
Necessary for delimited Strings that may contain numeric values
Neo4j cannot process arrays with mixed members
 */
fun String.parseToQuotedNeo4jStringList(sep: Char = '|'): String {
    if (this.isNotEmpty()){
        val list = this.parseOnDelimiter(sep).map { symbol -> "\"".plus(symbol).plus("\"") }
        return "[".plus(list.joinToString(separator = ",")).plus("]")
    } else  {
        return "[]"
    }
}

fun String.parseToNeo4jStringList(sep: Char = '|'): String {
    if (this.isNotEmpty()) {
        val list = this.parseOnDelimiter(sep).map { symbol -> symbol.formatNeo4jPropertyValue() }
        return "[".plus(list.joinToString(separator = ",")).plus("]")
    }
    return "[]"
}

fun String.predicateToBoolean():Boolean =
    when (this.lowercase(Locale.getDefault())) {
        "true" ->  true
        else ->  false
    }

fun String.parseValidInteger(): Int {
    val tmp = this.removeSurrounding("\"")
    return when (tmp.toIntOrNull()) {
        null -> 0
        else -> tmp.toInt()
    }
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
fun String.nonEmptyDefault(): String =
    when (this.isNullOrEmpty()) {
        true -> "\"\""
        false -> this
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
