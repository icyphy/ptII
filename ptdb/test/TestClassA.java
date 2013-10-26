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
package ptdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClassA.class)
/**
 * TestClassA class.
 *
 * @author cxh
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class TestClassA {

    @Test
    public void testCreateDirectoryStructure() throws Exception {
        final String path = "directoryPath";
        File fileMock = PowerMock.createMock(File.class);

        ClassA tested = new ClassA();

        PowerMock.expectNew(File.class, path).andReturn(fileMock);

        EasyMock.expect(fileMock.exists()).andReturn(false);
        EasyMock.expect(fileMock.mkdirs()).andReturn(true);

        PowerMock.replay(fileMock, File.class);

        assertTrue(tested.createDirectoryStructure(path));

        PowerMock.verify(fileMock, File.class);
    }

    @Test
    public void testGetSunSign() throws Exception {
        ClassB mockClassB = PowerMock.createMock(ClassB.class);
        ClassA classA = new ClassA();

        PowerMock.expectNew(ClassB.class).andReturn(mockClassB);
        EasyMock.expect(mockClassB.getMonth("Jan")).andReturn(1);

        PowerMock.replayAll();

        String output = classA.getSunSign(1, "Jan");
        assertEquals("Capricorn", output);
        PowerMock.verifyAll();
    }
}
