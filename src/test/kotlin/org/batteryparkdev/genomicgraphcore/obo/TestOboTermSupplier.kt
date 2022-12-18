package org.batteryparkdev.genomicgraphcore.obo

import arrow.core.Either
import org.batteryparkdev.genomicgraphcore.ontology.obo.OboTermSupplier

fun main(args: Array<String>) {
    val filePathName = if (args.isNotEmpty()) args[0] else "./data/go/sample_gene_ontology.obo"
    println("Processing OBO-formatted file: $filePathName")
    val supplier = OboTermSupplier(filePathName)
    var termCount = 0
    for (i in 1..200) {
        when ( val result = supplier.get()) {
            is Either.Right -> {
                termCount += 1
                val oboTerm = result.value
                println("OBO Term is valid: ${oboTerm.isValid()}")
                println("OboTerm:  ${oboTerm.id}  ${oboTerm.namespace}  ${oboTerm.name}  ${oboTerm.synonyms}")
                oboTerm.synonyms.forEach { syn ->
                    println("Synonym: ${syn.synonymType}  ${syn.synonymText}")
                }
                oboTerm.relationshipList.forEach { rel ->
                    println("   Relationship:  ${rel.type}  ${rel.qualifier} ${rel.targetId}   ${rel.description}") }
                oboTerm.xrefList.forEach { xref->
                    println("Xref: ${xref.source}  ${xref.id}  ${xref.description}")
                }
                oboTerm.pubmedIdList.forEach { pmid -> println("PMID: $pmid") }
            }
            is Either.Left -> {
                println("Message: ${result.value}  term count = $termCount")
                break
            }
        }
    }
}