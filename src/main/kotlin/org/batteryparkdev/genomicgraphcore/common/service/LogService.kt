package org.batteryparkdev.genomicgraphcore.common.service


import mu.KotlinLogging

/*
Responsible for providing a consistent logging structure for all components
 */
val logger = KotlinLogging.logger {}
object LogService {
    fun info(message: String) = logger.info { message }
    fun fine(message: String) = logger.debug { message }
    fun warn(message: String) = logger.warn { message }
    fun error(message: String) = logger.error{message}
    fun exception(e:Exception) = logger.error{e.message}
}
// extension function for logging exceptions
fun Exception.log(): Unit {
    LogService.exception(this)
    this.printStackTrace()
}
