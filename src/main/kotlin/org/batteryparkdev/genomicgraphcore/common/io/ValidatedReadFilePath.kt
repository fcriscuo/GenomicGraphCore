package org.batteryparkdev.genomicgraphcore.common.io

import arrow.core.Validated
import arrow.core.invalidNel
import arrow.core.valid
import org.apache.commons.io.FilenameUtils
import org.batteryparkdev.genomicgraphcore.common.errorhandling.ValidationError
import org.batteryparkdev.genomicgraphcore.common.errorhandling.ValidationErrors
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.streams.asSequence

/**
 * Inline class and supporting functions to validate that a specified file path name
 * is valid for the local file system and is readable
 * n.b. specified file path names must be absolute (e.g. /tmp/xyz.txt not xyz.txt)
 * Imported from legacy project by fcriscuo on 2022Jul28
 *
 */
@JvmInline
value class ValidatedReadFilePath private constructor (private val filePathName: String) {
    operator fun invoke() = filePathName
    companion object {
        fun valueOf(value: String?): Validated<ValidationErrors, ValidatedReadFilePath> =
            when {
                value.isNullOrEmpty() -> ValidationError("Filepath should not be null or empty").invalidNel()
                isAbsolutePath(value).not() -> ValidationError("$value is not an absolute Path").invalidNel()
                isReadable(value) -> ValidatedReadFilePath(value).valid()
                else -> ValidationError("${value} is not a readable file").invalidNel()
            }

        private fun isReadable(filePathName: String): Boolean =
            File(filePathName).exists().and(File(filePathName).canRead())

        private fun isAbsolutePath(filename:String): Boolean =
            FilenameUtils.getPrefix(filename) != File.separator
    }

    fun readFileAsStream(): Stream<String> = Files.lines(this.getPath())

    fun readFileAsSequence(): Sequence<String> =readFileAsStream().asSequence()

    fun getPath(): Path = Paths.get(filePathName)

}
