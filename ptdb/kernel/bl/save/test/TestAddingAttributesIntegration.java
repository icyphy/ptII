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

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.save.AttributesManager;

///////////////////////////////////////////////////////////////////
//// TestAddingAttributesIntegration

/**
 * Integration test case for adding attributes BL and DB layers.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestAddingAttributesIntegration {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
