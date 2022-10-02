package org.batteryparkdev.genomicgraphcore.common.service

object DataSourcesPropertyService {
private val config = ApplicationProperties("data_sources.config")
val hgncFtpUrl= config.getConfigPropertyAsString("hgnc.ftp.complete.set.url")
val hgncLocalCompleteSetFilename= config.getConfigPropertyAsString("hgnc.local.filename")
}