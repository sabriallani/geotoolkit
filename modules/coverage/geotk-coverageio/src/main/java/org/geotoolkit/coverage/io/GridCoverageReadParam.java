/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2010, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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
package org.geotoolkit.coverage.io;

import java.util.Arrays;


/**
 * Describes how a stream is to be decoded. Instances of this class are used to supply
 * information to instances of {@link GridCoverageReader}.
 *
 * {@note This class is conceptually equivalent to the <code>ImageReadParam</code> class provided
 * in the standard Java library. The main difference is that <code>GridCoverageReadParam</code>
 * works with geodetic coordinates while <code>ImageReadParam</code> works with pixel coordinates.}
 *
 * @author Johann Sorel (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.15
 *
 * @see javax.imageio.ImageReadParam
 *
 * @since 3.09
 * @module
 */
public class GridCoverageReadParam extends GridCoverageStoreParam {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -7515981676576102704L;

    /**
     * The set of destination bands where data will be placed. By default, the value is
     * {@code null}, indicating that all destination bands should be written in order.
     */
    private int[] destinationBands;

    /**
     * Creates a new {@code GridCoverageReadParam} instance. All properties are
     * initialized to {@code null}. Callers must invoke setter methods in order
     * to provide information about the way to decode the stream.
     */
    public GridCoverageReadParam() {
    }

    /**
     * Creates a new {@code GridCoverageReadParam} instance initialized to the same
     * values than the given parameters.
     *
     * @param param The parameters to copy, or {@code null} if none.
     *
     * @since 3.15
     */
    public GridCoverageReadParam(final GridCoverageStoreParam param) {
        super(param);
        if (param instanceof GridCoverageReadParam) {
            final GridCoverageReadParam rp = (GridCoverageReadParam) param;
            destinationBands = rp.destinationBands;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        destinationBands = null;
        super.clear();
    }

    /**
     * Returns the set of destination bands where data will be placed. By default, the value
     * is {@code null}, indicating that all destination bands should be written in order.
     *
     * @return The set of destination bands where data will be placed, or {@code null}.
     *
     * @see javax.imageio.ImageReadParam#getDestinationBands()
     *
     * @since 3.10
     */
    public int[] getDestinationBands() {
        final int[] bands = destinationBands;
        return (bands != null) ? bands.clone() : null;
    }

    /**
     * Sets the indices of the destination bands where data will be placed. A null value
     * indicates that all destination bands will be used.
     * <p>
     * At the time of reading, an {@link IllegalArgumentException} will be thrown by the reader
     * if a value larger than the largest destination band index has been specified or if the
     * number of source bands and destination bands to be used differ.
     *
     * @param  bands The destination bands, or {@code null}.
     * @throws IllegalArgumentException If the given array is empty,
     *         or if it contains duplicated or negative values.
     *
     * @see javax.imageio.ImageReadParam#setDestinationBands(int[])
     *
     * @since 3.10
     */
    public void setDestinationBands(final int... bands) throws IllegalArgumentException {
        destinationBands = checkAndClone(bands);
    }

    /**
     * Invoked by the superclass in order to complete the string built by {@link #toString()}.
     */
    @Override
    final void toString(final StringBuilder buffer, String separator) {
        if (destinationBands != null) {
            buffer.append(separator).append("destinationBands=").append(Arrays.toString(destinationBands));
        }
    }
}
