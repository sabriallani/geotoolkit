/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2013, Geomatys
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
package org.geotoolkit.s52.procedure;

import java.util.List;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedObject;
import org.geotoolkit.s52.S52Context;
import org.geotoolkit.s52.S52Palette;
import org.geotoolkit.s52.symbolizer.S52Graphic;

/**
 * S-52 Annex A Part I p.141 (12.2.2)
 *
 * @author Johann Sorel (Geomatys)
 */
public class DATCVR02 extends Procedure{

    public DATCVR02() {
        super("DATCVR02");
    }

    @Override
    public void render(RenderingContext2D ctx, S52Context context, S52Palette colorTable,
            List<S52Graphic> all, S52Graphic graphic) throws PortrayalException {
        System.out.println("Procedure "+getName()+" not implemented yet");
        //TODO this is a general presentation procedure, not called by any function.
    }

}
