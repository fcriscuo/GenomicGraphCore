package org.batteryparkdev.genomicgraphcore.common.io

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.service.LogService
import java.io.FileReader
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.streams.asSequence

/*
Responsible for returning a Stream of CSVRecords from a
specified Path (i.e. CSV file)
First record in file is treated as a header
 */

class CsvRecordSequenceSupplier( aPath: Path) : Supplier<Sequence<CSVRecord>> {

    private var recordSequence: Sequence<CSVRecord> = Stream.empty<CSVRecord?>().asSequence()
   init {
       try {
           FileReader(aPath.toString()).use {
               val parser = CSVParser.parse(
                   aPath.toFile(), Charset.defaultCharset(),
                   CSVFormat.RFC4180.withFirstRecordAsHeader()
               )
               recordSequence = parser.records.asSequence()
           }
       } catch (e: IOException) {
          LogService.logException(e)
       }
   }

    override fun get(): Sequence<CSVRecord> {
        return recordSequence
    }
}
