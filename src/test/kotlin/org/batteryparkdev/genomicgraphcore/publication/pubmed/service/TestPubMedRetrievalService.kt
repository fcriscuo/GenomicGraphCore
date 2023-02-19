package org.batteryparkdev.genomicgraphcore.publication.pubmed.service

import arrow.core.Either
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel


fun main() {
    val pmidSet = setOf(24482543,9398840,23404334,11318610,19503838,12547822,15133129,12697679,9199174,11058585,22327280,
        16143467,9630625,9434167,9722953,1857421,8455946,11481388,8649988,11483573,11478881,10521492,24058416,10422847,8798635,
        19224922,8415637,9253601,10488091,18981177,23849777,8034651,10072593,22159419,10462049,7590735,22927989,15896705,12853149,
        22245584,15258860,15314642,21282186,10708573,11884129,11242046,15331635,16537643,24088571,17699778,18674751,15367757,21458045,
        28322786,21233210,28380446,29808028,9847074,18157129,10830953,29290337,25472942,11479539,12029067,23857908,23716552,18048406,
        22177090,15489334,28285769,23872636,28176794,26209633,28918047,15583978,23963736,14684992,26687479,26661329,17913455,8104875,
        22028030,28202721,21397064,26764097,282027213,16260194,23872946,7713523,28391028,9115293,9652388,10851247,11034345,1339312,
        9040789,25700553,16839881,19556885).map { it.toString() }.toSet()

    when ( val articleEither =PubmedRetrievalService.retrievePubMedArticleBatch(pmidSet)) {
        is Either.Right -> {
            articleEither.value
                .map { article -> PubmedModel.parsePubMedArticle(article) }
                .forEach {
                    println(" Retrieved PMID: ${it.pubmedId} title: ${it.articleTitle}")
                }
        }

        is Either.Left -> {
            println("Exception ${articleEither.value.toString()}")
        }
    }
}