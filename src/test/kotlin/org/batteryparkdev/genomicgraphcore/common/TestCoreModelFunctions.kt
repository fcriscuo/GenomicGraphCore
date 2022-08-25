package org.batteryparkdev.genomicgraphcore.service

import org.batteryparkdev.genomicgraphcore.common.formatIntList
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseOnPipe
import org.batteryparkdev.genomicgraphcore.common.parseToNeo4jStringList


fun main() {
    // test parse on delimiter
    val testPipeString  = "ABC|DEF|GHI|JKL"
    val splitOnPipe = testPipeString.parseOnPipe()
    println(testPipeString.parseToNeo4jStringList())
    println(splitOnPipe)
    // test format Neo4j property value
    println("14q24.3")
    println("14q24.3".formatNeo4jPropertyValue())
    println("protein-coding gene".formatNeo4jPropertyValue())
    println("42".formatNeo4jPropertyValue())
    /*
    fun formatIntList(intList: String): String
     */
   val intListString = "123|456|900"
    println(formatIntList(intListString))
    val ints = intListString.split("|")
    ints.map{it.toInt()}.forEach { println(it) }
    println(intListString.split("|").map {it.toInt()  })


}