/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2012, Geomatys
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


package org.geotoolkit.gml.xml.v321;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.geotoolkit.gml.xml.Coordinates;
import org.apache.sis.util.logging.Logging;


/**
 * This type is deprecated for tuples with ordinate values that are numbers.
 * CoordinatesType is a text string, intended to be used to record an array of tuples or coordinates.
 * While it is not possible to enforce the internal structure of the string through schema validation, some optional attributes have been provided in previous versions of GML to support a description of the internal structure. These attributes are deprecated. The attributes were intended to be used as follows:
 * Decimal  symbol used for a decimal point (default="." a stop or period)
 * cs           symbol used to separate components within a tuple or coordinate string (default="," a comma)
 * ts           symbol used to separate tuples or coordinate strings (default=" " a space)
 * Since it is based on the XML Schema string type, CoordinatesType may be used in the construction of tables of tuples or arrays of tuples, including ones that contain mixed text and numeric values.
 *
 * <p>Java class for CoordinatesType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CoordinatesType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="decimal" type="{http://www.w3.org/2001/XMLSchema}string" default="." />
 *       &lt;attribute name="cs" type="{http://www.w3.org/2001/XMLSchema}string" default="," />
 *       &lt;attribute name="ts" type="{http://www.w3.org/2001/XMLSchema}string" default=" " />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordinatesType", propOrder = {
    "value"
})
public class CoordinatesType implements Coordinates {

    @XmlValue
    private String value;
    @XmlAttribute
    private String decimal;
    @XmlAttribute
    private String cs;
    @XmlAttribute
    private String ts;

    public CoordinatesType() {

    }

    public CoordinatesType(final Coordinates that) {
        if (that != null) {
            this.cs      = that.getCs();
            this.decimal = that.getDecimal();
            this.ts      = that.getTs();
            this.value   = that.getValue();
        }
    }
    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public List<Double> getValues() {
        final String curentTs;
        if (ts == null) {
            curentTs = " ";
        } else {
            curentTs = ts;
        }

        final String curentCs;
        if (cs == null) {
            curentCs = ",";
        } else {
            curentCs = cs;
        }

        final List<Double> values = new ArrayList<Double>();
        if (value != null) {
            final StringTokenizer tokenizer = new StringTokenizer(value, curentTs);
            while (tokenizer.hasMoreTokens()) {
                final String v = tokenizer.nextToken();
                try {
                    int i = v.indexOf(curentCs);
                    if (i != -1) {
                        String first = v.substring(0, i);
                        String second = v.substring(i + 1);
                        values.add(Double.parseDouble(first));
                        values.add(Double.parseDouble(second));
                    }
                } catch (NumberFormatException ex) {
                    Logging.getLogger("org.geotoolkit.gml.xml.v321").log(Level.WARNING, "unable to parse coordiante value:{0}", v);
                }
            }
        }
        return values;
    }


    /**
     * Gets the value of the decimal property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getDecimal() {
        if (decimal == null) {
            return ".";
        } else {
            return decimal;
        }
    }

    /**
     * Sets the value of the decimal property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDecimal(String value) {
        this.decimal = value;
    }

    /**
     * Gets the value of the cs property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getCs() {
        if (cs == null) {
            return ",";
        } else {
            return cs;
        }
    }

    /**
     * Sets the value of the cs property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCs(String value) {
        this.cs = value;
    }

    /**
     * Gets the value of the ts property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getTs() {
        if (ts == null) {
            return " ";
        } else {
            return ts;
        }
    }

    /**
     * Sets the value of the ts property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTs(String value) {
        this.ts = value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 47 * hash + (this.decimal != null ? this.decimal.hashCode() : 0);
        hash = 47 * hash + (this.cs != null ? this.cs.hashCode() : 0);
        hash = 47 * hash + (this.ts != null ? this.ts.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CoordinatesType) {
            final CoordinatesType that = (CoordinatesType) object;


            return Objects.equals(this.cs,      that.cs) &&
                   Objects.equals(this.ts,      that.ts) &&
                   Objects.equals(this.value,   that.value) &&
                   Objects.equals(this.decimal, that.decimal);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("CoordinatesType[").append('\n');
        if (value != null) {
            s.append("value : ").append(value.toString()).append('\n');
        }
        if (decimal != null) {
            s.append("decimal : ").append(decimal.toString()).append('\n');
        }
        if (cs != null) {
            s.append("cs : ").append(cs.toString()).append('\n');
        }
        if (ts != null) {
            s.append("ts : ").append(ts.toString()).append('\n');
        }
        s.append("]");
        return s.toString();
    }
}
