/* A transformer that removes unnecessary assignments

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

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Options;
import soot.RefType;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.CompleteUnitGraph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
A transformer that removes unnecessary reference assignments.
Specifically, an assignment <i>a=b</i> can be removed if <i>a</i> and
<i>b</i> are already must-aliases of each other.
*/

public class AliasAssignmentEliminator extends BodyTransformer
{
    private static AliasAssignmentEliminator instance =
    new AliasAssignmentEliminator();
    private AliasAssignmentEliminator() {}

    public static AliasAssignmentEliminator v() {
        return instance;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug";
    }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;
        System.out.println("AliasAssignmentEliminator.internalTransform("
                + phaseName + ", " + body.getMethod() + ", " + options + ")");

        boolean debug = Options.getBoolean(options, "debug");
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        // The analyses that give us the information to transform the code.
        MustAliasAnalysis mustAliasAnalysis =
            new MustAliasAnalysis(unitGraph);

        // Loop through all the units
        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            if (unit instanceof DefinitionStmt) {
                DefinitionStmt stmt = (DefinitionStmt)unit;
                Value left = stmt.getLeftOp();
                Value right = stmt.getRightOp();
                if (left.getType() instanceof RefType &&
                        right.getType() instanceof RefType) {
                    Set leftMustAliases, rightMustAliases;
                    if (left instanceof Local) {
                        leftMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    (Local)left, unit);
                    } else if (left instanceof FieldRef) {
                        SootField field = ((FieldRef)left).getField();
                        leftMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    field, unit);
                    } else {
                        continue;
                    }
                    if (right instanceof Local) {
                        rightMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    (Local)right, unit);
                    } else if (right instanceof FieldRef) {
                        SootField field = ((FieldRef)right).getField();
                        rightMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    field, unit);
                    } else {
                        continue;
                    }

                    if (debug) {
                        System.out.println("Ref-ref assignment = " +
                                unit);
                        System.out.println("left aliases = " +
                                leftMustAliases);
                        System.out.println("right aliases = " +
                                rightMustAliases);
                    }

                    // Utter hack... Should be:
                    // Why doesn't alias analysis return the right things?
                    // if (mustAliasAnalysis.getAliasesOfBefore((Local)left, unit).contains(right)) {
                    Set intersection = leftMustAliases;
                    intersection.retainAll(rightMustAliases);
                    if (!intersection.isEmpty()) {
                        if (debug) {
                            System.out.println("Instances are already" +
                                    " equal.  Removing assignment");
                        }
                        body.getUnits().remove(stmt);
                    }
                }
                /* Not sure what to do here.. our alias analysis doesn't
                   handle nulls well...
                   else if (left.getType() instanceof RefType &&
                   right.getType() instanceof NullType) {
                   // Then the right side is the one we must analyze.
                   if (debug) System.out.println("Ref-Null unit = " + unit);
                   Local local = (Local)left;
                   if (nullPointerAnalysis.isAlwaysNullBefore(local, unit)) {
                   binop.getOp1Box().setValue(IntConstant.v(0));
                   binop.getOp2Box().setValue(IntConstant.v(0));
                   } else if (nullPointerAnalysis.isNeverNullBefore(local, unit)) {
                   binop.getOp1Box().setValue(IntConstant.v(0));
                   binop.getOp2Box().setValue(IntConstant.v(1));
                   }
                   }*/
            }
        }
    }
}

