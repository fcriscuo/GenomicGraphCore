package org.batteryparkdev.genomicgraphcore.common.obo

import org.batteryparkdev.genomicgraphcore.common.parseIntegerValue
import org.batteryparkdev.genomicgraphcore.common.resolveFirstWord
import org.batteryparkdev.genomicgraphcore.common.resolveQuotedString
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

data class OboTerm(
    val id: String, val namespace: String, val name: String,
    val definition: String,
    val isObsolete: Boolean,
    val pubmedIdList: List<Int>,
    val synonyms: List<OboSynonym>,
    val relationshipList: List<OboRelationship>,
    val xrefList: List<OboXref>
) {

    val nodeIdentifier = NodeIdentifier("OboTerm","obo_id", id.toString())
    fun isValid(): Boolean =
        (id.isNotBlank().and(name.isNotBlank().and(namespace.isNotBlank())))



    companion object {
        /*
        Function to process a list of text lines encompassing a GO Term
         */
        fun generateOboTerm(termlines: List<String>): OboTerm {
            var id: String = ""
            var name: String = ""
            var namespace: String = ""
            var definition: String = ""
            var obsolete = false
            termlines.forEach { line ->
                run {
                    when (line.resolveFirstWord()) {
                        "id" -> id = line.substring(line.indexOf("GO:"))
                        "name" -> name = line.substring(6)
                        "namespace" -> namespace = line.substring(11)
                        "def" -> definition = line.resolveQuotedString()
                        "is_obsolete" -> obsolete = resolveIsObsoleteBoolean(line)
                        else -> {}  // ignore other lines
                    }
                }
            }

            return OboTerm(
                id, namespace, name, definition,
                obsolete,
                resolvePubMedIdentifiers(id, termlines), OboSynonym.resolveSynonyms(termlines),
                OboRelationship.resolveRelationships(termlines), OboXref.resolveXrefs(termlines)
            )
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun resolveIsObsoleteBoolean(line: String): Boolean = line.lowercase().contains("true")

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
            val type = line.substring(startIndex, endIndex)
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
            return relationships
        }

        private fun resolveRelationship(line: String): OboRelationship {
            val type = line.resolveFirstWord()
            val targetStart = line.indexOf("GO:")
            val targetId = line.substring(targetStart, targetStart + 10)
            val description = line.substring(targetStart + 13)
            return OboRelationship(type, resolveQualifier(line), targetId, description)
        }

        private fun resolveQualifier(line: String): String {
            val colonIndex = line.indexOf(":") + 2
            val goIndex = line.indexOf("GO:")
            return line.substring(colonIndex, goIndex).trim()
        }
    }
}

data class OboXref(
    val source: String,
    val id: String,
    val description: String = ""
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
            return OboXref(source, id, line.resolveQuotedString())
        }
    }
}





