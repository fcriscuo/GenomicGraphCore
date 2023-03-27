package org.batteryparkdev.genomicgraphcore.ontology.obo

import arrow.core.Either
import io.kotlintest.matchers.startWith
import org.apache.commons.lang3.StringUtils.startsWith
import java.io.File
import java.util.*
import java.util.function.Supplier

/*
Responsible for parsing complete Ontology Terms ([Term]) from
a specified OBO-formatted file
Utilizes an intermediate object, Termlines, to accommodate the
variable number of synonyms, xrefs, and relationships in a OBO Term
 */
class OboTermSupplier(filename: String) : Supplier<Either<String, OboTerm>> {
    private val oboScanner = Scanner(File(filename))
    private val termLabel = "[Term]"
    private val typedefLabel = "[Typedef]"
    init {
        // skip OBO header lines to first [Term]
       while(oboScanner.hasNextLine()){
          if (oboScanner.nextLine().startsWith(termLabel)) break
       }
    }

   fun hasMoreLines():Boolean = oboScanner.hasNextLine()

    override fun get(): Either<String, OboTerm> = generateOboTerm()

    private fun generateOboTerm(): Either<String,OboTerm> {
        val termlines = Termlines()
        while (oboScanner.hasNextLine()) {
            val line = oboScanner.nextLine()
            when  {
                line.startsWith(termLabel) ->
                    return Either.Right(OboTerm.generateOboTerm(termlines.getTermlinesContent()))
                line.startsWith(typedefLabel) ->
                    return Either.Left("All OBO Terms in have been processed")
                line.isEmpty() -> continue
                else -> termlines.addTermlines(line)
            }
        }
        // should not reach this return statement if file has correct structure
        return Either.Left("Error: Invalid OBO file structure")
    }
}

data class Termlines(
    private val lines: MutableList<String> = mutableListOf()
) {
    fun getTermlinesContent(): List<String> = lines.toList()

    fun addTermlines(line: String) = lines.add(line)

    fun hasContent(): Boolean = lines.isNotEmpty()

}