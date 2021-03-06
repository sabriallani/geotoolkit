/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.wps.xml.v100;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.wps.xml.Format;


/**
 * A combination of format, encoding, and/or schema supported by a process input or output.
 *
 * <p>Java class for ComplexDataDescriptionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ComplexDataDescriptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MimeType" type="{http://www.opengis.net/ows/1.1}MimeType"/>
 *         &lt;element name="Encoding" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Schema" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 * @module
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplexDataDescriptionType", propOrder = {
    "mimeType",
    "encoding",
    "schema"
})
public class ComplexDataDescriptionType implements Format{

    @XmlElement(name = "MimeType", namespace = "", required = true)
    protected String mimeType;
    @XmlElement(name = "Encoding", namespace = "")
    @XmlSchemaType(name = "anyURI")
    protected String encoding;
    @XmlElement(name = "Schema", namespace = "")
    @XmlSchemaType(name = "anyURI")
    protected String schema;

    public ComplexDataDescriptionType() {

    }

    public ComplexDataDescriptionType( String encoding, String mimetype, String schema) {
        this.encoding = encoding;
        this.mimeType = mimetype;
        this.schema = schema;
    }

    /**
     * Gets the value of the mimeType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the value of the mimeType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setMimeType(final String value) {
        this.mimeType = value;
    }

    /**
     * Gets the value of the encoding property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setEncoding(final String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the schema property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Override
    public void setSchema(final String value) {
        this.schema = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(this.getClass().getSimpleName()).append("]\n");
        if (encoding != null) {
            sb.append("encoding:").append(encoding).append('\n');
        }
        if (mimeType != null) {
            sb.append("mimeType:").append(mimeType).append('\n');
        }
        if (schema != null) {
            sb.append("schema:").append(schema).append('\n');
        }
        return sb.toString();
    }

    /**
     * Verify that this entry is identical to the specified object.
     * @param object Object to compare
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ComplexDataDescriptionType) {
            final ComplexDataDescriptionType that = (ComplexDataDescriptionType) object;
            return Objects.equals(this.encoding, that.encoding) &&
                   Objects.equals(this.mimeType, that.mimeType) &&
                   Objects.equals(this.schema, that.schema);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.mimeType);
        hash = 41 * hash + Objects.hashCode(this.encoding);
        hash = 41 * hash + Objects.hashCode(this.schema);
        return hash;
    }
}
