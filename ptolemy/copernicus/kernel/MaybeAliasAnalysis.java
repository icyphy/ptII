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
import soot.Modifier;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityRef;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.HashMutableDirectedGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
An analysis that maps each local and field to the set of locals and
fields that may alias that value.
*/
public class MaybeAliasAnalysis extends FastForwardFlowAnalysis {
    /** Create a new analysis that does not rely on
     *  side effect information.  This is a more conservative,
     *  but computationally cheaper  approximation to the aliases
     *  than is possible with better side effect information.
     */
    public MaybeAliasAnalysis(UnitGraph g) {
        this(g, null, null);
    }

    /** Create a new analysis based on the given invoke graph
     *  and side effect information.
     */
    public MaybeAliasAnalysis(UnitGraph g,
            CallGraph callGraph,
            SideEffectAnalysis sideEffectAnalysis) {
        super(g);
        _callGraph = callGraph;
        _sideEffectAnalysis = sideEffectAnalysis;
        if (_debug) {
            _countMap = new HashMap();
        }

        // Initialize the representative objects for constructors.
        int index = 0;
        _constructorMap = new HashMap();
        for (Iterator units = g.getBody().getUnits().iterator();
             units.hasNext();) {
            Stmt stmt = (Stmt) units.next();
            if (stmt instanceof DefinitionStmt) {
                Value rvalue = ((DefinitionStmt)stmt).getRightOp();
                if (!_isAliasableType(rvalue.getType())) {
                    continue;
                }
                if (rvalue instanceof NewExpr ||
                        rvalue instanceof NewArrayExpr) {
                    Object object = new String("Object" + index++ + ":" +
                            rvalue.toString());
                    _constructorMap.put(rvalue, object);
                } else if (rvalue instanceof Constant) {
                    Object object = new String("Constant" + index++ + ":" +
                            rvalue.toString());
                    _constructorMap.put(rvalue, object);
                }
            }
        }

        doAnalysis();
        if (_debug) {
            for (Iterator units = g.getBody().getUnits().iterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt) units.next();
                System.out.println("visited " + _countMap.get(stmt) +
                        " to " + stmt);
            }
        }
        _callGraph = null;
        _sideEffectAnalysis = null;
        _countMap = null;
    }

    /** Return the set of other fields and locals that may reference
     *  the same object as the given field, at a point before
     *  the given unit.  If there is no alias information, meaning the
     *  field can be aliased to anything, then return null.
     */
    public Set getAliasesOfBefore(SootField field, Unit unit) {
        GraphFlow map =
            (GraphFlow)getFlowBefore(unit);
        if (map.containsNode(field)) {
            HashSet set = new HashSet();
            for (Iterator objects = map.getSuccsOf(field).iterator();
                 objects.hasNext();) {
                set.addAll(map.getPredsOf(objects.next()));
            }
            return set;
        } else {
            return null;
        }
    }

    /** Return the set of other fields and locals that may reference
     *  the same object as the given field, at a point after
     *  the given unit.  If there is no alias information, meaning the
     *  field can be aliased to anything, then return null.
     */
    public Set getAliasesOfAfter(SootField field, Unit unit) {
        GraphFlow map =
            (GraphFlow)getFlowAfter(unit);
        if (map.containsNode(field)) {
            HashSet set = new HashSet();
            for (Iterator objects = map.getSuccsOf(field).iterator();
                 objects.hasNext();) {
                set.addAll(map.getPredsOf(objects.next()));
            }
            return set;
        } else {
            return null;
        }
    }

    /** Return the set of other fields and locals that may reference
     *  the same object as the given local, at a point before
     *  the given unit.  If there is no alias information, meaning the
     *  field can be aliased to anything, then return null.
     */
    public Set getAliasesOfBefore(Local local, Unit unit) {
        GraphFlow map =
            (GraphFlow)getFlowBefore(unit);
        if (map.containsNode(local)) {
            HashSet set = new HashSet();
            for (Iterator objects = map.getSuccsOf(local).iterator();
                 objects.hasNext();) {
                set.addAll(map.getPredsOf(objects.next()));
            }
            return set;
        } else {
            return null;
        }
    }

    /** Return the set of other fields and locals that may reference
     *  the same object as the given field, at a point after
     *  the given unit.  If there is no alias information, meaning the
     *  field can be aliased to anything, then return null.
     */
    public Set getAliasesOfAfter(Local local, Unit unit) {
        GraphFlow map =
            (GraphFlow)getFlowAfter(unit);
        if (map.containsNode(local)) {
            HashSet set = new HashSet();
            for (Iterator objects = map.getSuccsOf(local).iterator();
                 objects.hasNext();) {
                set.addAll(map.getPredsOf(objects.next()));
            }
            return set;
        } else {
            return null;
        }
    }


    // Formulation:
    protected Object newInitialFlow() {
        GraphFlow graph = new GraphFlow();
        graph.addNode(_universeRepresentative);
        graph.addNode(NullConstant.v());
        return graph;
    }

    protected void flowThrough(Object inValue, Object d, Object outValue) {
        GraphFlow in = (GraphFlow) inValue;
        GraphFlow out = (GraphFlow) outValue;
        Stmt unit = (Stmt)d;

        //    System.out.println("previous flow = " + outValue);
        // By default, the out is equal to the in.
        copy(inValue, outValue);

        if (_debug) System.out.println("maybe flow through " + d);

        if (_debug) {
            Integer i = (Integer)_countMap.get(d);
            if (i == null) {
                _countMap.put(d, new Integer(1));
            } else {
                _countMap.put(d, new Integer(i.intValue() + 1));
            }
        }

        // If we have a method invocation, then alias information
        // for fields that the method side effects is killed.
        // If no side effect information was specified in the constructor,
        // then conservatively kill the alias information for all fields.
        // This is a safe flow-insensitive approximation.
        if (unit.containsInvokeExpr()) {
            //if (_sideEffectAnalysis == null) {
            for (Iterator nodes = out.getNodes().iterator();
                 nodes.hasNext();) {
                Object node = nodes.next();
                if (node instanceof SootField) {
                    SootField field = (SootField) node;
                    // FIXME: This is not quite correct...
                    // A private field might
                    // be affected by a method that is called back.
                    // We really need to figure out a cheap side effect
                    // analysis to handle what the method call does.
                    if (field.isPrivate()) {
                        continue;
                    }
                    // If the field is final, then it can never be
                    // changed.  This is safe.
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (_debug) {
                        System.out.println("unit " + unit +
                                " kills " + field);
                    }
                    _setCanPointToAnything(out, node);
                }
            }
            /*  } else {
                InvokeExpr expr = (InvokeExpr)unit.getInvokeExpr();
                SootMethod method = expr.getMethod();
                if (_debug) System.out.println("invoking: " + method);
                Set allSideEffects = new HashSet();
                // Union the side effect sets over
                // all the possible targets
                List edges = _callGraph.getTargetsOf((Stmt)unit);
                for (Iterator i = edges.iterator();
                     i.hasNext();) {
                    Edge edge = (edge)i.next();
                    SootMethod target = edge.tgt();

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
                allSideEffects = out.keySet();
                }
                if (_debug) System.out.println("all Side effects = " + allSideEffects);
                for (Iterator i = allSideEffects.iterator();
                i.hasNext();) {
                Object object = i.next();
                if (object instanceof SootField) {
                _killAlias(out, object);
                }
                }
                }*/
        }
        if (unit instanceof DefinitionStmt) {
            DefinitionStmt assignStmt = (DefinitionStmt)unit;

            Value lvalue = assignStmt.getLeftOp();
            Value rvalue = assignStmt.getRightOp();

            if (_debug) {
                //   System.out.println("lvalueClass = " + lvalue.getClass());
                //    System.out.println("rvalueClass = " + rvalue.getClass());
            }

            // If we are dealing with aliasable objects.
            if (_isAliasableType(lvalue.getType())) {

                Object lobject = _getAliasObject(lvalue, out);
                Object robject = _getAliasObject(rvalue, out);

                if (!out.containsNode(lobject)) {
                    out.addNode(lobject);
                }
                if (!out.containsNode(robject) && robject != null) {
                    out.addNode(robject);
                }
                if (robject == null) {
                    //_setCanPointToAnything(out, lobject);
                } else {
                    _setCanPointTo(out, lobject, robject);
                }
            }
        }
        // otherwise, the alias info is unchanged.
        if (_debug) System.out.println("newflow = " + out);
    }

    protected void copy(Object inValue, Object outValue) {
        //      System.out.println("copy");
        GraphFlow in = (GraphFlow) inValue;
        GraphFlow out = (GraphFlow) outValue;

        if (in == out) return;

        // clear the output.
        out.clearAll();

        // Copy all the nodes.
        for (Iterator nodes = in.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            if (!out.containsNode(node)) {
                out.addNode(node);
            }
        }

        // Copy all the edges.
        for (Iterator nodes = in.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            for (Iterator successors = in.getSuccsOf(node).iterator();
                 successors.hasNext();) {
                Object successor = successors.next();
                out.addEdge(node, successor);
            }
        }
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        //     System.out.println("merge " + in1Value);
        //System.out.println(" with " + in2Value);
        GraphFlow in1 = (GraphFlow) in1Value;
        GraphFlow in2 = (GraphFlow) in2Value;
        GraphFlow out = (GraphFlow) outValue;

        copy(in1Value, outValue);

        // Union all the nodes.
        for (Iterator nodes = in2.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            if (!out.containsNode(node)) {
                out.addNode(node);
            }
        }

        // Union all the edges.
        for (Iterator nodes = in2.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            for (Iterator successors = in2.getSuccsOf(node).iterator();
                 successors.hasNext();) {
                out.addEdge(node, successors.next());
            }
        }
        //    System.out.println(" to " + outValue);
    }

    // Add lobject to the set of things that are aliased by rObject.
    private void _setCanPointTo(GraphFlow graph,
            Object lobject, Object robject) {
        // Remove all the old edges.
        List list = new ArrayList();
        list.addAll(graph.getSuccsOf(lobject));
        for (Iterator successors = list.iterator();
             successors.hasNext();) {
            Object target = successors.next();
            graph.removeEdge(lobject, target);
        }

        list.clear();

        list.addAll(graph.getSuccsOf(robject));
        // Add the edges from the right object.
        for (Iterator successors = list.iterator();
             successors.hasNext();) {
            Object target = successors.next();
            graph.addEdge(lobject, target);
        }
    }

    // Set the given object to point to all possible objects.
    private void _setCanPointToAnything(GraphFlow graph,
            Object object) {

        graph.addEdge(object, _universeRepresentative);
        for (Iterator constructors = _constructorMap.keySet().iterator();
             constructors.hasNext();) {
            Value constructor = (Value)constructors.next();
            //  System.out.println("constructorTarget = " +
            //        _constructorMap.get(constructor));
            //System.out.println("constructorType = " +
            //        constructor.getType());
            if (object instanceof SootField) {
                SootField field = (SootField)object;
                // Only things that have a compatible type can be pointed to..
                Type constructorType = constructor.getType();
                Type fieldType = field.getType();
                if (!_isCompatibleAliasAssignment(constructorType,
                        fieldType)) {
                    continue;
                }
            }
            Object target = _constructorMap.get(constructor);
            if (graph.containsNode(target)) {
                graph.addEdge(object, target);
            }
        }
    }

    private Object _getAliasObject(Value value,
            GraphFlow graph) {
        if (value instanceof Local) {
            return value;
        } else if (value instanceof FieldRef) {
            SootField field = ((FieldRef)value).getField();
            if (!graph.containsNode(field)) {
                // If we haven't defined the field yet, then it should
                // point to the universe.
                graph.addNode(field);
                graph.addEdge(field, _universeRepresentative);
            }
            return field;
        } else if (value instanceof ArrayRef) {
            Value base = ((ArrayRef)value).getBase();
            return base;
        } else if (value instanceof CastExpr) {
            return ((CastExpr)value).getOp();
        } else if (value instanceof NewExpr ||
                value instanceof NewArrayExpr ||
                value instanceof Constant) {
            // If we haven't defined the expression yet, then it should
            // point to a new object.
            Object object = _constructorMap.get(value);
            if (!graph.containsNode(value)) {
                graph.addNode(object);
                graph.addNode(value);
                graph.addEdge(value, object);
            }
            return value;
        } else if (value instanceof IdentityRef) {
            if (!graph.containsNode(value)) {
                // If we haven't defined the method argument yet,
                // then it should point to the universe.
                graph.addNode(value);
                graph.addEdge(value, _universeRepresentative);
            }
            return value;
        } else {
            return null;
        }
    }

    private boolean _isAliasableType(Type type) {
        // We include null type here, since we can have null constants...
        if (type instanceof ArrayType ||
                type instanceof RefType ||
                type instanceof NullType) {
            return true;
        } else {
            return false;
        }
    }

    // Return true if a reference object with the given type could
    // possibly point to an aliasable object of the given type.
    private boolean _isCompatibleAliasAssignment(Type objectType,
            Type referenceType) {
        // System.out.println("objectType = " + objectType);
        //System.out.println("referenceType = " + objectType);
        if (objectType instanceof ArrayType &&
                referenceType instanceof ArrayType) {
            objectType = ((ArrayType)objectType).baseType;
            referenceType = ((ArrayType)referenceType).baseType;
            // If either one is not an array of objects, then
            // return false.
            if (!(objectType instanceof RefType) ||
                    !(referenceType instanceof RefType)) {
                if (objectType.equals(referenceType)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // If they are both object types, or both arrays of objects,
        // then check if the classes of each are compatible.
        if (objectType instanceof RefType &&
                referenceType instanceof RefType) {
            Type mergeType = objectType.merge(referenceType, Scene.v());
            //    System.out.println("mergeType = " + mergeType);
            // If the field cannot point to the object
            // created by the constructor, then we don't need
            // to create a possible alias.
            if (mergeType.equals(referenceType)) {
                //  System.out.println("killing");
                return true;
            }
        }
        // They aren't even both object references,
        return false;
    }

    // A class that represents the points-to graph at a point in the
    // program.
    private class GraphFlow extends HashMutableDirectedGraph {
        public boolean equals(Object object) {
            if (!(object instanceof GraphFlow)) {
                return false;
            }
            GraphFlow flow = (GraphFlow)object;

            if (!_listEquals(getNodes(), flow.getNodes())) {
                //  System.out.println("nodes");
                return false;
            }
            for (Iterator heads = flow.getNodes().iterator();
                 heads.hasNext();) {
                Object head = heads.next();
                if (!_listEquals(flow.getSuccsOf(head), getSuccsOf(head))) {
                    //  System.out.println("output from head = " + head);
                    return false;
                }
            }
            //   System.out.println("returning true");
            return true;
        }

        public String toString() {
            String string = "nodes = " + getNodes();
            for (Iterator nodes = getNodes().iterator();
                 nodes.hasNext();) {
                Object source = nodes.next();
                string += "\nsource = " + source + " targets = ";
                for (Iterator succs = getSuccsOf(source).iterator();
                     succs.hasNext();) {
                    string += succs.next() + ",";
                }
            }
            return string;
        }
        private boolean _listEquals(List list1, List list2) {
            List copy1 = new ArrayList();
            copy1.addAll(list1);
            Collections.sort(copy1, _comparator);
            List copy2 = new ArrayList();
            copy2.addAll(list2);
            Collections.sort(copy2, _comparator);
            boolean flag = copy1.equals(copy2);
            // if (!flag && _debug) {
            //    System.out.println("list1 = " + list1);
            //    System.out.println("list2 = " + list2);
            //}
            return flag;
        }

        private Comparator _comparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    int flag = o1.toString().compareTo(o2.toString());
                    if (flag == 0) {
                        if (o1.hashCode() < o2.hashCode()) {
                            return -1;
                        } else if (o1.hashCode() > o2.hashCode()) {
                            return 1;
                        }
                    }
                    return flag;
                }
            };
    }

    // A map from a statement to the number of times that statement
    // is visited.  Used for debugging and profiling.
    private HashMap _countMap;

    // An object that symbolically represents objects that are not
    // explicitly represented.  Unknown references are assumed to
    // point here until we see an assignment statement that sets the
    // reference to point to some other object.
    private Object _universeRepresentative =
    new String("universeRepresentative");

    // A map from constructor statements to the object that represents
    // the result of the constructor.  This is used to ensure that we
    // allocate exactly one representative for each constructor statement.
    private Map _constructorMap;

    // The invoke graph that is used to track method calls.
    private CallGraph _callGraph;

    // The analysis that provides side effect information for methods.
    private SideEffectAnalysis _sideEffectAnalysis;

    // This object is used as a key into the flow maps.
    // It maps to the set of objects that we encounter
    // which can be mapped to anything.
    private static Object allAliasKey = new Object();

    // True for debugging output
    private static boolean _debug = false;
}
