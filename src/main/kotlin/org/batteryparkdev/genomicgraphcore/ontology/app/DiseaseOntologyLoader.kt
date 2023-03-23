package org.batteryparkdev.genomicgraphcore.ontology.app

import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

class DiseaseOntologyLoader(): OntologyFileLoader() {

    override val localFilename: String
        get() = "/tmp/disease_ontology.obo"
    override val ontologyName: String
        get() = "disease_ontology"
    override val labelList
        get() = listOf<String>("GoTerm","OboTerm")

    override fun loadOntologyFile() {
        val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.diseaseOntologyDownloadUrl, localFilename)
        if (result.isRight()) {
            loadOntologyData()
        } else {
            result.tapLeft {  e -> println(e.message) }
        }
    }
}

// main function for stand-alone loading of DO data
fun main() {
    DiseaseOntologyLoader().loadOntologyFile()
}

