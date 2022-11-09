package org.batteryparkdev.genomicgraphcore.uniprot.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotIdModel
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val stopwatch = Stopwatch.createStarted()
    val filename = UniprotIdModel.retrieveRemoteDataFile()
    if (Neo4jConnectionService.isTestingContext()) {
        deleteTestNodes(listOf("UniProtEntry"))
        println("Loading data from sample file: $filename")
        CoreModelLoader(UniprotIdModel).loadDataFile(filename)
        println("Retrieving and loading UniProt identifiers required ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    } else {
        println("ERROR: Data loading tests can only be run against the sample Neo4j database")
    }
}