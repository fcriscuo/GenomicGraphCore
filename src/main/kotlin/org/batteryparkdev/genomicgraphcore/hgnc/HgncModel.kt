package org.batteryparkdev.genomicgraphcore.hgnc

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier


/*
Represents the data used to define an HGNC node in the Neo4j database
 */

data class HgncModel(
    val hgncId: String, val geneName: String, val geneSymbol: String,
    val locusGroup: String, val locusType: String, val status: String,
    val location: String, val locationSortable: String, val aliasSymbol: String,
    val aliasNames: String, val prevSymbols: String, val prevNames: String,
    val geneGroups: String, val geneGroupId: Int, val entrezId: Int,
    val ensemblGeneId: String, val vegaId: String, val ucscId: String, val ena: String,
    val refSeqAccession: String, val ccdsId: String, val uniprotIdList: List<String>,
    val pubmedIds: List<String>, val mgdId: String, val regId: String, val lsdb: String,
    val cosmic: String, val omimId: Int, val mirbase: String, val homeodb: String,
    val snormabase: String, val bioparadigmsSls: String, val orphanet: Int,
    val pseudogeneOrg: String, val hordeId: String, val merops: String,
    val imgt: String, val iuphar: String, val kznfGeneCatalog: String,
    val mamitTrnadb: String, val cd: String, val lncrnadb: String, val enzymeId: String,
    val intermediateFilamentDb: String, val rnaCentralIdList: List<String>,
    val lncipedia: String, val gtRnaDb: String, val agr: String,
    val maneSelect: String, val gencc: String
) : CoreModel {

    override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier("HGNC", "hgnc_id", hgncId)

    override fun generateLoadModelCypher(): String = HgncDao(this).generateHgncCypher()

    override fun isValid(): Boolean = hgncId.isNotEmpty()
        .and(geneName.isNotEmpty())
        .and(geneSymbol.isNotEmpty())
        .and(status == "Approved")


    override fun getPubMedIds(): List<Int> = when (pubmedIds.isEmpty()) {
        true -> emptyList<Int>()
        false -> pubmedIds.map {it.toInt()  }
    }

    val isApprovedLocus: Boolean = status == "Approved"
    val isApprovedLocusTypeGroup = listOf("protein-coding gene", "non-coding RNA").contains(locusGroup)

    companion object : CoreModelCreator {

        val nodename = "hgnc"   // used as a variable in Cypher statements for this entity

        fun parseCsvRecord(record: CSVRecord): CoreModel = HgncModel(
            record.get("hgnc_id"), record.get("name"), record.get("symbol"),
            record.get("locus_group"), record.get("locus_type"), record.get("status"),
            record.get("location"), record.get("location_sortable"),
            record.get("alias_symbol"),
            record.get("alias_name"),
            record.get("prev_symbol"),
            record.get("prev_name"),
            record.get("gene_group"),
            record.get("gene_group_id").parseValidInteger(),
            record.get("entrez_id").parseValidInteger(),
            record.get("ensembl_gene_id"), record.get("vega_id"), record.get("ucsc_id"),
            record.get("ena"), record.get("refseq_accession"),
            record.get("ccds_id"),
            record.get("uniprot_ids").parseOnPipe(),
            record.get("pubmed_id").parseOnPipe(),
            record.get("mgd_id"), record.get("rgd_id"), record.get("lsdb"), record.get("cosmic"),
            record.get("omim_id").parseValidInteger(),
            record.get("mirbase"), record.get("homeodb"),
            record.get("snornabase"), record.get("bioparadigms_slc"),
            record.get("orphanet").parseValidInteger(),
            record.get("pseudogene.org"), record.get("horde_id"),
            record.get("merops"), record.get("imgt"), record.get("iuphar"), record.get("kznf_gene_catalog"),
            record.get("mamit-trnadb"), record.get("cd"), record.get("lncrnadb"),
            record.get("enzyme_id"), record.get("intermediate_filament_db"),
            record.get("rna_central_ids").parseOnPipe(),
            record.get("lncipedia"), record.get("gtrnadb"), record.get("agr"),
            record.get("mane_select"), record.get("gencc")
        )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel = ::parseCsvRecord


    }

}