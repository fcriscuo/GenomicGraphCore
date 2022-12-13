package org.batteryparkdev.genomicgraphcore.ontology.obo.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboRelationship
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for creating and managing neo4j labeled relationships between
OboTerm nodes

OboTerm -[HAS_OBO_RELATIONSHIP] -> OboRelationship
 */
object OboRelationshipDao {

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadOboTermRelationship(oboId: String, oboRel: OboRelationship) {
        val relType = when (oboRel.type == "relationship") {
            true -> oboRel.qualifier.uppercase()
            false -> oboRel.type.uppercase()
        }

        Neo4jConnectionService.executeCypherCommand(
            "MATCH (got1:OboTerm), (got2:OboTerm) WHERE " +
                    " got1.obo_id = ${oboId.formatNeo4jPropertyValue()}  " +
                    " AND got2.obo_id = ${oboRel.targetId.formatNeo4jPropertyValue()} " +
                    " MERGE (got1) - [r:$relType] -> (got2) " +
                    " RETURN r"
        )
    }

    /*
    Public function to create a Relationship node for each of a GO Term's relationships
    A placeholder OboTerm node is created for the relationship target node if that term
    has not been loaded yet
     */
    fun persistOboTermRelationships(oboTerm: OboTerm) {
        val goId = oboTerm.id
        oboTerm.relationshipList
            .forEach {rel ->
            run {
                if (Neo4jUtils.nodeExistsPredicate( NodeIdentifier("OboTerm", "obo_id", rel.targetId)).not()) {
                    createPlaceholderOboTerm(rel.targetId,rel.description, oboTerm.namespace)
                }
                loadOboTermRelationship(goId, rel)
            }
        }
    }

}