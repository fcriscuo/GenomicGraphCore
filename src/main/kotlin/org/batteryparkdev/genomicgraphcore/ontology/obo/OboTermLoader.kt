package org.batteryparkdev.genomicgraphcore.ontology.obo

import arrow.core.Either
import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService
import org.batteryparkdev.genomicgraphcore.ontology.obo.dao.OboTermDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

/*
Responsible for loading OboTerm nodes and relationships into
the Neo4j database
 */
class OboTermLoader (val filename:String, val ontology: String, private val labelList:List<String>){

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
                        println("Message: ${result.value}")
                        break  // no more terms to process
                    }
                }
            }
            println("OBO file $filename has no more OBO terms")
        }

    /*
    Filter out obsolete GO Terms
     */
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    fun CoroutineScope.filterOboTerms(OboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (OboTerm in OboTerms) {
                if (OboTerm.isObsolete.not()) {
                    send(OboTerm)
                    delay(10)
                } else {
                    println("+++++Obo term: ${OboTerm.id} has been marked obsolete and will be skipped")
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
                    OboTermDao(ontology, labelList).persistOboTerm(oboTerm)
                    send(oboTerm)
                    delay(30)
                }
            }
        }

    /*
    Create Publication placeholder nodes for this OboTerms PubMed entries
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.persistOboTermPublications(oboTerms: ReceiveChannel<OboTerm>) =
        produce<OboTerm> {
            for (oboTerm in oboTerms) {
                oboTerm.pubmedIdList
                    .map { pmid -> createPublicationRelationshipDefinition(pmid, oboTerm) }
                    .forEach { relDef ->
                        NodeIdentifierDao.defineRelationship(relDef)
                        updatePublicationUrl(relDef.childNode)
                    }
                send(oboTerm)
            }
        }

    // update the PubMed node with it's NCBU URL
    private fun  updatePublicationUrl (nodeIdentifier: NodeIdentifier) {
        val url = XrefUrlPropertyService.resolveXrefUrl("PubMed",nodeIdentifier.idValue)
        NodeIdentifierDao.updateNodeProperty(nodeIdentifier,"url",url)

    }

    private fun createPublicationRelationshipDefinition(pmid: Int, oboTerm: OboTerm): RelationshipDefinition =
        RelationshipDefinition(
            oboTerm.nodeIdentifier, NodeIdentifier(
                "Publication", "pub_id",
                pmid.toString(), "PubMed"
            ), "HAS_PUBLICATION"
        )

    /*
    Public function to persist OboTerms into the Neo4j database
     */
    fun loadOboTerms() = runBlocking {
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val goIds =
            persistOboTermPublications(
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
            "$ontology data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}

