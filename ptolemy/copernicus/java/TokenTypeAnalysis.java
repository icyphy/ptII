/* An analysis for detecting objects that must be aliased to eachother.

 Copyright (c) 2001 The Regents of the University of California.
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
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.copernicus.kernel.SootUtilities;

import java.util.*;

/**
An analysis that maps each local variable that represents a token
onto the particular type of the token.
*/
public class TokenTypeAnalysis extends ForwardFlowAnalysis {
    public TokenTypeAnalysis(SootMethod method, CompleteUnitGraph g) {
        super(g);
        _method = method;
        _localDefs = new SimpleLocalDefs(g);
        _localUses = new SimpleLocalUses(g, _localDefs);
        doAnalysis();
    }
    
    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public ptolemy.data.type.Type getTypeOfAfter(Local local, Unit unit) {
        Map map = (Map)getFlowAfter(unit);
        Object object = map.get(local);
        if(object == null) {
            throw new RuntimeException("Unknown token type for object: " + local);
        }
        return (ptolemy.data.type.Type)object;
    }
    
    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public ptolemy.data.type.Type getTypeOfBefore(Local local, Unit unit) {
        Map map = (Map)getFlowBefore(unit);
        Object object = map.get(local);
        if(object == null) {
            throw new RuntimeException("Unknown token type for unit: " + local + " in " + unit);
        }
        return (ptolemy.data.type.Type)object;
    }

    // Formulation:
    // The dataflow information is stored in a map from each aliasable object (SootField or Local)
    // to a set of aliases.  
    protected Object newInitialFlow() {
        return new HashMap();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue) {
        Map in = (Map) inValue, out = (Map) outValue;
        Stmt stmt = (Stmt)d;

        // System.out.println("flowing " + d + " " + in);

        // By default, the out is equal to the in.
        copy(inValue, outValue);

        if(stmt instanceof AssignStmt) {
            Value leftOp = ((AssignStmt)stmt).getLeftOp();
            if(!_isTokenType(leftOp.getType())) return;
            //  System.out.println("from " + in);

            Value rightOp = ((AssignStmt)stmt).getRightOp();
            
            if(rightOp instanceof StaticInvokeExpr) {
                StaticInvokeExpr r = (StaticInvokeExpr)rightOp;
                if(r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                    out.put(r.getArg(0), in.get(r.getArg(2)));
                }
            } else if(rightOp instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr r = (InstanceInvokeExpr)rightOp;
                String methodName = r.getMethod().getName();
                //   System.out.println("invokeExpr = " + r);
                SootClass baseClass = ((RefType)r.getBase().getType()).getSootClass();
                // FIXME: match better.
                // If we are invoking a method on a token, then...
                if(SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.tokenClass)) {
                    if(methodName.equals("one") ||
                            methodName.equals("zero")) {
                        // The returned type must be equal to the type  
                        // we are calling the method on.
                        out.put(leftOp, in.get(r.getBase()));
                    } else if(methodName.equals("add") ||
                            methodName.equals("addReverse") ||
                            methodName.equals("subtract") ||
                            methodName.equals("subtractReverse") ||
                            methodName.equals("multiply") ||
                            methodName.equals("multiplyReverse") ||
                            methodName.equals("divide") ||
                            methodName.equals("divideReverse") ||
                            methodName.equals("modulo") ||
                            methodName.equals("moduloReverse")) {
                        out.put(leftOp, in.get(r.getArg(0)));
                    } else if(methodName.equals("convert")) {
                        // The return rightOp type is equal to the base type.
                        // The first argument type is less than or equal to the base type.
                        out.put(leftOp, in.get(r.getBase()));
                    } else if(methodName.equals("getElement") ||
                            methodName.equals("arrayValue")) {
                        ptolemy.data.type.Type arrayType = (ptolemy.data.type.Type)in.get(r.getBase());
                        if(arrayType != null && arrayType instanceof ArrayType) {
                            out.put(leftOp, ((ArrayType)arrayType).getElementType());
                        }
                    }
                } else if(SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.portClass)) {
                    // If we are invoking a method on a port.
                    TypedIOPort port = (TypedIOPort)
                        InlinePortTransformer.getPortValue(
                                _method,
                                (Local)r.getBase(),
                                stmt, 
                                _localDefs, 
                                _localUses);
                    if(methodName.equals("broadcast")) {
                        // The type of the argument must be less than the 
                        // type of the port.
                   
                    } else if(methodName.equals("get")) {
                        out.put(leftOp, port.getType());
                    } else if(methodName.equals("send")) {
                        if(r.getArgCount() == 3) {
                            // The type of the argument must be less than the 
                            // type of the port.
                     
                            //r.getArg(1));
                        } else if(r.getArgCount() == 2) {
                            // The type of the argument must be less than the 
                            // type of the port.
                            //            r.getArg(1));
                        }
                    }                        
                } else if(SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.attributeClass)) {
                    // If we are invoking a method on a parameter.
                    Attribute attribute = (Attribute)
                        InlineParameterTransformer.getAttributeValue(
                                _method, 
                                (Local)r.getBase(),
                                stmt, 
                                _localDefs, 
                                _localUses);
                    if(attribute == null) {
                        // A method invocation with a null base is bogus,
                        // so don't create a type constraint.
                    }
                    if(attribute instanceof Variable) {
                        Variable parameter = (Variable)attribute;
                        if(methodName.equals("setToken")) {
                            // The type of the argument must be less than the 
                            // type of the parameter.
                            // r.getArg(0));
                        
                        } else if(methodName.equals("getToken")) {
                            // Return the type of the parameter.
                            out.put(leftOp, parameter.getType());
                        }
                    }
                }
            } else if(rightOp instanceof ArrayRef) {
                out.put(leftOp, in.get(((ArrayRef)rightOp).getBase()));
            } else if(rightOp instanceof CastExpr) {
                CastExpr castExpr = (CastExpr)rightOp;
                Type type = castExpr.getType();
                //            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
                //if(tokenType != null) {
                out.put(leftOp, in.get(castExpr.getOp()));
                // } else {
                // Otherwise there is nothing to be done.
                //}
            } else if(rightOp instanceof NewExpr) {
                NewExpr newExpr = (NewExpr)rightOp;
                RefType type = newExpr.getBaseType();
                SootClass castClass = type.getSootClass();
                // If we are creating a Token type...
                if(SootUtilities.derivesFrom(castClass,
                        PtolemyUtilities.tokenClass)) {
                    // Then the rightOp of the expression is the type of the
                    // constructor.
                    out.put(leftOp, PtolemyUtilities.getTokenTypeForSootType(type));
                } else {
                    // Otherwise there is nothing to be done.
                }
            } else if(rightOp instanceof NewArrayExpr) {
                // Since arrays are aliasable, we must update their types.
                NewArrayExpr newExpr = (NewArrayExpr)rightOp;
                Type type = newExpr.getBaseType();
                RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
                if(tokenType != null) {
                    out.put(leftOp, PtolemyUtilities.getTokenTypeForSootType(tokenType));
                }
                // Otherwise there is nothing to be done.
            }// else FieldRef?
            //  System.out.println("type of " + leftOp + " set to " + out.get(leftOp));
        }
        //   System.out.println("equals " + in + " == " + out + " = " + in.equals(out));
    }

    private boolean _isTokenType(Type type) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        return tokenType != null;
    }

    protected void copy(Object inValue, Object outValue) {
        Map in = (Map) inValue, out = (Map) outValue;
        out.clear();
        for(Iterator i = in.keySet().iterator(); i.hasNext();) {
            Object object = i.next();
            out.put(object, in.get(object));
        }
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        Map in1 = (Map) in1Value, in2 = (Map) in2Value, out = (Map) outValue;
       
        // System.out.println("merging " + in1 + " and " + in2 + " into " + out);
       
        Set allKeys = new HashSet();
        allKeys.addAll(in1.keySet());
        allKeys.addAll(in2.keySet());
        for(Iterator i = allKeys.iterator(); i.hasNext();) {
            Object object = i.next();
            ptolemy.data.type.Type in1Type = 
                (ptolemy.data.type.Type)in1.get(object);
            ptolemy.data.type.Type in2Type = 
                (ptolemy.data.type.Type)in2.get(object);
            if(in1Type == null) {
                in1Type = BaseType.UNKNOWN;
            }
            if(in2Type == null) {
                in2Type = BaseType.UNKNOWN;
            }
            if(in1Type.equals(in2Type)) {
                out.put(object, in1Type);
            } else {
                out.put(object, TypeLattice.lattice().leastUpperBound(in1Type, in2Type));
            }
        }
        //      System.out.println("result = " + out);

    }

    private SootMethod _method;
    private SimpleLocalDefs _localDefs;
    private SimpleLocalUses _localUses;
}
