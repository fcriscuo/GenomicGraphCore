package org.batteryparkdev.genomicgraphcore.uniprot.dao

import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotModel

class UniprotModelDao(private val uniprotModel: UniprotModel) {

    fun generateUniprotCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${UniprotModel.nodename} \n")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node(['UniProt'], {entry_id: ${uniprotModel.entryId.formatNeo4jPropertyValue()} }," +
                "{ entry_name: ${uniprotModel.entryName.formatNeo4jPropertyValue()}, " +
                " protein_names: ${uniprotModel.proteinNames.parseToQuotedNeo4jStringList()}, " +
                " gene_names: ${uniprotModel.geneNames.parseToNeo4jStringList(' ')}, " +
                " protein_length: ${uniprotModel.length}, " +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS ${UniprotModel.nodename} \n"

    companion object : CoreModelDao {
        /*
    Complete the relationships for this UniProt node
    UniProt - [HAS_PUBLICATION] -> Publication
    UniProt - [INTERACTS_WITH] - UniProt
    Hgnc - [HAS_UNIPROT] -> UniProt
     */
        private fun completeRelationships(model: CoreModel): Unit {
            createPubMedRelationships(model)
            if (model is UniprotModel) {
                completeInteractionRelationships(model)
                completeHgncRelationship(model)
            }
        }

        private fun completeHgncRelationship(model: UniprotModel) {
            if(model.hgncId.isNotEmpty()){
                NodeIdentifierDao.defineRelationship(RelationshipDefinition(HgncModel.generateNodeIdentifierByValue(model.hgncId),
                model.getNodeIdentifier(),"HAS_UNIPROT"))
            }
        }

        private fun completeInteractionRelationships(model: UniprotModel) {
            model.interactList.map { it -> NodeIdentifier("UniProt", "entry_id", it) }
                .filter { Neo4jUtils.nodeExistsPredicate(it) }
                .map { nodeId -> RelationshipDefinition(model.getNodeIdentifier(), nodeId, "INTERACTS_WITH") }
                .filter { relDef -> Neo4jUtils.relationshipExistsPredicate(relDef).not() }
                .forEach { relDef -> NodeIdentifierDao.defineRelationship(relDef) }
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit = ::completeRelationships
    }
}