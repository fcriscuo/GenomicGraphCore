package org.batteryparkdev.genomicgraphcore.common.datamining

import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

fun main() {
    val ftpUrl = FilesPropertyService.hgncFtpUrl
    val testFilename = "/tmp/test_hgnc_data.tsv"
    val badFilename = "ABC"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(ftpUrl,badFilename)
    when (result.isRight()) {
        true -> result.tap{ println(it) }
        false -> result.tapLeft {  e -> println(e.message) }
    }
}