package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.neo4j.service.defineConstraints

/*
A collection of Neo4j database constraint definitions in Cypher
These constraints should be defined prior to loading the database with
any initial data.
 */
val constraints by lazy {
    listOf<String>(
        "CREATE CONSTRAINT unique_oboterm_id IF NOT EXISTS ON (obo: OboTerm) ASSERT obo.obo_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obosyn_coll_id IF NOT EXISTS ON (syncoll: OboSynonymCollection) ASSERT syncoll.go_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obo_xref_id IF NOT EXISTS ON (xref: OboXref) ASSERT xref.xref_key IS UNIQUE",
        "CREATE CONSTRAINT unique_pubmed_id IF NOT EXISTS ON (pub: Publication) ASSERT pub.pub_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obosyn_id IF NOT EXISTS ON (syn: GoSynonym) ASSERT syn.synonym_id IS UNIQUE"
    )
}

fun defineGoDatabaseConstraints() {
   defineConstraints(constraints)
}

// stand-alone invocation
fun main(){
    defineGoDatabaseConstraints()
}