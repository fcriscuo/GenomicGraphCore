package org.batteryparkdev.genomicgraphcore.neo4j

import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

fun main() {
    val cqlFileName = "/Users/fcriscuo/CoreConstraints.cql"
    Neo4jConnectionService.executeCqlSchemaFile(cqlFileName)
}