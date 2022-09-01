package org.batteryparkdev.genomicgraphcore.hgnc

import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "/Volumes/SSD870/HGNC/hgnc_complete_set.tsv"
    CoreModelLoader(HgncModel, HgncDao).loadDataFile(filename)
}