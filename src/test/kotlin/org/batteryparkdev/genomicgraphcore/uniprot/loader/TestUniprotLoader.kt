package org.batteryparkdev.genomicgraphcore.uniprot.loader

import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotModel

fun deleteTestNodes(nodeLabelList: List<String>): Unit =
    nodeLabelList.forEach { label -> Neo4jUtils.detachAndDeleteNodesByName(label) }

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "/Volumes/SSD870/data/UniProt/uniprot-2022.09.16-21.09.47.10.tsv"
    if (Neo4jConnectionService.isSampleContext()) {
        deleteTestNodes(listOf("UniProt"))
        println("Loading data from sample file: $filename")
        CoreModelLoader(UniprotModel).loadDataFile(filename)
    } else {
        println("ERROR: Data loading tests can only be run against the sample Neo4j database")
    }
}