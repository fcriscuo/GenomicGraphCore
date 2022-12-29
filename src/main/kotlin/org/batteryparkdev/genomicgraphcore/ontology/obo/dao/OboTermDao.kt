package org.batteryparkdev.genomicgraphcore.ontology.obo.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.common.removeInternalQuotes
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

/*
Kotlin class to load an OboTerm into a Neo4j database
Optional constructor parameter is a specialized label for the OboTerm node
(e.g. GeneOntology)
args: Array<String>
 */
class OboTermDao(private val ontology: String, private val labels: List<String>) {

    fun persistOboTerm(oboTerm: OboTerm){
        mergeGoTerm(oboTerm)
        if (oboTerm.synonyms.isNotEmpty()) {
            OboSynonymDao.persistGoSynonymData(oboTerm)
        }
        if (oboTerm.xrefList.isNotEmpty()){
            OboXrefDao.persistXrefs(oboTerm)
        }
        if(oboTerm.relationshipList.isNotEmpty()){
            OboRelationshipDao.persistOboTermRelationships(oboTerm)
        }
        println ("Loaded OboTerm id: ${oboTerm.id}")
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun formatLabels(): String {
        var additionaLabels: String = ""
        labels.asSequence().map { it -> it.replaceFirstChar { it.uppercase() }
                .replace(' ','_')}
            .forEach { additionaLabels += ",${it.formatNeo4jPropertyValue()}" }
        return additionaLabels
    }

    /*
   map OboTerm properties to Neo4j node properties
   n.b. change internal quotes (i.e. ") to single quotes (i.e. ') in comments & definition fields
    */
    private fun mergeGoTerm(oboTerm: OboTerm): String =
        Neo4jConnectionService.executeCypherCommand(
            "CALL apoc.merge.node(['OboTerm'${formatLabels()}], " +
                    " {obo_id: ${oboTerm.id.formatNeo4jPropertyValue()}, " +
                    " name: ${oboTerm.name.formatNeo4jPropertyValue()}, " +
                    " definition: ${oboTerm.definition.removeInternalQuotes().formatNeo4jPropertyValue()}, " +
                    " comment: ${oboTerm.comment.removeInternalQuotes().formatNeo4jPropertyValue()}," +
                    " url: ${
                        XrefUrlPropertyService.resolveXrefUrl(
                            ontology,
                            oboTerm.id
                        ).formatNeo4jPropertyValue()
                    }, " +
                    " created: datetime()}, " +
                    " { last_mod: datetime()}) YIELD node RETURN node \n"
        )
}