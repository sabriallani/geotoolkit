/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2014, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.geotoolkit.feature.FeatureTypeExt;
import org.geotoolkit.feature.ReprojectFeatureType;
import org.geotoolkit.feature.ViewFeatureType;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.memory.GenericFeatureWriter;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.Selector;
import org.geotoolkit.data.query.Source;
import org.geotoolkit.data.session.DefaultSession;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.util.NamesExt;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.storage.StorageEvent;
import org.geotoolkit.storage.StorageListener;
import org.geotoolkit.version.Version;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.opengis.util.GenericName;
import org.geotoolkit.storage.DataStore;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.feature.MismatchedFeatureException;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.internal.metadata.NameToIdentifier;
import org.apache.sis.internal.storage.MetadataBuilder;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.IllegalNameException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.BackingStoreException;
import org.geotoolkit.storage.DefaultAggregate;
import org.geotoolkit.storage.Resource;
import org.opengis.feature.AttributeType;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ScopedName;


/**
 * Uncomplete implementation of a feature store that handle most methods
 * by fallbacking on others. It also offer some generic methods to
 * handle query parameters and events.
 *
 * @author Johann Sorel (Geomatys)
 * @module
 */
public abstract class AbstractFeatureStore extends DataStore implements FeatureStore {

    /**
     * Static variables refering to GML model.
     */
    public static final String GML_311_NAMESPACE = "http://www.opengis.net/gml";
    public static final String GML_32_NAMESPACE = "http://www.opengis.net/gml/3.2";
    public static final String GML_NAME = "name";
    public static final String GML_DESCRIPTION = "description";

    protected static final String NO_NAMESPACE = "no namespace";

    private static final Logger logger = Logging.getLogger("org.geotoolkit.data");

    protected final Parameters parameters;
    protected final Set<StorageListener> listeners = new HashSet<>();

    protected AbstractFeatureStore(final ParameterValueGroup params) {
        this.parameters = Parameters.castOrWrap(params);
    }

    @Override
    public Parameters getOpenParameters() {
        return parameters;
    }

    protected Logger getLogger(){
        return logger;
    }

    @Override
    protected Metadata createMetadata() throws DataStoreException {
        final MetadataBuilder builder = new MetadataBuilder();
        builder.addIdentifier(null, "InMemory", MetadataBuilder.Scope.ALL);

        final Set<GenericName> names = getNames();
        try {
            // Retrieve all feature types in the data source
            names.stream()
                    .flatMap(this::getFeatureTypeHierarchy)
                    .distinct()
                    /* Register found types in the metadata. We don't count instances,
                     * as it's possibly a costly operation.
                     */
                    .peek(type -> builder.addFeatureType(type, null))
                    /* Search for all available reference systems. Do not query
                     * parent properties, as we've got all abstract types through
                     * "getFeatureTypeHierarchy"
                     */
                    .flatMap(type -> type.getProperties(false).stream())
                    .filter(p -> p instanceof AttributeType)
                    .map(p -> (AttributeType) p)
                    .map(p -> {
                        Object result = p.characteristics().get(AttributeConvention.CRS_CHARACTERISTIC.toString());
                        if (result instanceof AttributeType)
                            result = ((AttributeType) result).getDefaultValue();
                        return result;
                    })
                    .filter(crs -> crs instanceof CoordinateReferenceSystem)
                    .map(crs -> (CoordinateReferenceSystem) crs)
                    .distinct()
                    .forEach(builder::addReferenceSystem);

            return builder.build(false);
        } catch (BackingStoreException e) {
            throw e.unwrapOrRethrow(DataStoreException.class);
        }
    }

    private Stream<FeatureType> getFeatureTypeHierarchy(final GenericName name) throws BackingStoreException {
        try {
            return getFeatureTypeHierarchy(name.tip().toString()).stream();
        } catch (DataStoreException e) {
            throw new BackingStoreException(e);
        }
    }

    @Override
    public Resource getRootResource() throws DataStoreException {
        final DefaultAggregate ds = new DefaultAggregate(NamesExt.create("root"));
        for (GenericName name : getNames()) {
            ds.addResource(new DefaultFeatureResource(this, name));
        }
        return ds;
    }

    /**
     * Get a collection of all available names.
     * @return {@literal Set<Name>}, never null, but can be empty.
     */
    public Set<GenericName> getNames() throws DataStoreException {
        final Set<GenericName> names = new HashSet<>();
        listNames(getRootResource(), names);
        return names;
    }

    private static void listNames(org.apache.sis.storage.Resource resource, Set<GenericName> names) throws DataStoreException {
        final Identifier identifier = ((Resource)resource).getIdentifier();
        if (identifier instanceof GenericName) {
            names.add((GenericName) identifier);
        } else {
            names.add(NamesExt.create(identifier.getCode()));
        }
        if (resource instanceof Aggregate) {
            final Aggregate ds = (Aggregate) resource;
            for (org.apache.sis.storage.Resource rs : ds.components()) {
                listNames(rs,names);
            }
        }
    }

    @Override
    public Resource findResource(String name) throws DataStoreException {
        Resource res = findResource(getRootResource(), name);
        if (res==null) {
            throw new IllegalNameException("No resource for name : "+name);
        }
        return (Resource) res;
    }

    private Resource findResource(final org.apache.sis.storage.Resource candidate, String name) throws DataStoreException {

        final boolean match = NameToIdentifier.isHeuristicMatchForIdentifier(Collections.singleton(((Resource)candidate).getIdentifier()), name);
        Resource result = match ? (Resource)candidate : null;

        if (candidate instanceof Aggregate) {
            final Aggregate ds = (Aggregate) candidate;
            for (org.apache.sis.storage.Resource rs : ds.components()) {
                Object rr = findResource(rs,name);
                if (rr instanceof Resource) {
                    if (result!=null) {
                        throw new DataStoreException("Multiple resources match the name : "+name);
                    }
                    result = (Resource) rr;
                }
            }
        }
        return result;
    }

    /**
     * Overwrite to enable versioning.
     */
    @Override
    public VersionControl getVersioning(String typeName) throws VersioningException{
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

    @Override
    public FeatureType getFeatureType(final Query query) throws DataStoreException, MismatchedFeatureException {

        final Source source = query.getSource();

        if(Query.GEOTK_QOM.equalsIgnoreCase(query.getLanguage()) && source instanceof Selector){
            final Selector selector = (Selector) source;
            FeatureType ft = selector.getSession().getFeatureStore().getFeatureType(query.getTypeName());
            final String[] properties = query.getPropertyNames();
            if (properties!=null && FeatureTypeExt.isAllProperties(ft, properties)) {
                ft = new ViewFeatureType(ft, properties);
            }
            if(query.getCoordinateSystemReproject()!=null){
                ft = new ReprojectFeatureType(ft, query.getCoordinateSystemReproject());
            }

            return ft;
        }

        throw new DataStoreException("Can not deduce feature type of query : " + query);
    }

    /**
     * Default implementation, will return a list with the single feature tpe from method
     * {@link #getFeatureType(org.geotoolkit.feature.type.Name) }
     */
    @Override
    public List<FeatureType> getFeatureTypeHierarchy(String typeName) throws DataStoreException {
        return Collections.singletonList((FeatureType)getFeatureType(typeName));
    }

    /**
     * {@inheritDoc }
     *
     * This implementation will try to aquire a writer and return true if it
     * succeed.
     */
    @Override
    public boolean isWritable(final String typeName) throws DataStoreException {
        //while raise an error if type doesnt exist
        getFeatureType(typeName);

        FeatureWriter writer = null;
        try{
            writer = getFeatureWriter(QueryBuilder.filtered(typeName, Filter.EXCLUDE));
            return true;
        }catch(Exception ex){
            //catch anything, log it
            getLogger().log(Level.WARNING, "Type not writable : {0}", ex.getMessage());
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
     */
    @Override
    public Envelope getEnvelope(Query query) throws DataStoreException, FeatureStoreRuntimeException {

        if(query.retrieveAllProperties()){
            //we simplify it, get only geometry attributes + sort attribute
            final FeatureType ft = getFeatureType(query.getTypeName());
            final List<String> names = new ArrayList<>();
            for(PropertyType desc : ft.getProperties(true)){
                if(AttributeConvention.isGeometryAttribute(desc)){
                    names.add(desc.getName().toString());
                } else if (query.getSortBy() != null) {
                    for (SortBy sortBy : query.getSortBy()) {
                        final String propName = sortBy.getPropertyName().getPropertyName();
                        if (desc.getName().toString().equals(propName) ||
                            desc.getName().tip().toString().equals(propName)) {
                            names.add(desc.getName().toString());
                        }
                    }
                }
            }

            if(names.isEmpty()){
                //no geometry field
                return null;
            }

            final QueryBuilder qb = new QueryBuilder(query);
            qb.setProperties(names.toArray(new String[names.size()]));
            query = qb.buildQuery();
        }


        final String[] wantedProp = query.getPropertyNames();
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
    public final List<FeatureId> addFeatures(String groupName, Collection<? extends Feature> newFeatures) throws DataStoreException {
        return addFeatures(groupName,newFeatures,new Hints());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws DataStoreException {
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
    protected void fireSchemaAdded(final GenericName name, final FeatureType type){
        sendStructureEvent(FeatureStoreManagementEvent.createAddEvent(this, name, type));
    }

    /**
     * Fires a schema update event to all listeners.
     *
     * @param name updated schema name
     * @param oldType featuretype before change
     * @param newType featuretype after change
     */
    protected void fireSchemaUpdated(final GenericName name, final FeatureType oldType, final FeatureType newType){
        sendStructureEvent(FeatureStoreManagementEvent.createUpdateEvent(this, name, oldType, newType));
    }

    /**
     * Fires a schema delete event to all listeners.
     *
     * @param name deleted schema name
     * @param type feature type of the deleted schema
     */
    protected void fireSchemaDeleted(final GenericName name, final FeatureType type){
        sendStructureEvent(FeatureStoreManagementEvent.createDeleteEvent(this, name, type));
    }

    /**
     * Fires a features add event.
     *
     * @param name of the schema where features where added.
     * @param ids modified feature ids
     */
    protected void fireFeaturesAdded(final GenericName name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createAddEvent(this, name, ids));
    }

    /**
     * Fires a features update event.
     *
     * @param name of the schema where features where updated.
     * @param ids modified feature ids
     */
    protected void fireFeaturesUpdated(final GenericName name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createUpdateEvent(this, name, ids));
    }

    /**
     * Fires a features delete event.
     *
     * @param name of the schema where features where deleted
     * @param ids modified feature ids
     */
    protected void fireFeaturesDeleted(final GenericName name, final Id ids){
        sendContentEvent(FeatureStoreContentEvent.createDeleteEvent(this, name, ids));
    }

    ////////////////////////////////////////////////////////////////////////////
    // useful methods for feature store that doesn't implement all query parameters/
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convenient method to check that the given type name exist.
     * Will raise a datastore exception if the name does not exist in this FeatureStore.
     *
     * @param candidate Name to test.
     * @throws DataStoreException if name does not exist, or if it'ambiguous.
     * Ambiguity is raised in case there's not any exact match, but input name
     * is equal to the local part of multiple names in the store.
     */
    protected void typeCheck(final String candidate) throws DataStoreException{
        ArgumentChecks.ensureNonNull("type name", candidate);
        int count = 0;
        boolean exactMatching = false;
        final Collection<GenericName> names = getNames();
        for (GenericName name : names) {
            if (candidate.equals(name.toString())) {
                exactMatching = true;
                break;
            }

            while (name instanceof ScopedName) {
                name = ((ScopedName)name).tail();
                if (candidate.equals(name.toString())) {
                    count++;
                    if (count>1) break;
                }
            }
        }

        if (exactMatching) return;

        if (count>1) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Multiple match for name ");
            sb.append(candidate);
            sb.append(", available names are : ");
            for(final GenericName n : names){
                sb.append(n).append(", ");
            }
            throw new DataStoreException(sb.toString());
        } else if (count==0) {
            final StringBuilder sb = new StringBuilder("Type name : ");
            sb.append(candidate);
            sb.append(" do not exist in this feature store, available names are : ");
            for(final GenericName n : names){
                sb.append(n).append(", ");
            }
            throw new DataStoreException(sb.toString());
        }
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     *
     * @return list of ids of the features added.
     */
    protected List<FeatureId> handleAddWithFeatureWriter(final String groupName, final Collection<? extends Feature> newFeatures,
            final Hints hints) throws DataStoreException{

        try(FeatureWriter featureWriter = getFeatureWriter(QueryBuilder.filtered(groupName, Filter.EXCLUDE))) {
            while (featureWriter.hasNext()) {
                featureWriter.next();
            }
            return FeatureStoreUtilities.write(featureWriter, newFeatures);
        }catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     */
    protected void handleUpdateWithFeatureWriter(final String groupName, final Filter filter,
            final Map<String, ?> values) throws DataStoreException {

        try(FeatureWriter writer = getFeatureWriter(QueryBuilder.filtered(groupName, filter))) {
            while(writer.hasNext()){
                final Feature f = writer.next();
                for(final Entry<String,?> entry : values.entrySet()){
                    f.setPropertyValue(entry.getKey(), entry.getValue());
                }
                writer.write();
            }
        } catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * Convinient method to handle adding features operation by using the
     * FeatureWriter.
     */
    protected void handleRemoveWithFeatureWriter(final String groupName, final Filter filter) throws DataStoreException {
        try(FeatureWriter writer = getFeatureWriter(QueryBuilder.filtered(groupName, filter))) {
            while(writer.hasNext()){
                writer.next();
                writer.remove();
            }
        } catch(FeatureStoreRuntimeException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * Convenient method to handle modification operation by using the
     * add, remove, update methods.
     */
    protected FeatureWriter handleWriter(Query query) throws DataStoreException {
        final Filter filter = query.getFilter();
        final String groupName = query.getTypeName();
        if (Filter.EXCLUDE.equals(filter) ) {
            return GenericFeatureWriter.wrapAppend(this, groupName);
        } else {
            return GenericFeatureWriter.wrap(this, groupName, filter);
        }
    }

    public static GenericName ensureGMLNS(final String local){
        if(local.equals(GML_NAME)){
            return NamesExt.create(GML_311_NAMESPACE, GML_NAME);
        }else if(local.equals(GML_DESCRIPTION)){
            return NamesExt.create(GML_311_NAMESPACE, GML_DESCRIPTION);
        }else{
            return NamesExt.create(local);
        }
    }

    @Override
    public void addStorageListener(final StorageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeStorageListener(final StorageListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Forward a structure event to all listeners.
     * @param event event to send to listeners.
     */
    protected void sendStructureEvent(final StorageEvent event){
        final StorageListener[] lst;
        synchronized (listeners) {
            lst = listeners.toArray(new StorageListener[listeners.size()]);
        }
        for(final StorageListener listener : lst){
            listener.structureChanged(event);
        }
    }

    /**
     * Forward a data event to all listeners.
     * @param event event to send to listeners.
     */
    protected void sendContentEvent(final StorageEvent event){
        final StorageListener[] lst;
        synchronized (listeners) {
            lst = listeners.toArray(new StorageListener[listeners.size()]);
        }
        for(final StorageListener listener : lst){
            listener.contentChanged(event);
        }
    }

    /**
     * Forward given event, changing the source by this object.
     * For implementation use only.
     */
    public void forwardStructureEvent(StorageEvent event){
        sendStructureEvent(event.copy(this));
    }

    /**
     * Forward given event, changing the source by this object.
     * For implementation use only.
     */
    public void forwardContentEvent(StorageEvent event){
        sendContentEvent(event.copy(this));
    }

}
