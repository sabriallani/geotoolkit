/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2015, Geomatys
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

package org.geotoolkit.feature.xml.jaxp;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.jaxb.JAXBEventHandler;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.JTSGeometry;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.aggregate.JTSMultiCurve;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.internal.jaxb.JTSWrapperMarshallerPool;
import org.geotoolkit.internal.jaxb.LineStringPosListType;
import org.geotoolkit.internal.jaxb.PolygonType;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.Namespaces;
import org.geotoolkit.xml.StaxStreamReader;

import org.opengis.util.GenericName;

import static javax.xml.stream.events.XMLEvent.*;
import net.iharder.Base64;
import org.geotoolkit.feature.FeatureExt;
import org.apache.sis.internal.feature.AttributeConvention;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.geometry.JTSLineString;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.GMLMarshallerPool;
import org.opengis.util.FactoryException;
import org.apache.sis.util.Numbers;
import org.apache.sis.util.Utilities;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.nio.IOUtilities;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeType;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureAssociationRole;
import org.opengis.feature.FeatureType;
import org.opengis.feature.Operation;
import org.opengis.feature.PropertyNotFoundException;
import org.opengis.feature.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;


/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class JAXPStreamFeatureReader extends StaxStreamReader implements XmlFeatureReader {

    private static final JAXBEventHandler JAXBLOGGER = new JAXBEventHandler();

    public static final String READ_EMBEDDED_FEATURE_TYPE = "readEmbeddedFeatureType";
    public static final String SKIP_UNEXPECTED_PROPERTY_TAGS = "skipUnexpectedPropertyTags";
    public static final String BINDING_PACKAGE = "bindingPackage";
    protected static final Logger LOGGER = Logging.getLogger("org.geotoolkit.feature.xml.jaxp");
    private Unmarshaller unmarshaller;
    public static final String LONGITUDE_FIRST = "longitudeFirst";

    /**
     * GML namespace for this class.
     */
    private static final String GML = "http://www.opengis.net/gml";
    protected List<FeatureType> featureTypes;
    private URI base = null;
    //benchmarked 07/04/2015 : reduce by 10% reading time
    private final Map<QName,GenericName> nameCache = new HashMap<QName,GenericName>(){
        @Override
        public GenericName get(Object key) {
            GenericName n = super.get(key);
            if(n==null){
                n = Utils.getNameFromQname((QName) key);
                put((QName)key, n);
            }
            return n;
        }
    };


    private final Map<String,String> schemaLocations = new HashMap<>();

    public JAXPStreamFeatureReader() {
        this(new ArrayList<FeatureType>());
    }

    public JAXPStreamFeatureReader(final FeatureType featureType) {
        this(Arrays.asList(featureType));
    }

    public JAXPStreamFeatureReader(final List<FeatureType> featureTypes) {
        this.featureTypes = featureTypes;
        this.properties.put(READ_EMBEDDED_FEATURE_TYPE, false);
    }

    /**
     * XSD Schema locations.
     * Will be filled only if reading feature type was asked.
     */
    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setFeatureType(final FeatureType featureType) {
        this.featureTypes = Arrays.asList(featureType);
    }

    @Override
    public void dispose() {
        // do nothing
        if(unmarshaller!=null){
            getPool().recycle(unmarshaller);
            unmarshaller = null;
        }
    }

    @Override
    public Object read(final Object xml) throws IOException, XMLStreamException  {
        setInput(xml);
        return read();
    }

    @Override
    public FeatureReader readAsStream(final Object xml) throws IOException, XMLStreamException {
        setInput(xml);
        return new JAXPStreamIterator();
    }

    @Override
    public void setInput(Object input) throws IOException, XMLStreamException {
        super.setInput(input);
        if (input instanceof URL) {
            try {
                base = ((URL) input).toURI();
            } catch (URISyntaxException ex) {
               throw new IOException(ex);
            }
        } else if (input instanceof URI) {
            base = (URI) input;
        } else if (input instanceof Path) {
            base = ((Path) input).toUri();
        } else if (input instanceof File) {
            base = ((File) input).toURI();
        } else {
            base = null;
        }

        if (unmarshaller == null) {
            try {
                unmarshaller = getPool().acquireUnmarshaller();
                unmarshaller.setEventHandler(JAXBLOGGER);
            } catch (JAXBException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
    }

    public URI getInput(){
        return base;
    }

    /**
     * Start to read An object from the XML datasource.
     * @return A feature or featureCollection described in the XML stream.
     */
    private Object read() throws XMLStreamException {
        while (reader.hasNext()) {
            final int event = reader.getEventType();

            //we are looking for the root mark
            if (event == START_ELEMENT) {
                readFeatureTypes();

                final GenericName name  = nameCache.get(reader.getName());
                String id = "no-gml-id";
                for(int i=0,n=reader.getAttributeCount();i<n;i++){
                    final QName attName = reader.getAttributeName(i);
                    //search and id property from any namespace
                    if("id".equals(attName.getLocalPart()) && attName.getNamespaceURI().startsWith(GML)){
                        id = reader.getAttributeValue(i);
                    }
                }
                final StringBuilder expectedFeatureType = new StringBuilder();

                if (name.tip().toString().equals("FeatureCollection")) {
                    final Object coll = readFeatureCollection(id);
                    if (coll == null) {
                        if (featureTypes.size() == 1) {
                            return FeatureStoreUtilities.collection(id, featureTypes.get(0));
                        } else {
                            return FeatureStoreUtilities.collection(id, null);
                        }
                    }
                    return coll;

                } else if (name.tip().toString().equals("Transaction")) {
                    return extractFeatureFromTransaction();

                } else {
                    for (FeatureType ft : featureTypes) {
                        if (ft.getName().equals(name)) {
                            return readFeature(ft);
                        }
                        expectedFeatureType.append(ft.getName()).append('\n');
                    }
                }

                throw new IllegalArgumentException("The xml does not describe the same type of feature: \n " +
                                                   "Expected: " + expectedFeatureType.toString() + '\n' +
                                                   "But was: "  + name);
            }
            reader.next();
        }
        return null;
    }

    private void readFeatureTypes(){
        // we search an embedded featureType description
        String schemaLocation = reader.getAttributeValue(Namespaces.XSI, "schemaLocation");
        if (isReadEmbeddedFeatureType() && schemaLocation != null) {
            final JAXBFeatureTypeReader featureTypeReader = new JAXBFeatureTypeReader();
            schemaLocation = schemaLocation.trim();
            final String[] urls = schemaLocation.split("\\s+");
            for (int i = 0; i < urls.length; i++) {
                final String namespace = urls[i];
                if (!(namespace.equalsIgnoreCase("http://www.opengis.net/gml") || namespace.equalsIgnoreCase("http://www.opengis.net/wfs")) && i + 1 < urls.length) {
                    final String fturl = urls[i + 1];
                    schemaLocations.put(namespace, fturl);
                    try {
                        final URI uri = Utils.resolveURI(base, fturl);
                        List<FeatureType> fts = (List<FeatureType>) featureTypeReader.read(IOUtilities.open(uri));
                        for (FeatureType ft : fts) {
                            if (!featureTypes.contains(ft)) {
                                featureTypes.add(ft);
                            }
                        }
                    } catch (MalformedURLException | URISyntaxException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    } catch (IOException | JAXBException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                    i = i + 2;
                } else if(namespace.equalsIgnoreCase("http://www.opengis.net/gml") || namespace.equalsIgnoreCase("http://www.opengis.net/wfs")) {
                    i++;
                }
            }
        }
    }

    private Object readFeatureCollection(final String id) throws XMLStreamException {
        FeatureCollection collection = null;
        while (reader.hasNext()) {
            int event = reader.next();


            //we are looking for the root mark
            if (event == START_ELEMENT) {
                final GenericName name = nameCache.get(reader.getName());

                String fid = null;
                if (reader.getAttributeCount() > 0) {
                    fid = reader.getAttributeValue(0);
                }

                if (name.tip().toString().equals("featureMember") || name.tip().toString().equals("featureMembers")) {
                    continue;

                } else if (name.tip().toString().equals("boundedBy")) {
                    while (reader.hasNext()) {
                        event = reader.next();
                        if (event == START_ELEMENT) {
                            break;
                        }
                    }
                    String srsName = null;
                    if (reader.getAttributeCount() > 0) {
                        srsName = reader.getAttributeValue(0);
                    }
                    final JTSEnvelope2D bounds = readBounds(srsName);

                } else {
                    if (fid == null) {
                        LOGGER.info("Missing feature id : generating a random one");
                        fid = UUID.randomUUID().toString();
                    }

                    boolean find = false;
                    StringBuilder expectedFeatureType = new StringBuilder();
                    for (FeatureType ft : featureTypes) {
                        if (ft.getName().equals(name)) {
                            if (collection == null) {
                                collection = FeatureStoreUtilities.collection(id, ft);
                            }
                            collection.add((Feature)readFeature(ft));
                            find = true;
                        }
                        expectedFeatureType.append(ft.getName()).append('\n');
                    }

                    if (!find) {
                        throw new IllegalArgumentException("The xml does not describe the same type of feature: \n "
                                + "Expected: " + expectedFeatureType.toString() + '\n'
                                + "But was: " + name);
                    }
                }
            }
        }
        return collection;
    }

    private Feature readFeature(final FeatureType featureType) throws XMLStreamException {
        return readFeature(featureType, featureType.getName());
    }

    private Feature readFeature(final FeatureType featureType, final GenericName tagName) throws XMLStreamException {

        final Feature feature = featureType.newInstance();

        /*
         * We create a map and a collection because we can encounter two cases :
         * - The case featureType defines a property with max occur > 1.
         * - The case featureType defines a property with max occur = 1, and its
         * value instance of collection or map.
         * We store all encountered name with its linked property in the map, so
         * at each value parsed, we can add it in the existing property if its
         * value is a list or map. The collection is the final property store,
         * we add the all the created properties in it (so we can put multiple
         * properties with the same name).
         */
        final List<Entry<Operation,Object>> ops = new ArrayList<>();

        //read attributes
        final int nbAtts = reader.getAttributeCount();
        for(int i=0;i<nbAtts;i++){
            final QName attName = reader.getAttributeName(i);
            try {
                final AttributeType pd = (AttributeType) featureType.getProperty("@"+attName.getLocalPart());
                final String attVal = reader.getAttributeValue(i);
                final Object val = ObjectConverters.convert(attVal, pd.getValueClass());
                feature.setPropertyValue(pd.getName().toString(), val);
            } catch(PropertyNotFoundException ex) {
                //do nothing
            }
        }

        boolean doNext = true;
        //read a real complex type
        while (!doNext || reader.hasNext()) {
            if(doNext){
                reader.next();
            }
            doNext = true;
            int event = reader.getEventType();

            if (event == START_ELEMENT) {
                final GenericName propName = nameCache.get(reader.getName());

                // we skip the boundedby attribute if it's present
                if ("boundedBy".equals(propName.tip().toString())) {
                    toTagEnd("boundedBy");
                    continue;
                }

                final String nameAttribute = reader.getAttributeValue(null, "name");
                final PropertyType propertyType;
                try {
                    propertyType = featureType.getProperty(propName.toString());

                }catch(PropertyNotFoundException ex) {
                    if (Boolean.TRUE.equals(this.properties.get(SKIP_UNEXPECTED_PROPERTY_TAGS))) {
                        toTagEnd(propName.tip().toString());
                        continue;
                    } else {
                        try {
                            //convert the content as a dom node
                            final AttributeType pd = (AttributeType) featureType.getProperty("_any");
                            final Document doc = readAsDom(propName.tip().toString());
                            feature.setPropertyValue(pd.getName().toString(), doc);
                            doNext = false;
                            continue;
                        }catch(PropertyNotFoundException e) {
                            throw new IllegalArgumentException("Unexpected attribute:" + propName + " not found in :\n" + featureType);
                        }
                    }
                }

                if(propertyType instanceof Operation){
                    final Operation opType = (Operation) propertyType;
                    final PropertyType resultType = (PropertyType) opType.getResult();
                    final Object value = readPropertyValue(resultType,false);
                    ops.add(new AbstractMap.SimpleImmutableEntry<>((Operation)propertyType,value));
                    if(resultType.getName().equals(propertyType.getName())){
                        //we are already on the next element here, jaxb ate one
                        doNext = false;
                    }
                    continue;
                }


                //read attributes
                if (propertyType instanceof AttributeType) {
                    final AttributeType<?> attType = (AttributeType) propertyType;
                    final int nbPropAtts = reader.getAttributeCount();
                    if (nbPropAtts>0) {
                        final Attribute att = (Attribute) feature.getProperty(propName.toString());
                        for(int i=0;i<nbPropAtts;i++){
                            final QName qname = reader.getAttributeName(i);
                            final GenericName attName = nameCache.get(new QName(qname.getNamespaceURI(), "@"+qname.getLocalPart()));
                            final AttributeType<?> charType = attType.characteristics().get(attName.toString());
                            if (charType!=null) {
                                final String attVal = reader.getAttributeValue(i);
                                final Object val = ObjectConverters.convert(attVal, charType.getValueClass());
                                final Attribute chara = charType.newInstance();
                                chara.setValue(val);
                                att.characteristics().put(attName.toString(), chara);
                            }
                        }
                    }
                }

                //parse the value
                final Object value = readPropertyValue(propertyType,true);

                ////////////////////////////////////////////////////////////

                final Object previousVal = feature.getPropertyValue(propName.toString());

                if(propertyType instanceof FeatureAssociationRole){
                    final FeatureAssociationRole role = (FeatureAssociationRole) propertyType;

                    if (role.getMaximumOccurs() > 1) {
                        final List vals = new ArrayList((Collection) previousVal);
                        vals.add(value);
                        feature.setPropertyValue(propName.toString(), vals);
                    } else {
                        if (previousVal!=null) {
                            if (previousVal instanceof List) {
                                ((List) previousVal).add(value);
                            } else if (previousVal instanceof Map) {
                                if (nameAttribute != null) {
                                    ((Map) previousVal).put(nameAttribute, value);
                                } else {
                                    LOGGER.severe("unable to read a composite attribute : no name has been found");
                                }
                            }
                        } else {
                            feature.setPropertyValue(propName.toString(), value);
                        }
                    }
                }else{

                    if(previousVal!=null){
                        if(previousVal instanceof Map){
                            if(nameAttribute!=null){
                                ((Map) previousVal).put(nameAttribute, value);
                            }else{
                                LOGGER.severe("unable to read a composite attribute : no name has been found");
                            }
                        }else if (previousVal instanceof Collection) {
                            final List vals = new ArrayList((Collection) previousVal);
                            vals.add(value);
                            feature.setPropertyValue(propName.toString(), vals);
                        }else{
                            throw new XMLStreamException("Expected a multivalue property");
                        }
                    }else{
                        //new property
                        if(nameAttribute!=null){
                            final Map<String, Object> map = new LinkedHashMap<>();
                            map.put(nameAttribute, value);
                            feature.setPropertyValue(propName.toString(), map);
                        }else{
                            feature.setPropertyValue(propName.toString(), value);
                        }
                    }
                }
                ////////////////////////////////////////////////////////////


            } else if (event == END_ELEMENT) {
                final QName q = reader.getName();
                if (q.getLocalPart().equals("featureMember") || nameCache.get(q).equals(tagName)) {
                    break;
                }
            }
        }


//        //apply operations (alias/susbstitutionGroups)
//        for(Entry<OperationDescriptor,Object> entry : ops){
//            final OperationType type = entry.getKey().getType();
//            type.invokeSet(feature, entry.getValue());
//        }

        return feature;
    }

    private Object readPropertyValue(PropertyType propertyType, boolean skipCurrent) throws XMLStreamException{
        final GenericName propName = nameCache.get(reader.getName());

        Object value = null;
        if (AttributeConvention.isGeometryAttribute(propertyType)) {
            int event;
            if(skipCurrent){
                event = reader.next();
            }else{
                event = reader.getEventType();
            }
            while (event != START_ELEMENT) {
                if (event == END_ELEMENT) {
                    return null;
                }
                event = reader.next();
            }
            try {
                final boolean longitudeFirst;
                if (getProperty(LONGITUDE_FIRST) != null) {
                    longitudeFirst = (boolean) getProperty(LONGITUDE_FIRST);
                } else {
                    longitudeFirst = true;
                }

                final Geometry jtsGeom;
                final Object geometry = ((JAXBElement) unmarshaller.unmarshal(reader)).getValue();
                if (geometry instanceof JTSGeometry) {
                    final JTSGeometry isoGeom = (JTSGeometry) geometry;
                    if (isoGeom instanceof JTSMultiCurve) {
                        ((JTSMultiCurve)isoGeom).applyCRSonChild();
                    }
                    jtsGeom = isoGeom.getJTSGeometry();
                } else if (geometry instanceof PolygonType) {
                    final PolygonType polygon = ((PolygonType)geometry);
                    jtsGeom = polygon.getJTSPolygon().getJTSGeometry();
                    if(polygon.getCoordinateReferenceSystem() != null) {
                        JTS.setCRS(jtsGeom, polygon.getCoordinateReferenceSystem());
                    }
                } else if (geometry instanceof LineStringPosListType) {
                    final JTSLineString line = ((LineStringPosListType)geometry).getJTSLineString();
                    jtsGeom = line.getJTSGeometry();
                    if(line.getCoordinateReferenceSystem() != null) {
                        JTS.setCRS(jtsGeom, line.getCoordinateReferenceSystem());
                    }
                } else if (geometry instanceof AbstractGeometry) {
                    try {
                        jtsGeom = GeometrytoJTS.toJTS((AbstractGeometry) geometry, longitudeFirst);
                    } catch (FactoryException ex) {
                        throw new XMLStreamException("Factory Exception while transforming GML object to JTS", ex);
                    }
                } else {
                    throw new IllegalArgumentException("unexpected geometry type:" + geometry);
                }
                value = jtsGeom;

                //verify geometry crs
                final CoordinateReferenceSystem baseCrs = FeatureExt.getCRS(propertyType);
                if (baseCrs!=null && jtsGeom!=null && jtsGeom.getUserData() instanceof CoordinateReferenceSystem) {
                   if (!Utilities.equalsIgnoreMetadata(baseCrs,jtsGeom.getUserData())) {
                       throw new XMLStreamException("Unvalid geometry declaration, propertyType CRS is different from geometry CRS.\n"+baseCrs+"\n"+jtsGeom.getUserData());
                   }
                }

            } catch (JAXBException ex) {
                String msg = ex.getMessage();
                if (msg == null && ex.getLinkedException() != null) {
                    msg = ex.getLinkedException().getMessage();
                }
                throw new IllegalArgumentException("JAXB exception while reading the feature geometry: " + msg, ex);
            }

        } else if (propertyType instanceof FeatureAssociationRole) {

            final FeatureAssociationRole far = (FeatureAssociationRole) propertyType;
            final FeatureType valueType = far.getValueType();

            //GML properties have one level of encapsulation (in properties which follow the ogc convention)
            final QName currentName = reader.getName();
            if (currentName != null && valueType.getName().tip().toString().equals(currentName.getLocalPart())) {
                //no encapsulation
                value = readFeature(((FeatureAssociationRole) propertyType).getValueType(), nameCache.get(currentName));

            } else {

                boolean doNext = true;
                while (!doNext || reader.hasNext()) {
                    if(doNext){
                        reader.next();
                    }
                    doNext = true;
                    int event = reader.getEventType();

                    if (event == START_ELEMENT) {
                        final GenericName subName = nameCache.get(reader.getName());
                        value = readFeature(((FeatureAssociationRole) propertyType).getValueType(), subName);
                    } else if (event == END_ELEMENT) {
                        final QName q = reader.getName();
                        if (nameCache.get(q).equals(propName)) {
                            break;
                        }
                    }
                }
            }

        } else if (propertyType instanceof AttributeType) {
            final String content = reader.getElementText();
            final Class typeBinding = ((AttributeType)propertyType).getValueClass();

            if(List.class.equals(typeBinding) || Map.class.equals(typeBinding)){
                value = content;
            }else{
                value = readValue(content, (AttributeType)propertyType);
            }
        }

        return value;
    }

    public Object readValue(final String content, final AttributeType type){
        Object value = content;
        if(type.getValueClass()== byte[].class && content != null){
            try {
                value = Base64.decode(content);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Failed to parser binary64 : "+ex.getMessage(),ex);
            }
        }else{
            value = ObjectConverters.convert(value, Numbers.primitiveToWrapper(type.getValueClass()));
        }
        return value;
    }

    private Object extractFeatureFromTransaction() throws XMLStreamException {
        final List<Feature> features = new ArrayList<Feature>();
        boolean insert = false;
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == END_ELEMENT) {
                GenericName name  = nameCache.get(reader.getName());
                if (name.tip().toString().equals("Insert")) {
                    insert = false;
                }


            //we are looking for the root mark
            } else if (event == START_ELEMENT) {
                GenericName name  = nameCache.get(reader.getName());

                if (name.tip().toString().equals("Insert")) {
                    insert = true;
                    continue;

                } else if (insert) {

                    if (name.tip().toString().equals("FeatureCollection")) {
                        return readFeatureCollection("");
                    }
                    boolean find = false;
                    StringBuilder expectedFeatureType = new StringBuilder();
                    for (FeatureType ft : featureTypes) {
                        if (ft.getName().equals(name)) {
                            features.add((Feature)readFeature(ft));
                            find = true;
                        }
                        expectedFeatureType.append(ft.getName()).append('\n');
                    }

                    if (!find) {
                        throw new IllegalArgumentException("The xml does not describe the same type of feature: \n " +
                                                           "Expected: " + expectedFeatureType.toString()     + '\n'  +
                                                           "But was: "  + name);
                    }
                }
            }
        }
        return features;
    }

    @Override
    public Map<String, String> extractNamespace(final String xml) {
        try {
            final XMLInputFactory XMLfactory = XMLInputFactory.newInstance();
            XMLfactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

            final XMLStreamReader streamReader = XMLfactory.createXMLStreamReader(new StringReader(xml));
            final Map<String, String> namespaceMapping = new LinkedHashMap<>();
            while (streamReader.hasNext()) {
                int event = streamReader.next();
                if (event == START_ELEMENT) {
                    for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
                        namespaceMapping.put(streamReader.getNamespacePrefix(i), streamReader.getNamespaceURI(i));
                    }
                }
            }
            return namespaceMapping;
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while extracting namespace: {0}", ex.getMessage());
        }
        return null;
    }

    /**
     * Extract An envelope from the BoundedBy XML mark of a feature collection.
     *
     * @param srsName The extracted CRS identifier.
     *
     * @return An envelope of the collection bounds.
     * @throws XMLStreamException
     */
    private JTSEnvelope2D readBounds(final String srsName) throws XMLStreamException {
       JTSEnvelope2D bounds = null;
       while (reader.hasNext()) {
            int event = reader.next();
            if (event == END_ELEMENT) {
                QName endElement = reader.getName();
                if (endElement.getLocalPart().equals("boundedBy")) {
                    return null;
                }
            }

       }
        return bounds;
    }

    /**
     * Return a MarshallerPool depending on the property BINDING_PACKAGE.
     *
     * accepted values : "JTSWrapper" or null (default). => JTSWrapperMarshallerPool
     *                   "GML"      (default).                     => GMLMarshallerPool
     */
    private MarshallerPool getPool() {
        final String bindingPackage = (String) properties.get(BINDING_PACKAGE);
        if ("JTSWrapper".equals(bindingPackage)) {
            return JTSWrapperMarshallerPool.getInstance();
        } else if (bindingPackage == null || "GML".equals(bindingPackage)) {
            return GMLMarshallerPool.getInstance();
        } else {
            throw new IllegalArgumentException("Unexpected property value for BINDING_PACKAGE:" + bindingPackage);
        }
    }

    /**
     * @deprecated use getProperty(READ_EMBEDDED_FEATURE_TYPE)
     */
    @Deprecated
    public boolean isReadEmbeddedFeatureType() {
        return (Boolean) this.properties.get(READ_EMBEDDED_FEATURE_TYPE);
    }

    /**
     * * @deprecated use getProperties().put(READ_EMBEDDED_FEATURE_TYPE)
     */
    @Deprecated
    public void setReadEmbeddedFeatureType(boolean readEmbeddedFeatureType) {
        this.properties.put(READ_EMBEDDED_FEATURE_TYPE, readEmbeddedFeatureType);
    }

    private final class JAXPStreamIterator implements FeatureReader{

        private boolean singleFeature = false;
        private FeatureType type = null;
        private Feature next = null;

        public JAXPStreamIterator() throws XMLStreamException {
            while (reader.hasNext()) {
                final int event = reader.getEventType();

                //we are looking for the root mark
                if (event == START_ELEMENT) {
                    readFeatureTypes();

                    final GenericName name  = nameCache.get(reader.getName());
                    String id = "no-gml-id";
                    for(int i=0,n=reader.getAttributeCount();i<n;i++){
                        final QName attName = reader.getAttributeName(i);
                        //search and id property from any namespace
                        if("id".equals(attName.getLocalPart()) && attName.getNamespaceURI().startsWith(GML)){
                            id = reader.getAttributeValue(i);
                        }
                    }
                    final StringBuilder expectedFeatureType = new StringBuilder();

                    if (name.tip().toString().equals("FeatureCollection")) {
                        singleFeature = false;
                        return;

                    } else if (name.tip().toString().equals("Transaction")) {
                        throw new XMLStreamException("Transaction types are not supported as stream");

                    } else {
                        for (FeatureType ft : featureTypes) {
                            if (ft.getName().equals(name)) {
                                singleFeature = true;
                                next = (Feature) readFeature(ft);
                                type = next.getType();
                                return;
                            }
                            expectedFeatureType.append(ft.getName()).append('\n');
                        }
                    }

                    throw new IllegalArgumentException("The xml does not describe the same type of feature: \n " +
                                                       "Expected: " + expectedFeatureType.toString() + '\n' +
                                                       "But was: "  + name);
                }
                reader.next();
            }
        }

        @Override
        public FeatureType getFeatureType() {
            findNext();
            if(type==null){
                //collection is empty
                if(!featureTypes.isEmpty()){
                    //return the first feature type in the xsd
                    return featureTypes.get(0);
                }
            }
            return type;
        }

        @Override
        public boolean hasNext() throws FeatureStoreRuntimeException {
            findNext();
            return next != null;
        }

        @Override
        public Feature next() throws FeatureStoreRuntimeException {
            findNext();
            Feature t = next;
            next = null;
            return t;
        }

        private void findNext() throws FeatureStoreRuntimeException{
            if(next!=null || singleFeature) return;

            try{
                //read a feature in the collection
                while (reader.hasNext()) {
                    int event = reader.next();

                    //we are looking for the root mark
                    if (event == START_ELEMENT) {
                        final GenericName name = nameCache.get(reader.getName());

                        String fid = null;
                        if (reader.getAttributeCount() > 0) {
                            fid = reader.getAttributeValue(0);
                        }

                        if (name.tip().toString().equals("featureMember") || name.tip().toString().equals("featureMembers")) {
                            continue;

                        } else if (name.tip().toString().equals("boundedBy")) {
                            while (reader.hasNext()) {
                                event = reader.next();
                                if (event == START_ELEMENT) {
                                    break;
                                }
                            }
                            String srsName = null;
                            if (reader.getAttributeCount() > 0) {
                                srsName = reader.getAttributeValue(0);
                            }
                            final JTSEnvelope2D bounds = readBounds(srsName);

                        } else {
                            if (fid == null) {
                                LOGGER.info("Missing feature id : generating a random one");
                                fid = UUID.randomUUID().toString();
                            }

                            boolean find = false;
                            StringBuilder expectedFeatureType = new StringBuilder();
                            for (FeatureType ft : featureTypes) {
                                if (ft.getName().equals(name)) {
                                    next = (Feature) readFeature(ft);
                                    find = true;
                                    if(type==null) type = next.getType();
                                    return;
                                }
                                expectedFeatureType.append(ft.getName()).append('\n');
                            }

                            if (!find) {
                                throw new IllegalArgumentException("The xml does not describe the same type of feature: \n "
                                        + "Expected: " + expectedFeatureType.toString() + '\n'
                                        + "But was: " + name);
                            }
                        }
                    }
                }
            }catch(XMLStreamException ex){
                throw new FeatureStoreRuntimeException(ex);
            }
        }

        @Override
        public void close() {
            dispose();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not supported");
        }
    }

}
