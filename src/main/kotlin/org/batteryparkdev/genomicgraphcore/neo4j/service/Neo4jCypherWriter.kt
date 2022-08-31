package  org.batteryparkdev.genomicgraphcore.neo4j.service

import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import java.io.File

/*
Responsible for logging MERGE and CREATE cypher commands to a specified file
 */
object Neo4jCypherWriter {
    private val cypherPath = Neo4jPropertiesService.resolveCypherLogFilename()
    private val cypherFileWriter = File(cypherPath).bufferedWriter()

    fun close()= cypherFileWriter.close()

    fun recordCypherCommand(command:String) =
        cypherFileWriter.write("$command\n")
    }