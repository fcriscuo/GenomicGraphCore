package org.batteryparkdev.genomicgraphcore.common.datamining

import arrow.core.Either
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.batteryparkdev.genomicgraphcore.common.io.FileFunctions
import org.batteryparkdev.genomicgraphcore.common.io.RefinedFilePath
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.URL

const val FTP_USER = "anonymous"
// The password for an anonymous ftp connection is, by practice, the user's email
val ftpPassword = FilesPropertyService.ftpUserEmail
const val FTP_PORT = 21

private val logger = KotlinLogging.logger {}

object FtpClient {
    private val ftp = FTPClient()

    init {
        ftp.addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
        ftp.enterLocalPassiveMode()
    }

    /*
Function to access a remote file via anonymous FTP and copy its contents to
the local filesystem at a specified location.
Parameters: ftpUrl - Complete URL for remote file
            localFilePath - local filesystem location
Returns: An Either - Left is an Exception, Right is a success message
 */
    fun retrieveRemoteFileByFtpUrl(ftpUrl: String, localFilePath: RefinedFilePath): Either<Exception, String> {
        val urlConnection = URL(ftpUrl)
        urlConnection.openConnection()
        // the FileUtils method closes the input stream
        try {
            FileUtils.copyInputStreamToFile(urlConnection.openStream(), localFilePath.getPath().toFile())
            if (FilenameUtils.getExtension(localFilePath.filePathName) in FileFunctions.compressedFileExtensions) {
                FileFunctions.gunzipFile(localFilePath.filePathName)
            }
            return Either.Right("$ftpUrl downloaded to  $localFilePath")
        } catch (e: Exception) {
            return Either.Left(e)
        }
    }
}
