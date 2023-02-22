package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import java.util.concurrent.TimeUnit

/*
Kotlin application to load the contenst of the Sequence Ontology (http://www.sequenceontology.org)  into a Neo4j database
The ontology file must be downloaded from https://github.com/The-Sequence-Ontology/SO-Ontologies to the local filesystem
 */

class SequenceOntologyApp(val soFilename: String) {

    private val labelList = listOf<String>("SoTerm", "OboTerm")
    val ontology = "sequence_ontology"

    fun loadSequenceOntologyData(): String {
        OboTermLoader(soFilename, ontology, labelList).loadOboTerms()
        return ("Sequence  Ontology data import task completed")
    }
}

fun main(args: Array<String>): Unit {
    val soFile = when (args.size > 0) {
        true -> args[0]
        false -> "./data/so/so.obo"
    }
    val app = SequenceOntologyApp(soFile)
    val database = Neo4jPropertiesService.neo4jDatabase
    println("Sequence Ontology data will now be loaded from: $soFile  into the $database Neo4j database")
    val stopwatch = Stopwatch.createStarted()
    app.loadSequenceOntologyData()
    println("Sequence Ontology data has been loaded into Neo4j")
    println("The elapsed time was: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds.")

}
