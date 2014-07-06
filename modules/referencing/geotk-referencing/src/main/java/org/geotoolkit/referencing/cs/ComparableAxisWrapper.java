/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
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
package org.geotoolkit.referencing.cs;

import java.util.Arrays;
import net.jcip.annotations.Immutable;

import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * Wraps a {@linkplain CoordinateSystemAxis coordinate system axis} for comparison purpose.
 * The sorting order tries to favor a right-handed system. Compass directions like
 * {@linkplain AxisDirection#NORTH North} or {@linkplain AxisDirection#EAST East} are first,
 * and vertical or temporal directions like {@linkplain AxisDirection#UP Up} are last.
 *
 * @author Martin Desruisseaux (IRD)
 * @version 3.00
 *
 * @since 2.4
 * @module
 */
@Immutable
final class ComparableAxisWrapper implements Comparable<ComparableAxisWrapper> {
    /**
     * The wrapped axis.
     */
    private final CoordinateSystemAxis axis;

    /**
     * The direction along meridian, or {@code null} if none.
     */
    private final DirectionAlongMeridian meridian;

    /**
     * Creates a new wrapper for the given axis.
     */
    private ComparableAxisWrapper(final CoordinateSystemAxis axis) {
        this.axis = axis;
        meridian = DirectionAlongMeridian.parse(axis.getDirection());
    }

    /**
     * Returns {@code true} if the specified direction is a compass direction.
     */
    private static boolean isCompassDirection(final AxisDirection direction) {
        int compass = Axes.getCompassAngle(direction, AxisDirection.NORTH);
        return compass != Integer.MIN_VALUE;
    }

    /**
     * Compares with the specified object. See <a href="#skip-navbar_top">class javadoc</a>
     * for a description of the sorting order.
     */
    @Override
    public int compareTo(final ComparableAxisWrapper that) {
        final AxisDirection d1 = this.axis.getDirection();
        final AxisDirection d2 = that.axis.getDirection();
        final int compass = Axes.getCompassAngle(d2, d1);
        if (compass != Integer.MIN_VALUE) {
            return compass;
        }
        if (isCompassDirection(d1)) {
            assert !isCompassDirection(d2) : d2;
            return -1;
        }
        if (isCompassDirection(d2)) {
            assert !isCompassDirection(d1) : d1;
            return +1;
        }
        if (meridian != null) {
            if (that.meridian != null) {
                return meridian.compareTo(that.meridian);
            }
            return -1;
        } else if (that.meridian != null) {
            return +1;
        }
        return 0;
    }

    /**
     * Sorts the specified axis in an attempt to create a right-handed system.
     * The sorting is performed in place. This method returns {@code true} if
     * at least one axis moved.
     */
    public static boolean sort(final CoordinateSystemAxis[] axis) {
        final ComparableAxisWrapper[] wrappers = new ComparableAxisWrapper[axis.length];
        for (int i=0; i<axis.length; i++) {
            wrappers[i] = new ComparableAxisWrapper(axis[i]);
        }
        Arrays.sort(wrappers);
        boolean changed = false;
        for (int i=0; i<axis.length; i++) {
            final CoordinateSystemAxis a = wrappers[i].axis;
            changed |= (axis[i] != a);
            axis[i] = a;
        }
        return changed;
    }
}
