package org.batteryparkdev.genomicgraphcore.common.obo.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboSynonym
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

/*
Responsible for creating OboSynonymCollection and GoSynonym nodes
  OboTerm -----1-> OboSynonymCollection ------n-> OboSynonym
 */
object OboSynonymDao {
    private fun createSynonymCollectionAndRelationship(oboId: String): String =
        Neo4jConnectionService.executeCypherCommand(
            "CALL apoc.merge.node(['OboSynonymCollection'],{obo_id: ${oboId.formatNeo4jPropertyValue()}}," +
                    " {created: datetime()}, " +
                    " { last_mod: datetime()}) YIELD node AS syncoll " +
                    " MATCH (oboterm:OboTerm {obo_id:${oboId.formatNeo4jPropertyValue()}}) " +
                    " CALL apoc.merge.relationship( oboterm, 'HAS_SYNONYM_COLLECTION',{}, {},syncoll) " +
                    " YIELD rel RETURN rel \n"
        )

    private fun createSynonymNodeAndCollectionRelationship(oboId: String, synList: List<OboSynonym>) {
        val index = 1
        synList.forEach { syn ->
            run {
                val synKey = oboId.plus("-").plus(index.toString()).formatNeo4jPropertyValue()
                Neo4jConnectionService.executeCypherCommand(
                    "CALL apoc.merge.node(['OboSynonym'], " +
                            " {syn_key: ${synKey.formatNeo4jPropertyValue()}}, " +
                            "{ text: ${syn.synonymText.formatNeo4jPropertyValue()}," +
                            " type: ${syn.synonymType.formatNeo4jPropertyValue()}, " +
                            " created: datetime()}, " +
                            " { last_mod: datetime()}) YIELD node AS syn " +
                            " MATCH (syncoll: OboSynonymCollection {obo_id: ${oboId.formatNeo4jPropertyValue()}}) " +
                            " CALL apoc.merge.relationship(syncoll, 'HAS_SYNONYM',{}, {}, syn ) " +
                            " YIELD rel RETURN rel \n"
                )
                index.inc()
            }
        }
    }

    /*
    Public method to persist GO Synonym nodes and relationships
     */
    fun persistGoSynonymData(oboTerm: OboTerm) {
        if (Neo4jUtils.nodeExistsPredicate(oboTerm.nodeIdentifier)
                .and(oboTerm.synonyms.isNotEmpty())
        ) {
            createSynonymCollectionAndRelationship(oboTerm.id)
            createSynonymNodeAndCollectionRelationship(oboTerm.id, oboTerm.synonyms)
        } else {
            when (Neo4jUtils.nodeExistsPredicate(oboTerm.nodeIdentifier)) {
                true -> println("GO Term ${oboTerm.id} does not have synonyms")
                false -> println("ERROR GO Term ${oboTerm.id} is not in the database")
            }
        }
    }

}