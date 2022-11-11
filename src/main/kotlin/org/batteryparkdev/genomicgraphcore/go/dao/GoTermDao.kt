package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for data access operations for GoTerm nodes in the neo4j database

 */
object GoTermDao {

    private const val cypherLoadTemplate = "MERGE (got:GoTerm{ go_id: GOID}) " +
            " SET got += {name:GONAME, definition:GODEFINITION} " +
            " RETURN got.go_id"

    fun loadGoTermNode(oboTerm: OboTerm): String {
        try {
            val merge = cypherLoadTemplate.replace("GOID", oboTerm.id.formatNeo4jPropertyValue())
                    .replace("GONAME", oboTerm.name.formatNeo4jPropertyValue())
                    .replace("GODEFINITION", oboTerm.definition.formatNeo4jPropertyValue())
            return Neo4jConnectionService.executeCypherCommand(merge)
        } catch (e: Exception) {
            println(e.message)
            println("Failed to merge GoTerm: ${oboTerm.id} ${oboTerm.name}")
        }
        return " "
    }

    /*
    Function to create a placeholder GoTerm which allows inter-GoTerm
    relationships to be defined before the target GoTerm is fully loaded
     */
    fun createPlaceholderGoTerm(goId: String):String =
       Neo4jConnectionService.executeCypherCommand(
           "MERGE (got:GoTerm{go_id: " +
                  " ${goId.formatNeo4jPropertyValue()}})  RETURN got.go_id"
       )

    /*
    Function to add a label to a GoTerm node based on the term's GO namespace value
     */
    fun addGoNamespaceLabel(oboTerm: OboTerm) {
        val id =oboTerm.id
        val label = oboTerm.namespace
        val nodeId = NodeIdentifier("GoTerm", "go_id",
           oboTerm.id,
            label)
        Neo4jUtils.addLabelToNode(nodeId)
    }

    /*
    Function to resolve a List of PubMed Ids from a GO Term
    Input parameter is a List of lines comprising a complete
    GO Term
    Format is PMID:7722643
     */
    private fun resolvePubMedRelationships(oboTerm: OboTerm): List<RelationshipDefinition> {
        val relDefinitions = mutableListOf<RelationshipDefinition>()
         oboTerm.pubmedIdList.forEach { id ->
            run {
                val childNode = NodeIdentifier(
                    "Publication", "pub_id",
                    id.toString(), "PubMed"
                )
                relDefinitions.add(RelationshipDefinition(oboTerm.nodeIdentifier, childNode, "HAS_PUBLICATION"))
            }
        }
        return relDefinitions.toList()
    }

    /*
    Function to determine if a GoTerm has been loaded into the database
     */
    fun goTermNodeExistsPredicate(oboTerm: OboTerm) :Boolean =
        Neo4jUtils.nodeExistsPredicate( oboTerm.nodeIdentifier)
}