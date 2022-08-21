package org.batteryparkdev.genomicgraphcore.common.service

import java.lang.reflect.Field
import io.github.cdimascio.dotenv.*

object EnvService {
    private val dotenv = dotenv()
    fun setEnv(key: String, value: String) {
        try {
            val env = System.getenv()
            val cl: Class<*> = env.javaClass
            val field: Field = cl.getDeclaredField("m")
            field.isAccessible = true
            val writableEnv = field.get(env) as MutableMap<String, String>
            writableEnv[key] = value
        } catch (e: Exception) {
            throw IllegalStateException("Failed to set environment variable", e)
        }
    }

    // getter function searches system env as well as .env file
    fun getEnvVariable(varname:String):String = dotenv.get(varname) ?: "undefined"
}