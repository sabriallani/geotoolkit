/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010-2016, Geomatys
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
package org.geotoolkit.display2d.style.renderer;

import java.awt.geom.Area;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.Map;

import org.opengis.coverage.Coverage;
import org.opengis.feature.PropertyNotFoundException;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.Symbolizer;
import org.opengis.util.FactoryException;

import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.operation.projection.ProjectionException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.NullArgumentException;
import org.apache.sis.util.Utilities;

import org.geotoolkit.coverage.Category;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridEnvelope2D;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.DisjointCoverageDomainException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;

import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.container.stateless.StatelessContextParams;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.ProjectedObject;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.style.CachedSymbolizer;
import static org.geotoolkit.display2d.style.renderer.AbstractSymbolizerRenderer.LOGGER;
import static org.geotoolkit.display2d.style.renderer.DefaultRasterSymbolizerRenderer.extractQuery;
import static org.geotoolkit.display2d.style.renderer.DefaultRasterSymbolizerRenderer.fixEnvelopeWithQuery;

import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.image.interpolation.ResampleBorderComportement;
import org.geotoolkit.internal.referencing.CRSUtilities;

import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.processing.coverage.resample.ResampleDescriptor;
import org.geotoolkit.processing.coverage.resample.ResampleProcess;

import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.storage.coverage.CoverageResource;


/**
 * Abstract renderer for symbolizer which only apply on coverages data.
 * This class will take care to implement the coverage hit method.
 *
 * @author Johann Sorel  (Geomatys)
 * @author Remi Marechal (Geomatys)
 * @module
 */
public abstract class AbstractCoverageSymbolizerRenderer<C extends CachedSymbolizer<? extends Symbolizer>> extends AbstractSymbolizerRenderer<C>{


    public AbstractCoverageSymbolizerRenderer(final SymbolizerRendererService service, final C symbol, final RenderingContext2D context){
        super(service, symbol,context);
    }

    @Override
    public void portray(final ProjectedObject graphic) throws PortrayalException {
        if(graphic instanceof ProjectedFeature){
            final ProjectedFeature pf = (ProjectedFeature) graphic;
            final String geomName = symbol.getSource().getGeometryPropertyName();
            Object obj;
            if(geomName == null || geomName.isEmpty()){
                try{
                    obj = pf.getCandidate().getPropertyValue(AttributeConvention.GEOMETRY_PROPERTY.toString());
                }catch(PropertyNotFoundException ex){
                    obj = null;
                }
            }else{
                obj = GO2Utilities.evaluate(GO2Utilities.FILTER_FACTORY.property(geomName), pf.getCandidate(), null, null);
            }
            if(obj instanceof GridCoverage2D){
                final GridCoverage2D cov = (GridCoverage2D) obj;
                CharSequence name = cov.getName();
                if (name==null) name = "unnamed";
                final CoverageMapLayer ml = MapBuilder.createCoverageLayer(cov, GO2Utilities.STYLE_FACTORY.style(), name.toString());
                final StatelessContextParams params = new StatelessContextParams(renderingContext.getCanvas(),ml);
                params.update(renderingContext);
                final ProjectedCoverage pc = new ProjectedCoverage(params, ml);
                portray(pc);
            }else  if(obj instanceof CoverageResource){
                final CoverageMapLayer ml = MapBuilder.createCoverageLayer((CoverageResource)obj);
                final StatelessContextParams params = new StatelessContextParams(renderingContext.getCanvas(),ml);
                params.update(renderingContext);
                final ProjectedCoverage pc = new ProjectedCoverage(params, ml);
                portray(pc);
            }else  if(obj instanceof GridCoverageReader){
                final CoverageMapLayer ml = MapBuilder.createCoverageLayer((GridCoverageReader)obj);
                final StatelessContextParams params = new StatelessContextParams(renderingContext.getCanvas(),ml);
                params.update(renderingContext);
                final ProjectedCoverage pc = new ProjectedCoverage(params, ml);
                portray(pc);
            }
        }
    }

    @Override
    public boolean hit(final ProjectedObject graphic, final SearchAreaJ2D mask, final VisitFilter filter) {
        if(graphic instanceof ProjectedFeature){
            final ProjectedFeature pf = (ProjectedFeature) graphic;
            final Object obj = GO2Utilities.evaluate(GO2Utilities.FILTER_FACTORY.property(
                    symbol.getSource().getGeometryPropertyName()), pf.getCandidate(), null, null);
            if(obj instanceof GridCoverage2D){
                final CoverageMapLayer ml = MapBuilder.createCoverageLayer((GridCoverage2D)obj, GO2Utilities.STYLE_FACTORY.style(), "");
                final StatelessContextParams params = new StatelessContextParams(renderingContext.getCanvas(),ml);
                params.update(renderingContext);
                final ProjectedCoverage pc = new ProjectedCoverage(params, ml);
                return hit(pc,mask,filter);
            }
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hit(final ProjectedCoverage projectedCoverage, final SearchAreaJ2D search, final VisitFilter filter) {

        //TODO optimize test using JTS geometries, Java2D Area cost to much cpu

        final Shape mask = search.getDisplayShape();
        final Shape[] shapes;
        try {
            shapes = projectedCoverage.getEnvelopeGeometry().getDisplayShape();
        } catch (TransformException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return false;
        }

        for(Shape shape : shapes){
            final Area area = new Area(mask);
            switch(filter){
                case INTERSECTS :
                    area.intersect(new Area(shape));
                    if(!area.isEmpty()) return true;
                    break;
                case WITHIN :
                    Area start = new Area(area);
                    area.add(new Area(shape));
                    if(start.equals(area)) return true;
                    break;
            }
        }
        return false;
    }

    /**
     * Returns expected {@link GridCoverage2D} from given {@link ProjectedCoverage},
     * adapted to asked {@linkplain #renderingContext internally rendering context} situation.
     *
     * @param projectedCoverage Convenient representation of a {@link Coverage} for rendering.
     * @return an expected slice 2D of given {@link ProjectedCoverage}.
     * @throws org.geotoolkit.coverage.io.CoverageStoreException if problem during coverage reading.
     * @throws org.opengis.referencing.operation.TransformException if problem during {@link Envelope} transformation.
     * @throws org.opengis.util.FactoryException if problem during {@link Envelope} study.
     * @throws org.geotoolkit.process.ProcessException if problem during resampling processing.
     * @see ResampleDescriptor
     * @see ResampleProcess
     * @see ProjectedCoverage#getCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     */
    protected final GridCoverage2D getObjectiveCoverage(final ProjectedCoverage projectedCoverage/*, final CanvasType displayOrObjective*/)
            throws CoverageStoreException, TransformException, FactoryException, ProcessException {
        return getObjectiveCoverage(projectedCoverage, renderingContext.getCanvasObjectiveBounds(),
                renderingContext.getResolution(), renderingContext.getObjectiveToDisplay(), false);
    }

    /**
     * Returns expected {@linkplain GridCoverage2D elevation coverage} from given {@link ProjectedCoverage},
     * adapted to asked {@linkplain #renderingContext internally rendering context} situation.
     *
     * @param projectedCoverage Convenient representation of a {@link Coverage} for rendering.
     * @return an expected slice 2D of given {@link ProjectedCoverage}.
     * @throws org.geotoolkit.coverage.io.CoverageStoreException if problem during coverage reading.
     * @throws org.opengis.referencing.operation.TransformException if problem during {@link Envelope} transformation.
     * @throws org.opengis.util.FactoryException if problem during {@link Envelope} study.
     * @throws org.geotoolkit.process.ProcessException if problem during resampling processing.
     * @see ResampleDescriptor
     * @see ResampleProcess
     * @see ProjectedCoverage#getElevationCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     */
    protected final GridCoverage2D getObjectiveElevationCoverage(final ProjectedCoverage projectedCoverage/*, final CanvasType displayOrObjective*/)
            throws CoverageStoreException, TransformException, FactoryException, ProcessException {
        return getObjectiveCoverage(projectedCoverage, renderingContext.getCanvasObjectiveBounds(),
                renderingContext.getResolution(), renderingContext.getObjectiveToDisplay(), true);
    }

    /**
     * Returns expected {@linkplain GridCoverage2D elevation coverage} or {@linkplain GridCoverage2D coverage}
     * from given {@link ProjectedCoverage}.
     *
     * @param projectedCoverage Convenient representation of a {@link Coverage} for rendering.
     * @param renderingBound Rendering context enveloppe
     * @param resolution Rendering resolution in envelope crs
     * @param objToDisp Objective to displace affine transform
     * @param isElevation {@code true} if we want elevation coverage, else ({@code false}) for features coverage.
     * @return expected {@linkplain GridCoverage2D elevation coverage} or {@linkplain GridCoverage2D coverage}
     * @throws org.geotoolkit.coverage.io.CoverageStoreException if problem during coverage reading.
     * @throws org.opengis.referencing.operation.TransformException if problem during {@link Envelope} transformation.
     * @throws org.opengis.util.FactoryException if problem during {@link Envelope} study.
     * @throws org.geotoolkit.process.ProcessException if problem during resampling processing.
     * @see ProjectedCoverage#getElevationCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     * @see ProjectedCoverage#getCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     */
    protected GridCoverage2D getObjectiveCoverage(final ProjectedCoverage projectedCoverage,
            Envelope renderingBound, double[] resolution, AffineTransform2D objToDisp, final boolean isElevation)
            throws CoverageStoreException, TransformException, FactoryException, ProcessException {
        return getObjectiveCoverage(projectedCoverage, renderingBound, resolution, objToDisp, isElevation, null);
    }

    /**
     * Returns expected {@linkplain GridCoverage2D elevation coverage} or {@linkplain GridCoverage2D coverage}
     * from given {@link ProjectedCoverage}.
     *
     * @param projectedCoverage Convenient representation of a {@link Coverage} for rendering.
     * @param renderingBound Rendering context enveloppe
     * @param resolution Rendering resolution in envelope crs
     * @param objToDisp2D Objective to displace affine transform
     * @param isElevation {@code true} if we want elevation coverage, else ({@code false}) for features coverage.
     * @param sourceBands coverage source bands to features
     * @return expected {@linkplain GridCoverage2D elevation coverage} or {@linkplain GridCoverage2D coverage}
     * @throws org.geotoolkit.coverage.io.CoverageStoreException if problem during coverage reading.
     * @throws org.opengis.referencing.operation.TransformException if problem during {@link Envelope} transformation.
     * @throws org.opengis.util.FactoryException if problem during {@link Envelope} study.
     * @throws org.geotoolkit.process.ProcessException if problem during resampling processing.
     * @see ProjectedCoverage#getElevationCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     * @see ProjectedCoverage#getCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam)
     */
    protected GridCoverage2D getObjectiveCoverage(final ProjectedCoverage projectedCoverage,
            Envelope renderingBound, double[] resolution, AffineTransform2D objToDisp2D, final boolean isElevation, int[] sourceBands)
            throws CoverageStoreException, TransformException, FactoryException, ProcessException {
        ArgumentChecks.ensureNonNull("projectedCoverage", projectedCoverage);

        ////////////////////////////////////////////////////////////////////////
        // 1 - Get informations data                                          //
        ////////////////////////////////////////////////////////////////////////

        //-- resolution of horizontal Part of CRS
        assert resolution.length == 2 : "DefaultRasterSymboliser : resolution from renderingContext should only defined in 2D.";

        resolution = checkResolution(resolution, renderingBound);

        final CoverageMapLayer coverageLayer               = projectedCoverage.getLayer();
        final CoverageResource ref                         = coverageLayer.getCoverageReference();
        final GridCoverageReader reader                    = ref.acquireReader();
        final GeneralGridGeometry gridGeometry             = reader.getGridGeometry(ref.getImageIndex());
        final Envelope inputCoverageEnvelope               = gridGeometry.getEnvelope();
        final CoordinateReferenceSystem inputCoverageCRS   = inputCoverageEnvelope.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem inputCoverageCRS2D = CRSUtilities.getCRS2D(inputCoverageCRS);
        ref.recycle(reader);


        ////////////////////////////////////////////////////////////////////////
        // 2 - Study Envelope rendering context to define                     //
        //     CoverageReader Param Envelope and resolutions                  //
        ////////////////////////////////////////////////////////////////////////

        final Map<String, Double> queryValues = extractQuery(projectedCoverage.getLayer());
        if (queryValues != null && !queryValues.isEmpty()) {
            renderingBound = fixEnvelopeWithQuery(queryValues, renderingBound, inputCoverageCRS);
        }

        /*
        * Study rendering context envelope and internal coverage envelope.
        * We try to define if the two geographic part from the two respectively
        * coverage and rendering envelope intersect.
        */
        final CoordinateReferenceSystem renderingContextObjectiveCRS2D = CRSUtilities.getCRS2D(renderingBound.getCoordinateReferenceSystem());
        final GeneralEnvelope renderingBound2D                         = GeneralEnvelope.castOrCopy(Envelopes.transform(renderingBound, renderingContextObjectiveCRS2D));
        GeneralEnvelope coverageIntoRender2DCRS                        = GeneralEnvelope.castOrCopy(Envelopes.transform(inputCoverageEnvelope, renderingContextObjectiveCRS2D));

        if (!containNAN(renderingBound2D) && !containNAN(coverageIntoRender2DCRS)
              && !coverageIntoRender2DCRS.intersects(renderingBound2D, true)) {
            //-- in future jdk8 version return an Optional<Coverage>
            final StringBuilder strB = new StringBuilder(isElevation ? "getObjectiveElevationCoverage()" : "getObjectiveCoverage()");
            strB.append(" : the 2D geographic part of rendering context does not intersect the 2D geographic part of coverage : ");
            strB.append("\n rendering context 2D CRS :  ");
            strB.append(renderingContextObjectiveCRS2D);
            strB.append("\n rendering context boundary : ");
            strB.append(renderingBound2D);
            strB.append("\n 2D coverage geographic part into rendering context CRS : ");
            strB.append(coverageIntoRender2DCRS);
            LOGGER.log(Level.FINE, strB.toString());
            return null;
        }
        //-- else
        //-- Note : in the case of NAN values we try later to clip requested envelope with coverage boundary.

        if (containNAN(coverageIntoRender2DCRS)) {
            /*
             * Envelope might contains NaN when for some reason the envelope is larger then the validity area.
             */
            final GeneralEnvelope normalizedEnvelope = new GeneralEnvelope(inputCoverageEnvelope);
            normalizedEnvelope.normalize();
            coverageIntoRender2DCRS = GeneralEnvelope.castOrCopy(Envelopes.transform(normalizedEnvelope,renderingContextObjectiveCRS2D));
        }

        //-- before try to features coverage in relation with rendering view boundary
        assert !renderingBound2D.isEmpty() : "2D rendering boundary should not be empty.";

        final GeneralEnvelope intersectionIntoRender2D = GeneralEnvelope.castOrCopy(coverageIntoRender2DCRS);
        intersectionIntoRender2D.intersect(renderingBound2D);
        /*
         * Study rendering context envelope and internal coverage envelope.
         * For example if we store data with a third dimension or more, with the 2 dimensional renderer
         * it is possible to miss some internal stored data.
         * To avoid this comportment we can "complete"(fill) render envelope with missing dimensions.
         */
        GeneralEnvelope paramEnvelope;
        try {
            paramEnvelope = org.geotoolkit.referencing.ReferencingUtilities.intersectEnvelopes(inputCoverageEnvelope, renderingBound);
        } catch(ProjectionException ex) {
           /*
            * Recently a new kind of exception is thrown when envelope is out of validity domain of target CRS.
            * To avoid no rendering, try to intersect 2D part and re-project into data CRS and add after others dimensions.
            */
            //-- 1: get intersection between dataBBox and renderBBox into render space
            //-- 2: projection into dataBBox space
            paramEnvelope = GeneralEnvelope.castOrCopy(Envelopes.transform(intersectionIntoRender2D, inputCoverageCRS2D));
            //-- 3: add others dataBBox dimensions
            paramEnvelope = org.geotoolkit.referencing.ReferencingUtilities.intersectEnvelopes(inputCoverageEnvelope, paramEnvelope);
        }

        assert paramEnvelope.getCoordinateReferenceSystem() != null : "DefaultRasterSymbolizerRenderer : CRS from param envelope cannot be null.";

        //-- Check if projected coverage has NAN values on other dimension than geographic 2D part
        if (containNAN(paramEnvelope) && !containNANInto2DGeographicPart(paramEnvelope)) {
            throw new DisjointCoverageDomainException("Rendering envelope extra dimensions does not intersect data envelope : " +
                    "has some NAN values on other dimension than geographic part."+paramEnvelope);
        }

        //-- We know we don't have NAN values on other dimension than geographic
        //-- We clip envelope with coverage boundary
        clipAndReplaceNANEnvelope(paramEnvelope, inputCoverageEnvelope, paramEnvelope);

        assert !containNAN(paramEnvelope) : "paramEnvelope can't contain NAN values";
        //-- paramEnveloppe is into CoverageCRS
        assert Utilities.equalsIgnoreMetadata(paramEnvelope.getCoordinateReferenceSystem(), inputCoverageCRS);

        GeneralEnvelope paramEnvelope2D = GeneralEnvelope.castOrCopy(Envelopes.transform(paramEnvelope, inputCoverageCRS2D));
        assert !paramEnvelope2D.isEmpty() : "2D coverage boundary should not be empty.";

        //--- Param Resolution
        //-- convert resolution adapted to coverage CRS (resolution from rendering context --> coverage resolution)
        final double[] paramRes = ReferencingUtilities.convertResolution(
                org.geotoolkit.referencing.ReferencingUtilities.intersectEnvelopes(renderingBound, intersectionIntoRender2D),
                resolution, inputCoverageCRS);


        /////////////////////////////////////////////////////////////////////////
        // 3 - CoverageReader Param founded, study CRS and Coordinate operation//
        //     to know if necessary to do reprojection                         //
        //     (CoverageCRS --> rendering Context Objective)                   //
        /////////////////////////////////////////////////////////////////////////

        /* It appears EqualsIgnoreMetadata can return false sometimes, even if the two CRS are equivalent.
         * But mathematics don't lie, so if they really describe the same transformation, conversion from
         * one to another will give us an identity matrix.
         */
        MathTransform coverageToObjective2D = CRS.findOperation(inputCoverageCRS2D, renderingContextObjectiveCRS2D, null).getMathTransform();

        //-- In this case reprojection is not required features and return Coverage directly

        GridCoverage2D dataCoverage;
        if (Utilities.equalsApproximatively(inputCoverageCRS2D, renderingContextObjectiveCRS2D)
                                            || coverageToObjective2D.isIdentity()) {
            dataCoverage = readCoverage(projectedCoverage, isElevation,
                                paramEnvelope, paramRes, sourceBands,
                                inputCoverageEnvelope);
        } else {

            ////////////////////////////////////////////////////////////////////////
            // 4 - Reprojection is required, study needed interpolation           //
            //     and expand ParamEnvelope if necessary                          //
            ////////////////////////////////////////////////////////////////////////

            /*
            * In case where coverage2D envelope into rendering CRS is not empty,
            * try to reproject a coverage which have already been clipped with the objective rendering context boundary.
            */
            GeneralEnvelope outputRenderingCoverageEnv2D = GeneralEnvelope.castOrCopy(Envelopes.transform(coverageToObjective2D, paramEnvelope2D));
            outputRenderingCoverageEnv2D.setCoordinateReferenceSystem(renderingContextObjectiveCRS2D);
            if (!outputRenderingCoverageEnv2D.isEmpty()) {
                outputRenderingCoverageEnv2D.intersect(renderingBound2D);
            } else {
                outputRenderingCoverageEnv2D = renderingBound2D;
            }

            //----------------------------- DISPLAY -------------------------------//
            //-- compute output grid Envelope into rendering context display
            //-- get destination image size
            final GeneralEnvelope dispEnv = Envelopes.transform(objToDisp2D, outputRenderingCoverageEnv2D);

            final int width               = (int) dispEnv.getSpan(0);
            final int height              = (int) dispEnv.getSpan(1);

            if (width <= 0 || height <= 0) {
                LOGGER.log(Level.FINE, "Coverage is out of rendering window.");
                return null;
            }

            //-- find most appropriate interpolation
            List<GridSampleDimension> sampleDimensions= null;
            try {
                sampleDimensions = reader.getSampleDimensions(ref.getImageIndex());
            } catch(Exception ex) {
                //-- do nothing
                //-- bilinear interpolation default choosen comportement if null sampleDimension.
            }

            InterpolationCase interpolation;
            if(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(hints.get(RenderingHints.KEY_INTERPOLATION))
            || (!(gridGeometry instanceof GridGeometry2D))
            || width  < 2
            || height < 2) {
                //hints forced nearest neighbor interpolation
                interpolation = InterpolationCase.NEIGHBOR;
            } else {
                interpolation = findInterpolationCase(sampleDimensions);
            }

           /*
            * Expand envelope by 1 or more pixel in function of choosen interpolation
            * and also multiply this value by the subsampling between origin coverage extent
            * and output rendering image size into pixel coordinates.
            */
            int coeffExpand = 1;
            if (!interpolation.equals(InterpolationCase.NEIGHBOR)) {
                final int horizontalAxis = CRSUtilities.firstHorizontalAxis(inputCoverageCRS);
                final double[] gridRes = gridGeometry.getResolution();
                int coeffx  = (int) Math.ceil(paramEnvelope2D.getSpan(0) / (gridRes[horizontalAxis]     * width));
                int coeffy  = (int) Math.ceil(paramEnvelope2D.getSpan(1) / (gridRes[horizontalAxis + 1] * height));
                coeffExpand = Math.max(coeffExpand, Math.max(coeffx, coeffy));
            }

            //-- expand param envelope if we use an interpolation
            switch(interpolation){
                case BILINEAR : expand(paramEnvelope, 1 * coeffExpand, gridGeometry); break;
                case BICUBIC  :
                case BICUBIC2 : expand(paramEnvelope, 2 * coeffExpand, gridGeometry); break;
                case LANCZOS  : expand(paramEnvelope, 4 * coeffExpand, gridGeometry); break;
            }

            ////////////////////////////////////////////////////////////////////////
            // 5 - Read Coverage from computed Params.                            //
            ////////////////////////////////////////////////////////////////////////
            dataCoverage = readCoverage(projectedCoverage, isElevation,
                                                       paramEnvelope, paramRes, sourceBands,
                                                       inputCoverageEnvelope);
        }

        /*
         * Check if we still need a reprojection.
         * Resources may declare a CRS but return a coverage in a different CRS.
         */
        if (dataCoverage == null) {
            return null;
        } else if (Utilities.equalsApproximatively(dataCoverage.getCoordinateReferenceSystem2D(), renderingContextObjectiveCRS2D)) {
            return dataCoverage;
        }

        return toObjective(dataCoverage, renderingBound2D, paramEnvelope);
    }

    private GridCoverage2D toObjective(GridCoverage2D dataCoverage, GeneralEnvelope renderingBound2D, GeneralEnvelope paramEnvelope)
            throws ProcessException, FactoryException, TransformException, CoverageStoreException {

        ////////////////////////////////////////////////////////////////////////
        // 6 - Reproject data                                                 //
        ////////////////////////////////////////////////////////////////////////

        /* If Coverage is only of PHOGRAPHIC type, transform it into Coverage
         * with transparency to avoid BLACK BORDER during resampling.
         * Example : photographic RGB raster become ARGB.
         * A Photographic Coverage is a Coverage without any SampleDimension.
         */
        dataCoverage = prepareCoverageToResampling(dataCoverage, symbol);

        final CoordinateReferenceSystem inputCoverageCRS2D = dataCoverage.getCoordinateReferenceSystem2D();
        final GridGeometry2D gridGeometry = dataCoverage.getGridGeometry();

        final GeneralEnvelope paramEnvelope2D = GeneralEnvelope.castOrCopy(Envelopes.transform(paramEnvelope, inputCoverageCRS2D));

        /*
         * NODATA
         *
         * 1 : Normally all NODATA for all gridSampleDimension for a same coverage are equals.
         * 2 : Normally all NODATA for each coverage internally samples are equals.
         */
        List<GridSampleDimension> sampleDimensions = null;
        try {
            sampleDimensions = Arrays.asList(dataCoverage.getSampleDimensions());
        } catch(Exception ex) {
            //-- do nothing
            //-- bilinear interpolation default choosen comportement if null sampleDimension.
        }
        double[] nodata = null;
        if (sampleDimensions != null && !sampleDimensions.isEmpty()) {
            nodata = sampleDimensions.get(0).getNoDataValues();
        }

        /*
         * If nodata is not know.
         * 1 : find a nodata value out of internal gridSampleDimension categories.
         * 2 : if category already contain all sample Datatype possible values,
         * transform image into a sample type with more bitspersample to define
         * an appropriate NODATA values out of categories borders.
         */
        if (nodata == null) {
            //-- TODO
        }

        /*
        * In case where coverage2D envelope into rendering CRS is not empty,
        * try to reproject a coverage which have already been clipped with the objective rendering context boundary.
        */
        final CoordinateReferenceSystem renderingContextObjectiveCRS2D = renderingContext.getObjectiveCRS2D();
        final MathTransform coverageToObjective2D = CRS.findOperation(inputCoverageCRS2D, renderingContextObjectiveCRS2D, null).getMathTransform();
        GeneralEnvelope outputRenderingCoverageEnv2D = GeneralEnvelope.castOrCopy(Envelopes.transform(coverageToObjective2D, paramEnvelope2D));
        outputRenderingCoverageEnv2D.setCoordinateReferenceSystem(renderingContextObjectiveCRS2D);
        if (!outputRenderingCoverageEnv2D.isEmpty()) {
            outputRenderingCoverageEnv2D.intersect(renderingBound2D);
        } else {
            outputRenderingCoverageEnv2D = renderingBound2D;
        }
        //-- compute output grid Envelope into rendering context display
        //-- get destination image size
        final GeneralEnvelope dispEnv = Envelopes.transform(renderingContext.getObjectiveToDisplay(), outputRenderingCoverageEnv2D);

        final int width               = (int) dispEnv.getSpan(0);
        final int height              = (int) dispEnv.getSpan(1);


        //-- find most appropriate interpolation
        InterpolationCase interpolation;
        if(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(hints.get(RenderingHints.KEY_INTERPOLATION))
            || (!(gridGeometry instanceof GridGeometry2D))
            || width  < 2
            || height < 2) {
            //hints forced nearest neighbor interpolation
            interpolation = InterpolationCase.NEIGHBOR;
        } else {
            interpolation = findInterpolationCase(sampleDimensions);
        }

        final GridGeometry2D gg = new GridGeometry2D(new GridEnvelope2D(0, 0, width, height), outputRenderingCoverageEnv2D);

        final ProcessDescriptor desc = ResampleDescriptor.INSTANCE;
        final Parameters params = Parameters.castOrWrap(desc.getInputDescriptor().createValue());
        params.getOrCreate(ResampleDescriptor.IN_COVERAGE).setValue(dataCoverage);
        params.getOrCreate(ResampleDescriptor.IN_BACKGROUND).setValue(nodata);
        params.getOrCreate(ResampleDescriptor.IN_COORDINATE_REFERENCE_SYSTEM).setValue(renderingContextObjectiveCRS2D);
        params.getOrCreate(ResampleDescriptor.IN_GRID_GEOMETRY).setValue(gg);
        params.getOrCreate(ResampleDescriptor.IN_INTERPOLATION_TYPE).setValue(interpolation);
        params.getOrCreate(ResampleDescriptor.IN_BORDER_COMPORTEMENT_TYPE).setValue(ResampleBorderComportement.FILL_VALUE);

        final org.geotoolkit.process.Process process = desc.createProcess(params);
        final ParameterValueGroup result = process.call();
        dataCoverage = (GridCoverage2D) result.parameter("result").getValue();

        return dataCoverage;
    }

    /**
     * {@inheritDoc }
     *
     * Prepare coverage for Raster rendering.
     */
    protected GridCoverage2D prepareCoverageToResampling(final GridCoverage2D coverageSource, final C symbolizer) {
        return coverageSource;
    }

    /**
     * Returns features Coverage from {@link ProjectedCoverage} and given initialized parameters.
     *
     * @param projectedCoverage Coverage where {@link GridCoverage2D} is features.
     * @param isElevation define if internaly method {@link ProjectedCoverage#getElevationCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam) }
     * or {@link ProjectedCoverage#getCoverage(org.geotoolkit.coverage.io.GridCoverageReadParam) } will be call.
     * @param paramEnvelope Requested envelope to features GridCoverage.
     * @param paramResolution Requested features resolution.
     * @param sourceBands Requested Read source bands. May be {@code null}.
     * @param inputCoverageEnvelope Envelope only use if problem during reading for log message.
     * @return features Coverage.
     * @throws CoverageStoreException if problem during reading action.
     */
    private static GridCoverage2D readCoverage(final ProjectedCoverage projectedCoverage, final boolean isElevation,
                                               final Envelope paramEnvelope, final double[] paramResolution, final int[] sourceBands,
                                               final Envelope inputCoverageEnvelope)
            throws CoverageStoreException {

        final GridCoverageReadParam param = new GridCoverageReadParam();
        param.setEnvelope(paramEnvelope);
        param.setResolution(paramResolution);
        if (sourceBands!=null) param.setSourceBands(sourceBands);

        GridCoverage2D dataCoverage = (isElevation) ? projectedCoverage.getElevationCoverage(param) : projectedCoverage.getCoverage(param);

        if (dataCoverage == null) {
            //-- in future jdk8 version return an Optional<Coverage>
            final StringBuilder strB = new StringBuilder(isElevation ? "getObjectiveElevationCoverage()" : "getObjectiveCoverage()");
            strB.append(" : \n impossible to read");
            strB.append((isElevation) ? " Elevation ":" Image ");
            strB.append("Coverage with internally projected coverage boundary : ");
            strB.append(inputCoverageEnvelope);
            strB.append("\nwith the following renderer requested Envelope.");
            strB.append(paramEnvelope);
            LOGGER.log(Level.FINE, strB.toString());
        }

        return dataCoverage;
    }

    /**
     * Clip requested envelope with internally {@link ProjectedCoverage} boundary.
     *
     * <strong>
     * In some case when the rendering boundary is reprojected into coverage space
     * some {@linkplain Double#NaN NAN} values can be computed, which is an expected comportment.
     * To avoid normally exception during coverage reading this method replace NAN values by coverage boundary values.
     * </strong>
     *
     * @param requestedEnvelope envelope which will be clipped.
     * @param coverageEnvelope reference coverage envelope.
     * @param result set result of clipping into this {@link GeneralEnvelope},
     * a new result envelope is built if it is {@code null}, you should pass the same Envelope as requestedEnvelope.
     * Moreover the result envelope is defined into same CRS than requestedEnvelope.
     * @return requested clipped envelope result.
     * @throws NullArgumentException if requestedEnvelope or coverageEnvelope are {@code null}.
     * @throws IllegalArgumentException if CRS from requestedEnvelope and coverageEnvelope are different.
     */
    private GeneralEnvelope clipAndReplaceNANEnvelope(final Envelope requestedEnvelope, final Envelope coverageEnvelope, GeneralEnvelope result) {
        ArgumentChecks.ensureNonNull("requestedEnvelope", requestedEnvelope);
        ArgumentChecks.ensureNonNull("coverageEnvelope",  coverageEnvelope);

        final CoordinateReferenceSystem requestCRS = requestedEnvelope.getCoordinateReferenceSystem();
        if (!Utilities.equalsIgnoreMetadata(requestCRS, coverageEnvelope.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("requestedEnvelope and coverage envelope will be able to have same CRS : "
                    + "\n Expected CRS : "+requestCRS
                    + "\n Found : "+coverageEnvelope.getCoordinateReferenceSystem());

        if (result == null) result = new GeneralEnvelope(requestCRS);

        for (int d = 0, dim = requestedEnvelope.getDimension(); d < dim; d++) {

            final double reqMin = requestedEnvelope.getMinimum(d);
            final double reqMax = requestedEnvelope.getMaximum(d);

            final double min = (Double.isNaN(reqMin) || Double.isInfinite(reqMin)
                    ? coverageEnvelope.getMinimum(d)
                    : StrictMath.max(reqMin, coverageEnvelope.getMinimum(d)));

            final double max = (Double.isNaN(reqMax) || Double.isInfinite(reqMax)
                    ? coverageEnvelope.getMaximum(d)
                    : StrictMath.min(reqMax, coverageEnvelope.getMaximum(d)));

            result.setRange(d, min, max);
        }
        return result;
    }

    /**
     * Detect the most appropriate interpolation type based on coverage sample dimensions.
     * Interpolation is possible only when data do not contain qualitative informations.
     */
    private static InterpolationCase findInterpolationCase(List<GridSampleDimension> sampleDimensions) throws CoverageStoreException{

        if (sampleDimensions != null) {
            for (GridSampleDimension sd : sampleDimensions) {
                final List<Category> categories = sd.getCategories();
                if (categories != null) {
                    for (Category cat : categories) {
                        if (!cat.isQuantitative() && !cat.getName().toString(Locale.ENGLISH).equals("No data")) {
                            return InterpolationCase.NEIGHBOR;
                        }
                    }
                }
            }
        }

        //no information on the data or datas are not qualitative, assume it can be interpolated
        //TODO : search geotk history for code made by Desruisseaux in old Resample operator,
        //       it contained such verifications.
        return InterpolationCase.BILINEAR;
    }

    /**
     * Expand the given envelope by the given number of points.
     *
     * @param env envelope to expand
     * @param nbPoint in grid crs to expand the envelope
     * @param gridGeom coverage grid geometry
     */
    private static void expand(final GeneralEnvelope env, final int nbPoint, final GeneralGridGeometry gridGeom)
            throws TransformException {
        if (!(gridGeom instanceof GridGeometry2D))
            throw new IllegalArgumentException("Impossible to compute expand for interpolation with no 2D GridGeometry.");
        final GridGeometry2D grigrid = (GridGeometry2D) gridGeom;

        //-- we are only interested in the 2D part.
        final MathTransform gridToCrs2D = grigrid.getGridToCRS2D(PixelOrientation.UPPER_LEFT);
        final MathTransform crsToGrid2D = gridToCrs2D.inverse();
        final Envelope env2D = Envelopes.transform(env, grigrid.getCoordinateReferenceSystem2D());

        final GeneralEnvelope gridEnv2D = Envelopes.transform(crsToGrid2D,env2D);
        gridEnv2D.setRange(0, Math.floor(gridEnv2D.getMinimum(0)-nbPoint), Math.ceil(gridEnv2D.getMaximum(0)+nbPoint));
        gridEnv2D.setRange(1, Math.floor(gridEnv2D.getMinimum(1)-nbPoint), Math.ceil(gridEnv2D.getMaximum(1)+nbPoint));

        final Envelope geoEnv2D = Envelopes.transform(gridToCrs2D,gridEnv2D);

        final int horizontalAxis = CRSUtilities.firstHorizontalAxis(env.getCoordinateReferenceSystem());
        env.setRange(horizontalAxis,     geoEnv2D.getMinimum(0), geoEnv2D.getMaximum(0));
        env.setRange(horizontalAxis + 1, geoEnv2D.getMinimum(1), geoEnv2D.getMaximum(1));
    }


    /**
     * Returns {@code true} if {@link Envelope} contain at least one
     * {@link Double#NaN} value, else {@code false}.
     *
     * @param envelope the envelope which will be verify.
     * @return {@code true} if {@link Envelope} contain at least one {@link Double#NaN} value, else {@code false}.
     * @see #containNAN(org.opengis.geometry.Envelope, int, int)
     */
    private static boolean containNAN(final Envelope envelope) {
        return containNAN(envelope, 0, envelope.getDimension() - 1);
    }

    /**
     * Returns {@code true} if {@link Envelope} contain at least one
     * {@link Double#NaN} value into its horizontal geographic part, else {@code false}.
     *
     * @param envelope the envelope which will be verify.
     * @return {@code true} if {@link Envelope} contain at least one {@link Double#NaN} value, else {@code false}.
     * @see CRSUtilities#firstHorizontalAxis(org.opengis.referencing.crs.CoordinateReferenceSystem)
     * @see #containNAN(org.opengis.geometry.Envelope, int, int)
     */
    private static boolean containNANInto2DGeographicPart(final Envelope envelope) {
        ArgumentChecks.ensureNonNull("Envelopes.containNANInto2DGeographicPart()", envelope);
        final int minOrdiGeo = CRSUtilities.firstHorizontalAxis(envelope.getCoordinateReferenceSystem());
        return containNAN(envelope, minOrdiGeo, minOrdiGeo + 1);
    }

    /**
     * Returns {@code true} if {@link Envelope} contain at least one
     * {@link Double#NaN} value on each inclusive dimension stipulate by
     * firstIndex and lastIndex, else {@code false}.
     *
     * @param envelope the envelope which will be verify.
     * @param firstIndex first inclusive dimension index.
     * @param lastIndex last <strong>INCLUSIVE</strong> dimension.
     * @return {@code true} if {@link Envelope} contain at least one {@link Double#NaN} value, else {@code false}.
     */
    private static boolean containNAN(final Envelope envelope, final int firstIndex, final int lastIndex) {
        ArgumentChecks.ensureNonNull("Envelopes.containNAN()", envelope);
        ArgumentChecks.ensurePositive("firstIndex", firstIndex);
        ArgumentChecks.ensurePositive("lastIndex", lastIndex);
        if (lastIndex >= envelope.getDimension())
            throw new IllegalArgumentException("LastIndex must be strictly lower than "
                    + "envelope dimension number. Expected maximum valid index = "+(envelope.getDimension() - 1)+". Found : "+lastIndex);
        ArgumentChecks.ensureValidIndex(lastIndex + 1, firstIndex);
        for (int d = firstIndex; d <= lastIndex; d++) {
            if (Double.isNaN(envelope.getMinimum(d))
             || Double.isNaN(envelope.getMaximum(d))) return true;
        }
        return false;
    }

}
