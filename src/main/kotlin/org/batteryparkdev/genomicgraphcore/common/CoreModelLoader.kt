package org.batteryparkdev.genomicgraphcore.common

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.io.CsvRecordSequenceSupplier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService

/*
Represents a class that will read a delimited file (e.g. csv, tsv), parse the individual records into
objects that implement the CoreModel interface, and then load those model objects into a Neo4j
database
 */
class CoreModelLoader(val creator: CoreModelCreator) {
    private var nodeCount = 0

    /*
    Generate a Sequence of CSVRecord objects from the specified input file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
   private fun CoroutineScope.produceCSVRecords(filename: String, dropCount:Int  = 0) =
        produce<CSVRecord> {
            CsvRecordSequenceSupplier(filename).get()
                .drop(dropCount)
                .filter { it.size()> 0 }
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
                delay(40L)
            }
        }

    /*
    Load the CoreModel object implementations into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
   private  fun CoroutineScope.loadModels(models: ReceiveChannel<CoreModel>) =
        produce<NodeIdentifier> {
            for (model in models) {
                // load the model data into Neo4j, then complete its relationships to
                // other nodes
                // The two operations are performed in the same coroutine to avoid race conditions
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
                model.createModelRelationships()
                send(model.getNodeIdentifier())
                delay(20)
            }
        }

    /*
    Public method to process the specified delimited file
    // dropCount represents the number of file rows that ere loaded by a previous execution
     */
    fun loadDataFile(filename: String, dropCount: Int = 0) = runBlocking {
       val identifiers = loadModels(generateModels(produceCSVRecords(filename, dropCount)))
        while(identifiers.iterator().hasNext()){
            nodeCount+= 1
        }
        println("Loaded record count for ${creator::class.java.name }= $nodeCount")
    }
}