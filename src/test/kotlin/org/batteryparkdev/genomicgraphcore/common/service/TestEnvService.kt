package org.batteryparkdev.genomicgraphcore.common.service

import io.github.cdimascio.dotenv.dotenv

fun main() {
    EnvService.setEnv("FAVORITE_COLOR", "Blue")
    println("Favorite color = ${EnvService.getEnvVariable("FAVORITE_COLOR")}")
    val dotenv = dotenv()
    for (e in dotenv.entries()) {
        println("dotenv  ${e.key} = ${e.value}")
    }
    println("Single fetch = ${EnvService.getEnvVariable("NEO4J_HOME")}}")

}