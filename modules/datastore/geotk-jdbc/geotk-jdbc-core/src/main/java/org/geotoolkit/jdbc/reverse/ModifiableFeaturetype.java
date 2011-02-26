/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.jdbc.reverse;

import java.util.Collection;
import java.util.List;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * FeatureType with modifiable properties.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
class ModifiableFeaturetype extends DefaultFeatureType implements ModifiableType {

    public ModifiableFeaturetype(final Name name, final Collection<PropertyDescriptor> schema, 
            final GeometryDescriptor defaultGeometry, final boolean isAbstract,
            final List<Filter> restrictions, final AttributeType superType, final InternationalString description) {
        super(name, schema, defaultGeometry, isAbstract, restrictions, superType, description);
    }

    @Override
    public void changeProperty(final int index, PropertyDescriptor desc) {
        descriptors[index] = desc;
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return descriptorsList;
    }
}
