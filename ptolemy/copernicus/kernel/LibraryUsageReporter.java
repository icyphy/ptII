/* A transformer that reports library methods used.

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

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.*;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


//////////////////////////////////////////////////////////////////////////
//// LibraryUsageReporter
/**
A Transformer that reports reachable methods in the Java libraries.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0

*/
public class LibraryUsageReporter extends SceneTransformer {

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static LibraryUsageReporter v() {
        return _instance;
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions();
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("LibraryUsageReporter.internalTransform("
                + phaseName + ", " + options + ")");

        InvokeGraph invokeGraph =
            ClassHierarchyAnalysis.newPreciseInvokeGraph(true);
        MethodCallGraph methodCallGraph =
            (MethodCallGraph)invokeGraph.newMethodGraph();
        List reachableList = methodCallGraph.getMethodsReachableFrom(Scene.v().getMainClass().getMethods());
        List list = new LinkedList();
        for(Iterator reachables = reachableList.iterator(); reachables.hasNext();) {
            SootMethod method = (SootMethod)reachables.next();
            String methodName = method.getSignature();
//             if(method.isStatic() || createableClasses.contains(method.getDeclaringClass())) {
                list.add(methodName);
                //           }
        }
        Collections.sort(list);
        for(Iterator names = list.iterator(); names.hasNext();) {
            System.out.println(names.next());
        }
    }
    private static LibraryUsageReporter _instance = new LibraryUsageReporter();
}














