package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboRelationship
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for creating and managing neo4j labeled relationships between
GoTerm nodes
 */
object GoRelationshipDao {

    private const val relationshipCypher =
        "MATCH (got1:GoTerm), (got2:GoTerm) WHERE " +
                "got1.go_id = SOURCE  AND got2.go_id = TARGET " +
                " MERGE (got1) - [r:RELATIONSHIP] -> (got2) " +
                " ON CREATE SET " +
                " r+= {description: DESCRIPTION}" +
                " RETURN r"

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadGoTermRelationship(goId: String, goRel: OboRelationship) {
        val cypher = relationshipCypher.replace("SOURCE", goId.formatNeo4jPropertyValue())
            .replace("TARGET", goRel.targetId.formatNeo4jPropertyValue())
            .replace("RELATIONSHIP", goRel.type.uppercase())
            .replace("DESCRIPTION", goRel.description.formatNeo4jPropertyValue())
        Neo4jConnectionService.executeCypherCommand(cypher)
    }

    /*
    Public function to create a Relationship node for each of a GO Term's relationships
    A placeholder GoTerm node is created for the relationship target node if that term
    has not been loaded yet
     */
    fun loadGoTermRelationships(oboTerm: OboTerm) {
        val goId = oboTerm.id
        oboTerm.relationshipList
            .forEach {rel ->
            run {
                if (Neo4jUtils.nodeExistsPredicate( NodeIdentifier("OboTerm", "obo_id", rel.targetId)).not()) {
                    GoTermDao.createPlaceholderGoTerm(rel.targetId)
                }
                loadGoTermRelationship(goId, rel)
            }
        }
    }

}