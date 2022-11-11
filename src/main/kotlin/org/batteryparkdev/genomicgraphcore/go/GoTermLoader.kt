package org.batteryparkdev.genomicgraphcore.go

import arrow.core.Either
import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.go.dao.GoRelationshipDao
import org.batteryparkdev.genomicgraphcore.go.dao.GoSynonymDao
import org.batteryparkdev.genomicgraphcore.go.dao.GoTermDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao

/*
Responsible for loading Gene Ontology nodes and relationships into
the Neo4j database
 */
object GoTermLoader {

    /*
    Generate a stream of GO terms from the supplied OBO file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.supplyGoTerms(filename: String) =
        produce<GoTerm> {
            val supplier = GoTermSupplier(filename)
            while (supplier.hasMoreLines()) {
                when (val result = supplier.get()) {
                    is Either.Right -> {
                        send(result.value)
                        delay(10)
                    }
                    is Either.Left -> {
                        println("Exception: ${result.value.message}")
                    }
                }
            }
        }

    /*
    Filter out obsolete GO Terms
     */
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    fun CoroutineScope.filterGoTerms(goTerms: ReceiveChannel<GoTerm>) =
        produce<GoTerm> {
            for (goTerm in goTerms) {
                if (goTerm.definition.uppercase().contains("OBSOLETE").not()) {
                    send(goTerm)
                    delay(10)
                } else {
                    println("+++++GO term: ${goTerm.goId} has been marked obsolete and will be skipped")
                }
            }
        }

    /*
    Persist the GoTerm node
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistGoTermNode(goTerms: ReceiveChannel<GoTerm>) =
        produce<GoTerm> {
            for (goTerm in goTerms) {
                if (goTerm.isValid()) {
                    GoTermDao.loadGoTermNode(goTerm)
                    send(goTerm)
                    delay(30)
                }
            }
        }

    /*
    Persist the GO Term's synonyms
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistGoTermSynonyms(goTerms: ReceiveChannel<GoTerm>) =
        produce<GoTerm> {
            for (goTerm in goTerms) {
                if (goTerm.synonyms.isNotEmpty()) {
                    GoSynonymDao.persistGoSynonymData(goTerm)
                }
                send(goTerm)
                delay(10)
            }
        }

    /*
    Create Publication placeholder nodes for this GO Term's PubMed entries
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistFoTermPublications(goTerms: ReceiveChannel<GoTerm>) =
        produce<GoTerm> {
            for (goTerm in goTerms) {
                goTerm.pubmedRelationshipDefinitions.forEach { it ->
                   // GoPubMedDao.loadGoPublication(it)
                    NodeIdentifierDao.defineRelationship(it)
                }
                send(goTerm)
            }
        }

    /*
    Persist the GO Term's relationships to other GO Terms
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistGoTermRelationships(goTerms: ReceiveChannel<GoTerm>) =
        produce<String> {
            for (goTerm in goTerms) {
                if (goTerm.relationshipList.isNotEmpty()) {
                    GoRelationshipDao.loadGoTermRelationships(goTerm)
                }
                send(goTerm.goId)
                delay(10)
            }
        }

    /*
    Public function to persist GO Terms into the Neo4j database
     */
    fun loadGoTerms(filename: String) = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val goIds = persistGoTermRelationships(
            persistFoTermPublications(
                persistGoTermSynonyms(
                    persistGoTermNode(
                        filterGoTerms(
                            supplyGoTerms(filename)
                        )
                    )
                )
            )
        )

        for (goId in goIds) {
            nodeCount += 1
        }
       println(
            "Gene Ontology data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}

