package org.batteryparkdev.genomicgraphcore.go

import arrow.core.Either

/*
Main function for integration testing
 */
fun main(args: Array<String>) {
    val filePathName = if (args.isNotEmpty()) args[0] else "./data/go/sample_go.obo"
    println("Processing OBO-formatted file: $filePathName")
    val supplier = GoTermSupplier(filePathName)
    for (i in 1..200) {
        when ( val result = supplier.get()) {
            is Either.Right -> {
                val goTerm = result.value
                println("GoTerm:  ${goTerm.goId}  ${goTerm.namespace}  ${goTerm.name}  ${goTerm.synonyms}")
                goTerm.relationshipList.forEach { rel ->
                    println("   Relationship:  ${rel.type}  ${rel.qualifier} ${rel.targetId}   ${rel.description}") }
                goTerm.xrefList.forEach { xref->
                    println("Xref: ${xref.source}  ${xref.id}  ${xref.description}")
                }
            }
            is Either.Left -> {
                println("Exception: ${result.value.message}")
            }
        }
    }
}