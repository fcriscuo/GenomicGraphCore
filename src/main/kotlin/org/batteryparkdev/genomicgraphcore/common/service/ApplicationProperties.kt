package org.batteryparkdev.genomicgraphcore.common.service

import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.io.File


/*
Responsible for resolving application properties from a specified property file
The directory for configuration file can be specified by the CONFIG_DIR environment
variable.
The deafult is $HOME/.genomicapps
 */
class ApplicationProperties(val configFileName: String) {
    private val configDir = getEnvVariable("CONFIG_DIR")
        ?: getEnvVariable("HOME").plus("/.genomicapps")

    private val config = configurationFromFile()

    fun getConfigPropertyAsString(propertyName: String): String =
        config.getString(propertyName, "")

    fun getConfigPropertyAsBoolean(propertyName: String): Boolean =
        config.getBoolean(propertyName, false)

    fun getConfigPropertyAsInt(propertyName: String): Int =
        config.getInt(propertyName, 0)

    private fun configurationFromFile(): Configuration {
        require(propertiesFileExists()){"Properties file $configFileName cannot be read"}
        val params = Parameters()
        val propertiesFile = File(configFileName)
        val builder = FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration::class.java
        )
            .configure(
                params.fileBased()
                    .setFile(propertiesFile)
                    .setBasePath(configDir)
            )
        return builder.configuration
    }

    private fun propertiesFileExists():Boolean {
        val fileName = configDir.plus("/").plus(configFileName)
        return  File(fileName).canRead()
    }

    fun getEnvVariable(varname:String):String? = System.getenv(varname)


}