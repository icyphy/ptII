/* JUnit test for the PtidesEvent

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

package ptolemy.domains.ptides.kernel.test.junit;

import static org.junit.Assert.assertArrayEquals;
import ptolemy.domains.ptides.kernel.PtidesEvent;

///////////////////////////////////////////////////////////////////
//// PtidesEvent
/**
 * Tests for PtidesEvent.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesEventJUnitTest {
    /**
     * Pass nulls to the PtidesEvent constructor.
     * @exception Exception If the Ptides event cannot be created.
     */
    @org.junit.Test
    public void constructorWithNulls() throws Exception {
        PtidesEvent event = new PtidesEvent(null, null, null, 1, 2, null, null);

        String knownGood = "PtidesEvent{time = null, microstep = 1, depth = 2, token = null, absoluteDeadline = null, dest = null.null.0, receiver = null, isPureEvent = true, sourceTimestamp = null}";
        //System.out.println("-->" +  event.toString() + "<--");
        assertArrayEquals(event.toString().getBytes(), knownGood.getBytes());
    }

    /** Invoke the test.
     *  @param args Ignored
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
                .main("ptolemy.kernel.test.junit.ExampleSystemJUnitTest");
    }
}
