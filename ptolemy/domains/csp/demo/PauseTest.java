/* A three actor simulation usedto test that pausing works.

 Copyright (c) 1998 The Regents of the University of California.
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


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;


//////////////////////////////////////////////////////////////////////////
//// PauseTest
/**
Source - Buffer - Sink

Used to check that pausing works by pausing and resuming the
simulation several times during one run.
<p>
@author Neil Smyth
@version $Id$
@see classname
@see full-classname
*/
public class PauseTest {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */
    public PauseTest() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "PauseTest");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector("Local Director");
            univ.setManager(manager);
            univ.setDirector(localdir);

	    CSPPausingSource source = new CSPPausingSource(univ, "Source");
	    CSPBuffer middle = new CSPBuffer(univ, "Buffer", 5);
            CSPSink sink = new CSPSink(univ, "Sink");

            IOPort out1 = source.output;
	    IOPort in1 = middle.input;
	    IOPort out2 = middle.output;
            IOPort in2 = sink.input;

            IORelation rel1 = (IORelation)univ.connect(out1, in1, "R1");
            IORelation rel2 = (IORelation)univ.connect(out2, in2, "R2");
            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}
