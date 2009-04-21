/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2009, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.internal.jaxb.text;

import java.net.URI;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.geotoolkit.xml.Namespaces;


/**
 * The {@code AnchorType} element, which is included in {@code CharacterString} elements.
 *
 * @author Guilhem Legal (Geomatys)
 * @version 3.0
 *
 * @since 2.5
 * @module
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = Namespaces.GMX)
public final class AnchorType implements CharSequence {
    /**
     * A URN.
     */
    @XmlAttribute(namespace = Namespaces.XLINK)
    public URI href;

    /**
     * Often a short textual description of the URN target.
     */
    @XmlValue
    public String value;

    /**
     * Creates a unitialized {@code AnchorType}.
     * This constructor is required by JAXB.
     */
    public AnchorType() {
    }

    /**
     * Creates an {@code AnchorType} initialized to the given value.
     *
     * @param href  A URN.
     * @param value Often a short textual description of the URN target.
     */
    public AnchorType(final URI href, final String value) {
        this.href  = href;
        this.value = value;
    }

    /**
     * Provided for compliance with the {@link CharSequence} contract,
     * but not expected to be used.
     */
    @Override
    public int length() {
        final String value = this.value;
        return (value != null) ? value.length() : 0;
    }

    /**
     * Provided for compliance with the {@link CharSequence} contract,
     * but not expected to be used.
     */
    @Override
    public char charAt(final int index) {
        return value.charAt(index);
    }

    /**
     * Provided for compliance with the {@link CharSequence} contract,
     * but not expected to be used.
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return value.subSequence(start, end);
    }

    /**
     * Returns the text as a string, or {@code null} if none.
     * The null value is expected by {@link TextAdapter#toString()}.
     */
    @Override
    public String toString() {
        return value;
    }
}
