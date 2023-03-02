package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import java.util.concurrent.TimeUnit

/*
Kotlin application to load the contenst of the Sequence Ontology (http://www.sequenceontology.org)  into a Neo4j database
The ontology file must be downloaded from https://github.com/The-Sequence-Ontology/SO-Ontologies to the local filesystem
 */

class SequenceOntologyLoader(val soFilename: String = "./data/so/so.obo" ): OntologyFileLoader {

    private val labelList = listOf<String>("SoTerm", "OboTerm")
    val ontology = "sequence_ontology"

  private fun loadSequenceOntologyData(): String {
        OboTermLoader(soFilename, ontology, labelList).loadOboTerms()
        return ("Sequence  Ontology data import task completed")
    }

    override fun loadOntologyFile() {
        val database = Neo4jPropertiesService.neo4jDatabase
        println("Sequence Ontology data will now be loaded from: $soFilename  into the $database Neo4j database")
        val stopwatch = Stopwatch.createStarted()
        loadSequenceOntologyData()
        println(
            "Sequence Ontology data has been loaded into Neo4j in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds."
        )
    }
}

fun main(args: Array<String>): Unit {
     SequenceOntologyLoader().loadOntologyFile()
}
