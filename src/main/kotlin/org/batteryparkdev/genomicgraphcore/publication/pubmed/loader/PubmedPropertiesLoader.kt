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
import org.batteryparkdev.genomicgraphcore.publication.pubmedNodeExistsPredicate
import org.batteryparkdev.genomicgraphcore.publication.referenceNodeExistsPredicate

class PubMedPropertiesLoader() {

    // resolve all the Publication/PubMed nodes that are currently in placeholder status
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generatePublicationPropertiesBatch() =
        produce<Set<String>> {
            getAllPublicationPlaceholderPubIdsByType("properties").chunked(100).asIterable().forEach {
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
    private  fun CoroutineScope.loadPubMedProperties(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models) {
                // load the model data into Neo4j, then complete its relationships to
                // other nodes
                // The two operations are performed in the same coroutine to avoid race conditions
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
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
                    model.referenceList.filter{it>0}.forEach { ref -> run {
                        Neo4jConnectionService.executeCypherCommand(generateReferencePlaceholderNodeCypher(model.pubmedId,ref))
                    } }
                }
                send(model)
                delay(20L)
            }
        }
    // Private function to generate the Cypher needed to optionally create a Reference placeholder node
    // and create a HAS_REFERENCE relationship from the PubMed node
    // If the Reference node already exists, only create the relationship
    // If a PubMed node with the refId exists, just add the Reference label and create the relationship

    private fun generateReferencePlaceholderNodeCypher(pubId: Int, refId: Int): String {
        var cypher = "MATCH (pub:PubMed{pub_id: ${pubId.toString()}})  SET pub.needs_refs = false\n  "
        if (pubmedNodeExistsPredicate(refId.toString())){
            cypher = cypher.plus(
                "WITH pub MATCH (ref:PubMed{pub_id: ${refId.toString()}})  SET ref:Reference\n"
            )
        } else if (!referenceNodeExistsPredicate(refId.toString())){
            cypher = cypher.plus(
                "MERGE (ref:Publication:Reference{pub_id: ${refId.toString()}})\n " +
                        "SET ref.url = genomiccore.resolvePubmedUrl(toString($refId))," +
                        " ref.needs_properties=true, ref.needs_refs=false \n"
            )
        }
        return cypher.plus(
            "MERGE (pub) -[r:HAS_REFERENCE]->(ref); "
        )
    }

    fun loadPublicationPubMedNodes() = runBlocking{
        val models = loadReferenceNodes(loadPubMedProperties(generatePubMedModels(generatePublicationPropertiesBatch())))
        for (model in models){
            if (model is PubmedModel) {
                println("PubMed Id: ${model.pubmedId}  Title: ${model.articleTitle}  Reference count = ${model.referenceList.size}")
            }
        }
    }
}

fun main() {
    println("Loading data into the Neo4j ${Neo4jPropertiesService.neo4jDatabase} database")
    println("There will be a 20 second delay. If this is not the intended database, hit CTRL-C to terminate")
   // Thread.sleep(20_000L)
    println("Proceeding....")
    PubMedPropertiesLoader().loadPublicationPubMedNodes()
}
