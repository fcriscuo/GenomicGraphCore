package org.batteryparkdev.genomicgraphcore.publication.pubmed.loader

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.publication.getAllPublicationPlaceholderPubIdsByType
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel
import org.batteryparkdev.genomicgraphcore.publication.pubmed.service.PubmedRetrievalService

class PubMedReferenceLoader() {

    // resolve all the Publication/Reference nodes that are currently in placeholder status
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generatePublicationPropertiesBatch() =
        produce<Set<String>> {
            getAllPublicationPlaceholderPubIdsByType("refs").chunked(60).asIterable().forEach {
                send(it.toSet())
                delay(20L)
            }
        }

    // process the set of PubMed nodes and map them to PubmedModel objects
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generatePubMedModels(batches: ReceiveChannel<Set<String>>) =
        produce<CoreModel> {
            for (batch in batches) {
                when (val articleEither = PubmedRetrievalService.retrievePubMedArticleBatch(batch)) {
                    is Either.Right -> {
                        articleEither.value
                            .map { article -> PubmedModel.parsePubMedArticle(article) }
                            .forEach {
                                send(it)
                                delay(20L)
                            }
                    }
                    is Either.Left -> {
                        println("Exception ${articleEither.value.toString()}")
                    }
                }
            }
        }

    /*
   Load the Reference nodes object implementations into the Neo4j database
    */
    @OptIn(ExperimentalCoroutinesApi::class)
    private  fun CoroutineScope.loadPubMedProperties(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models) {
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
                send(model)
                delay(20)
            }
        }

    fun loadPublicationReferenceNodes() = runBlocking{
        val models = loadPubMedProperties(generatePubMedModels(generatePublicationPropertiesBatch()))
        for (model in models){
            if (model is PubmedModel) {
                println("Reference PMID: ${model.pubmedId}  Title: ${model.articleTitle}  Reference count = ${model.referenceList.size}")
            }
        }
    }
}

fun main() {
    println("Loading reference node data into the Neo4j ${Neo4jPropertiesService.neo4jDatabase} database")
    println("There will be a 20 second delay. If this is not the intended database, hit CTRL-C to terminate")
   // Thread.sleep(20_000L)
    println("Proceeding....")
    PubMedReferenceLoader().loadPublicationReferenceNodes()
}
