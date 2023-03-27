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

private fun generateNeedsPropertiesQuery(limit: Int=Integer.MAX_VALUE):String =
    "MATCH (pub:Publication) WHERE pub.needs_properties=true " +
            " return pub.pub_id LIMIT $limit"

private fun generateNeedsRefsQuery(limit: Int=Integer.MAX_VALUE):String =
    "MATCH (pub:Publication) WHERE pub.needs_refs=true " +
            " return pub.pub_id LIMIT $limit"


@OptIn(ExperimentalStdlibApi::class)
fun getAllPublicationPlaceholderPubIdsByType(type:String): Sequence<String> = runBlocking {
  when (type.lowercase()){
        "properties" -> Neo4jConnectionService.executeCypherQuery(generateNeedsPropertiesQuery())
            .map { rec -> resolvePubMedIdentifier(rec) }
            .toList().asSequence()
       "refs" -> Neo4jConnectionService.executeCypherQuery(generateNeedsRefsQuery())
           .map { rec -> resolvePubMedIdentifier(rec) }
           .toList().asSequence()
       else -> emptySequence()
    }
}

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