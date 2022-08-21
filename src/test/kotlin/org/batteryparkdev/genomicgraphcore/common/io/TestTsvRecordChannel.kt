package org.batteryparkdev.genomicgraphcore.common.io

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_hgnc_set.tsv"
    TsvRecordChannel.displayRecords(filename)
}