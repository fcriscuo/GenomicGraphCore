package org.batteryparkdev.genomicgraphcore.common

import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "/Volumes/SSD870/HGNC/hgnc_complete_set.tsv"
    CoreModelLoader(HgncModel).loadDataFile(filename)
}