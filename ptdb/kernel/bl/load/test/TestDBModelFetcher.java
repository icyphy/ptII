/*
@Copyright (c) 2010-2011 The Regents of the University of California.
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
package ptdb.kernel.bl.load.test;

import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestDBModelFetcher

/**
* This JUnit tests that, assuming the Database layer is correct,
* load() not return null.
*
* @author Lyle Holsinger
* @since Ptolemy II 10.0
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DBModelFetcher.class, DBConnectorFactory.class,
        GetModelTask.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestDBModelFetcher {

    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoad() throws Exception {

        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelTask getModelTaskMock = PowerMock
                .createMock(GetModelTask.class);
        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBConnectionMock);
        PowerMock.expectNew(GetModelTask.class, loadModel).andReturn(
                getModelTaskMock);
        EasyMock.expect(
                dBConnectionMock.executeGetCompleteModelTask(getModelTaskMock))
                .andReturn(modelMock);

        dBConnectionMock.closeConnection();

        //Execute the test.  Verify that, load does not return null if the database layer is mocked.
        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = DBModelFetcher.load(loadModel);
        assertNotNull(dbModel);

        PowerMock.verifyAll();

    }

    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoadUsingId() throws Exception {

        String modelId = "model1";
        String modelName = null;

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelTask getModelTaskMock = PowerMock
                .createMock(GetModelTask.class);
        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBConnectionMock);
        PowerMock.expectNew(GetModelTask.class, modelName, modelId).andReturn(
                getModelTaskMock);
        EasyMock.expect(
                dBConnectionMock.executeGetCompleteModelTask(getModelTaskMock))
                .andReturn(modelMock);

        dBConnectionMock.closeConnection();

        //Execute the test.  Verify that, load does not return null if the database layer is mocked.
        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = DBModelFetcher.loadUsingId(modelId);
        assertNotNull(dbModel);

        PowerMock.verifyAll();

    }
}
