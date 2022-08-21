package org.batteryparkdev.genomicgraphcore.common.io

import java.nio.file.Paths

/*
 Test using a small sample file
  */
fun main() {
    val path = Paths.get("./data/classification.csv")
    println("Processing csv file ${path.fileName}")
    CsvRecordSequenceSupplier(path).get().asSequence().drop(6879)
        .forEach {record -> println(record.get(0)) }
}