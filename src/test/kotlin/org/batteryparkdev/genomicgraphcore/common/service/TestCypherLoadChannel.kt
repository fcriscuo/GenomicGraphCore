package org.batteryparkdev.genomicgraphcore.common.service

import kotlinx.coroutines.coroutineScope
import org.batteryparkdev.genomicgraphcore.neo4j.service.CypherLoadChannel.processCypher
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import kotlin.system.exitProcess

suspend fun main() = coroutineScope<Unit> {

    Neo4jConnectionService.executeCypherCommand("MATCH (dg:DummyGene) DETACH DELETE(dg);")
    listOf<String>("MERGE (cg:DummyGene{gene_symbol:'XYZ'}) return (cg.gene_symbol);",
        "MERGE (cg:DummyGene{gene_symbol:'BRCA1'}) return (cg.gene_symbol);" )
        .forEach { processCypher(it) }
    println("request 1")
    listOf<String>("MERGE (cg:DummyGene{gene_symbol:'BCL2'}) return (cg.gene_symbol);",
        "MERGE (cg:DummyGene{gene_symbol:'MYC'}) return (cg.gene_symbol);" )
        .forEach { processCypher(it) }
    println("Request 2")
    processCypher("MERGE (cg:DummyGene{gene_symbol:'BAK'}) return (cg.gene_symbol);")
    println("Request 3")
    Thread.sleep(4000L)
    println("FINIS....")
    exitProcess(0)
}