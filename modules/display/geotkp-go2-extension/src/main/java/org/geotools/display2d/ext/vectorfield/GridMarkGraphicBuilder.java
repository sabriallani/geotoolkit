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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display2d.primitive.GraphicJ2D;
import org.geotools.map.CoverageMapLayer;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;

import org.opengis.display.canvas.Canvas;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

/**
 * Graphic builder for Coverages to be displayed with
 * arrows or cercles.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class GridMarkGraphicBuilder implements GraphicBuilder<GraphicJ2D>{

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<GraphicJ2D> createGraphics(final MapLayer layer, final Canvas cvs) {

        if( !(cvs instanceof ReferencedCanvas2D) ){
            throw new IllegalArgumentException("Illegal canvas, must be a ReferencedCanvas2D");
        }

        final ReferencedCanvas2D canvas = (ReferencedCanvas2D) cvs;

        if(layer instanceof CoverageMapLayer){
            final CoverageMapLayer coverageLayer = (CoverageMapLayer) layer;
            
            
            //TODO fix to use the coveragereader
//            try {
//                feature = layer.getFeatureSource().getFeatures().features().next();
//            } catch (IOException ex) {
//                Logger.getLogger(GridMarkGraphicBuilder.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            GridCoverage2D coverage = (GridCoverage2D) feature.getProperty("grid").getValue();
            GridCoverage2D coverage = null;
            try {
                //get the default gridcoverage
                coverage = coverageLayer.getCoverageReader().read(null);
            } catch (FactoryException ex) {
                Logger.getLogger(GridMarkGraphicBuilder.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformException ex) {
                Logger.getLogger(GridMarkGraphicBuilder.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GridMarkGraphicBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(coverage != null){
                final RenderedGridMarks marks = new RenderedGridMarks(canvas,coverage);
                final Collection<GraphicJ2D> graphics = new ArrayList<GraphicJ2D>();
                graphics.add(marks);
                return graphics;
            }else{
                return Collections.emptyList();
            }

        }else{
            return Collections.emptyList();
        }
        
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<GraphicJ2D> getGraphicType() {
        return GraphicJ2D.class;
    }
    
}
