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
package org.geotools.display2d.ext.scalebar;

import java.awt.Font;
import java.awt.Paint;
import java.text.NumberFormat;
import javax.measure.unit.Unit;

/**
 * Template holding informations about the design of the scalebar to paint.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface ScaleBarTemplate {

    /**
     * @return thickness of the scalebar.
     */
    float getThickness();

    /**
     * @return height of the ticks
     */
    float getTickHeight();

    /**
     * Display the scale information. something like 1:25000 above the
     * scale bar.
     *
     * @return true if scale is visible
     */
    boolean isScaleVisible();

    /**
     * @return true if the calcul should use the geodesic ellipsoid
     */
    boolean calculateUsingGeodesic();

    /**
     * Display the complete name of the unit or only the abreviation.
     * ex : meters or m
     *
     * @return true to display abreviation
     */
    boolean useUnitAbreviation();

    /**
     * Display the tick values above or under the scale bar.
     *
     * @return true to display the tick values aboce the scale bar
     */
    boolean isTextAboveBar();

    /**
     * @return number of division in the scale bar
     */
    int getDivisionCount();

    /**
     * @return numberformat used for this scale bar.
     */
    NumberFormat getNumberFormat();

    /**
     * @return Paint used for the text and border of the scale bar
     */
    Paint getForeground();

    /**
     * @return Paint used for the first scale bar rectangle
     */
    Paint getFirstRectanglePaint();

    /**
     * @return Paint used for the second scale bar rectangle
     */
    Paint getSecondRectanglePaint();

    /**
     * @return font used for the text
     */
    Font getFont();

    /**
     * Returns the unit displayed.
     */
    Unit getUnit();

}
