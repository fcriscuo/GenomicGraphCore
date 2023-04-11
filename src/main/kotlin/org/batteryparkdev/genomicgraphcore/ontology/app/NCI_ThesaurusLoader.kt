package org.batteryparkdev.genomicgraphcore.ontology.app

/*
Kotlin application to load the contenst of the Sequence Ontology (http://www.sequenceontology.org)  into a Neo4j database
The ontology file must be downloaded from https://github.com/The-Sequence-Ontology/SO-Ontologies to the local filesystem
 */

class NCI_ThesaurusLoaderLoader( ): OntologyFileLoader() {

    override val localFilename: String
        get() = "/Volumes/Sea5TBExt/GenomicCoreData/NCI_Thesaurus_Ontology/NCI_Thesaurus.obo"
    override val ontologyName: String
        get() = "nci_thesaurus"
    override val labelList
        get() = listOf<String>("NCITerm")

    override fun loadOntologyFile() {
        loadOntologyData()
    }
}

fun main(args: Array<String>): Unit {
    NCI_ThesaurusLoaderLoader().loadOntologyFile()
}
