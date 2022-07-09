package org.batteryparkdev.genomicgraphcore.model.hgnc

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.model.CoreModel
import org.batteryparkdev.genomicgraphcore.model.ModelFunctions
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier

/*
Represents the data used to define a Hgnc node in the Neo4j database
 */

data class Hgnc( val hgncId: String, val geneName: String, val geneSymbol: String,
                 val locusGroup: String, val locusType: String, val status: String,
                 val location: String, val locationSortable: String, val aliasSymbol: String,
                 val aliasName: String, val prevSymbol: String, val prevName: String,
                 val geneGroup: String, val geneGroupId: String, val entrezId: Int,
                 val ensemblGeneId: String, val vegaId: String, val ucscId: String, val ena: String,
                 val refSeqAccession: String, val ccdsId: String, val uniprotIdList:List<String>,
                 val pubmedIdList: List<Int>,val mgdId: String, val regId: String, val lsdb: String,
                 val cosmic: String, val omimId: Int, val mirbase: String, val homeodb: String,
                 val snormabase: String, val bioparadigmsSls: String, val orphanet: Int,
                 val pseudogeneOrg: String, val hordeId: String, val merops: String,
                 val imgt: String, val iuphar: String, val kznfGeneCatalog: String,
                 val mamitTrnadb: String, val cd: String, val lncrnadb: String, val enzymeId: String,
                 val intermediateFilamentDb: String, val rnaCentralIdList : List<String>,
                 val lncipedia: String, val gtRnaDb: String, val agr: String,
                 val maneSelect: String, val gencc: String
): CoreModel {
    override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier("HGNC", "hgnc_id", hgncId)
    
    override fun generateLoadModelCypher(): String {
        TODO("Not yet implemented")
    }

    override fun isValid(): Boolean = hgncId.isNotEmpty()
        .and(geneName.isNotEmpty())
        .and(geneSymbol.isNotEmpty())
        .and(status == "Approved")

    override fun getPubMedId(): Int = when (pubmedIdList.isEmpty()){
        true -> 0
        false -> pubmedIdList[0]
    }

    companion object: ModelFunctions {
       val nodename = "hgnc"   // used a variable in Cypher statements for this entity

        fun parseCSVRecord(record: CSVRecord): Hgnc =
            Hgnc(
                record.get("hgnc_id"),  record.get("symbol"), record.get("name"),
                record.get("locus_group"), record.get("locus_type"), record.get("status"),
                record.get("location"), record.get("location_sortable"), record.get("alias_symbol"),
                record.get("alias_name"), record.get("prev_symbol"), record.get("prev_name"),
                record.get("gene_group"), record.get("gene_group_id"),
                parseValidIntegerFromString(record.get("entrez_id")),
                record.get("ensembl_gene_id"), record.get("vega_id"), record.get("ucsc_id"),
                record.get("ena"), record.get("refseq_accession"), record.get("ccds_id"),
                parseStringOnPipe(record.get("uniprot_ids")),
                parsePubMedIds(record.get("pubmed_id")),
                record.get("mgd_id"), record.get("rgd_id"), record.get("lsdb"), record.get("cosmic"),
                parseValidIntegerFromString(record.get("omim_id")),
                record.get("mirbase"), record.get("homeodb"),
                record.get("snornabase"), record.get("bioparadigms_slc"),
                parseValidIntegerFromString(record.get("orphanet")),
                record.get("pseudogene.org"), record.get("horde_id"),
                record.get("merops"), record.get("imgt"), record.get("iuphar"), record.get("kznf_gene_catalog"),
                record.get("mamit-trnadb"), record.get("cd"), record.get("lncrnadb"),
                record.get("enzyme_id"), record.get("intermediate_filament_db"),
                parseStringOnPipe(record.get("rna_central_ids")),
                record.get("lncipedia"), record.get("gtrnadb"), record.get("agr"),
                record.get("mane_select"), record.get("gencc")
            )

        private fun parsePubMedIds(pubmedIds: String): List<Int>  =
           when (pubmedIds.isEmpty()) {
               true -> listOf(0)
               false -> parseStringOnPipe(pubmedIds).map{it.toInt()}.toList()
           }
        }

}