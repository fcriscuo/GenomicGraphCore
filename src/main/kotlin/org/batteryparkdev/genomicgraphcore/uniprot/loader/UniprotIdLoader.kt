package org.batteryparkdev.genomicgraphcore.uniprot.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotIdModel
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val stopwatch = Stopwatch.createStarted()
    val filename = UniprotIdModel.retrieveRemoteDataFile()
    println("Loading UniProt identifiers from $filename")
    CoreModelLoader(UniprotIdModel).loadDataFile(filename)
    println("Loading UniProt identifiers required ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
}