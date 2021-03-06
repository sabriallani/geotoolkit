/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.sampling.xml;

import java.util.List;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.opengis.geometry.Geometry;
import org.opengis.metadata.Identifier;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface SamplingFeature extends org.opengis.observation.sampling.SamplingFeature {

    String getId();

    Identifier getName();

    String getDescription();

    Geometry getGeometry();

    List<? extends FeatureProperty> getSampledFeatures();
}
