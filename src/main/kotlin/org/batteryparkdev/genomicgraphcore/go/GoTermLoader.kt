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
import org.batteryparkdev.genomicgraphcore.go.dao.GoXrefDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

/*
Responsible for loading Gene Ontology nodes and relationships into
the Neo4j database
 */
object GoTermLoader {
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
    private fun CoroutineScope.persistGoTermPublications(oboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (oboTerm in oboTerms) {
                oboTerm.pubmedIdList
                    .map { pmid -> createPublicationRelationshipDefinition(pmid, oboTerm) }
                    .forEach { relDef ->
                        // GoPubMedDao.loadGoPublication(it)
                        NodeIdentifierDao.defineRelationship(relDef)
                    }
                send(oboTerm)
            }
        }

    private fun createPublicationRelationshipDefinition(pmid: Int, oboTerm: OboTerm): RelationshipDefinition =
        RelationshipDefinition(
            oboTerm.nodeIdentifier, NodeIdentifier(
                "Publication", "pub_id",
                pmid.toString(), "PubMed"
            ), "HAS_PUBLICATION"
        )


    /*
    Persist the GO Term's Xrefs
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermXrefs(oboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (oboTerm in oboTerms) {
                GoXrefDao.persistXrefs(oboTerm)
                send(oboTerm)
                delay(20)
            }
        }

    /*
    Persist the GO Term's relationships to other GO Terms
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermRelationships(oboTerms: ReceiveChannel<OboTerm>) =
        produce<String> {
            for (oboTerm in oboTerms) {
                if (oboTerm.relationshipList.isNotEmpty()) {
                    GoRelationshipDao.loadOboTermRelationships(oboTerm)
                }
                send(oboTerm.id)
                delay(10)
            }
        }

    /*
    Public function to persist GO Terms into the Neo4j database
     */
    fun loadGoTerms(filename: String) = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val goIds = persistOboTermRelationships(
            persistOboTermXrefs(
                persistGoTermPublications(
                    persistOboTermSynonyms(
                        persistOboTermNode(
                            filterOboTerms(
                                supplyOboTerms(filename)
                            )
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

