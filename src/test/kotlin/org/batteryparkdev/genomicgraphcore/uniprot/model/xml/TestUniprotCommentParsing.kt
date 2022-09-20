package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import java.net.URL
import javax.xml.bind.JAXBContext
/*
Test extracting comments from a UniProt entry in XML format
 */

fun main() {

    val text = URL("https://www.uniprot.org:443" +
            "/uniprot/P05067.xml?include=yes").readText()
    println("Text: ${text.subSequence(0,140)}")
    val jaxbContext = JAXBContext.newInstance(Uniprot::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    text.reader().use { it ->
        val uniprot =  unmarshaller.unmarshal(it) as Uniprot
        uniprot.getEntryList()?.forEach { entry ->
            run {
                println("names: ${entry.getNameList()}  comment count = ${entry.getCommentList()?.size}")
                entry.getCommentList()?.forEach {  comment ->
                    run{
                        println("** Comment Type: ${comment.type}")
                        println("name ${comment.name}")
                         comment.text?.forEach { evidencedStringType ->
                            println("Text: ${evidencedStringType.value}")
                        }
//                        println("ref id ${ref.id}  type: ${ref.type}  properties: ${ref.getPropertyList()?.size}")
//                        ref.getPropertyList()?.stream()?.filter { it -> it.type.equals("term") }
//                            ?.forEach { println("term:  ${it.value}" ) }
                    }
                }
            }
        }

    }
}