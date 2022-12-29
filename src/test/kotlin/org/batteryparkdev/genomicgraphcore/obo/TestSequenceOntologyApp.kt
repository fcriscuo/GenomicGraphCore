package org.batteryparkdev.genomicgraphcore.obo

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.ontology.app.SequenceOntologyApp
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit {
    val soFile = "./data/so/so.obo"
    val app = SequenceOntologyApp(soFile)
    val database = Neo4jPropertiesService.neo4jDatabase
    if (Neo4jConnectionService.isTestingContext().not()) {
        println("ERROR: $database is not a test or sample database")
        println("Execution terminated")
        System.exit(1)
    }
    println("WARNING: Invoking this application will delete ALL Ontology data from the $database database")
    println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
    Thread.sleep(20_000L)
    deleteOboNodes()
    println("Sequence Ontology data will now be loaded from: $soFile  into the $database Neo4j database")
    val stopwatch = Stopwatch.createStarted()
    app.loadSequenceOntologyData()
    println("Sequence  Ontology data has been loaded into Neo4j")
    println("The elapsed time was: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds.")

}