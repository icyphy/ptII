/* A transformer that removes unnecessary fields from classes.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;


import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.graph.*;

import soot.*;

//////////////////////////////////////////////////////////////////////////
//// ActorDataFlowAnalysis
/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class ActorDataFlowAnalysis {

    public ActorDataFlowAnalysis(Entity entity, SootClass theClass)
            throws IllegalActionException {

        DirectedGraph prefire_graph = _analyzeMethod(theClass,"prefire");
        DirectedGraph fire_graph = _analyzeMethod(theClass,"fire");
        DirectedGraph postfire_graph = _analyzeMethod(theClass,"postfire");

    }

    /**
     **/
    protected DirectedGraph _analyzeMethod(SootClass theClass,
            String methodName)
            throws IllegalActionException
    {

        SootMethod method = theClass.getMethodByName(methodName);
        return new IntervalBlockDirectedGraph(method);
    }

    public static void main(String args[]) {
        SootMethod method = ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);
    }

}
