//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 08:09:53 AM PDT 
//
package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import javax.xml.bind.annotation.*
import javax.xml.datatype.XMLGregorianCalendar

/**
 *
 * Java class for sequenceType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sequenceType">
 * &lt;simpleContent>
 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 * &lt;attribute name="length" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="mass" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="checksum" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;attribute name="modified" use="required" type="{http://www.w3.org/2001/XMLSchema}date" />
 * &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 * &lt;attribute name="precursor" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 * &lt;attribute name="fragment">
 * &lt;simpleType>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 * &lt;enumeration value="single"/>
 * &lt;enumeration value="multiple"/>
 * &lt;/restriction>
 * &lt;/simpleType>
 * &lt;/attribute>
 * &lt;/extension>
 * &lt;/simpleContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sequenceType", propOrder = ["value"])
class SequenceType {
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

    /**
     * Gets the value of the length property.
     *
     */
    /**
     * Sets the value of the length property.
     *
     */
    @XmlAttribute(name = "length", required = true)
    var length = 0

    /**
     * Gets the value of the mass property.
     *
     */
    /**
     * Sets the value of the mass property.
     *
     */
    @XmlAttribute(name = "mass", required = true)
    var mass = 0

    /**
     * Gets the value of the checksum property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the checksum property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "checksum", required = true)
    var checksum: String? = null

    /**
     * Gets the value of the modified property.
     *
     * @return
     * possible object is
     * [XMLGregorianCalendar]
     */
    /**
     * Sets the value of the modified property.
     *
     * @param value
     * allowed object is
     * [XMLGregorianCalendar]
     */
    @XmlAttribute(name = "modified", required = true)
    @XmlSchemaType(name = "date")
    var modified: XMLGregorianCalendar? = null

    /**
     * Gets the value of the version property.
     *
     */
    /**
     * Sets the value of the version property.
     *
     */
    @XmlAttribute(name = "version", required = true)
    var version = 0

    /**
     * Gets the value of the precursor property.
     *
     * @return
     * possible object is
     * [Boolean]
     */
    /**
     * Sets the value of the precursor property.
     *
     * @param value
     * allowed object is
     * [Boolean]
     */
    @XmlAttribute(name = "precursor")
    var isPrecursor: Boolean? = null

    /**
     * Gets the value of the fragment property.
     *
     * @return
     * possible object is
     * [String]
     */
    /**
     * Sets the value of the fragment property.
     *
     * @param value
     * allowed object is
     * [String]
     */
    @XmlAttribute(name = "fragment")
    var fragment: String? = null

}