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

import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.*;

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
public class LibraryUsageReporter extends SceneTransformer
    implements HasPhaseOptions {

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static LibraryUsageReporter v() {
        return _instance;
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "analyzeAllReachables:false";
    }

    public String getDeclaredOptions() {
        return "outFile analyzeAllReachables";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        String outFile = PhaseOptions.getString(options, "outFile");
        boolean analyzeAllReachables =
            PhaseOptions.getBoolean(options, "analyzeAllReachables");
        System.out.println("LibraryUsageReporter.internalTransform("
                + phaseName + ", " + options + ")");
        Scene.v().releaseCallGraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        ReachableMethods reachableMethods = new ReachableMethods(callGraph, 
                EntryPoints.v().application());

        reachableMethods.update();
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        
        final Set createableClasses = new HashSet();
        for (Iterator reachables = reachableMethods.listener(); 
             reachables.hasNext();) {
            SootMethod method = (SootMethod)reachables.next();
            String methodName = method.getSignature();
            if (method.getName().equals("<init>") &&
                    !method.getDeclaringClass().getName().startsWith("java")) {
                createableClasses.addAll(
                        hierarchy.getSuperclassesOfIncluding(
                                method.getDeclaringClass()));
                // FIXME: what about super interfaces
                createableClasses.addAll(
                        method.getDeclaringClass().getInterfaces());
            }
        }

        System.out.println("createableClasses = " + createableClasses);
        // Now create a new set of reachable methods that only
        // includes methods that are static or are declared in classes
        // that can are created.
        Filter filter = new Filter(
                new EdgePredicate() {
                    public boolean want(Edge e) {
                        SootMethod target = e.tgt();
                        return e.isExplicit() && (target.isStatic() || 
                            createableClasses.contains(
                                    target.getDeclaringClass()));
                    }
                });
        Set necessaryClasses = new HashSet();
        ReachableMethods RTAReachableMethods = new ReachableMethods(callGraph, 
                EntryPoints.v().application().iterator(), filter);
        RTAReachableMethods.update();
        List list = new LinkedList();
        for (Iterator reachables = RTAReachableMethods.listener(); 
             reachables.hasNext();) {
            SootMethod method = (SootMethod)reachables.next();
            String methodName = method.getSignature();
            list.add(methodName);
            SootClass declaringClass = method.getDeclaringClass();
            if (!declaringClass.getName().startsWith("java")) {
                necessaryClasses.add(declaringClass);
            }
        }

        // Print out all the used methods
        Collections.sort(list);
        for (Iterator names = list.iterator(); names.hasNext();) {
            System.out.println(names.next());
        }
        
        try {
            // Add to the set of necessary classes all that they depend on.
            DependedClasses dependedClasses = 
                new DependedClasses(necessaryClasses);
            FileWriter writer = new FileWriter(outFile);
            for (Iterator classes = dependedClasses.list().iterator();
                 classes.hasNext();) {
                SootClass theClass = (SootClass)classes.next();
                if(analyzeAllReachables) {
                    // Set the class to be an application class, so we can
                    // analyze it.
                    theClass.setApplicationClass();
                }
                writer.write(theClass.getName());
                writer.write("\n");
            }
            writer.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private static LibraryUsageReporter _instance = new LibraryUsageReporter();
}














