/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
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


package org.geotoolkit.data.query;

import org.junit.Test;
import static org.junit.Assert.*;

import org.geotoolkit.feature.FeatureExt;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.referencing.CommonCRS;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.memory.MemoryFeatureStore;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.util.NamesExt;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.util.GenericName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.apache.sis.internal.feature.AttributeConvention;

/**
 * Test query builder.
 *
 * @author Johann Sorel (Geomatys)
 */
public class QueryTest {

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);
    private static final double DELTA = 0.00001;


    private final MemoryFeatureStore store = new MemoryFeatureStore();
    private final GenericName name1;
    private final GenericName name2;

    private final String fid_1_0;
    private final String fid_1_1;
    private final String fid_1_2;
    private final String fid_1_3;
    private final String fid_1_4;
    private final String fid_2_0;
    private final String fid_2_1;
    private final String fid_2_2;
    private final String fid_2_3;
    private final String fid_2_4;
    private final String fid_2_5;

    public QueryTest() throws Exception {
        FeatureTypeBuilder builder = new FeatureTypeBuilder();

        //----------------------------------------------------------------------
        name1 = NamesExt.create("http://type1.com", "Type1");
        builder.setName(name1);
        builder.addAttribute(String.class).setName(AttributeConvention.IDENTIFIER_PROPERTY);
        builder.addAttribute(String.class).setName("http://type1.com", "att1");
        builder.addAttribute(Integer.class).setName("http://type1.com", "att2");
        final FeatureType sft1 = builder.build();
        store.createFeatureType(sft1);

        FeatureWriter fw = store.getFeatureWriter(QueryBuilder.filtered(name1.toString(),Filter.EXCLUDE));
        Feature sf = fw.next();
        sf.setPropertyValue("att1", "str1");
        sf.setPropertyValue("att2", 1);
        fw.write();
        fid_1_0 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att1", "str2");
        sf.setPropertyValue("att2", 2);
        fw.write();
        fid_1_1 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att1", "str3");
        sf.setPropertyValue("att2", 3);
        fw.write();
        fid_1_2 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att1", "str50");
        sf.setPropertyValue("att2", 50);
        fw.write();
        fid_1_3 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att1", "str51");
        sf.setPropertyValue("att2", 51);
        fw.write();
        fid_1_4 = FeatureExt.getId(sf).getID();

        fw.close();


        //----------------------------------------------------------------------
        name2 = NamesExt.create("http://type2.com", "Type2");
        builder = new FeatureTypeBuilder();
        builder.setName(name2);
        builder.addAttribute(String.class).setName(AttributeConvention.IDENTIFIER_PROPERTY);
        builder.addAttribute(Integer.class).setName("http://type2.com", "att3");
        builder.addAttribute(Double.class).setName("http://type2.com", "att4");
        final FeatureType sft2 = builder.build();
        store.createFeatureType(sft2);

        fw = store.getFeatureWriter(QueryBuilder.filtered(name2.toString(),Filter.EXCLUDE));
        sf = fw.next();
        sf.setPropertyValue("att3", 1);
        sf.setPropertyValue("att4", 10d);
        fw.write();
        fid_2_0 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att3", 2);
        sf.setPropertyValue("att4", 20d);
        fw.write();
        fid_2_1 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att3", 2);
        sf.setPropertyValue("att4", 30d);
        fw.write();
        fid_2_2 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att3", 3);
        sf.setPropertyValue("att4", 40d);
        fw.write();
        fid_2_3 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att3", 60);
        sf.setPropertyValue("att4", 60d);
        fw.write();
        fid_2_4 = FeatureExt.getId(sf).getID();

        sf = fw.next();
        sf.setPropertyValue("att3", 61);
        sf.setPropertyValue("att4", 61d);
        fw.write();
        fid_2_5 = FeatureExt.getId(sf).getID();

        fw.close();

    }

    /**
     * test static methods from querybuilder
     */
    @Test
    public void testStaticQueryBuilder() throws Exception {
        Query query = null;
        GenericName name = NamesExt.create("http://test.org", "testLocal");

        //test null values------------------------------------------------------
        try{
            QueryBuilder.all((GenericName)null);
            throw new Exception("We can not build a query without at least the type name.");
        }catch(NullPointerException ex){
            //ok
        }

        try{
            QueryBuilder.fids(null);
            throw new Exception("We can not build a query without at least the type name.");
        }catch(NullPointerException ex){
            //ok
        }

        try{
            QueryBuilder.filtered(null, Filter.EXCLUDE);
            throw new Exception("We can not build a query without at least the type name.");
        }catch(NullPointerException ex){
            //ok
        }

        try{
            QueryBuilder.sorted(null, new SortBy[]{FF.sort("att1", SortOrder.DESCENDING)});
            throw new Exception("We can not build a query without at least the type name.");
        }catch(NullPointerException ex){
            //ok
        }




        //all-------------------------------------------------------------------
        query = QueryBuilder.all(name);
        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.INCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertArrayEquals(query.getPropertyNames(), null);
        assertArrayEquals(query.getSortBy(), null);
        assertEquals(query.getStartIndex(), 0);

        //only ids--------------------------------------------------------------
        query = QueryBuilder.fids(name.toString());
        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.INCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertNotNull(query.getPropertyNames()); //must be an empty array, not null
        assertTrue(query.getPropertyNames().length == 1); //must have only one value
        assertArrayEquals(query.getSortBy(), null);
        assertEquals(query.getStartIndex(), 0);

        //only filter-----------------------------------------------------------
        query = QueryBuilder.filtered(name.toString(), Filter.EXCLUDE);
        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.EXCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertArrayEquals(query.getPropertyNames(), null);
        assertArrayEquals(query.getSortBy(), null);
        assertEquals(query.getStartIndex(), 0);

        //only sort by----------------------------------------------------------
        query = QueryBuilder.sorted(name.toString(), new SortBy[]{FF.sort("att1", SortOrder.DESCENDING)});
        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.INCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertArrayEquals(query.getPropertyNames(), null);
        assertNotNull(query.getSortBy());
        assertTrue(query.getSortBy().length == 1);
        assertEquals(query.getSortBy()[0], FF.sort("att1", SortOrder.DESCENDING));
        assertEquals(query.getStartIndex(), 0);

    }

    /**
     * test querybuilder
     */
    @Test
    public void testQueryBuilder() throws Exception {
        GenericName name = NamesExt.create("http://test.org", "testLocal");
        Query query = null;
        Query query2 = null;

        //test no parameters----------------------------------------------------
        QueryBuilder qb = new QueryBuilder();
        try{
            query = qb.buildQuery();
            throw new Exception("We can not build a query without at least the type name.");
        }catch(NullPointerException ex){
            //ok
        }

        //test all parameters---------------------------------------------------
        qb.setTypeName(name);
        qb.setCRS(CommonCRS.WGS84.normalizedGeographic());
        qb.setResolution(new double[]{45,31});
        qb.setFilter(Filter.EXCLUDE);
        qb.setMaxFeatures(10);
        qb.setProperties(new String[]{"att1","att2"});
        qb.setSortBy(new SortBy[]{FF.sort("att1", SortOrder.DESCENDING)});
        qb.setStartIndex(5);
        query = qb.buildQuery();

        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), CommonCRS.WGS84.normalizedGeographic());
        assertEquals(query.getResolution()[0], 45d,DELTA);
        assertEquals(query.getResolution()[1], 31d,DELTA);
        assertEquals(query.getFilter(), Filter.EXCLUDE);
        assertEquals(query.getMaxFeatures(), Integer.valueOf(10));
        assertEquals(query.getPropertyNames()[0], "att1");
        assertEquals(query.getPropertyNames()[1], "att2");
        assertEquals(query.getSortBy()[0], FF.sort("att1", SortOrder.DESCENDING));
        assertEquals(query.getStartIndex(), 5);

        query2 = query;

        //test reset------------------------------------------------------------
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.INCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertArrayEquals(query.getPropertyNames(), null);
        assertArrayEquals(query.getSortBy(), null);
        assertEquals(query.getStartIndex(), 0);

        //test copy-------------------------------------------------------------
        qb.copy(query2);
        query = qb.buildQuery();

        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), CommonCRS.WGS84.normalizedGeographic());
        assertEquals(query.getResolution()[0], 45d, DELTA);
        assertEquals(query.getResolution()[1], 31d, DELTA);
        assertEquals(query.getFilter(), Filter.EXCLUDE);
        assertEquals(query.getMaxFeatures(), Integer.valueOf(10));
        assertEquals(query.getPropertyNames()[0], "att1");
        assertEquals(query.getPropertyNames()[1], "att2");
        assertEquals(query.getSortBy()[0], FF.sort("att1", SortOrder.DESCENDING));
        assertEquals(query.getStartIndex(), 5);

        //test constructor with query-------------------------------------------
        qb = new QueryBuilder(query2);
        query = qb.buildQuery();

        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), CommonCRS.WGS84.normalizedGeographic());
        assertEquals(query.getResolution()[0], 45d, DELTA);
        assertEquals(query.getResolution()[1], 31d, DELTA);
        assertEquals(query.getFilter(), Filter.EXCLUDE);
        assertEquals(query.getMaxFeatures(), Integer.valueOf(10));
        assertEquals(query.getPropertyNames()[0], "att1");
        assertEquals(query.getPropertyNames()[1], "att2");
        assertEquals(query.getSortBy()[0], FF.sort("att1", SortOrder.DESCENDING));
        assertEquals(query.getStartIndex(), 5);

        //test constructor with name--------------------------------------------
        qb = new QueryBuilder(name.toString());
        query = qb.buildQuery();

        assertEquals(query.getTypeName(), name.toString());
        assertEquals(query.getCoordinateSystemReproject(), null);
        assertEquals(query.getResolution(), null);
        assertEquals(query.getFilter(), Filter.INCLUDE);
        assertEquals(query.getMaxFeatures(), null);
        assertArrayEquals(query.getPropertyNames(), null);
        assertArrayEquals(query.getSortBy(), null);
        assertEquals(query.getStartIndex(), 0);

    }

    /**
     * Test that cross featurestore queries works correctly.
     */
    @Test
    public void testInnerJoinQuery() throws Exception{
        final Session session = store.createSession(false);

        final QueryBuilder qb = new QueryBuilder();
        final Join join = new DefaultJoin(
                new DefaultSelector(session, name1.toString(), "s1"),
                new DefaultSelector(session, name2.toString(), "s2"),
                JoinType.INNER,
                FF.equals(FF.property("att2"), FF.property("att3")));
        qb.setSource(join);

        final Query query = qb.buildQuery();

        final FeatureCollection col = session.getFeatureCollection(query);

        FeatureIterator ite = col.iterator();
        Feature f = null;
        Feature c1 = null;
        Feature c2 = null;

        int count = 0;
        while(ite.hasNext()){
            count++;
            f = ite.next();
            if(FeatureExt.getId(f).getID().equals(fid_1_0 +" "+fid_2_0)){
                c1 = (Feature) f.getPropertyValue("s1");
                c2 = (Feature) f.getPropertyValue("s2");
                assertEquals("str1", c1.getProperty("att1").getValue());
                assertEquals(1, c1.getProperty("att2").getValue());
                assertEquals(1, c2.getProperty("att3").getValue());
                assertEquals(10d, c2.getProperty("att4").getValue());
            }else if(FeatureExt.getId(f).getID().equals(fid_1_1 +" "+fid_2_1)){
                c1 = (Feature) f.getPropertyValue("s1");
                c2 = (Feature) f.getPropertyValue("s2");
                assertEquals("str2", c1.getProperty("att1").getValue());
                assertEquals(2, c1.getProperty("att2").getValue());
                assertEquals(2, c2.getProperty("att3").getValue());
                assertEquals(20d, c2.getProperty("att4").getValue());
            }else if(FeatureExt.getId(f).getID().equals(fid_1_1 +" "+fid_2_2)){
                c1 = (Feature) f.getPropertyValue("s1");
                c2 = (Feature) f.getPropertyValue("s2");
                assertEquals("str2", c1.getProperty("att1").getValue());
                assertEquals(2, c1.getProperty("att2").getValue());
                assertEquals(2, c2.getProperty("att3").getValue());
                assertEquals(30d, c2.getProperty("att4").getValue());
            }else if(FeatureExt.getId(f).getID().equals(fid_1_2 +" "+fid_2_3)){
                c1 = (Feature) f.getPropertyValue("s1");
                c2 = (Feature) f.getPropertyValue("s2");
                assertEquals("str3", c1.getProperty("att1").getValue());
                assertEquals(3, c1.getProperty("att2").getValue());
                assertEquals(3, c2.getProperty("att3").getValue());
                assertEquals(40d, c2.getProperty("att4").getValue());
            }else{
                fail("unexpected feature");
            }
        }

        assertEquals("Was expecting 4 features.",4, count);
        ite.close();
    }

    /**
     * Test that cross featurestore queries works correctly.
     */
    @Test
    public void testOuterLeftQuery() throws Exception{
        final Session session = store.createSession(false);

        final QueryBuilder qb = new QueryBuilder();
        final Join join = new DefaultJoin(
                new DefaultSelector(session, name1.toString(), "s1"),
                new DefaultSelector(session, name2.toString(), "s2"),
                JoinType.LEFT_OUTER,
                FF.equals(FF.property("att2"), FF.property("att3")));
        qb.setSource(join);

        final Query query = qb.buildQuery();

        final FeatureCollection col = session.getFeatureCollection(query);
        final FeatureIterator ite = col.iterator();

        boolean foundStr1 = false;
        boolean foundStr20 = false;
        boolean foundStr21 = false;
        boolean foundStr3 = false;
        boolean foundStr50 = false;
        boolean foundStr51 = false;

        int count = 0;
        while(ite.hasNext()){
            final Feature f = ite.next();
            final Feature c1 = (Feature) f.getPropertyValue("s1");
            final Feature c2 = (Feature) f.getPropertyValue("s2");
            final String att1 = c1.getProperty("att1").getValue().toString();

            if(att1.equals("str1")){
                foundStr1 = true;
                assertEquals(c1.getProperty("att2").getValue(), 1);
                assertEquals(c2.getProperty("att3").getValue(), 1);
                assertEquals(c2.getProperty("att4").getValue(), 10d);
            }else if(att1.equals("str2")){
                assertEquals(c1.getProperty("att2").getValue(), 2);
                assertEquals(c2.getProperty("att3").getValue(), 2);
                double att4 = (Double)c2.getProperty("att4").getValue();
                if(att4 == 20d){
                    foundStr20 = true;
                }else if(att4 == 30d){
                    foundStr21 = true;
                }
            }else if(att1.equals("str3")){
                foundStr3 = true;
                assertEquals(c1.getProperty("att2").getValue(), 3);
                assertEquals(c2.getProperty("att3").getValue(), 3);
                assertEquals(c2.getProperty("att4").getValue(), 40d);
            }else if(att1.equals("str50")){
                foundStr50 = true;
                assertEquals(c1.getProperty("att2").getValue(), 50);
                assertNull(c2);
            }else if(att1.equals("str51")){
                foundStr51 = true;
                assertEquals(c1.getProperty("att2").getValue(), 51);
                assertNull(c2);
            }
            count++;
        }

        assertTrue(foundStr1);
        assertTrue(foundStr20);
        assertTrue(foundStr21);
        assertTrue(foundStr3);
        assertTrue(foundStr50);
        assertTrue(foundStr51);
        assertEquals(6, count);

        ite.close();
    }

    /**
     * Test that cross featurestore queries works correctly.
     */
    @Test
    public void testOuterRightQuery() throws Exception{
        final Session session = store.createSession(false);

        final QueryBuilder qb = new QueryBuilder();
        final Join join = new DefaultJoin(
                new DefaultSelector(session, name1.toString(), "s1"),
                new DefaultSelector(session, name2.toString(), "s2"),
                JoinType.RIGHT_OUTER,
                FF.equals(FF.property("att2"), FF.property("att3")));
        qb.setSource(join);

        final Query query = qb.buildQuery();

        final FeatureCollection col = session.getFeatureCollection(query);
        final FeatureIterator ite = col.iterator();

        boolean foundStr1 = false;
        boolean foundStr20 = false;
        boolean foundStr21 = false;
        boolean foundStr3 = false;
        boolean foundStr60 = false;
        boolean foundStr61 = false;

        int count = 0;
        while(ite.hasNext()){
            final Feature f = ite.next();
            final Feature c1 = (Feature) f.getPropertyValue("s1");
            final Feature c2 = (Feature) f.getPropertyValue("s2");

            if(c1 != null){
                final Object att1 = c1.getProperty("att1").getValue();
                if("str1".equals(att1)){
                    foundStr1 = true;
                    assertEquals(c1.getProperty("att2").getValue(), 1);
                    assertEquals(c2.getProperty("att3").getValue(), 1);
                    assertEquals(c2.getProperty("att4").getValue(), 10d);
                }else if("str2".equals(att1)){
                    assertEquals(c1.getProperty("att2").getValue(), 2);
                    assertEquals(c2.getProperty("att3").getValue(), 2);
                    double att4 = (Double)c2.getProperty("att4").getValue();
                    if(att4 == 20d){
                        foundStr20 = true;
                    }else if(att4 == 30d){
                        foundStr21 = true;
                    }
                }else if("str3".equals(att1)){
                    foundStr3 = true;
                    assertEquals(c1.getProperty("att2").getValue(), 3);
                    assertEquals(c2.getProperty("att3").getValue(), 3);
                    assertEquals(c2.getProperty("att4").getValue(), 40d);
                }
            }else{
                int att3 = (Integer)c2.getProperty("att3").getValue();

                if(att3 == 60){
                    assertEquals(c2.getProperty("att4").getValue(), 60d);
                    foundStr60 = true;
                }else if(att3 == 61){
                    assertEquals(c2.getProperty("att4").getValue(), 61d);
                    foundStr61 = true;
                }
            }

            count++;
        }

        assertTrue(foundStr1);
        assertTrue(foundStr20);
        assertTrue(foundStr21);
        assertTrue(foundStr3);
        assertTrue(foundStr60);
        assertTrue(foundStr61);
        assertEquals(6, count);

    }

}
