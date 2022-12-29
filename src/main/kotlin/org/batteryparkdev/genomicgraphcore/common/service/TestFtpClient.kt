package org.batteryparkdev.genomicgraphcore.common.service

import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient

fun main() {
    val tempFilename = "/tmp/human_phenotype.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, tempFilename)
}