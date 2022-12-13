package org.batteryparkdev.genomicgraphcore.ontology.obo.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboXref
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

/*
Responsible for persistence operations between OboXref instances and the Neo4j database
OboTerm - [HAS_XREF_COLLECTION] -> OboXrefCollection - [HAS_XREF] -> OboXref
 */

object OboXrefDao {

    fun persistXrefs(obo: OboTerm){
        if (obo.xrefList.isEmpty().not()) {
            createXrefCollectionNodeAndRelationship(obo.id)
            obo.xrefList.forEach { xref -> createXrefNodeAndCollection(obo.id, xref) }
        }
    }

    private fun createXrefCollectionNodeAndRelationship(oboId: String): String =
        Neo4jConnectionService.executeCypherCommand(
            "CALL apoc.merge.node(['OboXrefCollection'],{obo_id: ${oboId.formatNeo4jPropertyValue()}}," +
                    " {created: datetime()}, " +
                    " { last_mod: datetime()}) YIELD node AS xrefcoll " +
                    " MATCH (oboterm:OboTerm {obo_id:${oboId.formatNeo4jPropertyValue()}}) " +
                    " CALL apoc.merge.relationship( oboterm, 'HAS_XREF_COLLECTION',{}, {},xrefcoll) " +
                    " YIELD rel RETURN rel \n"
        )

    private fun createXrefNodeAndCollection(oboId: String, xref: OboXref) =
        Neo4jConnectionService.executeCypherCommand( "CALL apoc.merge.node(['OboXref','Xref'], " +
                " {xref_key: ${xref.xrefKey.formatNeo4jPropertyValue()}}, " +
                "{ source: ${xref.source.formatNeo4jPropertyValue()}," +
                " xref_id: ${xref.id.formatNeo4jPropertyValue()}, " +
                " description: ${xref.description.formatNeo4jPropertyValue()}, " +
                " url: ${xref.url.formatNeo4jPropertyValue()}, " +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS xref " +
                " MATCH (xrefcoll: OboXrefCollection {obo_id: ${oboId.formatNeo4jPropertyValue()}}) " +
                " CALL apoc.merge.relationship(xrefcoll, 'HAS_XREF',{}, {}, xref ) " +
                " YIELD rel RETURN rel \n")
}