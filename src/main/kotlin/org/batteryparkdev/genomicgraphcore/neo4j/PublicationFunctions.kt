package org.batteryparkdev.genomicgraphcore.neo4j

import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Function to create a HAS_PUBLICATION relationship between a specified node and
a Publication node
A placeholder Publication node will be created if it does not exist
 */

private const val pubRelType = "HAS_PUBLICATION"

fun processPublications(model: CoreModel) {
    val nodeIdentifier = model.getNodeIdentifier()
    model.getPubMedIds().map{it.toString()}.forEach { registerPublication(nodeIdentifier, it) }
}

fun registerPublication(parent: NodeIdentifier,  pubId: String) {
    val pubNode = NodeIdentifier("Publication","pub_id", pubId,"PubMed")
    if (!Neo4jUtils.nodeExistsPredicate(pubNode).not()) {
        NodeIdentifierDao.createPlaceholderNode(pubNode)
    }
    NodeIdentifierDao.defineRelationship(RelationshipDefinition(parent, pubNode, pubRelType))
}

