//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 08:09:53 AM PDT 
//
package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import org.batteryparkdev.genomicgraphcore.uniprot.model.xml.DbReferenceType
import java.math.BigInteger
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType

/**
 *
 * Describes the source of the data using a database cross-reference (or a 'ref' attribute when the source cannot be found in a public data source, such as PubMed, and is cited only within the UniProtKB entry).
 *
 *
 *
 * Java class for sourceType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sourceType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="dbReference" type="{http://uniprot.org/uniprot}dbReferenceType" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}integer" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sourceType", propOrder = ["dbReference"])
class SourceType {
    /**
     * Gets the value of the dbReference property.
     *
     * @return
     * possible object is
     * [DbReferenceType]
     */
    /**
     * Sets the value of the dbReference property.
     *
     * @param value
     * allowed object is
     * [DbReferenceType]
     */
    var dbReference: DbReferenceType? = null

    /**
     * Gets the value of the ref property.
     *
     * @return
     * possible object is
     * [BigInteger]
     */
    /**
     * Sets the value of the ref property.
     *
     * @param value
     * allowed object is
     * [BigInteger]
     */
    @XmlAttribute(name = "ref")
    var ref: BigInteger? = null

}