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
    "./data/load_hgnc.cql",
    "./data/load_entrez_gene.cql",
    "./data/load_entrez_gene_go.cql",
    "./data/load_entrez_gene_pubmed.cql",
    "./data/load_entrez_gene_reactome.cql",
    "./data/load_uniprot.cql",
    "./data/load_uniprot_reactome.cql",
    "./data/load_disgenet.cql",
    "./data/load_nhgri_gene.cql"
    )

private fun defineConstraints(){
    Neo4jConnectionService.executeCypherCommand("CALL apoc.cypher.runFile(${"./data/CoreConstraints.cql".formatNeo4jPropertyValue()})")
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
}