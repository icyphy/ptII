/*
 * 
 */
package ptdb.kernel.bl.save.test;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.save.AttributesManager;

///////////////////////////////////////////////////////////////
//// TestAddingAttributesIntegration

/**
 * Integration test case for adding attributes BL and DB layers. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestAddingAttributesIntegration {

    //////////////////////////////////////////////////////////////////////
    ////                    public methods                            ////

    /**
     * Test the integration of fetching user defined attributes from the 
     * database. 
     * 
     * @exception DBExecutionException Thrown if there is error occurs during
     * the execution of fetching attributes. 
     * @exception DBConnectionException Thrown if meeting problem to get the 
     * db connection. 
     */
    @Test
    public void testGetDBAttributes() throws DBExecutionException,
            DBConnectionException {
        AttributesManager attributesManager = new AttributesManager();

        List<XMLDBAttribute> attributes = attributesManager.getDBAttributes();

        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            XMLDBAttribute xmldbAttribute = (XMLDBAttribute) iterator.next();
            Assert.assertNotNull(xmldbAttribute);
        }

    }

    /**
     * Test the work flow of creating, updating and deleting an attribute. 
     * 
     * @exception DBConnectionException Thrown if there is error occurs during
     * the execution for attributes. 
     * @exception DBExecutionException Thrown if meeting problem to get the 
     * db connection.
     */
    @Test
    public void testCreateUpdateDeleteAttributes()
            throws DBConnectionException, DBExecutionException {

        AttributesManager attributesManager = new AttributesManager();

        // Test creating the attributes. 
        XMLDBAttribute newAttribute = new XMLDBAttribute(
                "New Attribute Name 2", XMLDBAttribute.ATTRIBUTE_TYPE_STRING);

        attributesManager.createAttribute(newAttribute);

        List<XMLDBAttribute> attributes = attributesManager.getDBAttributes();

        boolean containsFlag = false;

        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            XMLDBAttribute xmldbAttribute = (XMLDBAttribute) iterator.next();
            if (xmldbAttribute.getAttributeName().equals(
                    newAttribute.getAttributeName())) {
                newAttribute = xmldbAttribute;
                containsFlag = true;
                break;
            }
        }

        Assert.assertTrue(containsFlag);

        // Test updating
        newAttribute.setAttributeName("updated name 2");
        newAttribute.setAttributeType(XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN);

        attributesManager.updateAttribute(newAttribute);

        attributes = attributesManager.getDBAttributes();
        containsFlag = false;

        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            XMLDBAttribute xmldbAttribute = (XMLDBAttribute) iterator.next();

            if (xmldbAttribute.getAttributeName().equals(
                    newAttribute.getAttributeName())
                    && xmldbAttribute.getAttributeType().equals(
                            newAttribute.getAttributeType())) {
                newAttribute = xmldbAttribute;
                containsFlag = true;
                break;
            }
        }

        Assert.assertTrue(containsFlag);

        // Test deleting. 
        attributesManager.deleteAttribute(newAttribute);

        attributes = attributesManager.getDBAttributes();
        containsFlag = false;

        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            XMLDBAttribute xmldbAttribute = (XMLDBAttribute) iterator.next();

            if (xmldbAttribute.getAttributeName().equals(
                    newAttribute.getAttributeName())) {
                containsFlag = true;
                break;
            }
        }

        Assert.assertFalse(containsFlag);
    }

}
