//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 08:09:53 AM PDT 
//
package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import java.util.*
import javax.xml.bind.annotation.*

/**
 *
 * Java class for evidencedStringType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="evidencedStringType">
 * &lt;simpleContent>
 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 * &lt;attribute name="evidence" type="{http://uniprot.org/uniprot}intListType" />
 * &lt;/extension>
 * &lt;/simpleContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "evidencedStringType", propOrder = ["value"])
class EvidencedStringType {
    /**
     * Gets the value of the value property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the value property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlValue
    var value: String? = null

    @XmlAttribute(name = "evidence")
    protected var evidence: List<Int>? = null

    /**
     * Gets the value of the evidence property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the evidence property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getEvidence().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [Integer]
     *
     *
     */
    fun getEvidenceList(): List<Int>? {
        if (evidence == null) {
            evidence = ArrayList()
        }
        return evidence
    }
}