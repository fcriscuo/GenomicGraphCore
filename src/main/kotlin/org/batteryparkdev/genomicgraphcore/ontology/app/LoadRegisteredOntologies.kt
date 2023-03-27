package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

private fun loadOntologies() {
    GeneOntologyLoader().loadOntologyFile()
    DiseaseOntologyLoader().loadOntologyFile()
    HumanPhenotypeOntologyLoader().loadOntologyFile()
    SequenceOntologyLoader().loadOntologyFile()
}

private fun createConstraints() {
    listOf<String>(
        "CREATE CONSTRAINT unique_oboterm_id IF NOT EXISTS FOR (obo: OboTerm)  REQUIRE obo.obo_id IS UNIQUE;",
        "CREATE CONSTRAINT unique_obo_xref_id IF NOT EXISTS FOR (xref: OboXref)  REQUIRE xref.xref_key IS UNIQUE;"
    ).forEach { Neo4jConnectionService.executeCypherCommand(it) }
}

fun main() {
    createConstraints()
    loadOntologies()
}