/* JUnit test the Kieler Layout mechanism

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.layout.kieler.test.junit;

import static org.junit.Assert.assertArrayEquals;

///////////////////////////////////////////////////////////////////
//// KielerJUnitTest
/** 
 * Test out Kieler by open models, stripping the graphical elements,
 * using Kieler to layout the graph and then doing undo and redo.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */ 
public class KielerJUnitTest {
    /** 
     *  
     */ 
    @org.junit.Test 
    public void run() throws Exception {
        //assertArrayEquals(example.toString().getBytes(), knownGood.getBytes());
    }

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("ptolemy.vergil.basic.layout.kieler.test.junit.KielerJUnitTest");
    }

    private static String _eol = System.getProperty("line.separator");
}