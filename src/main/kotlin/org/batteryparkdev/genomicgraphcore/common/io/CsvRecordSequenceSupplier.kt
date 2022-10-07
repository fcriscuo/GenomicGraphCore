package org.batteryparkdev.genomicgraphcore.common.io

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.ensureNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.streams.asSequence

/*
Responsible for returning a Stream of CSVRecords from a
specified Path (i.e. CSV file)
First record in file is treated as a header
 */

@JvmInline
value class CsvRecordSequence(val body: Sequence<CSVRecord>)
sealed interface FileError
@JvmInline value class SecurityError(val msg: String?): FileError
@JvmInline value class FileNotFound (val path: String): FileError
object EmptyPath: FileError {
    override fun toString() = "EmptyPath"
}

class CsvRecordSequenceSupplier( val filename: String) : Supplier<Sequence<CSVRecord>> {

    private var recordSequence: Sequence<CSVRecord> = Stream.empty<CSVRecord?>().asSequence()
    private val charset = Charset.forName("ISO-8859-1")
  init  {
      generateCSVRecordSequence()
  }
     fun  generateCSVRecordSequence() = runBlocking {
        recordSequence = mapFileToCSVRecordSequence(filename).toValidated().toOption().orNull()?.body!!
    }

    private fun mapFileToCSVRecordSequence(path: String?): Effect<FileError, CsvRecordSequence> = effect {
        ensureNotNull(path) { EmptyPath}
        ensure(path.isNotEmpty()) {EmptyPath}
        ensure(File(path).exists()) {EmptyPath}
        try{
            val reader = withContext(Dispatchers.IO) {
                Files.newBufferedReader(Paths.get(path))
            }
            val parser = CSVParser.parse(
                reader,
                generateCSVFormat(path)
            )
            CsvRecordSequence(parser.records.asSequence())
        }catch(e: FileNotFoundException) {
            shift(FileNotFound(path))
        } catch (e: SecurityException) {
            shift(SecurityError(e.message))
        }
    }
    //TODO: refactor deprecated operations
    private fun generateCSVFormat(path:String):CSVFormat =
        when (path.endsWith("tsv")) {
            true -> CSVFormat.TDF.withFirstRecordAsHeader().withQuote(null).withIgnoreEmptyLines()
            false -> CSVFormat.RFC4180.withHeader().withIgnoreEmptyLines().withQuote(null)
        }

//   init {
//       try {
//           FileReader(aPath.toString()).use {
//               val parser = CSVParser.parse(
//                   aPath.toFile(), Charset.defaultCharset(),
//                   CSVFormat.RFC4180.withFirstRecordAsHeader()
//               )
//               recordSequence = parser.records.asSequence()
//           }
//       } catch (e: IOException) {
//          e.log()
//       }
//   }

    override fun get(): Sequence<CSVRecord> {
        return recordSequence
    }
}
