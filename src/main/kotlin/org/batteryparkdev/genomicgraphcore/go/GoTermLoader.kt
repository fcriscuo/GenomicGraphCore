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
import org.batteryparkdev.genomicgraphcore.common.obo.dao.OboTermDao
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
    private fun CoroutineScope.persistOboTermNode(oboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (oboTerm in oboTerms) {
                if (oboTerm.isValid()) {
                    OboTermDao("gene_ontology", listOf("GoTerm", oboTerm.namespace)).persistOboTerm(oboTerm)
                    send(oboTerm)
                    delay(30)
                }
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
    Public function to persist GO Terms into the Neo4j database
     */
    fun loadGoTerms(filename: String) = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val goIds =
            persistGoTermPublications(
                persistOboTermNode(
                    filterOboTerms(
                        supplyOboTerms(filename)
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

