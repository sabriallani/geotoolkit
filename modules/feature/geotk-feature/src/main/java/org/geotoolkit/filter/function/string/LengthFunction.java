/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.function.string;

import org.geotoolkit.filter.function.AbstractFunction;
import org.opengis.filter.expression.Expression;


public class LengthFunction extends AbstractFunction {

    public LengthFunction(final Expression expression) {
        super(StringFunctionFactory.LENGTH, new Expression[]{expression}, null);
    }

    @Override
    public Object evaluate(final Object feature) {
        String arg0;

        if(feature instanceof CharSequence){
            return ((CharSequence)feature).length();
        }

        try { // attempt to get value and perform conversion
            arg0 = parameters.get(0).evaluate(feature, String.class);
        } catch (Exception e) { // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function strLength argument #0 - expected type String");
        }

        return arg0==null ? 0 : arg0.length();
    }
}
