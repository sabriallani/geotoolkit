/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Johann Sorel
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
package org.geotoolkit.ncwms;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.geotoolkit.client.AbstractClientFactory;
import org.geotoolkit.storage.coverage.CoverageStoreFactory;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.storage.DataType;
import org.geotoolkit.storage.DefaultFactoryMetadata;
import org.geotoolkit.storage.FactoryMetadata;
import org.geotoolkit.wms.xml.WMSVersion;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.*;

/**
 * NcWMS Server factory.
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module
 */
public class NcWMSClientFactory extends AbstractClientFactory implements CoverageStoreFactory{

    /** factory identification **/
    public static final String NAME = "ncWMS";
    public static final DefaultServiceIdentification IDENTIFICATION;
    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }

    public static final ParameterDescriptor<String> IDENTIFIER = createFixedIdentifier(NAME);

    /**
     * Version, Mandatory.
     */
    public static final ParameterDescriptor<String> VERSION;
    static{
        final WMSVersion[] values = WMSVersion.values();
        final String[] validValues =  new String[values.length];
        for(int i=0;i<values.length;i++){
            validValues[i] = values[i].getCode();
        }
        VERSION = createVersionDescriptor(validValues, WMSVersion.v130.getCode());
    }

    public static final ParameterDescriptorGroup PARAMETERS =
            new ParameterBuilder().addName("NcWMSParameters").createGroup(IDENTIFIER,URL,VERSION,SECURITY,TIMEOUT);

    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }

    @Override
    public ParameterDescriptorGroup getOpenParameters() {
        return PARAMETERS;
    }

    @Override
    public CharSequence getDisplayName() {
        return Bundle.formatInternational(Bundle.Keys.serverTitle);
    }

    @Override
    public CharSequence getDescription() {
        return Bundle.formatInternational(Bundle.Keys.serverDescription);
    }

    @Override
    public FactoryMetadata getMetadata() {
        return new DefaultFactoryMetadata(DataType.GRID, true, false, false);
    }

    @Override
    public NcWebMapClient open(ParameterValueGroup params) throws DataStoreException {
        ensureCanProcess(params);
        return new NcWebMapClient(params);
    }

    @Override
    public NcWebMapClient open(Map<String, ? extends Serializable> params) throws DataStoreException {
        return (NcWebMapClient) super.open(params);
    }

    @Override
    public NcWebMapClient create(Map<String, ? extends Serializable> params) throws DataStoreException {
        throw new DataStoreException("Can not create new ncWMS coverage store.");
    }

    @Override
    public NcWebMapClient create(ParameterValueGroup params) throws DataStoreException {
        throw new DataStoreException("Can not create new ncWMS coverage store.");
    }

}
