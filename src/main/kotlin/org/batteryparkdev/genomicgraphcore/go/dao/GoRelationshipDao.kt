package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboRelationship
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for creating and managing neo4j labeled relationships between
OboTerm nodes
 */
object GoRelationshipDao {

    private const val relationshipCypher =
        "MATCH (got1:OboTerm), (got2:OboTerm) WHERE " +
                "got1.obo_id = SOURCE  AND got2.obo_id = TARGET " +
                " MERGE (got1) - [r:RELATIONSHIP] -> (got2) " +
                " RETURN r"

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadOboTermRelationship(goId: String, goRel: OboRelationship) {
        val relType = when (goRel.type == "relationship") {
            true -> goRel.qualifier.uppercase()
            false -> goRel.type.uppercase()
        }
        val cypher = relationshipCypher.replace("SOURCE", goId.formatNeo4jPropertyValue())
            .replace("TARGET", goRel.targetId.formatNeo4jPropertyValue())
            .replace("RELATIONSHIP", relType)
         //   .replace("REL_TYPE", goRel.type)
        Neo4jConnectionService.executeCypherCommand(cypher)
    }

    /*
    Public function to create a Relationship node for each of a GO Term's relationships
    A placeholder OboTerm node is created for the relationship target node if that term
    has not been loaded yet
     */
    fun loadOboTermRelationships(oboTerm: OboTerm) {
        val goId = oboTerm.id
        oboTerm.relationshipList
            .forEach {rel ->
            run {
                if (Neo4jUtils.nodeExistsPredicate( NodeIdentifier("OboTerm", "obo_id", rel.targetId)).not()) {
                    GoTermDao.createPlaceholderOboTerm(rel.targetId,rel.description, oboTerm.namespace)
                }
                loadOboTermRelationship(goId, rel)
            }
        }
    }

}