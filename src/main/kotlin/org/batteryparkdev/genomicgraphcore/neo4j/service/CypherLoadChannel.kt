package org.batteryparkdev.genomicgraphcore.neo4j.service

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/*
Represents a Kotlin channel responsible for receiving Cypher load statements from
multiple sources and forwarding them to a Kotlin object that will execute them
in a connected Neo4j database.
 */
object CypherLoadChannel {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun sendCypher(cyphers: ReceiveChannel<String>):ReceiveChannel<String>  =
        coroutineScope {
            produce<String> {
                for (cypher in cyphers) {
                    send(cypher)
                    delay(20)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun receiveCypher(statements: ReceiveChannel<String>): ReceiveChannel<String> =
        coroutineScope {
            produce<String> {
                for (statement in statements) {
                    Neo4jConnectionService.executeCypherLoadAsync(statement)
                    send(statement.substring(0, 20))  // to support debugging if necessary
                    delay(20)
                }
            }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun CoroutineScope.processCypher(cypher:String): Unit {
        val cypherChannel = produce{send(cypher)}
        coroutineScope { receiveCypher(sendCypher(cypherChannel)) }
    }
}




