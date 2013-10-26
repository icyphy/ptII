/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
/*
 *
 */
package ptdb.kernel.database.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.RebuildReferenceFile;

///////////////////////////////////////////////////////////////
//// TestRebuildReferenceFile

/**
 * Test class RebuildReferenceFile.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@PrepareForTest({ TestRebuildReferenceFile.class, RebuildReferenceFile.class })
@RunWith(PowerMockRunner.class)
public class TestRebuildReferenceFile {

    /**
     * @exception java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @exception java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @exception java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @exception java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link ptdb.kernel.database.RebuildReferenceFile#RebuildReferenceFile(ptdb.common.dto.DBConnectionParameters)}.
     */
    @Test
    public void testRebuildReferenceFile() {

        DBConnectionParameters dbConnectionParameters = DBConnectorFactory
                .getDBConnectionParameters();
        try {
            new RebuildReferenceFile(dbConnectionParameters);

        } catch (DBConnectionException e) {
            fail("Failed with exception - " + e.getMessage());
        }
    }

    /**
     * Test method for {@link ptdb.kernel.database.RebuildReferenceFile#main(java.lang.String[])}.
     * @exception Exception
     */
    @Test
    public void testMain() throws Exception {
        try {

            BufferedReader mockBufferedReader = PowerMock
                    .createMock(BufferedReader.class);
            InputStreamReader mockInputStreamReader = PowerMock
                    .createMock(InputStreamReader.class);

            PowerMock.expectNew(InputStreamReader.class, System.in).andReturn(
                    mockInputStreamReader);
            PowerMock.expectNew(BufferedReader.class, mockInputStreamReader)
                    .andReturn(mockBufferedReader);

            EasyMock.expect(mockBufferedReader.readLine()).andReturn("Y");
            mockBufferedReader.close();
            PowerMock.replay(InputStreamReader.class, BufferedReader.class,
                    mockBufferedReader, mockInputStreamReader);

            RebuildReferenceFile.main(new String[1]);
            PowerMock.verify();

        } catch (IOException e) {
            fail("Failed with IO Exception - " + e.getMessage());
        } catch (DBConnectionException e) {
            fail("Failed with DBConnectionException - " + e.getMessage());
        } catch (DBExecutionException e) {
            fail("Failed with DBExecutionException - " + e.getMessage());
        } catch (XMLDBModelParsingException e) {
            fail("Failed with XMLDBModelParsingException - " + e.getMessage());
        }
    }
}
