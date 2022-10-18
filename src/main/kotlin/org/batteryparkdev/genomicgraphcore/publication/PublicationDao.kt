package org.batteryparkdev.genomicgraphcore.publication

import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.service.LogService
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel
import org.neo4j.driver.Record

const val pubNodeName = "publication"


// supply a Neo4j query that will return all/limited PubMed node Ids that are
// currently placeholders
fun generatePublicationPlaceholderQuery( limit: Int = 0): String{
    return when (limit > 0){
        true ->  "MATCH (pub) WHERE (pub:Publication) AND NOT EXISTS(pub.title) " +
                " return pub.pub_id LIMIT $limit"
        false -> "MATCH (pub) WHERE (pub:Publication) AND NOT EXISTS(pub.title) " +
                " return pub.pub_id"
    }
}

fun getAllPlaceholderPubMedNodeIds(): Sequence<String> = runBlocking {
    Neo4jConnectionService.executeCypherQuery(generatePublicationPlaceholderQuery(0))
        .map { rec -> resolvePubMedIdentifier(rec) }
        .toList().asSequence()
}


fun gatAllPubmedIds(): Sequence<String> =
    Neo4jConnectionService.executeCypherQuery(
        "MATCH (pub:PubMed) RETURN pub.pub_id ")
        .map { rec -> resolvePubMedIdentifier(rec) }
        .toList().asSequence()

private fun resolvePubMedIdentifier(record: Record): String =
    record.asMap()["pub.pub_id"].toString()

fun pubmedNodeExistsPredicate(pubId: String): Boolean =
    publicationNodeExistsPredicate(
        NodeIdentifier(
            PubmedModel.nodelabel, PubmedModel.nodeIdProperty,
            pubId, "PubMed"
        )
    )

fun referenceNodeExistsPredicate(pubId: String): Boolean =
   publicationNodeExistsPredicate(
        NodeIdentifier(
            PubmedModel.nodelabel, PubmedModel.nodeIdProperty,
            pubId, "Reference"
        )
    )

private fun resolvePublicationLabelCondition(nodeIdentifier: NodeIdentifier): String =
    when (nodeIdentifier.secondaryLabel.isNotEmpty()){
       true -> "where $pubNodeName:${nodeIdentifier.primaryLabel} AND $pubNodeName:${nodeIdentifier.secondaryLabel} "
        false -> "where $pubNodeName:${nodeIdentifier.primaryLabel} "
    }

fun publicationNodeExistsPredicate(nodeId: NodeIdentifier): Boolean {
    when (nodeId.isValid()) {
        true -> {
            val cypher = "OPTIONAL MATCH ($pubNodeName) " +
                    resolvePublicationLabelCondition(nodeId) +
                    " AND $pubNodeName.pub_id = ${nodeId.idValue} " +
                    "return  $pubNodeName IS NOT NULL AS Predicate"
             println(cypher)
            return try {
                Neo4jConnectionService.executeCypherCommand(cypher).toBoolean()
            } catch (e: Exception) {
                LogService.exception(e)
                return false
            }
        }

        false -> LogService.warn("Invalid NodeIdentifier: $nodeId")
    }
    return false
}

fun main() {
    val pubmedNode = NodeIdentifier(
        PubmedModel.nodelabel, PubmedModel.nodeIdProperty,
        "16923108", "PubMed"
    )
    println("Should be true: ${pubmedNodeExistsPredicate("16923108")}")
    println("Should be false: ${referenceNodeExistsPredicate("10499589")}")
}