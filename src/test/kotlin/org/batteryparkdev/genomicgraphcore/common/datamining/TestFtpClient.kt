package org.batteryparkdev.genomicgraphcore.common.datamining

import org.batteryparkdev.genomicgraphcore.common.io.RefinedFilePath
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

fun main() {
    val email = FilesPropertyService.ftpUserEmail
    val ftpUrl = FilesPropertyService.hgncFtpUrl
    val hgncFileName = FilesPropertyService.hgncLocalCompleteSetFilename
    FtpClient.retrieveRemoteFileByFtpUrl(ftpUrl, RefinedFilePath(hgncFileName))
}