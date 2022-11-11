package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.go.GoTerm
import org.batteryparkdev.genomicgraphcore.go.Relationship
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

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
    private fun loadGoTermRelationship(goId: String, goRel: Relationship) {
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
    fun loadGoTermRelationships(goTerm: GoTerm) {
        val goId = goTerm.goId
        goTerm.relationshipList.forEach { rel ->
            run {
                if (GoTermDao.goTermNodeExistsPredicate(rel.targetId).not()) {
                    GoTermDao.createPlaceholderGoTerm(rel.targetId)
                }
                loadGoTermRelationship(goId, rel)
            }
        }
    }

}