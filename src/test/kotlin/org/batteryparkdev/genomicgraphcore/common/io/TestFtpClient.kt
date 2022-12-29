import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

fun main(args: Array<String>) {
    val tempFilename = "/tmp/human_phenotype.obo"
    val url = FilesPropertyService.humanPhenotypeDownloadUrl
    println("URL = $url")
    val result = FtpClient.retrieveRemoteFileByFtpUrl(url, tempFilename)
}