package org.batteryparkdev.genomicgraphcore.publication.pubmed.loader

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.publication.getAllPlaceholderPubMedNodeIds
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel
import org.batteryparkdev.genomicgraphcore.publication.pubmed.service.PubmedRetrievalService

class PubMedModelLoader() {

    // resolve all the Publication/PubMed nodes that are currently in placeholder status
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generatePublicationBatch() =
        produce<Set<String>> {
            getAllPlaceholderPubMedNodeIds().chunked(50).asIterable().forEach {
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
   Load the PubMed nodes object implementations into the Neo4j database
    */
    @OptIn(ExperimentalCoroutinesApi::class)
    private  fun CoroutineScope.loadPubMedNodes(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models) {
                // load the model data into Neo4j, then complete its relationships to
                // other nodes
                // The two operations are performed in the same coroutine to avoid race conditions
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
                model.createModelRelationships()
                send(model)
                delay(20)
            }
        }
    /*
    Load the Reference nodes into the database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private  fun CoroutineScope.loadReferenceNodes(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models){
                if(model is PubmedModel && model.referenceList.isNotEmpty()){
                    model.referenceList.forEach { ref -> run {
                        Neo4jConnectionService.executeCypherCommand(ref.generateLoadModelCypher())
                        ref.createModelRelationships()
                    } }
                }
                send(model)
                delay(20L)
            }
        }

    fun loadPublicationNodes() = runBlocking{
        val models = loadReferenceNodes(loadPubMedNodes(generatePubMedModels(generatePublicationBatch())))
        for (model in models){
            if (model is PubmedModel) {
                println("PubMed Id ${model.pubmedId}  Title: ${model.articleTitle}  Reference count = ${model.referenceList.size}")
            }
        }
    }

}
fun main() {
    PubMedModelLoader().loadPublicationNodes()
}
