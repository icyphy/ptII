/* An analysis for detecting objects that must be aliased to each other.

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

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
An analysis that maps each local or field to the set of objects or
fields that must alias it, at a particular point in the code.
*/
public class MustAliasAnalysis extends FastForwardFlowAnalysis {
    public MustAliasAnalysis(UnitGraph g) {
        this(g, null, null);
    }

    /** Create a new analysis based on the given invoke graph
     *  and side effect information.
     */
    public MustAliasAnalysis(UnitGraph g,
            InvokeGraph invokeGraph,
            SideEffectAnalysis sideEffectAnalysis) {
        super(g);
        _invokeGraph = invokeGraph;
        _sideEffectAnalysis = sideEffectAnalysis;
        doAnalysis();
        _invokeGraph = null;
        _sideEffectAnalysis = null;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public Set getAliasesOfBefore(SootField field, Unit unit) {
        Map map = (Map)getFlowBefore(unit);
        Set set = new HashSet();
        if (map.get(field) != null) {
            set.addAll((Set)map.get(field));
        }
        set.remove(field);
        return set;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point after
     *  the given unit.
     */
    public Set getAliasesOfAfter(SootField field, Unit unit) {
        Map map = (Map)getFlowAfter(unit);
        Set set = new HashSet();
        if (map.get(field) != null) {
            set.addAll((Set)map.get(field));
        }
        set.remove(field);
        return set;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given local, at a point before
     *  the given unit.
     */
    public Set getAliasesOfBefore(Local local, Unit unit) {
        Map map = (Map)getFlowBefore(unit);
        Set set = new HashSet();
        if (map.get(local) != null) {
            set.addAll((Set)map.get(local));
        }
        set.remove(local);
        return set;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point after
     *  the given unit.
     */
    public Set getAliasesOfAfter(Local local, Unit unit) {
        Map map = (Map)getFlowAfter(unit);
        Set set = new HashSet();
        if (map.get(local) != null) {
            set.addAll((Set)map.get(local));
        }
        set.remove(local);
        return set;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given local, at a point before
     *  the given unit.
     */
    public Set getAliasesOfBefore(NewExpr constructor, Unit unit) {
        Map map = (Map)getFlowBefore(unit);
        Set set = new HashSet();
        if (map.get(constructor) != null) {
            set.addAll((Set)map.get(constructor));
        }
        set.remove(constructor);
        return set;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point after
     *  the given unit.
     */
    public Set getAliasesOfAfter(NewExpr constructor, Unit unit) {
        Map map = (Map)getFlowAfter(unit);
        Set set = new HashSet();
        if (map.get(constructor) != null) {
            set.addAll((Set)map.get(constructor));
        }
        set.remove(constructor);
        return set;
    }

    // Formulation: The dataflow information is stored in a map from
    // each aliasable object (SootField or Local) to a set of aliases.
    // Note that for each alias-set there is exactly one instance of
    // HashSet stored in the map.  This is implemented as a
    // flow-sensitive intraprocedural analysis.  Method calls are handled
    // conservatively, and we assume that they affect the values of
    // all fields (i.e. aliases for all fields are killed.  If the
    // object has no other aliases, or any maybe-aliases, then it
    // points to null.
    protected Object newInitialFlow() {
        return new HashMap();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue)
    {
        Map in = (Map) inValue, out = (Map) outValue;
        Stmt unit = (Stmt)d;

        // By default, the out is equal to the in.
        copy(inValue, outValue);

        //        System.out.println("flow through " + d);

        // if we have a method invocation, then alias information
        // for all fields is killed.
        // This is a safe flow-insensitive approximation.
        if (unit.containsInvokeExpr()) {
            if (_sideEffectAnalysis == null) {
                for (Iterator i = out.keySet().iterator();
                     i.hasNext();) {
                    Object object = i.next();
                    if (object instanceof SootField) {
                        SootField field = (SootField) object;
                        // FIXME: properly compute this..
                        boolean targetsAreInDifferentClass = true;
                        if (field.isPrivate() && targetsAreInDifferentClass) {
                            continue;
                        } else {
                            _killAlias(out, object);
                        }
                    }
                }
            } else {
                InvokeExpr expr = (InvokeExpr)unit.getInvokeExpr();
                SootMethod method = expr.getMethod();
                System.out.println("invoking: " + method);
                Set allSideEffects = new HashSet();
                // Union the side effect sets over
                // all the possible targets
                List targets = _invokeGraph.getTargetsOf((Stmt)unit);
                for (Iterator i = targets.iterator();
                     i.hasNext();) {
                    SootMethod target = (SootMethod)i.next();

                    Set newSet = _sideEffectAnalysis.getSideEffects(method);

                    if (newSet != null) {
                        allSideEffects.addAll(newSet);
                    } else {
                        allSideEffects = null;
                        break;
                    }
                }
                if (allSideEffects != null) {
                    // kill the alias for anything that was in the set,
                    // and is in our flow.
                    allSideEffects.retainAll(out.keySet());
                } else {
                    // If we have unknown side effects, then we have
                    // to kill alias information for *all* fields.
                    // Note that this set includes all the locals
                    // as well.
                    allSideEffects = out.keySet();
                }
                System.out.println("all Side effects = " + allSideEffects);
                for (Iterator i = allSideEffects.iterator();
                     i.hasNext();) {
                    Object object = i.next();
                    if (object instanceof SootField) {
                        _killAlias(out, object);
                    }
                }
            }
        }
        if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt)unit;

            Value lvalue = assignStmt.getLeftOp();
            Value rvalue = assignStmt.getRightOp();
            Object lobject = _getAliasObject(lvalue);
            Object robject = _getAliasObject(rvalue);
            if (lobject != null) {
                // First remove the left side from its
                // current set of aliases.  (Kill rule)
                _killAlias(out, lobject);

                if (robject != null) {
                    // If the type is aliasable,
                    if (lvalue.getType() instanceof ArrayType ||
                            lvalue.getType() instanceof RefType) {

                        // add the left side to its new set of
                        // aliases. (Gen rule)
                        _createAlias(out, lobject, robject);
                    }
                }
                //           System.out.println("aliases for " + lobject +
                //        " = " + out.get(lobject));
            }
        }

        // otherwise, the alias info is unchanged.
    }

    protected void copy(Object inValue, Object outValue) {
        //System.out.println("copy");
        Map in = (Map) inValue, out = (Map) outValue;
        out.clear();
        List aliasValues = new LinkedList(in.keySet());
        while (aliasValues.size() > 0) {
            Object object = aliasValues.get(0);
            aliasValues.remove(object);
            Set inSet = (Set)in.get(object);
            if (inSet == null) {
                out.put(object, null);
            } else {
                Set outSet = new HashSet();
                outSet.addAll(inSet);
                out.put(object, outSet);
                for (Iterator i = outSet.iterator();
                     i.hasNext();) {
                    out.put(i.next(), outSet);
                }
                aliasValues.removeAll(inSet);
            }
        }
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        //System.out.println("merge");
        Map in1 = (Map) in1Value, in2 = (Map) in2Value, out = (Map) outValue;

        // First set the output to the first input.
        if (in1 != out)
            copy(in1, out);

        // Now merge in the second input.
        for (Iterator i = in1.keySet().iterator(); i.hasNext();) {
            Object object = i.next();
            Set in1Set = (Set)in1.get(object);
            Set in2Set = (Set)in2.get(object);
            if (in1Set == null) {
                // If both inputs have maybe aliases, or no
                // alias information, then the output
                // is the same.
                out.put(object, null);
            } else if (!in1Set.equals(in2Set)) {
                // If the input alias sets are not equal,
                // then we can't tell anything for sure about
                // what the union is.
                _killAlias(out, object);
            }
        }
    }


    private static void _createAlias(Map map, Object lObject, Object rObject) {
        //System.out.println("createAlias");
        // Get its new set of aliases.
        Set rset = (Set)map.get(rObject);
        if (rset == null) {
            rset = new HashSet();
            rset.add(rObject);
        }

        // Add the object to the new set of aliases.
        rset.add(lObject);

        // And set its set of aliases.
        map.put(lObject, rset);
    }

    private static Object _getAliasObject(Value value) {
        if (value instanceof Local) {
            return value;
        } else if (value instanceof FieldRef) {
            /// NOTE: we can do better
            // if we return something that is
            // instance-dependent.
            return ((FieldRef)value).getField();
        } else if (value instanceof CastExpr) {
            // Must be a local.
            return ((CastExpr)value).getOp();
        } else if (value instanceof NewExpr ||
                value instanceof NewArrayExpr) {
            return value;
        } else return null;
    }

    private static void _killAlias(Map map, Object lObject) {
        // Get its old set of aliases.
        Set lset = (Set)map.get(lObject);
        if (lset != null) {
            // And remove.
            lset.remove(lObject);
            map.put(lObject, null);
        }
    }

    private InvokeGraph _invokeGraph;
    private SideEffectAnalysis _sideEffectAnalysis;
}
