package org.batteryparkdev.genomicgraphcore.common.datamining

import org.apache.commons.io.FileUtils
import org.batteryparkdev.genomicgraphcore.common.service.LogService

import java.io.File

fun deleteExistingTempFile(filename: String): Unit {
    val file: File = File(filename)
    if (file.exists()) {
        FileUtils.delete(file)
        LogService.info("Existing temp file: $filename deleted")
    }
}

fun main() {
    val filename = "/tmp/test_uniprot.tsv"
    deleteExistingTempFile(filename)
    val url: String =
        "https://rest.uniprot.org/uniprotkb/stream?fields=accession%2Creviewed%2Cid%2Cprotein_name%2Cgene_names%2Corganism_name%2Clength&format=tsv&query=%28reviewed%3Atrue%29+AND+%28model_organism%3A9606%29"
    val lineCount = loadFileFromUrl(filename, url)
    val success = (lineCount > 20_000)// file size may vary but should be > 20K
    LogService.info("line count = $lineCount   Success = $success")
    deleteExistingTempFile(filename)
}