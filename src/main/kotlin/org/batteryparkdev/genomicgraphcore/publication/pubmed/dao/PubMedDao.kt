package org.batteryparkdev.genomicgraphcore.publication.pubmed.dao

import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubMedModel

class PubMedDao(private val model: PubMedModel) {

    private final val nodename = "pubmed"

    fun generatePubMedCypher(): String = generateMergeCypher()
        .plus(" RETURN  $nodename \n")


    // mao PubMedModel properties to Neo4j node properties
    // used for node creation and node update
    private fun mapPubMedProperties(): String =
        " cited_by_count: ${model.citedByCount}, " +
                " author: ${model.authors.formatNeo4jPropertyValue()}, " +
                " journal_issue: ${model.journalIssue.formatNeo4jPropertyValue()}, " +
                " pmc_id: ${model.pmcId.formatNeo4jPropertyValue()}, " +
                " title: ${model.articleTitle.removeInternalQuotes().formatNeo4jPropertyValue()}, " +
                " doi_id: ${model.doiId.formatNeo4jPropertyValue()}," +
                " reference_ids: ${formatIntList(model.referenceSet.joinToString(separator = "|"))}, " +
                " reference_count: ${model.referenceSet.size}, " +
                " citation_ids: ${formatIntList(model.citationSet.joinToString("|"))}"


    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node(['Publication'], " +
                "{ pub_id: ${model.pubmedId}}, " +
                "{ ${mapPubMedProperties()}, created: datetime()}, " +
                "{ ${mapPubMedProperties()}, last_mod: datetime()} ) YIELD node AS $nodename \n"

    companion object: CoreModelDao {
        override val modelRelationshipFunctions: (CoreModel) -> Unit
            get() = TODO("Not yet implemented")

    }

}