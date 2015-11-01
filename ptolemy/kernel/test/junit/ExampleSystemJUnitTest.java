/* JUnit test for the example system

 Copyright (c) 2010-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.kernel.test.junit;

import static org.junit.Assert.assertArrayEquals;
import ptolemy.kernel.test.ExampleSystem;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ExampleSystemJUnitTest
/**
 * Run the ExampleSystem as a JUnit Test.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class ExampleSystemJUnitTest {
    /**
     * Create the example system and compare the value returned by the
     * toString() method to the known good results.
     * @exception IllegalActionException If thrown while constructing
     * the example system.
     * @exception NameDuplicationException If there is already an example system
     * in the default workspace.
     */
    @org.junit.Test
    public void run() throws IllegalActionException, NameDuplicationException {
        ExampleSystem example = new ExampleSystem();

        String knownGood = "----Methods of ComponentRelation----" + _eol
                + "linkedPorts:" + _eol + "R1: P1 P0 " + _eol + "R2: P1 P4 P3 "
                + _eol + "R3: P1 P2 " + _eol + "R4: P4 P7 " + _eol
                + "R5: P4 P5 " + _eol + "R6: P3 P6 " + _eol + "R7: P7 P13 P11 "
                + _eol + "R8: P9 P8 " + _eol + "R9: P10 P11 " + _eol
                + "R10: P8 P12 " + _eol + "R11: P12 P13 " + _eol
                + "R12: P14 P13 " + _eol + "" + _eol + "deepLinkedPorts:"
                + _eol + "R1: P1 " + _eol + "R2: P1 P9 P14 P10 P5 P3 " + _eol
                + "R3: P1 P2 " + _eol + "R4: P1 P3 P9 P14 P10 " + _eol
                + "R5: P1 P3 P5 " + _eol + "R6: P3 P6 " + _eol
                + "R7: P1 P3 P9 P14 P10 " + _eol + "R8: P9 P1 P3 P10 " + _eol
                + "R9: P10 P1 P3 P9 P14 " + _eol + "R10: P9 P1 P3 P10 " + _eol
                + "R11: P9 P1 P3 P10 " + _eol + "R12: P14 P1 P3 P10 " + _eol
                + "" + _eol + "----Methods of ComponentPort----" + _eol
                + "connectedPorts:" + _eol + "P0: " + _eol + "P1: P0 P4 P3 P2 "
                + _eol + "P2: P1 " + _eol + "P3: P1 P4 P6 " + _eol
                + "P4: P7 P5 " + _eol + "P5: P4 " + _eol + "P6: P3 " + _eol
                + "P7: P13 P11 " + _eol + "P8: P12 " + _eol + "P9: P8 " + _eol
                + "P10: P11 " + _eol + "P11: P7 P13 " + _eol + "P12: P8 "
                + _eol + "P13: P7 P11 " + _eol + "P14: P13 " + _eol + "" + _eol
                + "deepConnectedPorts:" + _eol + "P0: " + _eol
                + "P1: P9 P14 P10 P5 P3 P2 " + _eol + "P2: P1 " + _eol
                + "P3: P1 P9 P14 P10 P5 P6 " + _eol + "P4: P9 P14 P10 P5 "
                + _eol + "P5: P1 P3 " + _eol + "P6: P3 " + _eol
                + "P7: P9 P14 P10 " + _eol + "P8: P1 P3 P10 " + _eol
                + "P9: P1 P3 P10 " + _eol + "P10: P1 P3 P9 P14 " + _eol
                + "P11: P1 P3 P9 P14 " + _eol + "P12: P9 " + _eol
                + "P13: P1 P3 P10 " + _eol + "P14: P1 P3 P10 " + _eol;
        assertArrayEquals(example.toString().getBytes(), knownGood.getBytes());
    }

    /** Run the test that creates the example system.
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("ptolemy.kernel.test.junit.ExampleSystemJUnitTest");
    }

    private static String _eol = System.getProperty("line.separator");

}
