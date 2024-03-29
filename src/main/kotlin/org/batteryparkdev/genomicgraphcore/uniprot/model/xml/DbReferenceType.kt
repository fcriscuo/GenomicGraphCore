//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 08:09:53 AM PDT 
//
package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import java.util.*
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType

/**
 *
 * Describes a database cross-reference. Equivalent to the flat file DR-line.
 *
 *
 *
 * Java class for dbReferenceType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="dbReferenceType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="molecule" type="{http://uniprot.org/uniprot}moleculeType" minOccurs="0"/>
 * &lt;element name="property" type="{http://uniprot.org/uniprot}propertyType" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;attribute name="evidence" type="{http://uniprot.org/uniprot}intListType" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dbReferenceType", propOrder = ["molecule", "property"])
class DbReferenceType {
    /**
     * Gets the value of the molecule property.
     *
     * @return
     * possible object is
     * [MoleculeType]
     */
    /**
     * Sets the value of the molecule property.
     *
     * @param value
     * allowed object is
     * [MoleculeType]
     */
    var molecule: MoleculeType? = null
    protected var property: List<PropertyType>? = null

    /**
     * Gets the value of the type property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the type property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "type", required = true)
    var type: String? = null

    /**
     * Gets the value of the id property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the id property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "id", required = true)
    var id: String? = null

    @XmlAttribute(name = "evidence")
    protected var evidence: List<Int>? = null

    /**
     * Gets the value of the property property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getProperty().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [PropertyType]
     *
     *
     */
    fun getPropertyList(): List<PropertyType>? {
        if (property == null) {
            property = ArrayList()
        }
        return property
    }

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