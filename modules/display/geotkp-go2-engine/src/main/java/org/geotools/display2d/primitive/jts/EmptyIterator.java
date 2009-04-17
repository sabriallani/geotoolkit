/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.display2d.primitive.jts;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An iterator for empty geometries
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @since 2.9
 */
public class EmptyIterator extends GeometryIterator<Geometry> {

    public EmptyIterator() {
        super(null,null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isDone() {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void next() {
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int currentSegment(double[] coords) {
        return 0;
    }
}
