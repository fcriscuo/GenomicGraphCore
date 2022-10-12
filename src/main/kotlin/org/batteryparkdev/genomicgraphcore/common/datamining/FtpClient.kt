package org.batteryparkdev.genomicgraphcore.common.datamining

import arrow.core.Either
import arrow.core.handleError
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPClient
import org.batteryparkdev.genomicgraphcore.common.io.FileFunctions
import org.batteryparkdev.genomicgraphcore.common.io.ValidatedWriteFilePath
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import java.io.File
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
    fun retrieveRemoteFileByFtpUrl(ftpUrl: String, localFilePath: String): Either<Exception, String> {
        val validatedWritePath = ValidatedWriteFilePath.valueOf(localFilePath)
        if (validatedWritePath.isValid) {
            val urlConnection = URL(ftpUrl)
            urlConnection.openConnection()
            // the FileUtils method closes the input stream
            return try {
                FileUtils.copyInputStreamToFile(urlConnection.openStream(),File(localFilePath))
                if (FilenameUtils.getExtension(localFilePath) in FileFunctions.compressedFileExtensions) {
                    FileFunctions.gunzipFile(localFilePath)
                }
                Either.Right("$ftpUrl downloaded to  $localFilePath")
            } catch (e: Exception) {
                Either.Left(e)
            }
        } else {
            val exceptionMessage = "Write File Errors: "
            validatedWritePath.mapLeft { errors -> errors.map { it.message.plus("\n") } }
                .handleError { errors -> errors.fold(exceptionMessage){acc, error -> acc.plus(error) } }
            return Either.Left(Exception(exceptionMessage))

        }
    }
}
