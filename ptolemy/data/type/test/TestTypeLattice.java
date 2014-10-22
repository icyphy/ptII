/* Test TypeLattice

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.data.type.test;

import ptolemy.data.type.TypeLattice;
import ptolemy.graph.DirectedAcyclicGraph;

///////////////////////////////////////////////////////////////////
//// TestTypeLattice

/**
 Test multiple threads on a TypeLattice.
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)

 */
public class TestTypeLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create many threads and call bottom on the basic TypeLattice.
     *  @param args Ignored
     *  @exception Exception If there is a problem creating the threads
     *  or calling bottom() on the TypeLattice.
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((DirectedAcyclicGraph) TypeLattice.basicLattice())
                                .bottom();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            System.out.print(".");
            thread.start();
        }
        System.out.println("");
    }
}
