package org.batteryparkdev.genomicgraphcore.common.io

import java.nio.file.Paths

/*
 Test using a small sample file
  */
fun main() {
    val filename ="./data/sample-uniprot.tsv"
    println("Processing csv file $filename")
    CsvRecordSequenceSupplier(filename).get().drop(100)
        .forEach {record -> println(record.get(0)) }
}