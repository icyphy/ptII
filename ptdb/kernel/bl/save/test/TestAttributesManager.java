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
/*
 *
 */
package ptdb.kernel.bl.save.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.save.AttributesManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestAttributesManager

/**
 * JUnit test case for AttributesManager class.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AttributesManager.class })
@SuppressStaticInitializationFor({ "ptdb.common.util.DBConnectorFactory" })
public class TestAttributesManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Test the createAttribute() method in the case when the given attribute
     * is created in the database correctly.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testCreateAttribute() throws Exception {
        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING);

        CreateAttributeTask createAttributeTaskMock = PowerMock
                .createMockAndExpectNew(CreateAttributeTask.class,
                        xmldbAttribute);

        XMLDBAttribute updateAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "Author_999999999");

        expect(
                dbConnectionMock
                        .executeCreateAttributeTask(createAttributeTaskMock))
                .andReturn(updateAttribute);

        dbConnectionMock.commitConnection();

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        assertEquals(updateAttribute,
                attributesManager.createAttribute(xmldbAttribute));

        PowerMock.verifyAll();

    }

    /**
     * Test the createAttribute() method in the case when DBConnectionException
     *  is thrown.
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testCreateAttributeWithDBConnectionException() throws Exception {

        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andThrow(
                new DBConnectionException("Test DBConnectionException."));

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING);

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        try {
            attributesManager.createAttribute(xmldbAttribute);
        } catch (DBConnectionException e) {

        }

        PowerMock.verifyAll();

    }

    /**
     * Test the createAttribute() method in the case when DBExecutionException
     *  is thrown.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testCreateAttributeWithDBExecutionException() throws Exception {

        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING);

        CreateAttributeTask createAttributeTaskMock = PowerMock
                .createMockAndExpectNew(CreateAttributeTask.class,
                        xmldbAttribute);

        expect(
                dbConnectionMock
                        .executeCreateAttributeTask(createAttributeTaskMock))
                .andThrow(new DBExecutionException("Test DBExecutionException"));

        dbConnectionMock.abortConnection();
        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        try {
            attributesManager.createAttribute(xmldbAttribute);
        } catch (DBExecutionException e) {

        }

        PowerMock.verifyAll();

    }

    /**
     * Test the deleteAttribute() method in the case when the given attribute
     *  is deleted from the database successfully.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testDeleteAttribute() throws Exception {
        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "Test_8888888888");

        DeleteAttributeTask deleteAttributeTaskMock = PowerMock
                .createMockAndExpectNew(DeleteAttributeTask.class,
                        xmldbAttribute);

        dbConnectionMock.executeDeleteAttributeTask(deleteAttributeTaskMock);

        dbConnectionMock.commitConnection();

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        attributesManager.deleteAttribute(xmldbAttribute);

        PowerMock.verifyAll();
    }

    /**
     * Test the deleteAttribute() method in the case when DBExecutionException
     *  is thrown.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testDeleteAttributeWithDBExecutionException() throws Exception {
        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "Test_8888888888");

        DeleteAttributeTask deleteAttributeTaskMock = PowerMock
                .createMockAndExpectNew(DeleteAttributeTask.class,
                        xmldbAttribute);

        dbConnectionMock.executeDeleteAttributeTask(deleteAttributeTaskMock);
        PowerMock.expectLastCall().andThrow(
                new DBExecutionException("Testing DBExecutionException."));

        dbConnectionMock.abortConnection();

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        try {
            attributesManager.deleteAttribute(xmldbAttribute);
        } catch (DBExecutionException e) {

        }

        PowerMock.verifyAll();
    }

    /**
     * Test the getDBAttributes() method in the case when the attributes
     * are returned successfully.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testGetDBAttributes() throws Exception {

        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionMock);

        GetAttributesTask getAttributesTaskMock = PowerMock
                .createMockAndExpectNew(GetAttributesTask.class);
        ArrayList<XMLDBAttribute> returnedAttributes = new ArrayList<XMLDBAttribute>();

        for (int i = 0; i < 10; i++) {
            XMLDBAttribute xmldbAttribute = new XMLDBAttribute("attribute" + i,
                    XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "attributeId_" + i);
            returnedAttributes.add(xmldbAttribute);
        }

        expect(dbConnectionMock.executeGetAttributesTask(getAttributesTaskMock))
                .andReturn(returnedAttributes);

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        assertEquals(returnedAttributes, attributesManager.getDBAttributes());

        PowerMock.verifyAll();

    }

    /**
     * Test the getDBAttributes() method in the case when the
     * DBExecutionException is thrown.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testGetDBAttributesWithDBExecutionException() throws Exception {

        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionMock);

        GetAttributesTask getAttributesTaskMock = PowerMock
                .createMockAndExpectNew(GetAttributesTask.class);

        expect(dbConnectionMock.executeGetAttributesTask(getAttributesTaskMock))
                .andThrow(
                        new DBExecutionException(
                                "Testing DBExecutionException."));

        dbConnectionMock.abortConnection();
        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        try {
            attributesManager.getDBAttributes();
        } catch (DBExecutionException e) {

        }

        PowerMock.verifyAll();

    }

    /**
     * Test the method updateAttribute() method in the case when the given
     * attribute is updated in the database successfully.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testUpdateAttribute() throws Exception {

        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "Test_8888888888");

        UpdateAttributeTask updateAttributeTaskMock = PowerMock
                .createMockAndExpectNew(UpdateAttributeTask.class,
                        xmldbAttribute);

        dbConnectionMock.executeUpdateAttributeTask(updateAttributeTaskMock);

        dbConnectionMock.commitConnection();

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        attributesManager.updateAttribute(xmldbAttribute);

        PowerMock.verifyAll();
    }

    /**
     * Test the updateAttribute() method in the case when the
     *  DBExecutionException is thrown.
     *
     * @exception Exception Thrown by PowerMock if error occurs in the testing.
     */
    @Test
    public void testUpdateAttributeWithDBExecutionException() throws Exception {

        mockStatic(DBConnectorFactory.class);

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dbConnectionMock);

        XMLDBAttribute xmldbAttribute = new XMLDBAttribute("Author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "Test_8888888888");

        UpdateAttributeTask updateAttributeTaskMock = PowerMock
                .createMockAndExpectNew(UpdateAttributeTask.class,
                        xmldbAttribute);

        dbConnectionMock.executeUpdateAttributeTask(updateAttributeTaskMock);
        PowerMock.expectLastCall().andThrow(
                new DBExecutionException("Testing DBExecutionException."));

        dbConnectionMock.abortConnection();

        dbConnectionMock.closeConnection();

        // Start the testing.
        PowerMock.replayAll();

        AttributesManager attributesManager = new AttributesManager();

        try {
            attributesManager.updateAttribute(xmldbAttribute);
        } catch (DBExecutionException e) {

        }

        PowerMock.verifyAll();
    }

}
