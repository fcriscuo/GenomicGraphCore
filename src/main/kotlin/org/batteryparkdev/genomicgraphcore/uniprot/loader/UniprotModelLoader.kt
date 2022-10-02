package org.batteryparkdev.genomicgraphcore.uniprot.loader

import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotModel

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "/Volumes/SSD870/data/UniProt/uniprot-2022.09.16-21.09.47.10.tsv"
    CoreModelLoader(UniprotModel).loadDataFile(filename)
}