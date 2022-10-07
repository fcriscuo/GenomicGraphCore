package org.batteryparkdev.genomicgraphcore.common

import org.batteryparkdev.genomicgraphcore.common.io.ValidatedReadFilePath
import arrow.core.handleError

fun main(): Unit {

   // val testPathName: String = "/tmp/uniprot.tsv"
    val testPathName: String = "abc/nosuchfile.txt"
    val validatedReadPath= ValidatedReadFilePath.valueOf(testPathName)

    when (validatedReadPath.isValid) {
        false -> validatedReadPath
            .tapInvalid { errors -> errors.forEach(::println) }
            //we can map the errors to a new type and continue the flow
            .mapLeft { errors -> errors.map { it.message } }
            //we handle the errors and that's it, to be used as the last invocation
            //also note that error is now a list of strings, as a result of the previous map
            .handleError { errors -> errors.forEach(::println) }
        true -> validatedReadPath
            //we extract the value from the inline class
            .map { it() }
            //now we have a Validated<ValidationErrors, String>
            .map { println(it) }
    }
    if (validatedReadPath.isValid){
        validatedReadPath.map { it.readFileAsSequence().take(20).forEach { line -> println(line) }}

    }
}
