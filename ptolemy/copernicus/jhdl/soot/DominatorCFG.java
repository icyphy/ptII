/*

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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.copernicus.jhdl.*;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.soot.*;

import soot.Body;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.SootMethod;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

//////////////////////////////////////////////////////////////////////////
//// DominatorCFG
/**
 * This class will take a Soot block and create a DirectedGraph.
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class DominatorCFG extends DirectedAcyclicCFG {

    DominatorCFG(SootMethod method) throws IllegalActionException {
        super(method);
        _init();
    }

    DominatorCFG(Body body) throws IllegalActionException {
        super(body);
        _init();
    }

    DominatorCFG(BriefBlockGraph bbg) throws IllegalActionException {
        super(bbg);
        _init();
    }

    /*
      public void update() throws IllegalActionException {
      super.update();
      _init();
      }
    */

    public boolean dominates(Node d, Node n) {
        return _dominators.dominates(d,n);
    }

    public Node getImmediatePostDominator(Node n) {
        return _postDominators.getImmediateDominator(n);
    }

    public Node getImmediateDominator(Node n) {
        return _dominators.getImmediateDominator(n);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Dominators\n"+_dominators);
        sb.append("Post Dominators\n"+_postDominators);
        sb.append("Immediate Dominators\n"+
                _dominators.immediateDominatorsString());
        sb.append("Immediate Post Dominators\n"+
                _postDominators.immediateDominatorsString());
        return sb.toString();
    }

    protected void _init() throws IllegalActionException {
        _dominators = new DominatorHashMap(this);
        _postDominators = new DominatorHashMap(this,true);
    }

    /**
     * This method will create a DominatorCFG object for the
     * Class and Method specified by the String arguments.
     * Before creating the CFG, this method will apply the
     * Conditional control compaction transformation
     * ({@link ConditionalControlCompactor#compact(SootMethod)})
     * and the Boolean NOT compaction transformation
     * ({@link BooleanNotCompactor#compact(SootMethod)}).
     *
     * @param args Specifies the Classname (args[0]) and the
     * Methodname (args[1]).
     * @param writeGraphs If set true, this method will create
     * ".dot" file graphs for intermediate results.
     * Specifically,
     * this method will create a file called "<method name>.dot"
     * that represents the CFG of the method after the transformations
     * have taken place.
     *
     * @see DominatorCFG#createDominatorCFG(String[],boolean)
     **/
    public static DominatorCFG createDominatorCFG(String args[],
            boolean writeGraphs) {
        soot.SootMethod testMethod =
            ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);
        DominatorCFG _cfg=null;
        try {
            ConditionalControlCompactor.compact(testMethod);
            BooleanNotCompactor.compact(testMethod);
            soot.Body body = testMethod.retrieveActiveBody();
            BriefBlockGraph bbgraph = new BriefBlockGraph(body);
            _cfg = new DominatorCFG(bbgraph);
            if (writeGraphs) {
                PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
                toDotty.writeDotFile(".", testMethod.getName(),_cfg);
            }
        } catch (IllegalActionException e) {
            System.err.println(e);
            System.exit(1);
        }
        return _cfg;
    }

    public static void main(String args[]) {
        DominatorCFG dcfg = createDominatorCFG(args,true);
    }

    protected DominatorHashMap _dominators;
    protected DominatorHashMap _postDominators;

}
