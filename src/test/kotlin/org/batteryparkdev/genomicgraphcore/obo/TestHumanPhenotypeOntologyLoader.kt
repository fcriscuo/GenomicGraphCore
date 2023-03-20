package org.batteryparkdev.genomicgraphcore.obo

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import org.batteryparkdev.genomicgraphcore.ontology.app.HumanPhenotypeOntologyLoader
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTerm
import java.util.concurrent.TimeUnit


fun main(args: Array<String>): Unit {
    val tempFilename = "/tmp/human_phenotype.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, tempFilename)
    if (result.isRight()) {
        val app = HumanPhenotypeOntologyLoader(tempFilename)
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
        println("Human Phenotype Ontology data will now be loaded from: $tempFilename  into the $database Neo4j database")
        val stopwatch = Stopwatch.createStarted()
        app.loadOntologyFile()
        println("Human Phenotype Ontology data has been loaded into Neo4j")
        println("The elapsed time was: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds.")
    } else {
        result.tapLeft {  e -> println(e.message) }
    }
}


fun deleteOboNodes():String {
    OboTerm.nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
    return "Human Phenotype nodes & relationships deleted"
}