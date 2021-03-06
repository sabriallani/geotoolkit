/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012-2017, Geomatys
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
package org.geotoolkit.storage.coverage;

import java.awt.Image;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageReader;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.GridCoverageWriter;
import org.geotoolkit.coverage.io.ImageCoverageReader;
import org.geotoolkit.coverage.memory.MemoryCoverageReader;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

/**
 * CoverageReference implementation wrapping a coverage.
 *
 * @author Johann Sorel
 */
public class DefaultCoverageResource extends AbstractCoverageResource{

    private final GridCoverage2D coverage;
    private final Object input;
    private final int imageIndex;

    public DefaultCoverageResource(final CoverageStore store, final GridCoverage2D coverage, GenericName name) {
        super(store,name);
        this.coverage = coverage;
        this.input = null;
        this.imageIndex = 0;
    }

    public DefaultCoverageResource(final GridCoverage2D coverage, GenericName name) {
        super(null,name);
        this.coverage = coverage;
        this.input = null;
        this.imageIndex = 0;
    }

    public DefaultCoverageResource(final Object input, GenericName name) {
        super(null,name);
        this.coverage = null;
        this.input = input;
        this.imageIndex = 0;
    }

    @Override
    public Metadata getMetadata() throws CoverageStoreException {
        Metadata metadata = acquireReader().getMetadata();
        return metadata;
    }

    @Override
    public int getImageIndex() {
        return imageIndex;
    }

    @Override
    public boolean isWritable() throws DataStoreException {
        return false;
    }

    @Override
    public GridCoverageReader acquireReader() throws CoverageStoreException {
        if (coverage != null) {
            return new MemoryCoverageReader(coverage);
        } else if (input instanceof GridCoverage2D) {
            return new MemoryCoverageReader((GridCoverage2D) input);
        } else if (input instanceof GridCoverageReader) {
            return (GridCoverageReader) input;
        } else {
            ImageCoverageReader reader = new ImageCoverageReader();
            reader.setInput(input);
            return reader;
        }
    }

    @Override
    public GridCoverageWriter acquireWriter() throws CoverageStoreException {
        throw new CoverageStoreException("Writing not supported.");
    }

    @Override
    public void recycle(CoverageReader reader) {
        if (input instanceof GridCoverageReader) {
            //do not dispose it, it will be reused
        } else {
            super.recycle(reader);
        }
    }

    @Override
    public Image getLegend() throws DataStoreException {
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (input instanceof GridCoverageReader) {
            dispose((GridCoverageReader)input);
        }
        super.finalize();
    }

}
