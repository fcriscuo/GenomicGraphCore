package org.batteryparkdev.genomicgraphcore.common.datamining

import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

fun main() {
    val ftpUrl = FilesPropertyService.geneontologyDownloadUrl
    val testFilename = "/tmp/geneontology.obo"
    val badFilename = "ABC"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(ftpUrl,testFilename)
    when (result.isRight()) {
        true -> result.tap{ println(it) }
        false -> result.tapLeft {  e -> println(e.message) }
    }
}