package org.batteryparkdev.genomicgraphcore.hgnc

import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

fun deleteTestNodes(nodeLabelList: List<String>): Unit =
    nodeLabelList.forEach { label -> Neo4jUtils.detachAndDeleteNodesByName(label) }

fun main(args: Array<String>) {
    if (Neo4jConnectionService.isTestingContext()) {
        deleteTestNodes(listOf("Hgnc"))
        CoreModelLoader(HgncModel).loadDataFile(HgncModel.retrieveRemoteDataFile())
    } else {
        println("ERROR: Data loading tests can only be run against the sample Neo4j database")
    }
}
