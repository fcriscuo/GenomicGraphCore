package org.batteryparkdev.genomicgraphcore.ontology.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermLoader
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import java.util.concurrent.TimeUnit

class GeneOntologyLoader(val goFilename: String = "/tmp/gene_ontology.obo"): OntologyFileLoader {

    private val labelList = listOf<String>("GoTerm","OboTerm")
    val ontology = "gene_ontology"

    private val nodeNameList = listOf<String>("OboTerm","OboSynonymCollection",
        "OboSynonym","OboXref","OboXrefCollection")

     private fun loadGeneOntologyData():String {
         OboTermLoader(goFilename,ontology,labelList).loadOboTerms()
        return ("Gene Ontology data import task completed")
    }

   private  fun deleteGoNodes():String {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
        return "GoTerm-related nodes & relationships deleted"
    }

    override fun loadOntologyFile() {
        val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.geneontologyDownloadUrl, goFilename)
        if (result.isRight()) {
            val database = Neo4jPropertiesService.neo4jDatabase
            if (Neo4jConnectionService.isTestingContext()) {
                deleteGoNodes()
            }
            println("Gene Ontology data will now be loaded from: $goFilename  into the $database Neo4j database")
            val stopwatch = Stopwatch.createStarted()
            loadGeneOntologyData()
            println("Gene Ontology data has been loaded into Neo4j in ${stopwatch.elapsed(TimeUnit.SECONDS)} " +
                    "seconds.")
        } else {
            result.tapLeft {  e -> println(e.message) }
        }
    }
}

// main function for stand-alone loading of GO data
fun main() {
    GeneOntologyLoader().loadOntologyFile()
}

