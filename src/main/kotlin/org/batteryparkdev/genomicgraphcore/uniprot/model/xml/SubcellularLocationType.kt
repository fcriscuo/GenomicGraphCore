//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 08:09:53 AM PDT 
//
package org.batteryparkdev.genomicgraphcore.uniprot.model.xml

import org.batteryparkdev.genomicgraphcore.uniprot.model.xml.EvidencedStringType
import java.util.*
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * Describes the subcellular location and optionally the topology and orientation of a molecule.
 *
 *
 *
 * Java class for subcellularLocationType complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="subcellularLocationType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="location" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded"/>
 * &lt;element name="topology" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;element name="orientation" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
</pre> *
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subcellularLocationType", propOrder = ["location", "topology", "orientation"])
class SubcellularLocationType {
    @XmlElement(required = true)
    protected var location: List<EvidencedStringType>? = null
    protected var topology: List<EvidencedStringType>? = null
    protected var orientation: List<EvidencedStringType>? = null

    /**
     * Gets the value of the location property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the location property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getLocation().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [EvidencedStringType]
     *
     *
     */
    fun getLocationList(): List<EvidencedStringType>? {
        if (location == null) {
            location = ArrayList()
        }
        return location
    }

    /**
     * Gets the value of the topology property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the topology property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getTopology().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [EvidencedStringType]
     *
     *
     */
    fun getTopologyList(): List<EvidencedStringType>? {
        if (topology == null) {
            topology = ArrayList()
        }
        return topology
    }

    /**
     * Gets the value of the orientation property.
     *
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orientation property.
     *
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     * getOrientation().add(newItem);
    </pre> *
     *
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * [EvidencedStringType]
     *
     *
     */
    fun getOrientationList(): List<EvidencedStringType>? {
        if (orientation == null) {
            orientation = ArrayList()
        }
        return orientation
    }
}