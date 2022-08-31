package org.batteryparkdev.genomicgraphcore.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.io.CSVRecordSupplier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.processPublications
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import java.nio.file.Paths
import kotlin.streams.asSequence

/*
Represents a class that will read a delimited file (e.g. csv, tsv), parse the individual records into
objects that implement the CosmicModel interface, and then load those model objects into a Neo4j
database
 */
class CoreModelLoader(val creator: CoreModelCreator ) {
    private var nodeCount = 0

    /*
    Generate a Sequence of CSVRecord objects from the specified input file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
   private fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    /*
    Map the CSVRecord objects to the specified CoreModel implementation
     */
    @OptIn(ExperimentalCoroutinesApi::class)
   private fun CoroutineScope.generateModels(records: ReceiveChannel<CSVRecord>) =
        produce<CoreModel> {
            for (record in records){
                val model = creator.createCoreModelFunction(record)
                if (model.isValid()) {
                    send(model)
                }
                delay(20L)
            }
        }

    /*
    Load the CoreModel object implementations into the Neo
    4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
   private  fun CoroutineScope.loadModels(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models) {
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
                send(model)
            }
        }

    /*
    Create a relationship to a
     */


    /*
    Create a relationship to any PubMed Ids referenced in the CoreModel implementation
     */

     @OptIn(ExperimentalCoroutinesApi::class)
   private fun CoroutineScope.processPubMedIds(models: ReceiveChannel<CoreModel>)=
        produce<CoreModel> {
            for (model in models){
                if(model.getPubMedIds().isNotEmpty()){
                    model.createPubMedRelationships()
                }
                send(model)
            }
        }


    /*
    Public method to process the specified delimited file
     */
    fun loadDataFile(filename: String) = runBlocking {
        val identifiers = processPubMedIds(loadModels(generateModels(produceCSVRecords(filename))))
        for (identifier in identifiers) {
            nodeCount += 1
            if (nodeCount % 500 == 0 ) {
                println("$nodeCount   HGNC: $identifier")
            }
        }
        println("Loaded record count for ${creator::class.java.name }= $nodeCount")
    }
}