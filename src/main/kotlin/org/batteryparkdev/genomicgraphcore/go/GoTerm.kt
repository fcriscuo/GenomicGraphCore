package org.batteryparkdev.genomicgraphcore.go

import org.batteryparkdev.genomicgraphcore.common.parseIntegerValue
import org.batteryparkdev.genomicgraphcore.common.resolveFirstWord
import org.batteryparkdev.genomicgraphcore.common.resolveQuotedString
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

data class GoTerm(
    val goId: String, val namespace: String, val name: String,
    val definition: String,
    val pubmedRelationshipDefinitions: List<RelationshipDefinition>,
    val synonyms: List<GoSynonym>,
    val relationshipList: List<Relationship>,
    val xrefList: List<Xref>
) {
    fun isValid(): Boolean =
        (goId.isNotBlank().and(name.isNotBlank().and(namespace.isNotBlank())))

    companion object  {
        /*
        Function to process a list of text lines encompassing a GO Term
         */
        fun generateGoTerm(termlines: List<String>): GoTerm {
            var goId: String = " "
            var goName: String = " "
            var goNamespace: String = " "
            var goDefinition: String = ""
            termlines.forEach { line ->
                run {
                    when (line.resolveFirstWord()) {
                        "id" -> goId = line.substring(line.indexOf("GO:"))
                        "name" -> goName = line.substring(6)
                        "namespace" -> goNamespace = line.substring(11)
                        "def" -> goDefinition = line.resolveQuotedString()
                        else -> {}  // ignore other lines
                    }
                }
            }

            return GoTerm(
                goId, goNamespace, goName, goDefinition,
                resolvePubMedIdentifiers(goId, termlines), GoSynonym.resolveSynonyms(termlines),
                Relationship.resolveRelationships(termlines), Xref.resolveXrefs(termlines)
            )
        }

        /*
    Function to resolve a List of PubMed Ids from a GO Term
    Input parameter is a List of lines comprising a complete
    GO Term
    Format is PMID:7722643
     */
        fun resolvePubMedIdentifiers(goId: String, lines: List<String>): List<RelationshipDefinition> {
            val pmidSet = mutableSetOf<Int>()
            val pmidLabel = "PMID"
            val pmidLength = 12
            val parentNode = NodeIdentifier(
                "GoTerm", "go_id",
                goId
            )
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
            val relDefinitions = mutableListOf<RelationshipDefinition>()
            pmidSet.forEach { id ->
                run {
                    val childNode = NodeIdentifier(
                        "Publication", "pub_id",
                       id.toString(), "PubMed"
                    )
                    relDefinitions.add(RelationshipDefinition(parentNode, childNode, "HAS_PUBLICATION"))
                }
            }
            return relDefinitions.toList()
        }
    }
}

data class GoSynonym(
    val synonymText: String,
    val synonymType: String
) {
    companion object  {
        fun resolveSynonyms(termLines: List<String>): List<GoSynonym> {
            val synonyms = mutableListOf<GoSynonym>()
            termLines.filter { line -> line.startsWith("synonym:") }
                .forEach { syn -> synonyms.add(resolveSynonym(syn)) }
            return synonyms.toList()
        }

        private fun resolveSynonym(line: String): GoSynonym {
            val text = line.resolveQuotedString()
            val startIndex = line.lastIndexOf('"') + 2
            val endIndex = startIndex + line.substring(startIndex).indexOf(' ')
            val type = line.substring(startIndex, endIndex)
            return GoSynonym(text, type)
        }
    }
}

data class Relationship(
    val type: String,
    val qualifier: String = "",
    val targetId: String,
    val description: String
) {
    companion object  {

        private fun relationshipFilter(line: String): Boolean =
            when (line.resolveFirstWord()) {
                "is_a", "intersection_of", "relationship" -> true
                else -> false
            }

        fun resolveRelationships(termlines: List<String>): List<Relationship> {
            val relationships = mutableListOf<Relationship>()
            termlines.filter { line -> relationshipFilter(line) }
                .forEach { line -> relationships.add(resolveRelationship(line)) }
            return relationships
        }

        private fun resolveRelationship(line: String): Relationship {
            val type = line.resolveFirstWord()
            val targetStart = line.indexOf("GO:")
            val targetId = line.substring(targetStart, targetStart + 10)
            val description = line.substring(targetStart + 13)
            return Relationship(type, resolveQualifier(line), targetId, description)
        }

        private fun resolveQualifier(line: String): String {
            val colonIndex = line.indexOf(":") + 2
            val goIndex = line.indexOf("GO:")
            return line.substring(colonIndex, goIndex).trim()
        }
    }
}

data class Xref(
    val source: String,
    val id: String,
    val description: String = ""
) {
    companion object  {
        fun resolveXrefs(termLines: List<String>): List<Xref> {
            val xrefs = mutableListOf<Xref>()
            termLines.filter { line -> line.startsWith("xref") }
                .forEach { line -> xrefs.add(resolveXref(line)) }
            return xrefs
        }

        private fun resolveXref(line: String): Xref {
            val sourceAndId = line.split(" ")[1].split(":")
            val source = sourceAndId[0]
            val id = sourceAndId[1]
            return Xref(source, id, line.resolveQuotedString())

        }
    }
}




