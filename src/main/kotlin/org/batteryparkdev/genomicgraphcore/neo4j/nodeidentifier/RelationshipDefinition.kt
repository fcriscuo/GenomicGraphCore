package org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

/*
Represents the data attributes needed to identify two (2) nodes and create a
specified relationship between them

 */
data class RelationshipDefinition (
    val parentNode: NodeIdentifier,
    val childNode: NodeIdentifier,
    val relationshipType: String,
    val relationshipProperty: RelationshipProperty = RelationshipProperty()
) {
    fun isValid():Boolean =
        parentNode.isValid().and(childNode.isValid()).and(relationshipType.isNotBlank())

    fun generateRelationshipCypher() =
        "MERGE (p: ${parentNode.primaryLabel}{ ${parentNode.idProperty}: " +
                "${parentNode.idValue.formatNeo4jPropertyValue()}}) " +
                " MERGE (c: ${childNode.primaryLabel}{ ${childNode.idProperty}: " +
                "${childNode.idValue.formatNeo4jPropertyValue()} })" +
                "MERGE (p) -[r: ${relationshipType} ] -> (c) " +
                "RETURN p.${parentNode.idProperty} "

    fun deleteRelationshipDefinitionCypher() =
        "MATCH (parent:${parentNode.primaryLabel}), " +
                " (child:${childNode.primaryLabel}) WHERE " +
                " parent.${parentNode.idProperty} = " +
                "${parentNode.idValue.formatNeo4jPropertyValue()} " +
                " AND child.${childNode.idProperty} = " +
                "${childNode.idValue.formatNeo4jPropertyValue()} " +
                " MATCH  (parent) -[r:${relationshipType}] -> (child) " +
                " DELETE r "
}

data class RelationshipProperty(
    val relPropertyName:String ="",
    val relPropertyValue: String =""
) {
    fun isValid():Boolean =
        relPropertyName.isNotEmpty().and(relPropertyValue.isNotEmpty())
}
