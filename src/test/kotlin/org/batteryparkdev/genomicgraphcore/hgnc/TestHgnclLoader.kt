package org.batteryparkdev.genomicgraphcore.common

import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

fun deleteTestNodes(nodeLabelList: List<String>): Unit =
    nodeLabelList.forEach { label -> Neo4jUtils.detachAndDeleteNodesByName(label) }

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "/Volumes/SSD870/HGNC/hgnc_complete_set.tsv"
    if (Neo4jConnectionService.isSampleContext()) {
        deleteTestNodes(listOf("Hgnc"))
        println("Loading data from sample file: $filename")
        CoreModelLoader(HgncModel).loadDataFile(filename)
    } else {
        println("ERROR: Data loading tests can only be run against the sample Neo4j database")
    }
}