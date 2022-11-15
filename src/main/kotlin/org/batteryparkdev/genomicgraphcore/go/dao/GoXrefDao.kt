package org.batteryparkdev.genomicgraphcore.go.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.common.obo.OboXref
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

/*
Responsible for persistance operations between OboXref instances and the Neo4j database
 */

object GoXrefDao {

    fun persistXrefs(obo: OboTerm){
        if (obo.xrefList.isEmpty().not()) {
            createXrefCollectionNodeAndRelationship(obo.id)
            obo.xrefList.forEach { xref -> createXrefNodeAndCollection(obo.id, xref) }
        }
    }

    private fun createXrefCollectionNodeAndRelationship(oboId: String): String =
        Neo4jConnectionService.executeCypherCommand(
            generateGoXrefCollectionCypher(oboId)
        )

    private fun generateGoXrefCollectionCypher(oboId: String): String =
        "CALL apoc.merge.node(['GoXrefCollection'],{obo_id: ${oboId.formatNeo4jPropertyValue()}}," +
                " {created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS xrefcoll " +
                " MATCH (goterm:GoTerm {obo_id:${oboId.formatNeo4jPropertyValue()}}) " +
                " CALL apoc.merge.relationship( goterm, 'HAS_XREF_COLLECTION',{}, {},xrefcoll) " +
                " YIELD rel RETURN rel \n"

    private fun createXrefNodeAndCollection(oboId: String, xref: OboXref) =
        Neo4jConnectionService.executeCypherCommand(generateXrefCypher(oboId, xref))

      private fun generateXrefCypher(oboId: String, xref: OboXref): String =
          "CALL apoc.merge.node(['GoXref','Xref'], {xref_key: ${xref.xrefKey.formatNeo4jPropertyValue()}}, " +
                  "{ source: ${xref.source.formatNeo4jPropertyValue()}," +
                  " xref_id: ${xref.id.formatNeo4jPropertyValue()}, " +
                  " description: ${xref.description.formatNeo4jPropertyValue()}, " +
                  " url: ${
                      XrefUrlPropertyService.resolveXrefUrl(xref.source,
                      xref.id).formatNeo4jPropertyValue()}, " +
                  " created: datetime()}, " +
                  " { last_mod: datetime()}) YIELD node AS xref " +
                  " MATCH (xrefcoll: GoXrefCollection {obo_id: ${oboId.formatNeo4jPropertyValue()}}) " +
                  " CALL apoc.merge.relationship(xrefcoll, 'HAS_XREF',{}, {}, xref ) " +
                  " YIELD rel RETURN rel \n"


}