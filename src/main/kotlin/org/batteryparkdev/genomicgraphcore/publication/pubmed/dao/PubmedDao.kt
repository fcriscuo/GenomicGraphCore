package org.batteryparkdev.genomicgraphcore.publication.pubmed.dao

import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedReference
import org.batteryparkdev.genomicgraphcore.publication.pubmedNodeExistsPredicate
import org.batteryparkdev.genomicgraphcore.publication.referenceNodeExistsPredicate

class PubmedDao(private val model: PubmedModel) {

    private final val nodename = "pubmed"

    fun generatePubMedPropertiesCypher(): String = generateMergeCypher()
        .plus(" RETURN  $nodename \n")

    // map PubMedModel properties to Neo4j node properties
    // used for node creation and node update
    private fun mapPubMedProperties(): String =
        " cited_by_count: ${model.citedByCount}, " +
                " authors: [${model.authors.joinToString(separator = ",")}], " +
                " journal_issue: ${model.journalIssue.formatNeo4jPropertyValue()}, " +
                " pmc_id: ${model.pmcId.formatNeo4jPropertyValue()}, " +
                " title: ${model.articleTitle.removeInternalQuotes().formatNeo4jPropertyValue()}, " +
                " abstract: ${model.abstract.removeInternalQuotes().formatNeo4jPropertyValue()}, " +
                " doi_id: ${model.doiId.formatNeo4jPropertyValue()}," +
                " reference_ids: ${formatIntList(model.getPubMedIds().joinToString(separator = "|"))}, " +
                " reference_count: ${model.referenceList.size}, " +
                " citation_ids: ${formatIntList(model.citationSet.joinToString("|"))}"

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node(['Publication','PubMed'], " +
                "{ pub_id: ${model.pubmedId}}, " +
                "{ ${mapPubMedProperties()}, created: datetime()}, " +
                "{ ${mapPubMedProperties()}, last_mod: datetime()} ) YIELD node AS $nodename \n" +
                " MATCH (p:Publication) WHERE p.pub_id = ${model.pubmedId} SET p.needs_properties=false"



    companion object : CoreModelDao {
       // create placeholder nodes for references
        private fun completeReferenceRelationships(model: CoreModel): Unit {
            if (model is PubmedModel){

            }
        }
        override val modelRelationshipFunctions: (CoreModel) -> Unit = ::completeReferenceRelationships
    }
}

class ReferenceDao(private val model: PubmedReference) {
    /*
    A Reference node with the same id, may have been loaded previously, either as a reference for another publication
    or as a PubMed entry. If there is a PubMed node, add a Reference label. If the Reference
    already exists, do not load the  current one.
     */
    private final val nodename = "reference"
    fun generateReferenceCypher(): String {
        val defaultCypher = "MATCH (p:Publication{pub_id: ${model.referencePubmedId} }) RETURN p "
        if (pubmedNodeExistsPredicate((model.referencePubmedId.toString()))) {
            // add a Reference label to an existing PubMed node
            Neo4jUtils.addLabelToNode(model.getNodeIdentifier())
            return defaultCypher
        }
        return when (referenceNodeExistsPredicate(model.referencePubmedId.toString())) {
            true ->  defaultCypher
            false -> generateMergeCypher()
        }
    }

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node(['Publication','Reference'], " +
                "{ pub_id: ${model.referencePubmedId}}," +
                "{ citation: ${model.citation.citation.formatNeo4jPropertyValue()}, " +
                "  issue: ${model.citation.issue.formatNeo4jPropertyValue()}," +
                when (model.citation.doiUrl.isNotEmpty()){
                    true -> " doi_url: ${model.citation.doiUrl.formatNeo4jPropertyValue()}, "
                    false -> " "
                } +
                " created: datetime()}, {last_mod: datetime()} ) YIELD node AS $nodename "
                    .plus(" RETURN  $nodename \n")

    companion object : CoreModelDao {

        private fun completeRelationships(model: CoreModel) {
            if (model is PubmedReference) {
                val pubmedNode = PubmedModel.generateNodeIdentifierByValue(model.parentId.toString())
                val referenceNode = PubmedReference.generateNodeIdentifierByValue(model.referencePubmedId.toString())
                NodeIdentifierDao.defineRelationship(
                    RelationshipDefinition(
                        pubmedNode, referenceNode,
                        "HAS_REFERENCE"
                    )
                )
            }
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit = ::completeRelationships
    }
}