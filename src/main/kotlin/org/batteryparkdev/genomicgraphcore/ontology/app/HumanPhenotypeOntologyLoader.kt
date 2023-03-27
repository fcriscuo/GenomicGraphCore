package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

/*
Kotlin application to load the contents of the Human Phenotype Ontology into
a Neo4j database
 */

class HumanPhenotypeOntologyLoader( ): OntologyFileLoader() {

    override val localFilename: String
        get() = "/tmp/human_phenotype_ontology.obo"
    override val ontologyName: String
        get() = "human_phenotype_ontology"
    override val labelList
        get() = listOf<String>("HumanPhenotypeTerm")

    override fun loadOntologyFile() {
        val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, localFilename)
        if (result.isRight()) {
            loadOntologyData()
        } else {
            result.tapLeft {  e -> println(e.message) }
        }
    }
}

fun main(args: Array<String>): Unit {
   HumanPhenotypeOntologyLoader().loadOntologyFile()
}
