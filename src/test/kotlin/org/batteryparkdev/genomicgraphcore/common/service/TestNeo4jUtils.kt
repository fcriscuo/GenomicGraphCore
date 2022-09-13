package org.batteryparkdev.genomicgraphcore.common.service

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

fun testFormatNeo4jProperty() {
    val testBasicString = "ABCDEF"
    println("$testBasicString  ${testBasicString.formatNeo4jPropertyValue()}")
}

fun main() {
    testFormatNeo4jProperty()
}