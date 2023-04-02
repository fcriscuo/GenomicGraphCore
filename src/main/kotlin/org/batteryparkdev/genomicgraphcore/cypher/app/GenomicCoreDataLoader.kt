package org.batteryparkdev.genomicgraphcore.cypher.app

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jStringList
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.ontology.app.GeneOntologyLoader
import org.batteryparkdev.genomicgraphcore.ontology.app.HumanPhenotypeOntologyLoader
import org.batteryparkdev.genomicgraphcore.ontology.app.SequenceOntologyLoader

/*
Kotlin application to load ontology and core data files
 */

private val cqlFiles = listOf<String>(
    "./cql/load_genomic_entity.cql",
    "./cql/load_hgnc.cql",
    "./cql/load_entrez_gene.cql",
    "./cql/load_entrez_gene_reactome.cql",
    "./cql/load_uniprot.cql",
    "./cql/load_uniprot_reactome.cql",
    "./cql/load_disgenet.cql",
    "./cql/load_nhgri_gene.cql",
    "./cql/load_pubmed_ids"
    )

private fun defineRelationships() {
    Neo4jConnectionService.executeCypherCommand("CALL apoc.cypher.runFile(${"./cql/CoreConstraints.cql".formatNeo4jPropertyValue()})")
}

private fun defineConstraints(){
    Neo4jConnectionService.executeCypherCommand("CALL apoc.cypher.runFile(${"./cql/CoreConstraints.cql".formatNeo4jPropertyValue()})")
}

private fun processCqlScripts() {
    Neo4jConnectionService.executeCypherCommand("CALL apoc.cypher.runFiles(${formatNeo4jStringList(cqlFiles)})")
}

fun main() {
    // define constraints and indices
    defineConstraints()
    // load the ontologies first
    GeneOntologyLoader().loadOntologyFile()
    HumanPhenotypeOntologyLoader().loadOntologyFile()
    SequenceOntologyLoader().loadOntologyFile()
    // load the core data
    processCqlScripts()
    // define relationships
    defineRelationships()
}