/* An analysis for extracting the constructors of tokens.

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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.MustAliasAnalysis;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// FieldsForAttributesTransformer
/**
An analysis that establishes a constant value, if possible, of a token
constructed at a particular statement.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class TokenConstructorAnalysis {
    public TokenConstructorAnalysis(JimpleBody body, LocalDefs localDefs) {
        _constructorToValue = new HashMap();

        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Stmt unit = (Stmt)units.next();
            if (unit.containsInvokeExpr() &&
                    unit.getInvokeExpr() instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr invokeExpr =
                    (InstanceInvokeExpr)unit.getInvokeExpr();
                SootMethod invokedMethod = invokeExpr.getMethod();
                // If we invoke a Token class initializer
                if (invokedMethod.getName().equals("<init>") &&
                        SootUtilities.isSubtypeOf(
                                invokeExpr.getBase().getType(),
                                RefType.v(PtolemyUtilities.tokenClass))) {
                    // System.out.println("found token initializer: " + unit);
                    Unit constructor = _findConstructor(
                            (Local)invokeExpr.getBase(), unit, localDefs);
                    //  System.out.println("found token constructor: " + constructor);
                    if (constructor == null) {
                        continue;
                    }

                    Token token =
                        _evaluateInitializer(invokeExpr, invokedMethod);
                    if (token != null) {
                        _constructorToValue.put(constructor, token);
                    }
                }
            }
        }
    }

    /** Return the invocation that creates an object with the given name.
     */
    public Token getConstructedTokenValue(Stmt tokenConstructor) {
        return (Token)_constructorToValue.get(tokenConstructor);
    }

    private Token _evaluateInitializer(InvokeExpr invokeExpr,
            SootMethod invokedMethod) {
        Value[] argValues = (Value[])
            invokeExpr.getArgs().toArray(new Value[0]);
        for (int i = 0; i < argValues.length; i++) {
            if (Evaluator.isValueConstantValued(argValues[i])) {
                argValues[i] = Evaluator.getConstantValueOf(argValues[i]);
            } else {
                return null;
            }
        }
        try {
            return (Token) SootUtilities.reflectAndInvokeConstructor(
                    invokedMethod, argValues);
        } catch (Exception ex) {
            return null;
        }
    }
    private Unit _findConstructor(Local local, Unit location,
            LocalDefs localDefs) {
        NewExpr newExpr = null;
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof NewExpr) {
                return stmt;
            } else {
                throw new RuntimeException("Found something other" +
                        " than a constructor: " + stmt);
            }
        }
        return null;
    }

    private Map _constructorToValue;
}
