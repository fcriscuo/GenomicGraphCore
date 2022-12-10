package org.batteryparkdev.genomicgraphcore.common.service

object FilesPropertyService {

    private val config = ApplicationProperties("files.config")
    val ftpUserEmail = config.getConfigPropertyAsString("ftp.user.email")
    val baseDataPath = config.getConfigPropertyAsString("base.data.path")
    val baseDataSubdirectory = config.getConfigPropertyAsString("base.subdirectory.name")
    val hgncFtpUrl= config.getConfigPropertyAsString("hgnc.ftp.complete.set.url")
    val hgncLocalCompleteSetFilename= config.getConfigPropertyAsString("hgnc.local.filename")
    val uniprotIdFilename = config.getConfigPropertyAsString("uniprot.id.local.filename")
   // val uniprotRestUrl="https://rest.uniprot.org/uniprotkb/stream?fields=accession%2Creviewed%2Cid%2Cprotein_name%2Cgene_names%2Clength%2Cgene_synonym%2Corganelle%2Ccc_interaction%2Clit_pubmed_id%2Cxref_hgnc&format=tsv&query=%28%2A%29%20AND%20%28model_organism%3A9606%29%20AND%20%28reviewed%3Atrue%29"
   // val uniprotLocalFilename="/tmp/uniprot.tsv"
    val uniprotRestUrl=config.getConfigPropertyAsString("uniprot.core.rest.url")
    val uniprotIdentifiersRestUrl = config.getConfigPropertyAsString("uniprot.identifiers.url")
    val uniprotLocalFilename=config.getConfigPropertyAsString("uniprot.core.filename")
    val geneontologyDownloadUrl=config.getConfigPropertyAsString("geneontology.download.url")
    val humanPhenotypeDownloadUrl = config.getConfigPropertyAsString("humanpheno.ontology.download.url")

}