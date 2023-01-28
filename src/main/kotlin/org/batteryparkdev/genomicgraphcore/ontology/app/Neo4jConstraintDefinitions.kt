package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.neo4j.service.defineConstraints

/*
A collection of Neo4j database constraint definitions in Cypher
These constraints should be defined prior to loading the database with
any initial data.
Constraint definitions upgrded to Neo4j 5.x syntax
 */
val constraints by lazy {
    listOf<String>(
        "CREATE CONSTRAINT unique_oboterm_id IF NOT EXISTS FOR (obo: OboTerm) REQUIRE obo.obo_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obosyn_coll_id IF NOT EXISTS FOR (syncoll: OboSynonymCollection) REQUIRE syncoll.go_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obo_xref_id IF NOT EXISTS FOR (xref: OboXref) REQUIRE xref.xref_key IS UNIQUE",
        "CREATE CONSTRAINT unique_pubmed_id IF NOT EXISTS FOR (pub: Publication) REQUIRE pub.pub_id IS UNIQUE",
        "CREATE CONSTRAINT unique_obosyn_id IF NOT EXISTS FOR (syn: GoSynonym) REQUIRE syn.synonym_id IS UNIQUE"
    )
}

fun defineGoDatabaseConstraints() {
   defineConstraints(constraints)
}

// stand-alone invocation
fun main(){
    defineGoDatabaseConstraints()
}