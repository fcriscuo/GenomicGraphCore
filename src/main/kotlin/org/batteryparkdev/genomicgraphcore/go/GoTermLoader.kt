package org.batteryparkdev.genomicgraphcore.go

import arrow.core.Either
import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.obo.OboTerm
import org.batteryparkdev.genomicgraphcore.common.obo.OboTermSupplier
import org.batteryparkdev.genomicgraphcore.go.dao.GoRelationshipDao
import org.batteryparkdev.genomicgraphcore.go.dao.GoSynonymDao
import org.batteryparkdev.genomicgraphcore.go.dao.GoTermDao

import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao

/*
Responsible for loading Gene Ontology nodes and relationships into
the Neo4j database
 */
object OboTermLoader {

    /*
    Generate a stream of GO terms from the supplied OBO file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.supplyOboTerms(filename: String) =
        produce<OboTerm> {
            val supplier = OboTermSupplier(filename)
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
    fun CoroutineScope.filterOboTerms(OboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (OboTerm in OboTerms) {
                if (OboTerm.definition.uppercase().contains("OBSOLETE").not()) {
                    send(OboTerm)
                    delay(10)
                } else {
                    println("+++++GO term: ${OboTerm.id} has been marked obsolete and will be skipped")
                }
            }
        }

    /*
    Persist the OboTerm node
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermNode(OboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (OboTerm in OboTerms) {
                if (OboTerm.isValid()) {
                    GoTermDao.loadGoTermNode(OboTerm)
                    send(OboTerm)
                    delay(30)
                }
            }
        }

    /*
    Persist the GO Term's synonyms
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermSynonyms(OboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (OboTerm in OboTerms) {
                if (OboTerm.synonyms.isNotEmpty()) {
                    GoSynonymDao.persistGoSynonymData(OboTerm)
                }
                send(OboTerm)
                delay(10)
            }
        }

    /*
    Create Publication placeholder nodes for this GO Term's PubMed entries
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistFoTermPublications(OboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (OboTerm in OboTerms) {
                OboTerm.pubmedIdList.forEach { pmid ->
                   // GoPubMedDao.loadGoPublication(it)
                    NodeIdentifierDao.defineRelationship(it)
                }
                send(OboTerm)
            }
        }

    /*
    Persist the GO Term's relationships to other GO Terms
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermRelationships(OboTerms: ReceiveChannel<OboTerm>) =
        produce<String> {
            for (OboTerm in OboTerms) {
                if (OboTerm.relationshipList.isNotEmpty()) {
                    GoRelationshipDao.loadOboTermRelationships(OboTerm)
                }
                send(OboTerm.goId)
                delay(10)
            }
        }

    /*
    Public function to persist GO Terms into the Neo4j database
     */
    fun loadOboTerms(filename: String) = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val goIds = persistOboTermRelationships(
            persistFoTermPublications(
                persistOboTermSynonyms(
                    persistOboTermNode(
                        filterOboTerms(
                            supplyOboTerms(filename)
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

