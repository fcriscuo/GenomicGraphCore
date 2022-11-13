package org.batteryparkdev.genomicgraphcore.go.app

import kotlinx.coroutines.*
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.batteryparkdev.genomicgraphcore.go.GoTermLoader
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import kotlin.coroutines.CoroutineContext

/*
Application to load Gene Ontology (GO) data from an OBO-formatted text file
into a local Neo4j database. GO terms that have been marked as obsolete are not
included.
 */
class GoDataImporter(private val goFilename:String): CoroutineScope {
    
    @OptIn(DelicateCoroutinesApi::class)
    val job = GlobalScope.launch() {
        delay(2000)
    }

    // creating local CoroutineContext
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // extension function
    // source: https://stackoverflow.com/questions/53921470/how-to-run-two-jobs-in-parallel-but-wait-for-another-job-to-finish-using-kotlin
    fun <T> CoroutineScope.asyncIO(ioFun: () -> T) = async(Dispatchers.IO) { ioFun() }
    fun <T> CoroutineScope.asyncDefault(defaultFun: () -> T) = async(Dispatchers.Default) { defaultFun() }

    private val nodeNameList = listOf<String>("GoTerm","GoSynonymCollection", "GoSynonym")

    private fun loadGeneOntologyData():String {
        GoTermLoader.loadGoTerms(goFilename)
        return ("Gene Ontology data import task completed")
    }

    fun deleteGoNodes():String {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
        return "GoTerm-related nodes & relationships deleted"
    }

    /*
  Function to define the asynchronous workflow
   */
    @OptIn(DelicateCoroutinesApi::class)
    fun loadData() {
        GlobalScope.launch {
           // val task01 = asyncDefault { loadPubmedJob() }
            val task02 = asyncIO {  loadGeneOntologyData() }
            onDone( task02.await())
        }
    }
    private fun onDone( job2Result:String) {
        println("Executing onDone function")
        //println("task01 = $job1Result ")
        println("task02 = $job2Result ")
        job.cancel()
    }

//    private fun loadPubmedJob(): String {  // job 1
//        println("1 - Starting PubMed loader")
//        val taskDuration = 172_800_000L
//        val timerInterval = 60_000L
//        val scanTimer = AsyncPubMedPublicationLoader.scheduledPlaceHolderNodeScan(timerInterval)
//        try {
//            Thread.sleep(taskDuration)
//        } finally {
//            scanTimer.cancel();
//        }
//        return "PubMed data loaded"
//    }
}

fun main(args: Array<String>): Unit = runBlocking {
    val filename = if (args.isNotEmpty()) args[0] else "./data/go/sample_go.obo"
    val loader = GoDataImporter(filename)
    val database = Neo4jPropertiesService.neo4jDatabase
    if (Neo4jConnectionService.isTestingContext()) {
        println("WARNING: Invoking this application will delete all Gene Ontology data from the ${database} database")
        println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
       // Thread.sleep(20_000L)
        loader.deleteGoNodes()
    }
    println("Gene Ontology data will now be loaded from: $filename into the ${database} Neo4j database")
    defineGoDatabaseConstraints()
    loader.loadData()
    println("Gene Ontology data has been loaded into Neo4j")
    awaitCancellation()
}
