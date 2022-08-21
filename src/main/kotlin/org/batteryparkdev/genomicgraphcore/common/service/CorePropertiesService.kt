package org.batteryparkdev.genomicgraphcore.common.service

import org.batteryparkdev.property.service.ApplicationProperties

object CorePropertiesService {
    // map the core properties from the core.config properties file
    private val config =  ApplicationProperties("core.config")
}