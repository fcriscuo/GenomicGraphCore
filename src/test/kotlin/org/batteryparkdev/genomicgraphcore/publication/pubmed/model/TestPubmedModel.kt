package org.batteryparkdev.genomicgraphcore.publication.pubmed.model

import arrow.core.Either
import arrow.core.nonEmptyListOf
import kotlinx.coroutines.delay
import org.batteryparkdev.genomicgraphcore.publication.pubmed.service.PubmedRetrievalService

fun main() {
    val idList = setOf(
        24913355,
        10499589,
        16025998,
        9694699,
        23552839,
        9010225,
        17951403,
        28339046,
        18453543,
        26473374
    ).map { id -> id.toString() }
        .forEach { pubmedid ->
            when (val articleEither = PubmedRetrievalService.retrievePubMedArticle(pubmedid)) {
                is Either.Right -> {
                    val model = PubmedModel.parsePubMedArticle(articleEither.value, pubmedid)
                    println(
                        "PubMed ID: ${model.pubmedId}  tiltle = ${model.articleTitle} " +
                                " Reference count = ${model.referenceList.size}"
                    )
                    model.referenceList.forEach { ref -> println("PubMed ID: ${ref.parentId}  " +
                            " Reference ID: ${ref.referencePubmedId}" +
                            " Journal: ${ref.journal}  Issue: ${ref.issue}") }
                }
                is Either.Left -> {
                    println("Exception ${articleEither.value.toString()}")
                }
            }


        }
}
