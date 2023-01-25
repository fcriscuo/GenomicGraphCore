package org.batteryparkdev.genomicgraphcore.cypher.app

import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

private fun loadNhgriScript() {
    val cypher:String = "CALL apoc.cypher.runFile(\"/Users/fcriscuo/softwaredev/GenomicGraphCore/cql/nhgri_gene.cql\")"
    Neo4jConnectionService.executeCypherCommand(cypher)
}

fun main() {
    loadNhgriScript()
}