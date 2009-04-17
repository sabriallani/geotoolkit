/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2009, Geotools Project Managment Committee (PMC)
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

package org.geotools.display2d.container.stateless;

import org.geotools.display.canvas.VisitFilter;
import org.geotools.display.primitive.*;
import java.util.List;
import java.util.logging.Level;
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.canvas.RenderingContext;
import org.geotools.display2d.canvas.RenderingContext2D;
import org.geotools.display2d.primitive.GraphicJ2D;
import org.geotools.map.DynamicMapLayer;

import org.opengis.display.primitive.Graphic;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * Single object to represent a complete mapcontext.
 * This is a Stateless graphic object.
 *
 * @author johann sorel (Geomatys)
 */
public class StatelessDynamicLayerJ2D extends GraphicJ2D{
    
    private final DynamicMapLayer layer;
    
    public StatelessDynamicLayerJ2D(final ReferencedCanvas2D canvas, final DynamicMapLayer layer){
        super(canvas, getCRS(layer));
        this.layer = layer;
        
        try{
            setEnvelope(layer.getBounds());
        }catch(TransformException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * We asume the visibility test is already done when you call this method
     * This method is made for use in mutlithread.
     */
    public Object query(final RenderingContext2D renderingContext) throws PortrayalException{
        //we do not handle dynamic layers, the distant server does it
        return layer.query(renderingContext);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void paint(final RenderingContext2D renderingContext) {
                
        //we abort painting if the layer is not visible.
        if (!layer.isVisible()) return;        
        try {
            //we do not handle dynamic layers, the distant server does it
            layer.portray(renderingContext);
        } catch (PortrayalException ex) {
            renderingContext.getMonitor().exceptionOccured(ex, Level.SEVERE);
        }
    }
        
    private static CoordinateReferenceSystem getCRS(final DynamicMapLayer layer){
        return layer.getBounds().getCoordinateReferenceSystem();
    }

    @Override
    public List<Graphic> getGraphicAt(RenderingContext context, SearchArea mask, VisitFilter filter, List<Graphic> graphics) {
        //since this is a distant source, we have no way to find a child graphic.
        graphics.add(this);
        return graphics;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object getUserObject() {
        return layer;
    }

}
