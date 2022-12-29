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

class HumanPhenotypeOntologyApp( val hpFilename: String) {

    private val labelList = listOf<String>("HpTerm","OboTerm")
    val ontology = "human_phenotype"

     fun loadHumanPhenotypeOntologyData():String {
         OboTermLoader(hpFilename,ontology,labelList).loadOboTerms()
        return ("Human Phenotype Ontology data import task completed")
    }
}

fun main(args: Array<String>): Unit {
    val tempFilename = "/tmp/human_phenotype.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, tempFilename)
    if (result.isRight()) {
        val app = HumanPhenotypeOntologyApp(tempFilename)
        val database = Neo4jPropertiesService.neo4jDatabase
        println("Human Phenotype Ontology data will now be loaded from: $tempFilename  into the $database Neo4j database")
        val stopwatch = Stopwatch.createStarted()
        app.loadHumanPhenotypeOntologyData()
        println("Human Phenotype Ontology data has been loaded into Neo4j")
        println("The elapsed time was: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds.")
    } else {
        result.tapLeft {  e -> println(e.message) }
    }
}
