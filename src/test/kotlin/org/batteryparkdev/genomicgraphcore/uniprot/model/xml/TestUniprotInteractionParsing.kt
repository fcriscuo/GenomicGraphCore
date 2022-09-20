package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import java.net.URL
import javax.xml.bind.JAXBContext
/*
Test extracting interaction comments from a UniProt entry in XML format
 */

fun main() {

    val text = URL("https://www.uniprot.org:443" +
            "/uniprot/P05067.xml?include=yes").readText()
    println("Text: ${text.subSequence(0,140)}")
    val jaxbContext = JAXBContext.newInstance(Uniprot::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    text.reader().use { it ->
        val uniprot =  unmarshaller.unmarshal(it) as Uniprot
        uniprot.getEntryList()
        ?.forEach { entry ->
            run {
                entry.getCommentList()?.filter { commentType -> commentType.type == "interaction"  }
                    ?.map { commentType -> commentType.interactant }
                    ?.flatMap { it?.toList() ?: emptyList() }
                    ?.onEach {it -> println("Entry: ${entry.getAccessionList()?.get(0)} " +
                            " Interactant: ${it.id} intactId: ${it.intactId}  label: ${it.label}")  }
            }
        }

    }
}