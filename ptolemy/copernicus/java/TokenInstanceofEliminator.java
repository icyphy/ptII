/* A transformer that tried to statically instanceof token expressions.

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


package ptolemy.copernicus.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.data.type.BaseType;

import soot.Body;
import soot.BodyTransformer;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Local;
import soot.PhaseOptions;
import soot.Scene;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.CompleteUnitGraph;

//////////////////////////////////////////////////////////////////////////
//// TokenInstanceofEliminator
/**
A transformer that removes unnecessary instanceof checks for tokens.
This is similar to CastAndInstanceofEliminator, except here
we use a stronger type inference algorithm that is aware of
Ptolemy token types.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/

public class TokenInstanceofEliminator extends BodyTransformer 
    implements HasPhaseOptions {
    private static TokenInstanceofEliminator instance =
    new TokenInstanceofEliminator();
    private TokenInstanceofEliminator() {}

    public static TokenInstanceofEliminator v() { return instance; }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug";
    }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;

//         System.out.println("TokenInstanceofEliminator.internalTransform(" +
//                 body.getMethod() + ", " + phaseName + ")");

        boolean debug = PhaseOptions.getBoolean(options, "debug");

        eliminateCastsAndInstanceOf(body, phaseName, new HashSet(), debug);
    }

    public static void eliminateCastsAndInstanceOf(Body body,
            String phaseName, Set unsafeLocalSet, boolean debug) {
        // Analyze the types of variables which refer to tokens.
        TokenTypeAnalysis tokenTypes =
            new TokenTypeAnalysis(body.getMethod(),
                    new CompleteUnitGraph(body));

        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            for (Iterator boxes = unit.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();

                if (value instanceof CastExpr) {
                    // If the cast is to the same type as the
                    // operand already is, then replace with
                    // simple assignment.
                    CastExpr expr = (CastExpr)value;
                    Type castType = expr.getCastType();
                    Value op = expr.getOp();
                    if (!PtolemyUtilities.isTokenType(op.getType())) {
                        continue;
                    }

                    // Use the token type inference to get the actual
                    // type of the argument.
                    ptolemy.data.type.Type type =
                        tokenTypes.getTypeOfBefore((Local)op, unit);

                    // Don't try to replace non-instantiable types, since they
                    // might be more refined later.
                    // General, is unfortuantely, considered instantiable.
                    if(type.equals(BaseType.UNKNOWN) || //!type.isInstantiable() ||
                            type.equals(BaseType.GENERAL)) {
                        continue;
                    }

                    Type opType =
                        PtolemyUtilities.getSootTypeForTokenType(type);

                    //                     // Skip locals that are unsafe.
                    //                     if (castType.equals(opType) &&
                    //                             !unsafeLocalSet.contains(op)) {
                    //                         box.setValue(op);
                    //                     }
                    if (unsafeLocalSet.contains(op)) {
                        continue;
                    }

                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();

                    if(debug) System.out.println("checking cast in " + unit);
                    if(debug) System.out.println("op = " + op);
                    if(debug) System.out.println("opType = " + opType);
                    CastAndInstanceofEliminator.replaceCast(box, hierarchy,
                            castType, op, opType, debug);
                    
                } else if (value instanceof InstanceOfExpr) {
                    // If the operand of the expression is
                    // declared to be of a type that implies
                    // the instanceof is true, then replace
                    // with true.
                    InstanceOfExpr expr = (InstanceOfExpr)value;
                    Type checkType = expr.getCheckType();
                    Value op = expr.getOp();
                    if (!PtolemyUtilities.isTokenType(op.getType())) {
                        continue;
                    }

                    // Use the token type inference to get the actual
                    // type of the argument.
                    ptolemy.data.type.Type type =
                        tokenTypes.getTypeOfBefore((Local)op, unit);

                    // Don't try to replace non-instantiable types, since they
                    // might be more refined later.
                    // General, is unfortuantely, considered instantiable.
                    if(type.equals(BaseType.UNKNOWN) || //!type.isInstantiable() ||
                            type.equals(BaseType.GENERAL)) {
                        continue;
                    }

                    Type opType =
                        PtolemyUtilities.getSootTypeForTokenType(type);

                    if (debug) System.out.println("Checking instanceof check: " + expr);
                    CastAndInstanceofEliminator.replaceInstanceofCheck(
                            box, Scene.v().getActiveHierarchy(),
                            checkType, opType, debug);
                }
            }
        }
    }
}
