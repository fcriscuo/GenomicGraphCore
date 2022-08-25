package org.batteryparkdev.genomicgraphcore.hgnc.util

import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.neo4j.driver.Record
import java.util.concurrent.TimeUnit

/*
Responsible for refactoring the CosmicGraph database to remove the CosmicHGNC nodes and the legacy
CosmicGene to CosmicHGNC relationships and create new CosmicGene to Hgnc relationships.
TODO: Move this code to the CosmicGraphDb project as soon as that project has been refactored
to use The GenomicGraphCore project
 */

data class GeneProperties (val geneSymbol: String, val hgncId: String, val entrezId: Int = 0) {
}

class GeneRelationshipUtil {
    //
    //1. Sequence through the CosmicGene database nodes
    //2. For each CosmicGene node:
    //   2.a resolve the appropriate Hgnc node using the gene_symbol property
    //   2.b create a HAS_HGNC relationship to the Hgnc node
    //   2.c create an Entrez node based on the Entrez id for the gene
    //   2.d create a HAS_ENTREZ relationship to the Entrez node for the gene
    //


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.generateGeneProperties() =
        produce<GeneProperties> {
            Neo4jConnectionService.executeCypherQuery("match (cg:CosmicGene),(h:Hgnc) " +
                    "where cg.gene_symbol = h.gene_symbol return cg.gene_symbol as geneSymbol," +
                    " cg.entrez_gene_id as entrezId, h.hgnc_id as hgncId ")
                .map {resolveGeneProperties(it)}
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.resolveHgncRelationship(genes: ReceiveChannel<GeneProperties>) =
        produce<GeneProperties> {
            for (gene in genes){
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
            for (gene in genes){
                if (gene.entrezId > 0){
                    Neo4jConnectionService.executeCypherCommand(
                        "MERGE (e:Entrez{ entrez_id: ${gene.entrezId}} )  " +
                                " WITH e " +
                                " MATCH (cg:CosmicGene{gene_symbol: ${gene.geneSymbol.formatNeo4jPropertyValue()}}) " +
                                " CREATE (cg) -[r: HAS_ENTREZ] -> (e) RETURN r ")
                }
                send (gene)
                delay(20)
            }
        }

    fun updateCosmicGenes() = runBlocking{
        var geneCount =  0
        val stopwatch = Stopwatch.createStarted()
        val genes = resolveEntrezRelationship(resolveHgncRelationship(generateGeneProperties()))
        for (gene in genes) {
            println(gene)
            geneCount += 1
        }
        println("Update $geneCount genes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    }



    private fun resolveGeneProperties( record: Record):GeneProperties{
        val recordMap = record.asMap()
        return GeneProperties(recordMap["geneSymbol"].toString(),
        recordMap["hgncId"].toString(), recordMap["entrezId"].toString().parseValidInteger())
    }

}

fun main() {
    GeneRelationshipUtil().updateCosmicGenes()
}

