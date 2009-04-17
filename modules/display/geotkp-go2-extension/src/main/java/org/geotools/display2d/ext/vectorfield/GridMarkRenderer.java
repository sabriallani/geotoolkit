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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReadParam;
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.canvas.VisitFilter;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display2d.primitive.GraphicCoverageJ2D;
import org.geotools.display2d.primitive.ProjectedFeature;
import org.geotools.display.primitive.ReferencedGraphic.SearchArea;
import org.geotools.display2d.style.renderer.SymbolizerRenderer;
import org.geotoolkit.geometry.GeneralEnvelope;

import org.geotools.display2d.canvas.RenderingContext2D;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

/**
 * Renderer for vector field arrows.
 *
 * @author Johann Sorel (Geomatys)
 */
public class GridMarkRenderer implements SymbolizerRenderer<VectorFieldSymbolizer,CachedVectorFieldSymbolizer>{

    @Override
    public Class<VectorFieldSymbolizer> getSymbolizerClass() {
        return VectorFieldSymbolizer.class;
    }

    @Override
    public Class<CachedVectorFieldSymbolizer> getCachedSymbolizerClass() {
        return CachedVectorFieldSymbolizer.class;
    }

    @Override
    public CachedVectorFieldSymbolizer createCachedSymbolizer(VectorFieldSymbolizer symbol) {
        return new CachedVectorFieldSymbolizer(symbol);
    }

    @Override
    public void portray(ProjectedFeature graphic, CachedVectorFieldSymbolizer symbol,
            RenderingContext2D context) throws PortrayalException {
        //nothing to portray
    }

    @Override
    public void portray(final GraphicCoverageJ2D graphic, CachedVectorFieldSymbolizer symbol,
            RenderingContext2D context) throws PortrayalException {

        final GeneralEnvelope bounds = new GeneralEnvelope(context.getCanvasObjectiveBounds());
        bounds.setCoordinateReferenceSystem(context.getObjectiveCRS());
        final double[] resolution = context.getResolution();
        final CoverageReadParam param = new CoverageReadParam(bounds, resolution);

        GridCoverage2D coverage;
        try {
            coverage = graphic.getGridCoverage(param);
        } catch (FactoryException ex) {
            throw new PortrayalException(ex);
        } catch (IOException ex) {
            throw new PortrayalException(ex);
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }

        final ReferencedCanvas2D canvas = context.getCanvas();

        if(coverage != null){
            final RenderedGridMarks marks = new RenderedGridMarks(canvas,coverage);
            marks.paint(context);
        }

    }

    @Override
    public boolean hit(ProjectedFeature graphic, CachedVectorFieldSymbolizer symbol,
            RenderingContext2D context, SearchArea mask, VisitFilter filter) {
        return false;
    }

    @Override
    public boolean hit(GraphicCoverageJ2D graphic, CachedVectorFieldSymbolizer symbol,
            RenderingContext2D renderingContext, SearchArea mask, VisitFilter filter) {
        return false;
    }

    @Override
    public Rectangle2D estimate(ProjectedFeature graphic, CachedVectorFieldSymbolizer symbol,
            RenderingContext2D context, Rectangle2D rect) {
        return rect;
    }

    @Override
    public Rectangle2D glyphPreferredSize(CachedVectorFieldSymbolizer symbol) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void glyph(Graphics2D g, Rectangle2D rect, CachedVectorFieldSymbolizer symbol) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
