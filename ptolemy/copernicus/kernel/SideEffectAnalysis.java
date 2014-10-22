/* An analysis for detecting objects that must be aliased to each other.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.copernicus.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.EntryPoints;
import soot.Hierarchy;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.Sources;
import soot.util.queue.ChunkedQueue;

/**
 An analysis that determines which methods in a given call graph
 have no side effects.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SideEffectAnalysis {
    public SideEffectAnalysis() {
        _methodToEffectFlow = new HashMap();
        _unprocessedMethods = new ChunkedQueue();

        Iterator methods = _unprocessedMethods.reader();

        CallGraph callGraph = Scene.v().getCallGraph();
        _reachables = new ReachableMethods(callGraph, EntryPoints.v()
                .application());
        _reachables.update();

        // Process all the reachableMethods.
        for (Iterator reachableMethods = _reachables.listener(); reachableMethods
                .hasNext();) {
            _addMethod((SootMethod) reachableMethods.next());
        }

        while (methods.hasNext()) {
            SootMethod nextMethod = (SootMethod) methods.next();
            EffectFlow in = _getEffectFlow(nextMethod);
            EffectFlow out = _processMethod(nextMethod);

            // If the flow has changed, then add all the reachable
            // methods that invoke this method.
            if (!in.equals(out)) {
                _setEffectFlow(nextMethod, out);

                for (Iterator invokers = new Sources(
                        callGraph.edgesInto(nextMethod)); invokers.hasNext();) {
                    SootMethod invoker = (SootMethod) invokers.next();

                    if (_reachables.contains(invoker)) {
                        _addMethod(invoker);
                    }
                }
            }
        }
    }

    //    private void _addMethod(Collection set) {
    //        for (Iterator i = set.iterator(); i.hasNext();) {
    //            _addMethod((SootMethod) i.next());
    //        }
    //    }

    private void _addMethod(SootMethod method) {
        // System.out.println("adding method " + method);
        if (_getEffectFlow(method) == null) {
            _setEffectFlow(method, new EffectFlow());
        }

        _unprocessedMethods.add(method);
    }

    private EffectFlow _getEffectFlow(SootMethod method) {
        return (EffectFlow) _methodToEffectFlow.get(method);
    }

    // Merge the flow for the given method with the given flow.
    // If there is no flow for the given method (i.e. it is not reachable)
    // then set the given flow to have unknown side effects.
    private void _mergeFlow(EffectFlow flow, SootMethod method) {
        EffectFlow targetFlow = _getEffectFlow(method);

        if (targetFlow != null) {
            flow.mergeEffectFlow(targetFlow);
        } else {
            flow.setUnknownSideEffects();
        }
    }

    private void _setEffectFlow(SootMethod method, EffectFlow flow) {
        _methodToEffectFlow.put(method, flow);
    }

    /** Return the set of fields that the given method assigns
     *  to, or null if the side effects are unknown.
     */
    public Set getSideEffects(SootMethod method) {
        EffectFlow flow = _getEffectFlow(method);

        if (flow == null) {
            if (_debug) {
                System.out.println("SideEffectAnalysis: Method not found: "
                        + method);
            }

            return null;
        }

        if (flow.hasEffects()) {
            return flow.effectSet();
        } else {
            return new HashSet();
        }
    }

    /** Return true if the given method has any side effects.
     *  i.e. it assigns to any fields or arrays.
     */
    public boolean hasSideEffects(SootMethod method) {
        EffectFlow flow = _getEffectFlow(method);
        EffectFlow flow2 = _getEffectFlow(method);

        if (flow == null) {
            if (_debug) {
                System.out.println("SideEffectAnalysis: Method not found: "
                        + method);
            }

            return true;
        }

        return flow.hasEffects() || flow2.hasEffects();
    }

    /** Return true if the given method has any side effects
     *  on the given field.  i.e. it assigns to the given field.
     */
    public boolean hasSideEffects(SootMethod method, SootField field) {
        EffectFlow flow = _getEffectFlow(method);

        if (flow == null) {
            if (_debug) {
                System.out.println("SideEffectAnalysis: Method not found: "
                        + method);
            }

            return true;
        }

        return flow.hasEffects(field);
    }

    // Formulation:  An instance of the EffectFlow class.  If there
    // are no side effects, then the flow has _hasEffects == false;
    // if _hasEffects == true, then the effectSet is the set of fields
    // that are side effected, or null if any field may be side effected.
    private EffectFlow _processMethod(SootMethod method) {
        EffectFlow in = _getEffectFlow(method);
        EffectFlow out = new EffectFlow();

        if (_debug) {
            System.out.println("SideEffectAnalysis: method = " + method);
        }

        if (_debug) {
            System.out.println("input flow = " + in.effectSet());
        }

        out.setEffectFlow(in);

        // A method that is a context class is assumed to have side effects,
        // since we can't get it's method body.  Note that we could do better
        // by handling each method specifically.
        // (For Example, Thread.currentThread()
        // has no body, but also has no side effects).
        if (!method.isConcrete()) {
            if (_debug) {
                System.out.println("SideEffectAnalysis: has no body.");
            }

            out.setUnknownSideEffects();
            return out;
        }

        if (_debug) {
            System.out.println("output flow = " + out.hasEffects() + " "
                    + out.effectSet());
        }

        // A method has side effects if it sets the values of any fields.
        Body body = method.retrieveActiveBody();
        Scene.v().releaseActiveHierarchy();

        for (Iterator units = body.getUnits().iterator(); units.hasNext();) {
            Unit unit = (Unit) units.next();

            if (_debug) {
                System.out.println("unit = " + unit);
            }

            for (Iterator boxes = unit.getDefBoxes().iterator(); boxes
                    .hasNext();) {
                ValueBox box = (ValueBox) boxes.next();
                Value value = box.getValue();

                if (value instanceof FieldRef) {
                    if (_debug) {
                        System.out
                                .println("SideEffectAnalysis: assigns to field");
                    }

                    out.addSideEffect(((FieldRef) value).getField());
                }

                if (value instanceof ArrayRef) {
                    if (_debug) {
                        System.out
                                .println("SideEffectAnalysis: assigns to array.");
                    }

                    // Escape analysis might help in this case.
                    out.setUnknownSideEffects();
                    return out;
                }
            }

            // Method calls that are in the invokeGraph
            // have already been checked.
            // However, it turns out that context classes
            // are not included in the
            // invokeGraph!  This checks to see if there
            // are any invocations of
            // methods that are not in the invoke graph.  Conservatively
            // assume that they have side effects.
            for (Iterator boxes = unit.getUseBoxes().iterator(); boxes
                    .hasNext();) {
                ValueBox box = (ValueBox) boxes.next();
                Value expr = box.getValue();

                if (expr instanceof InvokeExpr) {
                    SootMethod invokedMethod = ((InvokeExpr) expr).getMethod();

                    // It appears that soot does not automatically
                    // release the hierarchy.
                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();

                    if (expr instanceof SpecialInvokeExpr) {
                        SootMethod target = hierarchy.resolveSpecialDispatch(
                                (SpecialInvokeExpr) expr, invokedMethod);
                        Scene.v().releaseActiveHierarchy();
                        _mergeFlow(out, target);
                    } else if (expr instanceof InstanceInvokeExpr) {
                        Type baseType = ((InstanceInvokeExpr) expr).getBase()
                                .getType();

                        if (!(baseType instanceof RefType)) {
                            // We can invoke methods on arrays...
                            // Ignore them here.
                            continue;
                        }

                        List list = hierarchy.resolveAbstractDispatch(
                                ((RefType) baseType).getSootClass(),
                                invokedMethod);
                        Scene.v().releaseActiveHierarchy();
                        for (Iterator targets = list.iterator(); targets
                                .hasNext();) {
                            SootMethod target = (SootMethod) targets.next();
                            _mergeFlow(out, target);
                        }
                    } else if (expr instanceof StaticInvokeExpr) {
                        SootMethod target = ((StaticInvokeExpr) expr)
                                .getMethod();
                        _mergeFlow(out, target);
                    }
                }
            }
        }

        if (_debug) {
            System.out.println("output flow = " + out.hasEffects() + " "
                    + out.effectSet());
        }

        return out;
    }

    private static class EffectFlow {
        public EffectFlow() {
            _hasEffects = false;
            _effectSet = null;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (o instanceof EffectFlow) {
                EffectFlow other = (EffectFlow) o;

                if (_hasEffects != other.hasEffects()) {
                    return false;
                } else if (_effectSet == null) {
                    if (other.effectSet() != null) {
                        return false;
                    } else {
                        // other.effectSet() == null
                        return true;
                    }
                } else {
                    return _effectSet.equals(((EffectFlow) o).effectSet());
                }
            } else {
                return false;
            }
        }

        /** Return the hash code for the EffectFlow object. If two
         *  EffectFlow objects contains the same EffectSet then they
         *  have the same hashcode.
         *  @return The hash code for this EffectFlow object.
         */
        @Override
        public int hashCode() {
            // See http://www.technofundo.com/tech/java/equalhash.html
            int hashCode = 31;
            if (_effectSet != null) {
                hashCode = 31 * hashCode + _effectSet.hashCode();
            }
            if (_hasEffects) {
                hashCode = 31 * hashCode + 1;
            }
            return hashCode;
        }

        public void addSideEffect(SootField field) {
            if (_hasEffects) {
                if (_effectSet != null) {
                    _effectSet.add(field);
                }
            } else {
                _hasEffects = true;
                _effectSet = new HashSet();
                _effectSet.add(field);
            }
        }

        public void mergeEffectFlow(EffectFlow flow) {
            if (flow.hasUnknownSideEffects()) {
                // If the flow has unknown effects, then we will
                // have unknown effects.
                setUnknownSideEffects();
            } else if (!flow.hasEffects()) {
                // If the flow has no effects, then we have no
                // change.
                return;
            } else if (_hasEffects) {
                if (_effectSet != null) {
                    _effectSet.addAll(flow.effectSet());
                } // else we have unknown side effects already.
            } else {
                _hasEffects = true;
                _effectSet = new HashSet();
                _effectSet.addAll(flow.effectSet());
            }
        }

        public boolean hasEffects(SootField field) {
            if (_hasEffects) {
                if (_effectSet != null) {
                    return _effectSet.contains(field);
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        public Set effectSet() {
            return _effectSet;
        }

        public boolean hasEffects() {
            return _hasEffects;
        }

        public void setEffectFlow(EffectFlow flow) {
            _hasEffects = flow.hasEffects();

            if (flow.effectSet() == null) {
                _effectSet = null;
            } else {
                _effectSet = new HashSet();
                _effectSet.addAll(flow.effectSet());
            }
        }

        public void setUnknownSideEffects() {
            _hasEffects = true;
            _effectSet = null;
        }

        public boolean hasUnknownSideEffects() {
            return _hasEffects && _effectSet == null;
        }

        private boolean _hasEffects;

        private Set _effectSet;
    }

    private boolean _debug = false;

    private ReachableMethods _reachables = null;

    private ChunkedQueue _unprocessedMethods = null;

    private HashMap _methodToEffectFlow = null;
}
