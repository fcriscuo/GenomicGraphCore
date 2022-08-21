package org.batteryparkdev.genomicgraphcore.hgnc


import org.batteryparkdev.genomicgraphcore.common.formatIntList
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseToLNeo4jStringList

class HgncDao (private val hgncModel: HgncModel)  {

    private final val nodename = "hgnc"

    fun generateHgncCypher(): String = generateMergeCypher()
        .plus(" RETURN  $nodename \n")
    
    /*
    Use locus type property as a second label
    Neo4j does not support multi-word labels  protein-coding gene -> protein-coding_gene
     */
    private fun generateLabels(): String =
        "\"Hgnc\", \"${hgncModel.locusType.replace(" ","_")}\""

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([${generateLabels()}], " +
                "{hgnc_id: ${hgncModel.hgncId.formatNeo4jPropertyValue()}}," +
                " { gene_name: ${hgncModel.geneName.formatNeo4jPropertyValue()}," +
                " gene_symbol: ${hgncModel.geneSymbol.formatNeo4jPropertyValue()}," +
                " locus_group: ${hgncModel.locusGroup.formatNeo4jPropertyValue()}," +
                " locus_type: ${hgncModel.locusType.formatNeo4jPropertyValue()}, " +
                " location: ${hgncModel.location.formatNeo4jPropertyValue()}, " +
                " location_sortable: ${hgncModel.locationSortable.formatNeo4jPropertyValue()}," +
                " alias_symbols: ${hgncModel.aliasSymbol.parseToLNeo4jStringList()}," +
                " alias_names: ${hgncModel.aliasNames.parseToLNeo4jStringList()}," +
                " prev_symbols: ${hgncModel.prevSymbols.parseToLNeo4jStringList()}, " +
                " prev_names: ${hgncModel.prevNames.parseToLNeo4jStringList()}, " +
                " gene_groups: ${hgncModel.geneGroups.parseToLNeo4jStringList()}, " +
                " gene_group_id: ${hgncModel.geneGroupId}, " +
                " entrez_id: ${hgncModel.entrezId}, " +
                " ensembl_gene_id: ${hgncModel.ensemblGeneId.formatNeo4jPropertyValue()}, " +
                " vega_id: ${hgncModel.vegaId.formatNeo4jPropertyValue()}, " +
                " ucsc_id: ${hgncModel.ucscId.formatNeo4jPropertyValue()}, " +
                " enas: ${hgncModel.ena.parseToLNeo4jStringList()}, " +
                " refseq_accession: ${hgncModel.refSeqAccession.formatNeo4jPropertyValue()}, " +
                " ccds_id: ${hgncModel.ccdsId.formatNeo4jPropertyValue()}, " +
                " pubmed_ids: ${formatIntList(hgncModel.pubmedIds)}, " +
                " mgd_ids: ${hgncModel.mgdId.parseToLNeo4jStringList()}, " +
                " reg_id: ${hgncModel.regId.formatNeo4jPropertyValue()}, " +
                " lsdb: ${hgncModel.lsdb.formatNeo4jPropertyValue()}, " +
                " cosmic: ${hgncModel.cosmic.formatNeo4jPropertyValue()}, " +
                " omim_id: ${hgncModel.omimId}," +
                " mirbase: ${hgncModel.mirbase.formatNeo4jPropertyValue()}, " +
                " homeodb: ${hgncModel.homeodb.formatNeo4jPropertyValue()}, " +
                " snormabase: ${hgncModel.snormabase.formatNeo4jPropertyValue()}," +
                " bioparadigms_sls: ${hgncModel.bioparadigmsSls.formatNeo4jPropertyValue()}," +
                " orphanet: ${hgncModel.orphanet}," +
                " pseudogene_org: ${hgncModel.pseudogeneOrg.formatNeo4jPropertyValue()}, " +
                " horde_id: ${hgncModel.hordeId.formatNeo4jPropertyValue()}, " +
                " merops: ${hgncModel.merops.formatNeo4jPropertyValue()}," +
                " imgt: ${hgncModel.imgt.formatNeo4jPropertyValue()}, " +
                " iuphar: ${hgncModel.iuphar.formatNeo4jPropertyValue()}, " +
                " kzng_gene_catalog: ${hgncModel.kznfGeneCatalog.formatNeo4jPropertyValue()}," +
                " mamit_trna_db: ${hgncModel.mamitTrnadb.formatNeo4jPropertyValue()}," +
                " cd: ${hgncModel.cd.formatNeo4jPropertyValue()}," +
                " lnc_rna_db: ${hgncModel.lncrnadb.formatNeo4jPropertyValue()}, " +
                " enzyme_id: ${hgncModel.enzymeId.formatNeo4jPropertyValue()}, " +
                " intermediate_filament_db: ${hgncModel.intermediateFilamentDb.formatNeo4jPropertyValue()} ," +
                " rna_central_ids: [${hgncModel.rnaCentralIdList.joinToString (separator = ",")}], " +
                " lncipedia: ${hgncModel.lncipedia.formatNeo4jPropertyValue()}, " +
                " gt_rna_db: ${hgncModel.gtRnaDb.formatNeo4jPropertyValue()}," +
                " agr: ${hgncModel.agr.formatNeo4jPropertyValue()}, " +
                " mane_select: ${hgncModel.maneSelect.formatNeo4jPropertyValue()}, " +
                " gencc: ${hgncModel.gencc.formatNeo4jPropertyValue()}," +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS $nodename \n"

}