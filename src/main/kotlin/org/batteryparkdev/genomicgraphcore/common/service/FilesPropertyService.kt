package org.batteryparkdev.genomicgraphcore.common.service

object FilesPropertyService {

    private val config = ApplicationProperties("files.config")
    val ftpUserEmail = config.getConfigPropertyAsString("ftp.user.email")
    val baseDataPath = config.getConfigPropertyAsString("base.data.path")
    val baseDataSubdirectory = config.getConfigPropertyAsString("base.subdirectory.name")
    val geneontologyDownloadUrl=config.getConfigPropertyAsString("geneontology.download.url")
    val humanPhenotypeDownloadUrl = config.getConfigPropertyAsString("humanpheno.ontology.download.url")

}