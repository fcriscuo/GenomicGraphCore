package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import java.util.concurrent.TimeUnit

abstract class OntologyFileLoader() {


    abstract fun loadOntologyFile()
    abstract val localFilename: String
    abstract val ontologyName: String
    abstract val labelList: List<String>

    val nodeNameList = listOf<String>(
        "OboTerm", "OboSynonymCollection",
        "OboSynonym", "OboXref", "OboXrefCollection"
    )

    fun loadOntologyData() {
        val database = Neo4jPropertiesService.neo4jDatabase
        println("Ontology data will now be loaded from: $localFilename  into the $database Neo4j database")
        val stopwatch = Stopwatch.createStarted()
        OboTermLoader(localFilename, ontologyName, labelList).loadOboTerms()
        println(
            "Ontology data has been loaded into Neo4j in ${stopwatch.elapsed(TimeUnit.SECONDS)} " +
                    "seconds."
        )
    }

}