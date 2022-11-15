package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.common.removeInternalQuotes
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for data access operations for GoTerm nodes in the neo4j database
 */
object GoTermDao {


    private const val cypherLoadTemplate = "MERGE (got:OboTerm{ obo_id: GOID}) " +
            " SET got += {name:GONAME, definition:GODEFINITION," +
            " comment: COMMENT, url: URL} " +
            " RETURN got.obo_id"

    fun loadGoTermNode(oboTerm: OboTerm): String {
        try {
           val ret1 = mergeGoTerm(oboTerm)

//            if (ret1.isNotEmpty()){
//                addGoTermLabel(oboTerm)
//                addGoNamespaceLabel(oboTerm)
//            }
            return ret1
        } catch (e: Exception) {
            println(e.message)
            println("Failed to merge GoTerm: ${oboTerm.id} ${oboTerm.name}")
        }
        return ""
    }

    /*
    map OboTerm properties to Neo4j node properties
    n.b. change internal quotes (i.e. ") to single quotes (i.e. ') in comments & definition fields
     */
    private fun mergeGoTerm(oboTerm: OboTerm): String =
        Neo4jConnectionService.executeCypherCommand(
            "CALL apoc.merge.node( ['OboTerm', 'GoTerm','${oboTerm.namespace}'], " +
                    " {obo_id: ${oboTerm.id.formatNeo4jPropertyValue()}, " +
                    " name: ${oboTerm.name.formatNeo4jPropertyValue()}, " +
                    " definition: ${oboTerm.definition.removeInternalQuotes().formatNeo4jPropertyValue()}, " +
                    " comment: ${oboTerm.comment.removeInternalQuotes().formatNeo4jPropertyValue()}," +
                    " url: ${XrefUrlPropertyService.resolveXrefUrl("GeneOntology",
                        oboTerm.id).formatNeo4jPropertyValue()}, " +
                    " created: datetime()}, " +
                    " { last_mod: datetime()}) YIELD node RETURN node"
        )

//            val merge = cypherLoadTemplate.replace("GOID", oboTerm.id.formatNeo4jPropertyValue())
//                .replace("GONAME", oboTerm.name.formatNeo4jPropertyValue())
//                .replace("GODEFINITION", oboTerm.definition.removeInternalQuotes().formatNeo4jPropertyValue())
//                .replace("COMMENT", oboTerm.comment.removeInternalQuotes().formatNeo4jPropertyValue())
//                .replace("URL",resolveUrL(oboTerm.id))
//            return Neo4jConnectionService.executeCypherCommand(merge)
//    }


    /*
    Function to create a placeholder GoTerm which allows inter-GoTerm
    relationships to be defined before the target GoTerm is fully loaded
    CALL apoc.create.node(["Person", "Actor"], {name: "Tom Hanks"});
     */
    fun createPlaceholderOboTerm(newId: String, name: String, namespace: String):String  =
        "CALL apoc.create.node(['OboTerm', 'GoTerm','${namespace}'], " +
                " {obo_id: ${newId.formatNeo4jPropertyValue()}}, " +
                " { name: ${name.formatNeo4jPropertyValue()}," +
                " url:${XrefUrlPropertyService.resolveXrefUrl("GeneOntology",
                    newId).formatNeo4jPropertyValue()}," +
                "created: datetime()};"

//
//        Neo4jConnectionService.executeCypherCommand(
//            "MERGE (got:OboTerm{obo_id: " +
//                    " ${newId.formatNeo4jPropertyValue()}})  RETURN got.obo_id"
//        )
//        val nodeId = NodeIdentifier("OboTerm", "obo_id",
//            newId,
//            "GoTerm")
//        Neo4jUtils.addLabelToNode(nodeId)
//        return newId
//    }

    /*
    Function to add GoTerm label to new OboTerm node
     */
    private fun addGoTermLabel(oboTerm: OboTerm) {
        val id = oboTerm.id
        val label = "GoTerm"
        val nodeId = NodeIdentifier("OboTerm", "obo_id",
            oboTerm.id,
            label)
        Neo4jUtils.addLabelToNode(nodeId)
    }

    /*
    Function to add a label to a GoTerm node based on the term's GO namespace value
     */
    private fun addGoNamespaceLabel(oboTerm: OboTerm) {
        val id =oboTerm.id
        val label = oboTerm.namespace
        val nodeId = NodeIdentifier("GoTerm", "obo_id",
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