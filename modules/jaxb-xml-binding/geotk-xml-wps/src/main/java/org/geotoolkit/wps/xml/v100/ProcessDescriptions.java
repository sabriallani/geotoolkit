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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wps/1.0.0}ResponseBaseType">
 *       &lt;sequence>
 *         &lt;element name="ProcessDescription" type="{http://www.opengis.net/wps/1.0.0}ProcessDescriptionType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 * @module
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "processDescription"
})
@XmlRootElement(name = "ProcessDescriptions")
public class ProcessDescriptions extends ResponseBaseType implements org.geotoolkit.wps.xml.ProcessOfferings {

    @XmlElement(name = "ProcessDescription", namespace = "", required = true)
    protected List<ProcessDescriptionType> processDescription;

    public ProcessDescriptions() {

    }

    public ProcessDescriptions(String lang, List<ProcessDescriptionType> processDescription) {
        super("WPS", "1.0.0", lang);
        this.processDescription = processDescription;
    }

    /**
     * Gets the value of the processDescription property.
     *
     * @return Objects of the following type(s) are allowed in the list
     * {@link ProcessDescriptionType }
     *
     *
     */
    public List<ProcessDescriptionType> getProcessDescription() {
        if (processDescription == null) {
            processDescription = new ArrayList<>();
        }
        return this.processDescription;
    }

    @Override
    public List<ProcessDescriptionType> getProcesses() {
        return getProcessDescription();
    }

}
