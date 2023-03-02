package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import java.util.concurrent.TimeUnit

/*
Kotlin application to load the contents of the Human Phenotype Ontology into
a Neo4j database
 */

class HumanPhenotypeOntologyLoader( val hpFilename: String = "/tmp/human_phenotype.obo"): OntologyFileLoader {

    private val labelList = listOf<String>("HpTerm","OboTerm")
    val ontology = "human_phenotype"

     private fun loadHumanPhenotypeOntologyData():String {
         OboTermLoader(hpFilename,ontology,labelList).loadOboTerms()
        return ("Human Phenotype Ontology data import task completed")
    }

    override fun loadOntologyFile() {
        val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, hpFilename)
        if (result.isRight()) {
            val database = Neo4jPropertiesService.neo4jDatabase
            println("Human Phenotype Ontology data will now be loaded from: $hpFilename " +
                    "into the $database Neo4j database")
            val stopwatch = Stopwatch.createStarted()
            loadHumanPhenotypeOntologyData()
            println("Human Phenotype Ontology data has been loaded into Neo4j in " +
                    "${stopwatch.elapsed(TimeUnit.SECONDS)} seconds." )
        } else {
            result.tapLeft {  e -> println(e.message) }
        }
    }
}

fun main(args: Array<String>): Unit {
   HumanPhenotypeOntologyLoader().loadOntologyFile()
}
