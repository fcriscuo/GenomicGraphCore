package org.batteryparkdev.genomicgraphcore.common.datamining

fun main() {
    val filename = "/tmp/uniprot.tsv"
    val url: String = "https://rest.uniprot.org/uniprotkb/stream?fields=accession%2Creviewed%2Cid%2Cprotein_name%2Cgene_names%2Corganism_name%2Clength&format=tsv&query=%28reviewed%3Atrue%29+AND+%28model_organism%3A9606%29"
   val lineCount = loadFileFromUrl(filename, url)
    val success =assert(lineCount > 20_000) // file size may vary but should be > 20K
    println("line count = $lineCount   Success = $success")

}