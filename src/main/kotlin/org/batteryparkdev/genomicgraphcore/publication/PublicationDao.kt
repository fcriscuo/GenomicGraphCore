package org.batteryparkdev.genomicgraphcore.publication

import org.batteryparkdev.genomicgraphcore.common.service.LogService
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubMedModel

const val pubNodeName = "publication"

fun pubmedNodeExistsPredicate(pubId: String): Boolean =
    publicationNodeExistsPredicate(
        NodeIdentifier(
            PubMedModel.nodelabel, PubMedModel.nodeIdProperty,
            pubId, "PubMed"
        )
    )

fun referenceNodeExistsPredicate(pubId: String): Boolean =
   publicationNodeExistsPredicate(
        NodeIdentifier(
            PubMedModel.nodelabel, PubMedModel.nodeIdProperty,
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
        PubMedModel.nodelabel, PubMedModel.nodeIdProperty,
        "16923108", "PubMed"
    )
    println("Should be true: ${pubmedNodeExistsPredicate("16923108")}")
    println("Should be false: ${referenceNodeExistsPredicate("10499589")}")
}