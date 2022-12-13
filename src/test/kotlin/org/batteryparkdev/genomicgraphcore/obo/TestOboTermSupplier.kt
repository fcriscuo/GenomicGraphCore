package org.batteryparkdev.genomicgraphcore.obo

import arrow.core.Either
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermSupplier

fun main(args: Array<String>) {
    val filePathName = if (args.isNotEmpty()) args[0] else "./data/go/sample_go.obo"
    println("Processing OBO-formatted file: $filePathName")
    val supplier = OboTermSupplier(filePathName)
    for (i in 1..200) {
        when ( val result = supplier.get()) {
            is Either.Right -> {
                val OboTerm = result.value
                println("OboTerm:  ${OboTerm.id}  ${OboTerm.namespace}  ${OboTerm.name}  ${OboTerm.synonyms}")
                OboTerm.synonyms.forEach { syn ->
                    println("Synonym: ${syn.synonymType}  ${syn.synonymText}")
                }
                OboTerm.relationshipList.forEach { rel ->
                    println("   Relationship:  ${rel.type}  ${rel.qualifier} ${rel.targetId}   ${rel.description}") }
                OboTerm.xrefList.forEach { xref->
                    println("Xref: ${xref.source}  ${xref.id}  ${xref.description}")
                }
                OboTerm.pubmedIdList.forEach { pmid -> println("PMID: $pmid") }
            }
            is Either.Left -> {
                println("Exception: ${result.value.message}")
            }
        }
    }
}