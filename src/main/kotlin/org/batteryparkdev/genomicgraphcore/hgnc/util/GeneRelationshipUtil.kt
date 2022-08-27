package org.batteryparkdev.genomicgraphcore.hgnc.util

import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.neo4j.driver.Record
import java.util.concurrent.TimeUnit

/*
Responsible for refactoring the CosmicGraph database  create new CosmicGene to Hgnc relationships
an new CosmicGene to Entrez relationships
Creates Entrez placeholder nodes.
TODO: Refactor code in CosmicGraphDb project to support new database model
to use The GenomicGraphCore project
 */

/*
Create a data class to encapsulate the CosmicGene properties needed for refactoring
 */
data class GeneProperties(val geneSymbol: String, val hgncId: String, val entrezId: Int = 0) {

    companion object {
        fun parseFromNeo4jQueryRecord(record: Record): GeneProperties {
            val recordMap = record.asMap()
            return GeneProperties(
                recordMap["geneSymbol"].toString(),
                recordMap["hgncId"].toString(), recordMap["entrezId"].toString().parseValidInteger()
            )
        }
    }
}

class GeneRelationshipUtil {

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generateGeneProperties() =
        produce<GeneProperties> {
            Neo4jConnectionService.executeCypherQuery(
                "match (cg:CosmicGene),(h:Hgnc) " +
                        "where cg.gene_symbol = h.gene_symbol return cg.gene_symbol as geneSymbol," +
                        " cg.entrez_gene_id as entrezId, h.hgnc_id as hgncId "
            )
                .map { GeneProperties.parseFromNeo4jQueryRecord(it) }
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.resolveHgncRelationship(genes: ReceiveChannel<GeneProperties>) =
        produce<GeneProperties> {
            for (gene in genes) {
                Neo4jConnectionService.executeCypherCommand(
                    "MATCH (cg:CosmicGene), (h:Hgnc)  WHERE " +
                            " cg.gene_symbol = ${gene.geneSymbol.formatNeo4jPropertyValue()} AND " +
                            " h.hgnc_id = ${gene.hgncId.formatNeo4jPropertyValue()} " +
                            " CREATE (cg) -[r: HAS_HGNC] -> (h) RETURN r"
                )
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.resolveEntrezRelationship(genes: ReceiveChannel<GeneProperties>) =
        produce<GeneProperties> {
            for (gene in genes) {
                if (gene.entrezId > 0) {
                    Neo4jConnectionService.executeCypherCommand(
                        "MERGE (e:Entrez{ entrez_id: ${gene.entrezId}} )  " +
                                " WITH e " +
                                " MATCH (cg:CosmicGene{gene_symbol: ${gene.geneSymbol.formatNeo4jPropertyValue()}}) " +
                                " CREATE (cg) -[r: HAS_ENTREZ] -> (e) RETURN r "
                    )
                }
                send(gene)
                delay(20)
            }
        }

    fun updateCosmicGenes() = runBlocking {
        var geneCount = 0
        val stopwatch = Stopwatch.createStarted()
        val genes = resolveEntrezRelationship(resolveHgncRelationship(generateGeneProperties()))
        for (gene in genes) {
            println(gene)
            geneCount += 1
        }
        println("Update $geneCount genes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    }
}

fun main() {
    GeneRelationshipUtil().updateCosmicGenes()
}

