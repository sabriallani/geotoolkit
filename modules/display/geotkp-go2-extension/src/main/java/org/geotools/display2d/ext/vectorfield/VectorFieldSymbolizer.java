/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2008, Geotools Project Managment Committee (PMC)
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

package org.geotools.display2d.ext.vectorfield;

import javax.measure.unit.NonSI;

import org.geotools.style.AbstractExtensionSymbolizer;
import org.geotools.style.StyleConstants;

/**
 * VectorField symbolizer, to render wind arrows
 *
 * @author Johann Sorel (Geomatys)
 */
public class VectorFieldSymbolizer extends AbstractExtensionSymbolizer{

    public static final String NAME = "VectorField";

    public VectorFieldSymbolizer(){
        super(NonSI.PIXEL,"","vectorField",StyleConstants.DEFAULT_DESCRIPTION);
    }

    @Override
    public String getExtensionName() {
        return NAME;
    }

}
