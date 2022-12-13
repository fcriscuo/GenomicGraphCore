package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import java.util.concurrent.TimeUnit

class HumanPhenotypeOntologyApp( val hpFilename: String) {

    private val labelList = listOf<String>("HpTerm","OboTerm")
    val ontology = "human_phenotype"

    private val nodeNameList = listOf<String>("OboTerm","OboSynonymCollection",
        "OboSynonym","OboXref","OboXrefCollection")

     fun loadHumanPhenotypeOntologyData():String {
         OboTermLoader(hpFilename,ontology,labelList).loadOboTerms()
        return ("Human Phenotype Ontology data import task completed")
    }

    fun deleteOboNodes():String {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
        return "Human Phenotype nodes & relationships deleted"
    }
}

fun main(args: Array<String>): Unit {
    val tempFilename = "/tmp/human_phenotype.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, tempFilename)
    if (result.isRight()) {
        val app = HumanPhenotypeOntologyApp(tempFilename)
        val database = Neo4jPropertiesService.neo4jDatabase
        if (Neo4jConnectionService.isTestingContext()) {
            println("WARNING: Invoking this application will delete all Ontology data from the $database database")
            println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
           // Thread.sleep(20_000L)
            app.deleteOboNodes()
        }
        println("Human Phenotype Ontology data will now be loaded from: $tempFilename  into the $database Neo4j database")
        val stopwatch = Stopwatch.createStarted()
        app.loadHumanPhenotypeOntologyData()
        println("Human Phenotype Ontology data has been loaded into Neo4j")
        println("The elapsed time was: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds.")
    } else {
        result.tapLeft {  e -> println(e.message) }
    }
}
