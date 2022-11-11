package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboSynonym
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

/*
Responsible for creating GoSynonymCollection and GoSynonym nodes
  GoTerm -----1-> GoSynonymCollection ------n-> GoSynonym
 */
object GoSynonymDao {
    /*
     Cypher database templates for Synonym related transactions
     */
    private const val synCollectLoadTemplate = "MERGE (gsc:GoSynonymCollection{go_id: GOID}) " +
            " RETURN gsc.go_id"
    private const val cypherRelationshipTemplate = "MATCH (got:GoTerm), (gsc:GoSynonymCollection) " +
            " WHERE got.go_id = GOID  AND gsc.go_id = GOID " +
            " MERGE (got) - [r:HAS_GO_SYNONYM_COLLECTION] -> (gsc) " +
            " RETURN r"
    private const val synonymLoadTemplate = "MERGE (gos:GoSynonym{synonym_id: SYNID}) " +
            " SET gos += {text: TEXT, type: TYPE} RETURN gos.synonym_id "
    private const val synonymRelationshipTemplate = "MATCH (gsc:GoSynonymCollection), " +
            " (gos:GoSynonym) WHERE gsc.go_id = GOID AND gos.synonym_id = SYNID " +
            " MERGE (gsc) - [r:HAS_GO_SYNONYM] -> (gos) RETURN r"

    /*
    Create the SynonymCollection node
     */
    private fun addSynonymCollectionNode(oboId: String): String {
        val loadCypher = synCollectLoadTemplate.replace("GOID",oboId.formatNeo4jPropertyValue())
        return Neo4jConnectionService.executeCypherCommand(loadCypher)
    }

    /*
    Create a relationship between the GoTerm and the new SynonymCollection Node
     */
    private fun addSynonymCollRelationship(oboId: String) {
        val relCypher = cypherRelationshipTemplate.replace("GOID", oboId.formatNeo4jPropertyValue())
        Neo4jConnectionService.executeCypherCommand(relCypher)
    }

    /*
    Create the Synonym node(s) and their relationship to the SynonymCollection Node
    Unique identifier for a synomym is the GO term id plus an index (e.g. GO:0000001-1)
     */
   private fun addSynonymNodes(goId: String, synonyms: List<OboSynonym>) {
        var index = 1
        synonyms.forEach { syn ->
            run {
                val synId = goId.formatNeo4jPropertyValue().plus("-").plus(index.toString())
                val loadCypher = synonymLoadTemplate.replace("SYNID", synId)
                    .replace("TEXT", syn.synonymText.formatNeo4jPropertyValue())
                    .replace("TYPE", syn.synonymType.formatNeo4jPropertyValue())
                Neo4jConnectionService.executeCypherCommand(loadCypher)
                val relCypher = synonymRelationshipTemplate.replace("GOID",goId.formatNeo4jPropertyValue())
                    .replace("SYNID", synId)
                Neo4jConnectionService.executeCypherCommand(relCypher)
                index += 1
            }
        }
    }
    /*
    Public method to persist GO Synonym nodes and relationships
     */
    fun persistGoSynonymData(oboTerm: OboTerm){
        if (GoTermDao.goTermNodeExistsPredicate(oboTerm)
                .and(oboTerm.synonyms.isNotEmpty())) {
            addSynonymCollectionNode(oboTerm.id)
            addSynonymCollRelationship(oboTerm.id)
            addSynonymNodes(oboTerm.id, oboTerm.synonyms)
        } else {
            when (GoTermDao.goTermNodeExistsPredicate(oboTerm)) {
                true -> println("GO Term ${oboTerm.id} does not have synonyms")
                false -> println("ERROR GO Term ${oboTerm.id} is not in the database")
            }
        }
    }

}