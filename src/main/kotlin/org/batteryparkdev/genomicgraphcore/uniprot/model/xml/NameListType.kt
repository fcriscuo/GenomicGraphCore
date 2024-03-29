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
 * Java class for nameListType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="nameListType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;choice maxOccurs="unbounded">
 * &lt;element name="consortium" type="{http://uniprot.org/uniprot}consortiumType"/>
 * &lt;element name="person" type="{http://uniprot.org/uniprot}personType"/>
 * &lt;/choice>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameListType", propOrder = ["consortiumOrPerson"])
class NameListType {
    @XmlElements(XmlElement(name = "consortium", type = ConsortiumType::class), XmlElement(name = "person", type = PersonType::class))
    protected var consortiumOrPerson: List<Any>? = null

    /**
     * Gets the value of the consortiumOrPerson property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consortiumOrPerson property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getConsortiumOrPerson().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [ConsortiumType]
     * [PersonType]
     *
     *
     */
    fun getConsortiumOrPersonList(): List<Any>? {
        if (consortiumOrPerson == null) {
            consortiumOrPerson = ArrayList()
        }
        return consortiumOrPerson
    }
}