package org.batteryparkdev.genomicgraphcore.common

fun main() {
    val cypherFiles = listOf<String>("./data/CoreConstraints.cql",
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
    println("Test formatting Neo4j String list: ${formatNeo4jStringList(cypherFiles)}")
}