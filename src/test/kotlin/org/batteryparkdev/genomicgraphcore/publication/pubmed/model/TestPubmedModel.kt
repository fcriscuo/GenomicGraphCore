package org.batteryparkdev.genomicgraphcore.publication.pubmed.model

import arrow.core.Either
import io.kotlintest.matchers.string.shouldNotBeBlank
import io.kotlintest.specs.FunSpec
import org.batteryparkdev.genomicgraphcore.publication.pubmed.service.PubmedRetrievalService


class TestPubmedModel: FunSpec({
   setOf(
        24913355,
        10499589,
        16025998,
        9694699,
        23552839,
        9010225
    ).map { id -> id.toString() }
        .forEach {
            test("$it title should not be blank"){
                val model = resolvePubmedModel(it)
                model.articleTitle.shouldNotBeBlank()
            }
        }
})



fun resolvePubmedModel( pubmedid: String):PubmedModel {
    when (val articleEither = PubmedRetrievalService.retrievePubMedArticle(pubmedid)) {
        is Either.Right -> {
            return PubmedModel.parsePubMedArticle(articleEither.value)
        }
        is Either.Left -> {
            throw Exception(articleEither.value.toString())
        }
    }
}


