package org.batteryparkdev.genomicgraphcore.neo4j.service

import org.batteryparkdev.genomicgraphcore.common.service.LogService


/*
A collection of Neo4j database constraint definitions in Cypher
These constraints should be defined prior to loading the database with
any initial data.

 */
val constraints by lazy {
    listOf<String>(
        "CREATE CONSTRAINT unique_hgnc_id IF NOT EXISTS ON (h:Hgnc) ASSERT h.hgnc_id IS UNIQUE",
        "CREATE CONSTRAINT unique_entry_id IF NOT EXISTS ON (u:UniProt) ASSERT u.entry_id IS UNIQUE",
        "CREATE CONSTRAINT unique_section_id IF NOT EXISTS ON (ps:PublicationSection) ASSERT ps.section_id IS UNIQUE",
        "CREATE CONSTRAINT unique_publication_id IF NOT EXISTS ON (p:Publication) ASSERT p.pub_id IS UNIQUE",
        "CREATE CONSTRAINT unique_uniprot_entry_id IF NOT EXISTS ON (u:UniProtEntry) ASSERT u.entry_id IS UNIQUE"

    )
}

fun defineConstraints() {
    constraints.forEach {
        Neo4jConnectionService.defineDatabaseConstraint(it)
        LogService.info("Constraint: $it  has been defined")
    }
}

// stand-alone invocation
fun main(){
    println("Define constraints for ${System.getenv("NEO4J_DATABASE")} database")
    defineConstraints()
}