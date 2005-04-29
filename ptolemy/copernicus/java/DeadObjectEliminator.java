/* A transformer that removes dead token and type creations.

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import soot.Body;
import soot.BodyTransformer;
import soot.Hierarchy;
import soot.Local;
import soot.Scene;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;


//////////////////////////////////////////////////////////////////////////
//// DeadObjectEliminator

/**
   A transformer that removes unnecessary object creations.  If
   an attribute, type or token is created, but never used anywhere, then
   it can be safely removed.  This is possible because types and tokens are
   immutable, and we know that the attribute constructor will not have
   any interesting side effects after code generation is completed.

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class DeadObjectEliminator extends BodyTransformer {
    /** Construct a new transformer
     */
    private DeadObjectEliminator() {
    }

    /* Return the instance of this transformer.
     */
    public static DeadObjectEliminator v() {
        return instance;
    }

    protected void internalTransform(Body body, String phaseName, Map options) {
        // Assume that all classes we care about have been loaded...
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        Set set = new HashSet();

        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.tokenClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.baseTypeClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.arrayTypeClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.recordTypeClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.matrixTypeClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           PtolemyUtilities.attributeClass));
        set.addAll(hierarchy.getSubclassesOfIncluding(
                           Scene.v().loadClassAndSupport("ptolemy.data.expr.PtParser")));

        _removeDeadObjectCreation(body, set);
    }

    /** Remove any creations of objects of the given class, or
     *  subclasses that are not directly used in the given body.  Note
     *  that this is not, technically a safe thing to do, since object
     *  creation may have side effects that will not be seen.  We use
     *  this when we have knowledge of the given class that side
     *  effects are not possible, or that the object is immutable.
     */
    private static void _removeDeadObjectCreation(Body body, Set classSet) {
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);

        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Stmt stmt = (Stmt) units.next();

            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            ValueBox box = stmt.getInvokeExprBox();
            Value value = box.getValue();

            if (value instanceof SpecialInvokeExpr) {
                SpecialInvokeExpr r = (SpecialInvokeExpr) value;

                //      System.out.println("compare " + r.getMethod().getDeclaringClass());
                //                 System.out.println("with " + theClass);
                if (classSet.contains(r.getMethod().getDeclaringClass())
                        && !liveLocals.getLiveLocalsAfter(stmt)
                        .contains(r.getBase())) {
                    // Remove the initialization and the constructor.
                    // Note: This assumes a fairly tight coupling between
                    // the new and the object constructor.  This may
                    // not be true.
                    body.getUnits().remove(stmt);

                    for (Iterator defs = localDefs.getDefsOfAt(
                                 (Local) r.getBase(), stmt).iterator();
                         defs.hasNext();) {
                        Unit defUnit = (Unit) defs.next();

                        if (defUnit instanceof DefinitionStmt) {
                            // If we are keeping a definition, then
                            // set the definition to be null.
                            ((DefinitionStmt) defUnit).getRightOpBox().setValue(NullConstant
                                    .v());
                        } else {
                            // I can't imagine when this would
                            // be true?
                            body.getUnits().remove(defUnit);
                        }
                    }
                }
            }
        }
    }

    private static DeadObjectEliminator instance = new DeadObjectEliminator();
}
