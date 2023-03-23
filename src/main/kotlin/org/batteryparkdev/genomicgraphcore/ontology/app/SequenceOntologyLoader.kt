package org.batteryparkdev.genomicgraphcore.ontology.app

/*
Kotlin application to load the contenst of the Sequence Ontology (http://www.sequenceontology.org)  into a Neo4j database
The ontology file must be downloaded from https://github.com/The-Sequence-Ontology/SO-Ontologies to the local filesystem
 */

class SequenceOntologyLoader( ): OntologyFileLoader() {

    override val localFilename: String
        get() = "/./data/so/so.obo"
    override val ontologyName: String
        get() = "sequence_ontology"
    override val labelList
        get() = listOf<String>("GoTerm","OboTerm")

    override fun loadOntologyFile() {
        loadOntologyData()
    }
}

fun main(args: Array<String>): Unit {
     SequenceOntologyLoader().loadOntologyFile()
}
