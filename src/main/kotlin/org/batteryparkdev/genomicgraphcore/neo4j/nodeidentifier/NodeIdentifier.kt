package org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

/*
Represents a data class whose properties contain sufficient information to
identify an individual node in a Neo4j graph database
 */
data class NodeIdentifier(
    val primaryLabel: String,
    val idProperty: String,
    val idValue: String,
    val secondaryLabel:String="",
){
    fun isValid():Boolean =
        primaryLabel.isNotBlank().and(idProperty.isNotBlank()).and(idValue.isNotBlank())

    fun mergeNodeIdentifierCypher():String =
        when (secondaryLabel.isNotEmpty()){
            true -> "MERGE (n:$primaryLabel:$secondaryLabel{$idProperty: " +
                    "${idValue.formatNeo4jPropertyValue()}}) " +
                    "RETURN n.$idProperty"
            false -> "MERGE (n:$primaryLabel {$idProperty: ${idValue.formatNeo4jPropertyValue()}}) " +
                    "RETURN n.$idProperty"
        }

    fun addNodeLabelCypher():String =
        "MATCH (child:$primaryLabel{$idProperty:" +
                " ${idValue.formatNeo4jPropertyValue()} }) " +
                " WHERE apoc.label.exists(child,${secondaryLabel.formatNeo4jPropertyValue()})  = false " +
                " CALL apoc.create.addLabels(child, [${secondaryLabel.formatNeo4jPropertyValue()}] )" +
                " yield node return node"


    fun mapNodeIdentifierToCypherString():String =
        "(:$primaryLabel { $idProperty: ${idValue.formatNeo4jPropertyValue()}})"

}