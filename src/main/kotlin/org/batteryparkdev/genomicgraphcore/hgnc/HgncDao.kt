package org.batteryparkdev.genomicgraphcore.hgnc


import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseToNeo4jStringList
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

/*
Responsible for data access operations for HGNC data in the Neo4j database
 */
class HgncDao(private val hgncModel: HgncModel){

    private final val nodename = "hgnc"

    fun generateHgncCypher(): String = generateMergeCypher()
        .plus(" RETURN  $nodename \n")

    /*
    Use locus type property as a second label
    Neo4j does not support multi-word labels
    Replace spaces with underscores: protein-coding gene -> protein-coding_gene
     */
    private fun generateLabels(): String =
        "\"Hgnc\", \"${hgncModel.locusType.replace(" ", "_")}\""

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([${generateLabels()}], " +
                "{hgnc_id: ${hgncModel.hgncId.formatNeo4jPropertyValue()}}," +
                " { gene_name: ${hgncModel.geneName.formatNeo4jPropertyValue()}," +
                " gene_symbol: ${hgncModel.geneSymbol.formatNeo4jPropertyValue()}," +
                " locus_group: ${hgncModel.locusGroup.formatNeo4jPropertyValue()}," +
                " locus_type: ${hgncModel.locusType.formatNeo4jPropertyValue()}, " +
                " location: ${hgncModel.location.formatNeo4jPropertyValue()}, " +
                " location_sortable: ${hgncModel.locationSortable.formatNeo4jPropertyValue()}," +
                " alias_symbols: ${hgncModel.aliasSymbol.parseToNeo4jStringList()}," +
                " alias_names: ${hgncModel.aliasNames.parseToNeo4jStringList()}," +
                " prev_symbols: ${hgncModel.prevSymbols.parseToNeo4jStringList()}, " +
                " prev_names: ${hgncModel.prevNames.parseToNeo4jStringList()}, " +
                " gene_groups: ${hgncModel.geneGroups.parseToNeo4jStringList()}, " +
                " gene_group_id: ${hgncModel.geneGroupId}, " +
                " entrez_id: ${hgncModel.entrezId}, " +
                " ensembl_gene_id: ${hgncModel.ensemblGeneId.formatNeo4jPropertyValue()}, " +
                " vega_id: ${hgncModel.vegaId.formatNeo4jPropertyValue()}, " +
                " ucsc_id: ${hgncModel.ucscId.formatNeo4jPropertyValue()}, " +
                " enas: ${hgncModel.ena.parseToNeo4jStringList()}, " +
                " refseq_accession: ${hgncModel.refSeqAccession.formatNeo4jPropertyValue()}, " +
                " ccds_id: ${hgncModel.ccdsId.parseToNeo4jStringList()}, " +
                " pubmed_ids: [${hgncModel.pubmedIds.joinToString(separator = ",")}], " +
                " mgd_ids: ${hgncModel.mgdId.parseToNeo4jStringList()}, " +
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
                " rna_central_ids: [${hgncModel.rnaCentralIdList.joinToString(separator = ",")}], " +
                " lncipedia: ${hgncModel.lncipedia.formatNeo4jPropertyValue()}, " +
                " gt_rna_db: ${hgncModel.gtRnaDb.formatNeo4jPropertyValue()}," +
                " agr: ${hgncModel.agr.formatNeo4jPropertyValue()}, " +
                " gencc: ${hgncModel.gencc.formatNeo4jPropertyValue()}," +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS $nodename \n"

    /*
    Complete the relationships for this Hgnc node
    Hgnc - [HAS_PUBLICATION] -> Publication

     */
    companion object: CoreModelDao{
        fun completeRelationships( model: CoreModel): Unit{
            createPubMedRelationships(model)
        }

        private fun createPubMedRelationships(model: CoreModel) {
            model.getPubMedIds().forEach { pubId ->
                run {
                    val parentNodeId = model.getNodeIdentifier()
                    val pubNodeId = NodeIdentifier("Publication", "pub_id", pubId.toString(), "PubMed")
                    NodeIdentifierDao.createPlaceholderNode(pubNodeId)
                    RelationshipDefinition(parentNodeId, pubNodeId, "HAS_PUBLICATION").also {
                        NodeIdentifierDao.defineRelationship(it)
                    }
                }
            }
        }

        /*
        Hgnc nodes typically only have incoming relationships
        Provide a function that allows any node with an HGNC ID property
        to establish a HAS_HGNC relationship
         */
        fun registerChildRelationshipToHgnc( hgncId: String, relatedModel: CoreModel) {
            val hgncNode = NodeIdentifier("Hgnc", "hgnc_id", hgncId)
            NodeIdentifierDao.defineRelationship(RelationshipDefinition(
                relatedModel.getNodeIdentifier(), hgncNode, "HAS_HGNC"
            ))
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
         = ::completeRelationships

    }



}