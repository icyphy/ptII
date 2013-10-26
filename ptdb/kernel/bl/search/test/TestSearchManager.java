/*
@Copyright (c) 2010-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/

package ptdb.kernel.bl.search.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.PTDBGenericAttribute;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.bl.search.AttributeSearcher;
import ptdb.kernel.bl.search.CommandSearcher;
import ptdb.kernel.bl.search.HierarchyFetcher;
import ptdb.kernel.bl.search.NameSearcher;
import ptdb.kernel.bl.search.PatternMatchGraphSearcher;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptdb.kernel.bl.search.XQueryGraphSearcher;
import ptdb.kernel.database.DBConnection;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.MatchResultRecorder;

///////////////////////////////////////////////////////////////////
//// TestSearchManager

/**
 * JUnit test case for class TestSearchManager.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SearchManager.class, AttributeSearcher.class,
        NameSearcher.class, CommandSearcher.class, XQueryGraphSearcher.class,
        PatternMatchGraphSearcher.class, HierarchyFetcher.class,
        SearchResultBuffer.class, GraphMatcher.class })
@SuppressStaticInitializationFor({ "ptdb.common.util.DBConnectorFactory",
        "ptdb.kernel.bl.load.DBModelFetcher" })
public class TestSearchManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Test the search() method in the case when all the search criteria are
     * set, and results returned smoothly.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearch() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //        attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);
        searchCriteria.setModelName("Test Model name");

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        Pattern patternMock = PowerMock.createMock(Pattern.class);
        dbGraphSearchCriteria.setPattern(patternMock);

        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
        componentEntities.add(new ComponentEntity());

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromAttributes.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        // Testing in name searcher.
        ModelNameSearchTask modelNameSearchTaskMock = PowerMock
                .createMockAndExpectNew(ModelNameSearchTask.class,
                        searchCriteria.getModelName());

        ArrayList<XMLDBModel> resultsFromNameSearcher = new ArrayList<XMLDBModel>();
        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromNameSearcher.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeModelNameSearchTask(modelNameSearchTaskMock))
                .andReturn(resultsFromNameSearcher);

        // Testing in command searcher.
        // To add later.

        // Testing in XQuery searcher.

        GraphSearchTask graphSearchTaskMock = PowerMock
                .createMockAndExpectNew(GraphSearchTask.class);

        graphSearchTaskMock.setGraphSearchCriteria(searchCriteria
                .getDBGraphSearchCriteria());

        ArrayList<XMLDBModel> resultsFromXQuery = new ArrayList<XMLDBModel>();

        for (int i = 5; i < 15; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromXQuery.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeGraphSearchTask(graphSearchTaskMock))
                .andReturn(resultsFromXQuery);

        // Testing in GraphPatternMatchingSearch.
        GraphMatcher graphMatcherMock = PowerMock
                .createMockAndExpectNew(GraphMatcher.class);

        MoMLParser parserMock = PowerMock
                .createMockAndExpectNew(MoMLParser.class);

        mockStatic(DBModelFetcher.class);

        ArrayList<XMLDBModel> patternMatchInitialModels = new ArrayList<XMLDBModel>();
        ArrayList<XMLDBModel> passModels = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {

            passModels.add(resultsFromXQuery.get(i));

            patternMatchInitialModels.add(new XMLDBModel("model_new" + i));

        }

        expect(DBModelFetcher.load(passModels)).andReturn(
                patternMatchInitialModels);

        ArrayList<XMLDBModel> resultsFromHierarchy = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {
            CompositeEntity namedObjMock = PowerMock
                    .createMock(CompositeEntity.class);

            parserMock.resetAll();

            expect(
                    parserMock.parse(patternMatchInitialModels.get(i)
                            .getModel())).andReturn(namedObjMock);

            MatchResultRecorder matchResultRecorderMock = PowerMock
                    .createMockAndExpectNew(MatchResultRecorder.class);

            graphMatcherMock.setMatchCallback(matchResultRecorderMock);

            expect(graphMatcherMock.match(patternMock, namedObjMock))
                    .andReturn(true);

            ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>();
            matchResults.add(new MatchResult());

            expect(matchResultRecorderMock.getResults())
                    .andReturn(matchResults);

            // Testing in hierarchy fetcher.

            FetchHierarchyTask fetchHierarchyTaskMock = PowerMock
                    .createMockAndExpectNew(FetchHierarchyTask.class);

            ArrayList<XMLDBModel> expectedResults = new ArrayList<XMLDBModel>();

            expectedResults.add(patternMatchInitialModels.get(i));

            fetchHierarchyTaskMock.setModelsList(expectedResults);

            for (int j = 15; j < 25; j++) {
                XMLDBModel xmldbModel = new XMLDBModel("model" + j);
                resultsFromHierarchy.add(xmldbModel);
            }

            expect(
                    dbConnectionAttributeMock
                            .executeFetchHierarchyTask(fetchHierarchyTaskMock))
                    .andReturn(resultsFromHierarchy);

        }

        dbConnectionAttributeMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertEquals(resultsFromHierarchy, searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case when DBConnectionException
     * is thrown during the searching.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchDBConnectionExecution() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //            attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        dbGraphSearchCriteria
                .setComponentEntitiesList(new ArrayList<ComponentEntity>());
        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        mockStatic(DBConnectorFactory.class);

        // DBConnectionException is thrown.
        expect(DBConnectorFactory.getSyncConnection(false)).andThrow(
                new DBConnectionException("testing"));

        // Start the testing.
        PowerMock.replayAll();

        try {
            searchManager.search(searchCriteria, searchResultBuffer);
        } catch (DBConnectionException e) {

        }

        assertNull(searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case when DBExecutionException
     * is thrown during the searching.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchDBExecutionExecution() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //        attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        dbGraphSearchCriteria
                .setComponentEntitiesList(new ArrayList<ComponentEntity>());
        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        // DBExecutionException is thrown.
        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andThrow(new DBExecutionException("testing"));

        dbConnectionAttributeMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        try {
            searchManager.search(searchCriteria, searchResultBuffer);
        } catch (DBExecutionException e) {

        }

        assertNull(searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case when there is no attribute
     * search criteria.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchNoAttributeCriteria() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        Pattern patternMock = PowerMock.createMock(Pattern.class);
        dbGraphSearchCriteria.setPattern(patternMock);

        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
        componentEntities.add(new ComponentEntity());

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        mockStatic(DBConnectorFactory.class);

        //Testing in XQuery searcher.
        DBConnection dbConnectionXQueryMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionXQueryMock);

        GraphSearchTask graphSearchTaskMock = PowerMock
                .createMockAndExpectNew(GraphSearchTask.class);

        graphSearchTaskMock.setGraphSearchCriteria(searchCriteria
                .getDBGraphSearchCriteria());

        ArrayList<XMLDBModel> resultsFromXQuery = new ArrayList<XMLDBModel>();

        for (int i = 5; i < 15; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);

            resultsFromXQuery.add(xmldbModel);
        }

        Collections.sort(resultsFromXQuery);

        expect(
                dbConnectionXQueryMock
                        .executeGraphSearchTask(graphSearchTaskMock))
                .andReturn(resultsFromXQuery);

        // Testing in GraphPatternMatchingSearch.
        GraphMatcher graphMatcherMock = PowerMock
                .createMockAndExpectNew(GraphMatcher.class);

        MoMLParser parserMock = PowerMock
                .createMockAndExpectNew(MoMLParser.class);

        mockStatic(DBModelFetcher.class);

        ArrayList<XMLDBModel> patternMatchInitialModels = new ArrayList<XMLDBModel>();
        ArrayList<XMLDBModel> passModels = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {

            passModels.add(resultsFromXQuery.get(i));

            patternMatchInitialModels.add(new XMLDBModel("model_new" + i));

        }

        expect(DBModelFetcher.load(passModels)).andReturn(
                patternMatchInitialModels);

        ArrayList<XMLDBModel> resultsFromHierarchy = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {
            CompositeEntity namedObjMock = PowerMock
                    .createMock(CompositeEntity.class);

            parserMock.resetAll();

            expect(
                    parserMock.parse(patternMatchInitialModels.get(i)
                            .getModel())).andReturn(namedObjMock);

            MatchResultRecorder matchResultRecorderMock = PowerMock
                    .createMockAndExpectNew(MatchResultRecorder.class);

            graphMatcherMock.setMatchCallback(matchResultRecorderMock);

            expect(graphMatcherMock.match(patternMock, namedObjMock))
                    .andReturn(true);

            ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>();
            matchResults.add(new MatchResult());

            expect(matchResultRecorderMock.getResults())
                    .andReturn(matchResults);

            // Testing in hierarchy fetcher.

            FetchHierarchyTask fetchHierarchyTaskMock = PowerMock
                    .createMockAndExpectNew(FetchHierarchyTask.class);

            ArrayList<XMLDBModel> expectedResults = new ArrayList<XMLDBModel>();

            expectedResults.add(patternMatchInitialModels.get(i));

            fetchHierarchyTaskMock.setModelsList(expectedResults);

            for (int j = 15; j < 25; j++) {
                XMLDBModel xmldbModel = new XMLDBModel("model" + j);
                resultsFromHierarchy.add(xmldbModel);
            }

            expect(
                    dbConnectionXQueryMock
                            .executeFetchHierarchyTask(fetchHierarchyTaskMock))
                    .andReturn(resultsFromHierarchy);

        }

        // Second batch.
        passModels = new ArrayList<XMLDBModel>();
        patternMatchInitialModels = new ArrayList<XMLDBModel>();

        for (int i = 5; i < 10; i++) {

            passModels.add(resultsFromXQuery.get(i));

            patternMatchInitialModels.add(new XMLDBModel("model_new" + i));

        }

        expect(DBModelFetcher.load(passModels)).andReturn(
                patternMatchInitialModels);

        for (int i = 0; i < 5; i++) {
            CompositeEntity namedObjMock = PowerMock
                    .createMock(CompositeEntity.class);

            parserMock.resetAll();

            expect(
                    parserMock.parse(patternMatchInitialModels.get(i)
                            .getModel())).andReturn(namedObjMock);

            MatchResultRecorder matchResultRecorderMock = PowerMock
                    .createMockAndExpectNew(MatchResultRecorder.class);

            graphMatcherMock.setMatchCallback(matchResultRecorderMock);

            expect(graphMatcherMock.match(patternMock, namedObjMock))
                    .andReturn(true);

            ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>();
            matchResults.add(new MatchResult());

            expect(matchResultRecorderMock.getResults())
                    .andReturn(matchResults);

            // Testing in hierarchy fetcher.

            FetchHierarchyTask fetchHierarchyTaskMock = PowerMock
                    .createMockAndExpectNew(FetchHierarchyTask.class);

            ArrayList<XMLDBModel> expectedResults = new ArrayList<XMLDBModel>();

            expectedResults.add(patternMatchInitialModels.get(i));
            fetchHierarchyTaskMock.setModelsList(expectedResults);

            for (int j = 15; j < 25; j++) {
                XMLDBModel xmldbModel = new XMLDBModel("model" + j);
                resultsFromHierarchy.add(xmldbModel);
            }

            expect(
                    dbConnectionXQueryMock
                            .executeFetchHierarchyTask(fetchHierarchyTaskMock))
                    .andReturn(resultsFromHierarchy);

        }

        dbConnectionXQueryMock.closeConnection();

        // Start testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertEquals(resultsFromHierarchy, searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case when there is no result returned
     *  in the attribute searching.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchNoAttributeResult() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //            attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        dbGraphSearchCriteria
                .setComponentEntitiesList(new ArrayList<ComponentEntity>());
        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        // No result returned.
        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        dbConnectionAttributeMock.closeConnection();

        // In command searcher, no match is called.

        // Start the testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

        assertTrue(searchResultBuffer.isWholeSearchDone());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case when no model name criteria is
     * specified.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchNoModelNameCriteria() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //        attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        Pattern patternMock = PowerMock.createMock(Pattern.class);
        dbGraphSearchCriteria.setPattern(patternMock);

        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
        componentEntities.add(new ComponentEntity());

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromAttributes.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        // Testing in name searcher.
        // Do nothing.

        // Testing in XQuery searcher.

        GraphSearchTask graphSearchTaskMock = PowerMock
                .createMockAndExpectNew(GraphSearchTask.class);

        graphSearchTaskMock.setGraphSearchCriteria(searchCriteria
                .getDBGraphSearchCriteria());

        ArrayList<XMLDBModel> resultsFromXQuery = new ArrayList<XMLDBModel>();

        for (int i = 5; i < 15; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromXQuery.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeGraphSearchTask(graphSearchTaskMock))
                .andReturn(resultsFromXQuery);

        // Testing in GraphPatternMatchingSearch.
        GraphMatcher graphMatcherMock = PowerMock
                .createMockAndExpectNew(GraphMatcher.class);

        MoMLParser parserMock = PowerMock
                .createMockAndExpectNew(MoMLParser.class);

        mockStatic(DBModelFetcher.class);

        ArrayList<XMLDBModel> patternMatchInitialModels = new ArrayList<XMLDBModel>();
        ArrayList<XMLDBModel> passModels = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {

            passModels.add(resultsFromXQuery.get(i));

            patternMatchInitialModels.add(new XMLDBModel("model_new" + i));

        }

        expect(DBModelFetcher.load(passModels)).andReturn(
                patternMatchInitialModels);

        ArrayList<XMLDBModel> resultsFromHierarchy = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 5; i++) {
            CompositeEntity namedObjMock = PowerMock
                    .createMock(CompositeEntity.class);
            parserMock.resetAll();
            expect(
                    parserMock.parse(patternMatchInitialModels.get(i)
                            .getModel())).andReturn(namedObjMock);

            MatchResultRecorder matchResultRecorderMock = PowerMock
                    .createMockAndExpectNew(MatchResultRecorder.class);

            graphMatcherMock.setMatchCallback(matchResultRecorderMock);

            expect(graphMatcherMock.match(patternMock, namedObjMock))
                    .andReturn(true);

            ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>();
            matchResults.add(new MatchResult());

            expect(matchResultRecorderMock.getResults())
                    .andReturn(matchResults);

            // Testing in hierarchy fetcher.

            FetchHierarchyTask fetchHierarchyTaskMock = PowerMock
                    .createMockAndExpectNew(FetchHierarchyTask.class);

            ArrayList<XMLDBModel> expectedResults = new ArrayList<XMLDBModel>();

            expectedResults.add(patternMatchInitialModels.get(i));

            fetchHierarchyTaskMock.setModelsList(expectedResults);

            for (int j = 15; j < 25; j++) {
                XMLDBModel xmldbModel = new XMLDBModel("model" + j);
                resultsFromHierarchy.add(xmldbModel);
            }

            expect(
                    dbConnectionAttributeMock
                            .executeFetchHierarchyTask(fetchHierarchyTaskMock))
                    .andReturn(resultsFromHierarchy);

        }

        dbConnectionAttributeMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertEquals(resultsFromHierarchy, searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case there is no result found in
     * the model name searcher.
     *
     * @exception Exception Thrown by PowerMock if error occurs during the
     *  execution of test cases.
     */
    @Test
    public void testSearchNoModelNameResult() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //            attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);
        searchCriteria.setModelName("Some weird name");

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
        componentEntities.add(new ComponentEntity());

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromAttributes.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        // Testing in name searcher.
        ModelNameSearchTask modelNameSearchTaskMock = PowerMock
                .createMockAndExpectNew(ModelNameSearchTask.class,
                        searchCriteria.getModelName());

        expect(
                dbConnectionAttributeMock
                        .executeModelNameSearchTask(modelNameSearchTaskMock))
                .andReturn(new ArrayList<XMLDBModel>());

        // Testing in XQuery searcher.

        dbConnectionAttributeMock.closeConnection();

        // Testing in hierarchy fetcher, found no match returned.

        // Start testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case there is no criteria for
     *  graph search.
     *
     * @exception Exception Thrown by PowerMock during the execution of test
     *  cases.
     */
    @Test
    public void testSearchNoXQueryGraphCriteria() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //        attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromAttributes.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        // Testing in hierarchy fetcher.
        FetchHierarchyTask fetchHierarchyTaskMock = PowerMock
                .createMockAndExpectNew(FetchHierarchyTask.class);

        fetchHierarchyTaskMock.setModelsList(resultsFromAttributes);

        ArrayList<XMLDBModel> resultsFromHierarchy = new ArrayList<XMLDBModel>();

        for (int i = 15; i < 25; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromHierarchy.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeFetchHierarchyTask(fetchHierarchyTaskMock))
                .andReturn(resultsFromHierarchy);

        dbConnectionAttributeMock.closeConnection();

        // Start testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertEquals(resultsFromHierarchy, searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

    /**
     * Test the search() method in the case there is no result found in
     * the XQuery graph searcher.
     *
     * @exception Exception Thrown by PowerMock if error occurs during the
     *  execution of test cases.
     */
    @Test
    public void testSearchNoXQueryGraphResult() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        StringParameter attribute = new StringParameter(new NamedObj(),
                "test name");
        //            attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        DBGraphSearchCriteria dbGraphSearchCriteria = new DBGraphSearchCriteria();

        ArrayList<ComponentEntity> componentEntities = new ArrayList<ComponentEntity>();
        componentEntities.add(new ComponentEntity());

        dbGraphSearchCriteria.setComponentEntitiesList(componentEntities);

        dbGraphSearchCriteria.setPortsList(new ArrayList<Port>());
        dbGraphSearchCriteria.setRelationsList(new ArrayList<Relation>());

        searchCriteria.setDBGraphSearchCriteria(dbGraphSearchCriteria);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        // Testing in attribute searcher.
        DBConnection dbConnectionAttributeMock = PowerMock
                .createMock(DBConnection.class);

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionAttributeMock);

        AttributeSearchTask attributeSearchTaskMock = PowerMock
                .createMockAndExpectNew(AttributeSearchTask.class);

        ArrayList<PTDBGenericAttribute> ptdbGenericAttributes = new ArrayList<PTDBGenericAttribute>();
        PTDBGenericAttribute genericAttribute = new PTDBGenericAttribute(
                attribute.getName());
        //        genericAttribute.setAttributeName(attribute.getName());
        genericAttribute.addValue(attribute.getExpression());
        genericAttribute.setClassName(attribute.getClassName());
        ptdbGenericAttributes.add(genericAttribute);

        attributeSearchTaskMock.setAttributesList(ptdbGenericAttributes);

        ArrayList<XMLDBModel> resultsFromAttributes = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 10; i++) {
            XMLDBModel xmldbModel = new XMLDBModel("model" + i);
            resultsFromAttributes.add(xmldbModel);
        }

        expect(
                dbConnectionAttributeMock
                        .executeAttributeSearchTask(attributeSearchTaskMock))
                .andReturn(resultsFromAttributes);

        // Testing in XQuery searcher.

        GraphSearchTask graphSearchTaskMock = PowerMock
                .createMockAndExpectNew(GraphSearchTask.class);

        graphSearchTaskMock.setGraphSearchCriteria(searchCriteria
                .getDBGraphSearchCriteria());

        ArrayList<XMLDBModel> resultsFromXQuery = new ArrayList<XMLDBModel>();

        expect(
                dbConnectionAttributeMock
                        .executeGraphSearchTask(graphSearchTaskMock))
                .andReturn(resultsFromXQuery);

        dbConnectionAttributeMock.closeConnection();

        // Testing in hierarchy fetcher, found no match returned.

        // Start testing.
        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

        PowerMock.verifyAll();

    }

}
