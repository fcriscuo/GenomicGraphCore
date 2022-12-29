package org.batteryparkdev.genomicgraphcore.ontology.obo

import org.batteryparkdev.genomicgraphcore.common.parseIntegerValue
import org.batteryparkdev.genomicgraphcore.common.resolveFirstWord
import org.batteryparkdev.genomicgraphcore.common.resolveQuotedString
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.common.validIntRange
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import java.util.*

data class OboTerm(
    val id: String, val namespace: String, val name: String,
    val definition: String,
    val comment: String,
    val isObsolete: Boolean,
    val pubmedIdList: List<Int>,
    val synonyms: List<OboSynonym>,
    val relationshipList: List<OboRelationship>,
    val xrefList: List<OboXref>,
    val altId: List<String>
) {

    val nodeIdentifier = NodeIdentifier("OboTerm", "obo_id", id.toString())
    fun isValid(): Boolean =
        (id.isNotBlank().and(name.isNotBlank().and(definition.isNotBlank())))

    companion object {

        val nodeNameList = listOf<String>("OboTerm","OboSynonymCollection",
            "OboSynonym","OboXref","OboXrefCollection")
        /*
        Function to process a list of text lines encompassing a OBO Term
         */
        fun generateOboTerm(termlines: List<String>): OboTerm {
            var id: String = ""
            var name: String = ""
            var namespace: String = ""
            var definition: String = ""
            var comment = ""
            var obsolete = false
            termlines.forEach { line ->
                run {
                    when (line.resolveFirstWord()) {
                        "id" -> id = line.replace("id: ", "").trim()
                        "name" -> name = line.replace("name: ", "").trim()
                        "comment" -> comment = line.replace("comment: ", "").trim()
                        "namespace" -> namespace = line.replace("namespace: ", "").trim()
                        "def" -> definition = line.resolveQuotedString()
                        "is_obsolete" -> obsolete = resolveIsObsoleteBoolean(line)
                        else -> {}  // ignore other lines
                    }
                }
            }

            return OboTerm(
                id, namespace, name, definition, comment,
                obsolete,
                resolvePubMedIdentifiers(id, termlines), OboSynonym.resolveSynonyms(termlines),
                OboRelationship.resolveRelationships(termlines), OboXref.resolveXrefs(termlines),
                resolveAltIdSet(termlines)
            )
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun resolveIsObsoleteBoolean(line: String): Boolean = line.lowercase().contains("true")


        private fun resolveAltIdSet(termlines: List<String>): List<String> {
            val altIdSet = mutableSetOf<String>()
            termlines.stream().filter { it.resolveFirstWord() == "alt_id:" }
                .forEach { altIdSet.add(it.replace("alt_id: ", "").trim()) }
            return altIdSet.toList()
        }

        /*
    Function to resolve a List of PubMed Ids from an OBO line
    Input parameter is a List of lines comprising a complete
    OBO Term
    Format is PMID:7722643
     */
        private fun resolvePubMedIdentifiers(goId: String, lines: List<String>): List<Int> {
            val pmidSet = mutableSetOf<Int>()
            val pmidLabel = "PMID"
            lines.stream().filter { line -> line.contains(pmidLabel) }
                .forEach { line ->
                    run {
                        var index = 0
                        var text = line
                        while (index != -1) {
                            index = text.indexOf(pmidLabel)
                            if (index >= 0)
                                pmidSet.add((text.substring(index + 5).parseIntegerValue()))
                            text = text.substring(index + 1)
                        }
                    }
                }
            return pmidSet.toList()
        }
    }
}

data class OboSynonym(
    val synonymText: String,
    val synonymType: String
) {
    companion object {
        fun resolveSynonyms(termLines: List<String>): List<OboSynonym> {
            val synonyms = mutableListOf<OboSynonym>()
            termLines.filter { line -> line.startsWith("synonym:") }
                .forEach { syn -> synonyms.add(resolveSynonym(syn)) }
            return synonyms.toList()
        }

        private fun resolveSynonym(line: String): OboSynonym {
            val text = line.resolveQuotedString()
            val startIndex = line.lastIndexOf('"') + 2
            val endIndex = startIndex + line.substring(startIndex).indexOf(' ')
            val type = when (validIntRange(startIndex, endIndex)) {
                true -> line.substring(startIndex, endIndex)
                false -> ""
            }
            return OboSynonym(text, type)
        }
    }
}

data class OboRelationship(
    val type: String,
    val qualifier: String = "",
    val targetId: String,
    val description: String
) {
    fun isValid(): Boolean = type.isNotEmpty().and(targetId.isNotEmpty())

    companion object {
        private fun relationshipFilter(line: String): Boolean =
            when (line.resolveFirstWord()) {
                "is_a", "intersection_of", "relationship" -> true
                else -> false
            }

        fun resolveRelationships(termlines: List<String>): List<OboRelationship> {
            val relationships = mutableListOf<OboRelationship>()
            termlines.filter { line -> relationshipFilter(line) }
                .forEach { line -> relationships.add(resolveRelationship(line)) }
            return relationships.toList()
        }

        private fun resolveRelationship(line: String): OboRelationship =
            when (line.resolveFirstWord()) {
                "is_a" -> parseRelationshipType(line,"IS_A")
                "intersection_of" -> parseRelationshipType(line,"INTERSECTS")
                "relationship" ->  parseRelationshipType(line,"HAS_RELATIONSHIP")
                else -> OboRelationship("", "", "","") // invalid representation
            }

        private fun parseRelationshipType(line: String, type:String): OboRelationship {
            val subList = line.substring(line.indexOf(": ")+1, line.indexOf('!')-1).split(' ')
            val targetId = subList.last()
            val description = line.substring(line.indexOf('!')+2)
            val qualifier = if (subList.size > 1) subList.first() else ""
            return OboRelationship(type, qualifier, targetId, description)
        }
    }
}

data class OboXref(
    val source: String,
    val id: String,
    val description: String = "",
    val url: String = "",
    val xrefKey: String = UUID.randomUUID().toString()
) {
    companion object {
        fun resolveXrefs(termLines: List<String>): List<OboXref> {
            val xrefs = mutableListOf<OboXref>()
            termLines.filter { line -> line.startsWith("xref") }
                .forEach { line -> xrefs.add(resolveXref(line)) }
            return xrefs
        }

        private fun resolveXref(line: String): OboXref {
            val sourceAndId = line.split(" ")[1].split(":")
            val source = sourceAndId[0]
            val id = sourceAndId[1]
            val url = resolveXrefUrl(line, source, id)
            return OboXref(source, id, line.resolveQuotedString(), url)
        }
        /*
        XrefUrlPropertyService.resolveXrefUrl(xref.source,
                        xref.id).formatNeo4jPropertyValue()}
         */
        private fun resolveXrefUrl(line: String, source: String, id: String): String =
              when (line.contains("http")) {
                  true -> line.split(' ')[1]
                  false ->  XrefUrlPropertyService.resolveXrefUrl(source,
                      id)
              }
    }

}





