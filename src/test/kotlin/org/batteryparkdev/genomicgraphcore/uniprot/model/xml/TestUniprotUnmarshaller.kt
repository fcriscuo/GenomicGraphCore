package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import java.net.URL
import javax.xml.bind.JAXBContext

fun main() {

    val text = URL("https://www.uniprot.org:443" +
            "/uniprot/A0A024R6A3.xml?include=yes").readText()
    println("Text: ${text.subSequence(0,140)}")
    val jaxbContext = JAXBContext.newInstance(Uniprot::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    text.reader().use { it ->
        val uniprot =  unmarshaller.unmarshal(it) as Uniprot
        uniprot.getEntryList()?.forEach { entry ->
            run {
                println("+++ UniProt ID: ${entry.getNameList()?.get(0)}")
                displayProteinNames(entry)
                displayInteractions(entry)

                entry.getGeneList()?.forEach { gene -> println(gene.getNameList()?.get(0)?.value )}
                println("name: ${entry.getNameList()} db reference count = ${entry.getDbReferenceList()?.size}")
                entry.getDbReferenceList()?.forEach { ref ->
                    run{
                        println("ref id ${ref.id}  type: ${ref.type}  properties: ${ref.getPropertyList()?.size}")
                        ref.getPropertyList()?.stream()?.filter { it -> it.type.equals("term") }
                            ?.forEach { println("term:  ${it.value}" ) }
                    }
                }
            }
        }

    }
}
fun displayProteinNames(entry: Entry):Unit {
    println("Recommended Protein Name: ${entry.protein?.recommendedName?.fullName?.value}")
    entry.protein?.getAlternativeNameList()?.forEach { alt ->
        println("-- Alternative Name: ${alt.fullName?.value}") }
}


fun displayInteractions(entry: Entry): Unit {
    entry.getCommentList()?.forEach { comment -> run {
        comment.interactant?.forEach { type ->
            println("Interaction id: ${type.id}   Interact ID: ${type.intactId}}")
            println("Label:  ${type.label}")
        }
    } }
}