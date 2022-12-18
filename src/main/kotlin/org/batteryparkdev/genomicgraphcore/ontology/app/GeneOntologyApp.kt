package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

class GeneOntologyApp( val goFilename: String) {

    private val labelList = listOf<String>("GoTerm","OboTerm")
    val ontology = "gene_ontology"

    private val nodeNameList = listOf<String>("OboTerm","OboSynonymCollection",
        "OboSynonym","OboXref","OboXrefCollection")

     fun loadGeneOntologyData():String {
         OboTermLoader(goFilename,ontology,labelList).loadOboTerms()
        return ("Gene Ontology data import task completed")
    }

    fun deleteGoNodes():String {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
        return "GoTerm-related nodes & relationships deleted"
    }
}

fun main(args: Array<String>): Unit {
    val tempFilename = "/tmp/gene_ontology.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.geneontologyDownloadUrl, tempFilename)
    if (result.isRight()) {
        val app = GeneOntologyApp(tempFilename)
        val database = Neo4jPropertiesService.neo4jDatabase
        if (Neo4jConnectionService.isTestingContext()) {
            println("WARNING: Invoking this application will delete all Ontology data from the ${database} database")
            println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
            // Thread.sleep(20_000L)
            app.deleteGoNodes()
        }
        println("Gene Ontology data will now be loaded from: $tempFilename  into the ${database} Neo4j database")
        defineGoDatabaseConstraints()
        app.loadGeneOntologyData()
        println("Gene Ontology data has been loaded into Neo4j")
    } else {
        result.tapLeft {  e -> println(e.message) }
    }
}
