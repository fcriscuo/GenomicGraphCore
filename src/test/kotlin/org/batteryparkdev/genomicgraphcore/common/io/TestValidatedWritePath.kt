package org.batteryparkdev.genomicgraphcore.common.io

import arrow.core.handleError

/**
 * Created by fcriscuo on 2022Sep30
 */
fun main() {
    val testFilePathName: String = "/tmp/validfile.tsv"
    //val testFilePathName: String = "/abc/nosuchfile.txt"
    val validatedWritePath= ValidatedWriteFilePath.valueOf(testFilePathName)
    when (validatedWritePath.isValid) {
        false -> validatedWritePath
            .tapInvalid { errors -> errors.forEach(::println) }
            //we can map the errors to a new type and continue the flow
            .mapLeft { errors -> errors.map { it.message } }

            .handleError { errors -> errors.forEach(::println) }
        true -> validatedWritePath
            //we extract the value from the inline class
            .map { it() }
            //now we have a Validated<ValidationErrors, String>
            .map { println(it) }
    }

}