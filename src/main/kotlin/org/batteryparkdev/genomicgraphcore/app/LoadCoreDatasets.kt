package org.batteryparkdev.genomicgraphcore.app

import com.google.common.base.Stopwatch
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotModel
import java.util.concurrent.TimeUnit

/*
Kotlin application to load core datasets into a Neo4j database
 */

fun loadHgncData() {
    val stopwatch = Stopwatch.createStarted()
    val dropCount = Neo4jUtils.resolveNodeCountByLabel(HgncModel.nodelabel)
    println("Starting HGNC data loader; skipping $dropCount records")
    CoreModelLoader(HgncModel).loadDataFile(HgncModel.retrieveRemoteDataFile(), dropCount)
    println("HGNC data loaded in ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
}

fun loadUniprotData() {
    val stopwatch = Stopwatch.createStarted()
    val dropCount = Neo4jUtils.resolveNodeCountByLabel(UniprotModel.nodelabel)
    println("Starting UniProt data loader; skipping $dropCount records")
    CoreModelLoader(UniprotModel).loadDataFile(UniprotModel.retrieveRemoteDataFile())
    println("Uniprot data loaded in ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
}

fun main() {
    println("Loading data into Neo4j  ${Neo4jPropertiesService.neo4jDatabase} database")
    loadHgncData()
    loadUniprotData()
    println("FINIS.....")

}
