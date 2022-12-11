package org.batteryparkdev.genomicgraphcore.common.obo

import arrow.core.Either
import java.io.File
import java.util.*
import java.util.function.Supplier

/*
Responsible for parsing complete Gene Ontology Terms from
a specified OBO-formatted file
Utilizes an intermediate object, Termlines, to accommodate the
variable number of synonyms and relationships in a GO Term
 */
class OboTermSupplier(filename: String) : Supplier<Either<Exception, OboTerm>> {
    private val oboScanner = Scanner(File(filename))
    private val termLabel = "[Term]"

   fun hasMoreLines():Boolean = oboScanner.hasNextLine()

    override fun get(): Either<Exception, OboTerm> {
        val OboTerm = generateOboTerm()
        return when (OboTerm.isValid()) {
            true -> Either.Right(OboTerm)
            false -> Either.Left(Exception("EOF"))
        }
    }

    private fun scanLine(): String =
        when  (oboScanner.hasNextLine()) {
            true -> oboScanner.nextLine()
            false -> "EOF"
        }

    private fun generateOboTerm(): OboTerm {
        advanceToNextTerm()
        val termlines = collectLines()
        return OboTerm.generateOboTerm(termlines.getTermlinesContent())
    }

    private fun collectLines(): Termlines {
        val termlines = Termlines()
        var line = scanLine()
        while (line.equals("EOF").not().and(line.isNotBlank())) {
            termlines.addTermlines(line)
            line = scanLine()
        }
        return termlines
    }

    private fun advanceToNextTerm() {
       var line = " "
      //  while (line.startsWith(termLabel).not().and(line.isNotBlank())) {
        while (line.startsWith(termLabel).not()) {
            line = scanLine()
        }
    }
}

/*
Main function for integration testing
 */
fun main(args: Array<String>) {
    val filePathName = if (args.isNotEmpty()) args[0] else "./data/sample_go.obo"
    println("Processing OBO-formatted file: $filePathName")
    val supplier = OboTermSupplier(filePathName)
    for (i in 1..200) {
      when ( val result = supplier.get()) {
          is Either.Right -> {
              val OboTerm = result.value
              //println("OboTerm:  ${OboTerm.id}  ${OboTerm.namespace}  ${OboTerm.name}  ${OboTerm.synonyms}")
              OboTerm.relationshipList.forEach { rel ->
                  println("   Relationship:  ${rel.type}  ${rel.qualifier} ${rel.targetId}  ") }
              OboTerm.xrefList.forEach { xref->
                  println("Xref: ${xref.source}  ${xref.id}  ${xref.description}")
              }
          }
          is Either.Left -> {
              println("Exception: ${result.value.message}")
          }
      }
    }
}

data class Termlines(
    private val lines: MutableList<String> = mutableListOf()
) {
    fun getTermlinesContent(): List<String> = lines.toList()

    fun addTermlines(line: String) = lines.add(line)

    fun hasContent(): Boolean = lines.isNotEmpty()

}