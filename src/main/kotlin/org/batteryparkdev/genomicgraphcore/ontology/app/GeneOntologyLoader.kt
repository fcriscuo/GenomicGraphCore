package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

class GeneOntologyLoader(): OntologyFileLoader() {

    override val localFilename: String
        get() = "/tmp/gene_ontology.obo"
    override val ontologyName: String
        get() = "gene_ontology"
    override val labelList
        get() = listOf<String>("GoTerm")

    override fun loadOntologyFile() {
        val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.geneontologyDownloadUrl, localFilename)
        if (result.isRight()) {
            loadOntologyData()
        } else {
            result.tapLeft {  e -> println(e.message) }
        }
    }
}

// main function for stand-alone loading of GO data
fun main() {
    GeneOntologyLoader().loadOntologyFile()
}

