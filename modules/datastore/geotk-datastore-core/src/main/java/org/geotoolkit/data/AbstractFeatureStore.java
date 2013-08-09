/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
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

package org.geotoolkit.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.memory.GenericEmptyFeatureIterator;
import org.geotoolkit.data.memory.GenericFeatureWriter;
import org.geotoolkit.data.memory.GenericFilterFeatureIterator;
import org.geotoolkit.data.memory.GenericMaxFeatureIterator;
import org.geotoolkit.data.memory.GenericReprojectFeatureIterator;
import org.geotoolkit.data.memory.GenericRetypeFeatureIterator;
import org.geotoolkit.data.memory.GenericSortByFeatureIterator;
import org.geotoolkit.data.memory.GenericStartIndexFeatureIterator;
import org.geotoolkit.data.memory.GenericTransformFeatureIterator;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.Selector;
import org.geotoolkit.data.query.Source;
import org.geotoolkit.data.session.DefaultSession;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.geometry.jts.transform.GeometryScaleTransformer;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.storage.AbstractStorage;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.version.Version;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 * Uncomplete implementation of a feature store that handle most methods
 * by fallbacking on others. It also offer some generic methods to
 * handle query parameters and events.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class AbstractFeatureStore extends AbstractStorage implements FeatureStore{

    /**
     * Static variables refering to GML model.
     */
    public static final String GML_NAMESPACE = "http://www.opengis.net/gml";
    public static final String GML_NAME = "name";
    public static final String GML_DESCRIPTION = "description";

    protected static final String NO_NAMESPACE = "no namespace";

    private final Logger Logger = Logging.getLogger(getClass().getPackage().getName());

    protected final ParameterValueGroup parameters;
    protected String defaultNamespace;

    protected AbstractFeatureStore(final ParameterValueGroup params) {
        
        this.parameters = params;
        String namespace = null;
        if(params != null){
            try{
                namespace = (String)Parameters.getOrCreate(AbstractFeatureStoreFactory.NAMESPACE, params).getValue();
            }catch(ParameterNotFoundException ex){
                //ignore this error, factory might not necesarly have a namespace parameter
                //example : gpx
            }
        }
        
        if (namespace == null) {
            defaultNamespace = "http://geotoolkit.org";
        } else if (namespace.equals(NO_NAMESPACE)) {
            defaultNamespace = null;
        } else {
            defaultNamespace = namespace;
        }
    }

    @Override
    public ParameterValueGroup getConfiguration() {
        return parameters;
    }
    
    protected String getDefaultNamespace() {
        return defaultNamespace;
    }

    protected Logger getLogger(){
        return Logger;
    }

    @Override
    public VersionControl getVersioning(String typeName) throws VersioningException{
        final Name n = DefaultName.valueOf(typeName);
        return getVersioning(n);
    }

    /**
     * Overwrite to enable versioning.
     * @param version 
     */
    @Override
    public VersionControl getVersioning(Name typeName) throws VersioningException{
        throw new VersioningException("Versioning not supported");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final Session createSession(final boolean async) {
        return createSession(async, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Session createSession(final boolean async, Version version) {
        return new DefaultSession(this, async,version);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public String[] getTypeNames() throws DataStoreException {
        final Set<Name> names = getNames();
        final Iterator<Name> ite = names.iterator();
        final String[] locals = new String[names.size()];
        int i=0;
        while(ite.hasNext()){
            locals[i] = ite.next().getLocalPart();
            i++;
        }
        return locals;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType getFeatureType(final String typeName) throws DataStoreException {
        for(final Name n : getNames()){
            if(n.getLocalPart().equals(typeName)){
                return getFeatureType(n);
            }
        }
        throw new DataStoreException("Schema : " + typeName + "doesnt exist in this feature store.");
    }

    @Override
    public FeatureType getFeatureType(final Query query) throws DataStoreException, SchemaException {

        final Source source = query.getSource();

        if(Query.GEOTK_QOM.equalsIgnoreCase(query.getLanguage()) && source instanceof Selector){
            final Selector selector = (Selector) source;
            FeatureType ft = selector.getSession().getFeatureStore().getFeatureType(query.getTypeName());
            ft = FeatureTypeUtilities.createSubType(ft, query.getPropertyNames(), query.getCoordinateSystemReproject());

            final Boolean hide = (Boolean) query.getHints().get(HintsPending.FEATURE_HIDE_ID_PROPERTY);
            if(hide != null && hide){
                ft = FeatureTypeUtilities.excludePrimaryKeyFields(ft);
            }

            return ft;
        }

        throw new DataStoreException("Can not deduce feature type of query : " + query);
    }

    /**
     * {@inheritDoc }
     *
     * This implementation will try to aquire a writer and return true if it
     * succeed.
     */
    @Override
    public boolean isWritable(final Name typeName) throws DataStoreException {
        //while raise an error if type doesnt exist
        getFeatureType(typeName);

        FeatureWriter writer = null;
        try{
            writer = getFeatureWriter(typeName, Filter.EXCLUDE);
            return true;
        }catch(Exception ex){
            //catch anything, log it
            getLogger().log(Level.WARNING, "Type not writeable : {0}", ex.getMessage());
            return false;
        }finally{
            if(writer != null){
                writer.close();
            }
        }
    }

    /**
     * {@inheritDoc }
     *
     * This implementation will aquiere a reader and iterate to count.
     * Subclasses should override this method if they have a faster way to
     * calculate count.
     */
    @Override
    public long getCount(Query query) throws DataStoreException {
        query = addSeparateFeatureHint(query);
        final FeatureReader reader = getFeatureReader(query);
        return FeatureStoreUtilities.calculateCount(reader);
    }

    /**
     * {@inheritDoc }
     *
     * This implementation will aquiere a reader and iterate to expend an envelope.
     * Subclasses should override this method if they have a faster way to
     * calculate envelope.
     * @throws DataStoreException 
     * @throws FeatureStoreRuntimeException
     */
    @Override
    public Envelope getEnvelope(Query query) throws DataStoreException, FeatureStoreRuntimeException {
        // TODO query = addSeparateFeatureHint(query);
        
        if(query.retrieveAllProperties()){
            //we simplify it, get only geometry attributs + sort attribute
            final FeatureType ft = getFeatureType(query.getTypeName());
            final List<Name> names = new ArrayList<Name>();
            for(PropertyDescriptor desc : ft.getDescriptors()){
                if(desc instanceof GeometryDescriptor){
                    names.add(desc.getName());
                } else if (query.getSortBy() != null) {
                    for (SortBy sortBy : query.getSortBy()) {
                        final String propName = sortBy.getPropertyName().getPropertyName();
                        if (desc.getName().toString().equals(propName) ||
                            desc.getName().getLocalPart().equals(propName)) {
                            names.add(desc.getName());
                        }
                    }
                }
            }
            
            if(names.isEmpty()){
                //no geometry field
                return null;
            }
            
            final QueryBuilder qb = new QueryBuilder(query);
            qb.setProperties(names.toArray(new Name[names.size()]));
            query = qb.buildQuery();
        }
        
        
        final Name[] wantedProp = query.getPropertyNames();
        if(wantedProp.length==0){
            return null;
        }
        
        final FeatureReader reader = getFeatureReader(query);
        return FeatureStoreUtilities.calculateEnvelope(reader);
    }

    private static Query addSeparateFeatureHint(final Query query){
        //hints never null on a query
        Hints hints = query.getHints();
        hints.put(HintsPending.FEATURE_DETACHED, Boolean.FALSE);
        return query;
    }

    @Override
    public final List<FeatureId> addFeatures(Name groupName, Collection<? extends Feature> newFeatures) throws DataStoreException {
        return addFeatures(groupName,newFeatures,new Hints());
    }

    /**
     * {@inheritDoc }
     *
     * This implementation fallback on
     * @see  #updateFeatures(org.opengis.feature.type.Name, org.opengis.filter.Filter, java.util.Map)
     */
    @Override
    public void updateFeatures(final Name groupName, final Filter filter, final PropertyDescriptor desc, final Object value) throws DataStoreException {
        updateFeatures(groupName, filter, Collections.singletonMap(desc, value));
    }

    @Override
    public final FeatureWriter getFeatureWriter(Name typeName, Filter filter) throws DataStoreException {
        return getFeatureWriter(typeName, filter, null);
    }
    
    /**
     * {@inheritDoc }
     *
     * Generic implementation, will aquiere a featurewriter and iterate to the end of the writer.
     */
    @Override
    public final FeatureWriter getFeatureWriterAppend(final Name typeName) throws DataStoreException {
        return getFeatureWriterAppend(typeName,null);
    }
    
    /**
     * {@inheritDoc }
     *
     * Generic implementation, will aquiere a featurewriter and iterate to the end of the writer.
     */
    @Override
    public FeatureWriter getFeatureWriterAppend(final Name typeName,final Hints hints) throws DataStoreException {
        final FeatureWriter writer = getFeatureWriter(typeName,Filter.INCLUDE, hints);

        while (writer.hasNext()) {
            writer.next(); // skip to the end to switch in append mode
        }

        return writer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // listeners methods ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Fires a schema add event to all listeners.
     * 
     * @param name added schema name
     * @param type added feature type
     */
    protected void fireSchemaAdded(final Name name, final FeatureType type){
        sendStructureEvent(FeatureStoreManagementEvent.createAddEvent(this, name, type));
    }

    /**
     * Fires a schema update event to all listeners.
     *
     * @param name updated schema name
     * @param oldType featuretype before change
     * @param newType featuretype after change
     */
    protected void fireSchemaUpdated(final Name name, final FeatureType oldType, final FeatureType newType){
        sendStructureEvent(FeatureStoreManagementEvent.createUpdateEvent(this, name, oldType, newType));
    }

    /**
     * Fires a schema delete event to all listeners.
     *
     * @param name deleted schema name
     * @param type feature type of the deleted schema
     */
    protected void fireSchemaDeleted(final Name name, final FeatureType type){
        sendStructureEvent(FeatureStoreManagementEvent.createDeleteEvent(this, name, type));
    }

    /**
     * Fires a features add event.
     *
     * @param name of the schema where features where added.
     * @param ids modified feature ids
     */
    protected void fireFeaturesAdded(final Name name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createAddEvent(this, name, ids));
    }

    /**
     * Fires a features update event.
     *
     * @param name of the schema where features where updated.
     * @param ids modified feature ids
     */
    protected void fireFeaturesUpdated(final Name name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createUpdateEvent(this, name, ids));
    }

    /**
     * Fires a features delete event.
     *
     * @param name of the schema where features where deleted
     * @param ids modified feature ids
     */
    protected void fireFeaturesDeleted(final Name name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createDeleteEvent(this, name, ids));
    }

    ////////////////////////////////////////////////////////////////////////////
    // useful methods for feature store that doesn't implement all query parameters/
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convinient method to check that the given type name exist.
     * Will raise a datastore exception if the name do not exist in this FeatureStore.
     * @param candidate Name to test.
     * @throws DataStoreException if name do not exist.
     */
    protected void typeCheck(final Name candidate) throws DataStoreException{

        final Collection<Name> names = getNames();
        if(!names.contains(candidate)){
            final StringBuilder sb = new StringBuilder("Type name : ");
            sb.append(candidate);
            sb.append(" do not exist in this feature store, available names are : ");
            for(final Name n : names){
                sb.append(n).append(", ");
            }
            throw new DataStoreException(sb.toString());
        }
    }

    /**
     * Wrap a feature reader with a query.
     * This method can be use if the FeatureStore implementation can not support all
     * filtering parameters. The returned reader will repect the remaining query
     * parameters but keep in mind that this is done in a generic way, which might
     * not be the most effective way.
     *
     * Becareful if you give a sortBy parameter in the query, this can cause
     * OutOfMemory errors since the generic implementation must iterate over all
     * feature and holds them in memory before ordering them.
     * It may be a better solution to say in the query capabilities that sortBy
     * are not handle by this FeatureStore implementation.
     *
     * @param reader FeatureReader to wrap
     * @param remainingParameters , query holding the parameters that where not handle
     * by the FeatureStore implementation
     * @return FeatureReader Reader wrapping the given reader with all query parameters
     * @throws IOException
     */
    protected FeatureReader handleRemaining(FeatureReader reader, final Query remainingParameters) throws DataStoreException{

        final Integer start = remainingParameters.getStartIndex();
        final Integer max = remainingParameters.getMaxFeatures();
        final Filter filter = remainingParameters.getFilter();
        final Name[] properties = remainingParameters.getPropertyNames();
        final SortBy[] sorts = remainingParameters.getSortBy();
        final double[] resampling = remainingParameters.getResolution();
        final CoordinateReferenceSystem crs = remainingParameters.getCoordinateSystemReproject();
        final Hints hints = remainingParameters.getHints();

        //we should take care of wrapping the reader in a correct order to avoid
        //unnecessary calculations. fast and reducing number wrapper should be placed first.
        //but we must not take misunderstanding assumptions neither.
        //exemple : filter is slow than startIndex and MaxFeature but must be placed before
        //          otherwise the result will be illogic.


        //wrap sort by ---------------------------------------------------------
        //This can be really expensive, and force the us to read the full iterator.
        //that may cause out of memory errors.
        if(sorts != null && sorts.length != 0){
            reader = GenericSortByFeatureIterator.wrap(reader, sorts);
        }

        //wrap filter ----------------------------------------------------------
        //we must keep the filter first since it impacts the start index and max feature
        if(filter != null && filter != Filter.INCLUDE){
            if(filter == Filter.EXCLUDE){
                //filter that exclude everything, use optimzed reader
                reader = GenericEmptyFeatureIterator.createReader(reader.getFeatureType());
                //close original reader
                reader.close();
            }else{
                reader = GenericFilterFeatureIterator.wrap(reader, filter);
            }
        }

        //wrap start index -----------------------------------------------------
        if(start != null && start > 0){
            reader = GenericStartIndexFeatureIterator.wrap(reader, start);
        }
        
        //wrap max -------------------------------------------------------------
        if(max != null){
            if(max == 0){
                //use an optimized reader
                reader = GenericEmptyFeatureIterator.createReader(reader.getFeatureType());
                //close original reader
                reader.close();
            }else{
                reader = GenericMaxFeatureIterator.wrap(reader, max);
            }
        }

        //wrap properties, remove primary keys if necessary --------------------
        final Boolean hide = (Boolean) hints.get(HintsPending.FEATURE_HIDE_ID_PROPERTY);
        final FeatureType original = reader.getFeatureType();
        FeatureType mask = original;
        if(properties != null){
            final List<Name> names = new ArrayList<Name>();
            loop:
            for(Name n : properties){
                for(Name dn : names){
                    if(DefaultName.match(n, dn)) continue loop;
                }
                names.add(n);
            }
            
            try {
                mask = FeatureTypeUtilities.createSubType(mask, names.toArray(new Name[0]));
            } catch (SchemaException ex) {
                throw new DataStoreException(ex);
            }
        }
        if(hide != null && hide){
            try {
                //remove primary key properties
                mask = FeatureTypeUtilities.excludePrimaryKeyFields(mask);
            } catch (SchemaException ex) {
                throw new DataStoreException(ex);
            }
        }
        if(mask != original){
            reader = GenericRetypeFeatureIterator.wrap(reader, mask, hints);
        }

        //wrap resampling ------------------------------------------------------
        if(resampling != null){
            reader = GenericTransformFeatureIterator.wrap(reader, 
                    new GeometryScaleTransformer(resampling[0], resampling[1]),hints);
        }

        //wrap reprojection ----------------------------------------------------
        if(crs != null){
            try {
                reader = GenericReprojectFeatureIterator.wrap(reader, crs, hints);
            } catch (FactoryException ex) {
                throw new DataStoreException(ex);
            } catch (SchemaException ex) {
                throw new DataStoreException(ex);
            }
        }

        return reader;
    }

    /**
     * Wrap a feature writer with a Filter.
     * This method can be used when the featurestore implementation is not
     * intelligent enough to handle filtering.
     *
     * @param writer featureWriter to filter
     * @param filter filter to use for hiding feature while iterating
     * @return Filtered FeatureWriter
     * @throws DataStoreException
     */
    protected FeatureWriter handleRemaining(FeatureWriter writer, Filter filter) throws DataStoreException{

        //wrap filter ----------------------------------------------------------
        if(filter != null && filter != Filter.INCLUDE){
            if(filter == Filter.EXCLUDE){
                //filter that exclude everything, use optimzed writer
                writer.close(); //close the previous writer
                writer = GenericEmptyFeatureIterator.createWriter(writer.getFeatureType());
            }else{
                writer = GenericFilterFeatureIterator.wrap(writer, filter);
            }
        }

        return writer;
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     *
     * @param groupName
     * @param newFeatures
     * @return list of ids of the features added.
     * @throws DataStoreException
     */
    protected List<FeatureId> handleAddWithFeatureWriter(final Name groupName, final Collection<? extends Feature> newFeatures,
            final Hints hints) throws DataStoreException{
        try{
            return FeatureStoreUtilities.write(getFeatureWriterAppend(groupName,hints), newFeatures);
        }catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     *
     * @param groupName
     * @param filter
     * @param values
     * @throws DataStoreException
     */
    protected void handleUpdateWithFeatureWriter(final Name groupName, final Filter filter,
            final Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {

        final FeatureWriter writer = getFeatureWriter(groupName,filter);

        try{
            while(writer.hasNext()){
                final Feature f = writer.next();
                for(final Entry<? extends PropertyDescriptor,? extends Object> entry : values.entrySet()){
                    f.getProperty(entry.getKey().getName()).setValue(entry.getValue());
                }
                writer.write();
            }
        } catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        } finally{
            writer.close();
        }
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     * 
     * @param groupName
     * @param filter
     * @throws DataStoreException
     */
    protected void handleRemoveWithFeatureWriter(final Name groupName, final Filter filter) throws DataStoreException {
        final FeatureWriter writer = getFeatureWriter(groupName,filter);

        try{
            while(writer.hasNext()){
                writer.next();
                writer.remove();
            }
        } catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        } finally{
            writer.close();
        }
    }

    /**
     * Convinient method to handle modification operation by using the
     * add, remove, update methods.
     *
     * @param groupName
     * @param filter
     * @throws DataStoreException
     */
    protected FeatureWriter handleWriter(final Name groupName, final Filter filter, final Hints hints) throws DataStoreException {
        return GenericFeatureWriter.wrap(this, groupName, filter);
    }

    protected FeatureWriter handleWriterAppend(final Name groupName, final Hints hints) throws DataStoreException {
        return GenericFeatureWriter.wrapAppend(this, groupName);
    }


    public static Name ensureGMLNS(final String namespace, final String local){
        if(local.equals(GML_NAME)){
            return new DefaultName(GML_NAMESPACE, GML_NAME);
        }else if(local.equals(GML_DESCRIPTION)){
            return new DefaultName(GML_NAMESPACE, GML_DESCRIPTION);
        }else{
            return new DefaultName(namespace, local);
        }
    }

    public static FeatureType ensureGMLNS(final FeatureType type){
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
        ftb.setName(type.getName());

        for(PropertyDescriptor desc : type.getDescriptors()){
            adb.reset();
            adb.copy((AttributeDescriptor) desc);
            if(desc.getName().getLocalPart().equals(GML_NAME)){
                adb.setName(GML_NAMESPACE, GML_NAME);
                ftb.add(adb.buildDescriptor());
            }else if(desc.getName().getLocalPart().equals(GML_DESCRIPTION)){
                adb.setName(GML_NAMESPACE, GML_DESCRIPTION);
                ftb.add(adb.buildDescriptor());
            }else{
                ftb.add(desc);
            }
        }

        ftb.setDefaultGeometry(type.getGeometryDescriptor().getName());

        return ftb.buildFeatureType();
    }

}
