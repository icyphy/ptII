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

import soot.Hierarchy;
import soot.Options;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.MethodCallGraph;

import java.io.FileWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        return super.getDeclaredOptions() + " outDir";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        String outDir = Options.getString(options, "outDir");
        System.out.println("LibraryUsageReporter.internalTransform("
                + phaseName + ", " + options + ")");
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        InvokeGraph invokeGraph =
            ClassHierarchyAnalysis.newPreciseInvokeGraph(true);
        MethodCallGraph methodCallGraph =
            (MethodCallGraph)invokeGraph.newMethodGraph();
        List reachableList = methodCallGraph.getMethodsReachableFrom(
                Scene.v().getMainClass().getMethods());

        Set createableClasses = new HashSet();
        for (Iterator reachables = reachableList.iterator(); reachables.hasNext();) {
            SootMethod method = (SootMethod)reachables.next();
            if (method.getName().equals("<init>")) {
                createableClasses.addAll(
                        hierarchy.getSuperclassesOfIncluding(
                                method.getDeclaringClass()));
                // FIXME: interfaces?
            }
        }

        Set RTAReachableClasses = new HashSet(createableClasses);
        List list = new LinkedList();
        for (Iterator reachables = reachableList.iterator(); reachables.hasNext();) {
            SootMethod method = (SootMethod)reachables.next();
            String methodName = method.getSignature();
            if (method.isStatic() ||
                    createableClasses.contains(method.getDeclaringClass())) {
                list.add(methodName);
                RTAReachableClasses.add(method.getDeclaringClass());
            }
        }
        Collections.sort(list);
        for (Iterator names = list.iterator(); names.hasNext();) {
            System.out.println(names.next());
        }
        try {
            FileWriter writer = new FileWriter(outDir + "/jarClassList.txt");
            for (Iterator classes = RTAReachableClasses.iterator();
                 classes.hasNext();) {
                SootClass theClass = (SootClass)classes.next();
                if (!theClass.getName().startsWith("java")) {
                    writer.write(theClass.getName());
                    writer.write("\n");
                }
            }
            writer.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private static LibraryUsageReporter _instance = new LibraryUsageReporter();
}














