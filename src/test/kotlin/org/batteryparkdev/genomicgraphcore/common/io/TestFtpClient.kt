import org.batteryparkdev.genomicgraphcore.common.datamining.FtpClient
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService

fun main() {
    val tempFilename = "/tmp/human_phenotype.obo"
    val result = FtpClient.retrieveRemoteFileByFtpUrl(FilesPropertyService.humanPhenotypeDownloadUrl, tempFilename)
}