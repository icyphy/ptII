/* A transformer that replaces port communication in an SDF model

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

import soot.HasPhaseOptions;
import soot.Local;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;


//////////////////////////////////////////////////////////////////////////
//// InlinePortTransformer
/**
A Transformer that is responsible for inlining the communication between ports.
The connections between the ports are taken from the model specified in the
constructor of this transformer.

FIXME: currently we try to speed things up if the buffersize is only
one by removing the index update overhead.  Note that there are other
optimizations that can be made here (for instance, if we can
statically determine all the channel references (which is trivially
true if there is only one channel), then there is no need to have the
index or portbuffer arrays.
@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class InlinePortTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private InlinePortTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlinePortTransformer v(CompositeActor model) {
        return new InlinePortTransformer(model);
    }

    /** Return the name of the field that is created to
     *  represent the given channel of the given type of the
     *  given relation.
     */
    public static String getBufferFieldName(TypedIORelation relation,
            int channel, ptolemy.data.type.Type type) {
        return "_" + StringUtilities.sanitizeName(relation.getName())
            + "_" + channel
            + "_" + StringUtilities.sanitizeName(type.toString());
    }

    /** Return the port inliner for the given model.
     */
    public static PortInliner getPortInliner(CompositeActor model) {
        return (PortInliner)_modelToPortInliner.get(model);
    }

    /** Set the port inliner for the given model.
     *  This method is expected to be called by the Director inliner when
     *  when a decision is made about how to inline ports.
     */
    public static void setPortInliner(
            CompositeActor model, PortInliner inliner) {
        _modelToPortInliner.put(model, inliner);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlinePortTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _phaseName = phaseName;
        _debug = PhaseOptions.getBoolean(options, "debug");

        _inlineAllPortsIn(ModelTransformer.getModelClass(), _model);

    }

    // Inline methods in all classes, starting at the bottom of the
    // hierarchy...
    private void _inlineAllPortsIn(
            SootClass modelClass, CompositeActor model) {
        Director director = model.getDirector();

        // Loop over all the model instance classes.
        for (Iterator entities = model.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);

            // recurse.
            if (entity instanceof CompositeActor) {
                _inlineAllPortsIn(entityClass, (CompositeActor)entity);
            }
        }

        PortInliner inliner = getPortInliner(model);
        if (inliner == null) {
            throw new RuntimeException("Port methods cannot be inlined for " +
                    director.getClass().getName());
        }

        // Initialize the inliner
        inliner.initialize();
        _inlinePortCalls(modelClass, model, inliner);
    }

    // inline inside port calls at for the given model, and
    // outside port calls for the entities of the given model.
    private void _inlinePortCalls(
            SootClass modelClass, CompositeActor model,
            PortInliner inliner) {

        // Loop through all the methods and inline calls on ports.
        for (Iterator methods = modelClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            if(_debug) {
                System.out.println("inline inside port body of " +
                        method + " = " + body);
            }
            boolean moreToDo = true;
            while (moreToDo) {
                moreToDo = _inlineInsideMethodCalls(
                        modelClass, method, body,
                        inliner, _debug);
                LocalNameStandardizer.v().transform(body,
                        _phaseName + ".lns");
            }
        }

        // Loop over all the model instance classes.
        for (Iterator entities = model.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);

            // Loop through all the methods and replace calls on ports.
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                //System.out.println("Replacing port invocations in" + method);
                // System.out.println("method = " + method);

                boolean moreToDo = true;
                while (moreToDo) {
                    moreToDo = _inlineMethodCalls(
                            modelClass, entityClass, method, body,
                            inliner, _debug);
                    LocalNameStandardizer.v().transform(body,
                            _phaseName + ".lns");
                }
            }
        }
    }

    private boolean _inlineMethodCalls(SootClass modelClass,
            SootClass theClass, SootMethod method, JimpleBody body,
            PortInliner inliner, boolean debug) {
        if (debug) System.out.println("Inlining method calls in method " + method);
                          
        boolean doneSomething = false;
        // System.out.println("portToIndexArrayField = " + portToIndexArrayField);
        //System.out.println("portToInsideIndexArrayField = " + portToInsideIndexArrayField);

        CompleteUnitGraph unitGraph =
            new CompleteUnitGraph(body);
        // This will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            if (!stmt.containsInvokeExpr()) {
                continue;
            }
            ValueBox box = stmt.getInvokeExprBox();
            Value value = stmt.getInvokeExpr();
            if (value instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr r = (InstanceInvokeExpr)value;

                if (r.getBase().getType() instanceof RefType) {
                    RefType type = (RefType)r.getBase().getType();

                    // Inline calls to connections changed.
                    if (r.getMethod().equals(PtolemyUtilities.connectionsChangedMethod)) {
                        // If we are calling connections changed on one of the classes
                        // we are generating code for, then inline it.
                        if (type.getSootClass().isApplicationClass()) {
                            SootMethod inlinee = null;
                            if (r instanceof VirtualInvokeExpr) {
                                // Now inline the resulting call.
                                List methodList =
                                    Scene.v().getActiveHierarchy().resolveAbstractDispatch(
                                            type.getSootClass(), PtolemyUtilities.connectionsChangedMethod);
                                if (methodList.size() == 1) {
                                    // Inline the method.
                                    inlinee = (SootMethod)methodList.get(0);
                                } else {
                                    String string = "Can't inline " + stmt +
                                        " in method " + method + "\n";
                                    for (int i = 0; i < methodList.size(); i++) {
                                        string += "target = " + methodList.get(i) + "\n";
                                    }
                                    System.out.println(string);
                                }
                            } else if (r instanceof SpecialInvokeExpr) {
                                inlinee = Scene.v().getActiveHierarchy().resolveSpecialDispatch(
                                        (SpecialInvokeExpr)r, method);
                            }
                            if (!inlinee.getDeclaringClass().isApplicationClass()) {
                                inlinee.getDeclaringClass().setLibraryClass();
                            }
                            inlinee.retrieveActiveBody();
                            if (debug) System.out.println("Inlining method call: " + r);
                            SiteInliner.inlineSite(inlinee, stmt, method);

                            doneSomething = true;
                        } else {
                                // FIXME: this is a bit of a hack, but
                                // for right now it seems to work.
                                // How many things that aren't
                                // the actors we are generating
                                // code for do we really care about here?
                                // Can we do this without having to create
                                // a class for the port too????
                            body.getUnits().remove(stmt);
                            doneSomething = true;
                        }
                    }

                    // Statically evaluate constant arguments.
                    Value argValues[] = new Value[r.getArgCount()];
                    int constantArgCount = 0;
                    for (Iterator args = r.getArgs().iterator();
                         args.hasNext();) {
                        Value arg = (Value)args.next();
                        //System.out.println("arg = " + arg);
                        if (Evaluator.isValueConstantValued(arg)) {
                            argValues[constantArgCount++] = Evaluator.getConstantValueOf(arg);
                                // System.out.println("argument = " + argValues[argCount-1]);
                        } else {
                            break;
                        }
                    }
                    boolean allArgsAreConstant = (r.getArgCount() == constantArgCount);

                    if (SootUtilities.derivesFrom(type.getSootClass(),
                            PtolemyUtilities.componentPortClass)) {
                        // If we are invoking a method on a port
                        // class, then attempt to get the constant
                        // value of the port.
                        TypedIOPort port = (TypedIOPort)
                            getPortValue(method, (Local)r.getBase(),
                                    stmt, localDefs, localUses);
                        //     System.out.println("reference to port = " + port);

                        if (port == null) {
                            continue;
                        }

                        // If we do this, then we have to get rid of
                        // the ports.
                        if (port instanceof Typeable) {
                            PtolemyUtilities.inlineTypeableMethods(body,
                                   stmt, box, r, (Typeable)port);

                        }

                        // Inline namedObj methods on the attribute.
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getFullNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    port.getFullName()));
                        }
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    port.getName()));
                        }

                        String methodName = r.getMethod().getName();
                        if (port.getWidth() == 0 &&
                                (methodName.equals("hasToken") ||
                                        methodName.equals("hasRoom") ||
                                        methodName.equals("get") ||
                                        methodName.equals("put"))) {
                            // NOTE: broadcast is legal on a zero
                            // width port.
                            
                            // If we try to get on a port with
                            // zero width, then throw a runtime
                            // exception.
                            Local local = SootUtilities.createRuntimeException(body, stmt,
                                    methodName + "() called on a port with zero width: " +
                                    port.getFullName() + "!");
                            body.getUnits().insertBefore(Jimple.v().newThrowStmt(local),
                                    stmt);
                            if (stmt instanceof DefinitionStmt) {
                                // be sure we replace with the
                                // right return type.
                                if (methodName.equals("hasToken") ||
                                        methodName.equals("hasRoom")) {
                                    box.setValue(IntConstant.v(0));
                                } else {
                                    box.setValue(NullConstant.v());
                                }
                            } else {
                                body.getUnits().remove(stmt);
                            }
                            continue;
                        }

                        if (r.getMethod().getName().equals("isInput")) {
                            if(debug) {
                                System.out.println(
                                        "replacing isInput at " 
                                        + stmt);
                            } 
                            if (port.isInput()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("isOutput")) {
                            if(debug) {
                                System.out.println(
                                        "replacing isOutput at " 
                                        + stmt);
                            }   
                            if (port.isOutput()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("isMultiport")) {
                            if(debug) {
                                System.out.println(
                                        "replacing isMultiport at " 
                                        + stmt);
                            }      
                            if (port.isMultiport()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("getWidth")) {
                            if(debug) {
                                System.out.println("replacing getWidth at " 
                                        + stmt);
                            }  
                            // Reflect and invoke the same method on our port
                            Object object = 
                                SootUtilities.reflectAndInvokeMethod(
                                        port, r.getMethod(), argValues);
                            // System.out.println("method result  = " + constant);
                            Constant constant =
                                SootUtilities.convertArgumentToConstantValue(
                                        object);
                            
                            // replace the method invocation.
                            box.setValue(constant);
                        } else if (r.getMethod().getName().equals("hasToken")) {
                            // return true.
                            if(debug) {
                                System.out.println("replacing hasToken at " 
                                        + stmt);
                            }  
                            box.setValue(IntConstant.v(1));
                        } else if (r.getMethod().getName().equals("hasRoom")) {
                            // return true.
                            if(debug) {
                                System.out.println("replacing hasRoom at " + stmt);
                            }
                            box.setValue(IntConstant.v(1));
                        } else if (r.getMethod().getName().equals("get")) {
                            // Could be get that takes a channel and
                            // returns a token, or get that takes a
                            // channel and a count and returns an
                            // array of tokens.  In either case,
                            // replace the get with circular array
                            // ref.
                            if(debug) {
                                System.out.println("replacing get at " + stmt);
                            }
                            inliner.inlineGet(body, stmt, box, r, port);


                        } else if (r.getMethod().getName().equals("send")) {
                            // Could be send that takes a channel and
                            // returns a token, or send that takes a
                            // channel and an array of tokens.  In
                            // either case, replace the send with
                            // circular array ref.
                            if(debug) {
                                System.out.println("replacing send at " 
                                        + stmt);
                            }
                            inliner.inlineSend(body, stmt, r, port);

                        } else if (r.getMethod().getName().equals("broadcast")) {
                            // Broadcasting on a port of zero width does
                            // nothing.
                            if (port.getWidth() == 0) {
                                if(debug) {
                                    System.out.println(
                                            "removing width zero broadcast at" 
                                            + stmt);
                                }
                                body.getUnits().remove(stmt);
                            } else {
                                // Could be broadcast that takes a
                                // token, or broadcast that takes an
                                // array of tokens.  In either case,
                                // replace the broadcast with circular
                                // array ref.
                                if(debug) {
                                    System.out.println(
                                            "replacing broadcast at" 
                                            + stmt);
                                }
                                inliner.inlineBroadcast(body, stmt, r, port);
                            }
                        }
                    }
                }
            }
        }
        return doneSomething;
    }

    private boolean _inlineInsideMethodCalls(
            SootClass theClass, SootMethod method, JimpleBody body,
            PortInliner inliner, boolean debug) {
        if (debug) System.out.println("Inlining inside method calls in method " + method);
                          
        boolean doneSomething = false;

        // System.out.println("portToIndexArrayField = " + portToIndexArrayField);
        //System.out.println("portToInsideIndexArrayField = " + portToInsideIndexArrayField);

        CompleteUnitGraph unitGraph =
            new CompleteUnitGraph(body);
        // This will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            if (!stmt.containsInvokeExpr()) {
                continue;
            }
            ValueBox box = stmt.getInvokeExprBox();
            Value value = stmt.getInvokeExpr();
            if (value instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr r = (InstanceInvokeExpr)value;

                if (r.getBase().getType() instanceof RefType) {
                    RefType type = (RefType)r.getBase().getType();

                    // Inline calls to connections changed.
                    if (r.getMethod().equals(PtolemyUtilities.connectionsChangedMethod)) {
                        // If we are calling connections changed on one of the classes
                        // we are generating code for, then inline it.
                        if (type.getSootClass().isApplicationClass()) {
                            SootMethod inlinee = null;
                            if (r instanceof VirtualInvokeExpr) {
                                // Now inline the resulting call.
                                List methodList =
                                    Scene.v().getActiveHierarchy().resolveAbstractDispatch(
                                            type.getSootClass(), PtolemyUtilities.connectionsChangedMethod);
                                if (methodList.size() == 1) {
                                    // Inline the method.
                                    inlinee = (SootMethod)methodList.get(0);
                                } else {
                                    String string = "Can't inline " + stmt +
                                        " in method " + method + "\n";
                                    for (int i = 0; i < methodList.size(); i++) {
                                        string += "target = " + methodList.get(i) + "\n";
                                    }
                                    System.out.println(string);
                                }
                            } else if (r instanceof SpecialInvokeExpr) {
                                inlinee = Scene.v().getActiveHierarchy().resolveSpecialDispatch(
                                        (SpecialInvokeExpr)r, method);
                            }
                            if (!inlinee.getDeclaringClass().isApplicationClass()) {
                                inlinee.getDeclaringClass().setLibraryClass();
                            }
                            inlinee.retrieveActiveBody();
                            if (debug) System.out.println("Inlining method call: " + r);
                            SiteInliner.inlineSite(inlinee, stmt, method);

                            doneSomething = true;
                        } else {
                                // FIXME: this is a bit of a hack, but
                                // for right now it seems to work.
                                // How many things that aren't
                                // the actors we are generating
                                // code for do we really care about here?
                                // Can we do this without having to create
                                // a class for the port too????
                            body.getUnits().remove(stmt);
                            doneSomething = true;
                        }
                    }

                    // Statically evaluate constant arguments.
                    Value argValues[] = new Value[r.getArgCount()];
                    int constantArgCount = 0;
                    for (Iterator args = r.getArgs().iterator();
                         args.hasNext();) {
                        Value arg = (Value)args.next();
                        //System.out.println("arg = " + arg);
                        if (Evaluator.isValueConstantValued(arg)) {
                            argValues[constantArgCount++] = Evaluator.getConstantValueOf(arg);
                                // System.out.println("argument = " + argValues[argCount-1]);
                        } else {
                            break;
                        }
                    }
                    boolean allArgsAreConstant = (r.getArgCount() == constantArgCount);

                    if (SootUtilities.derivesFrom(type.getSootClass(),
                            PtolemyUtilities.componentPortClass)) {
                        // If we are invoking a method on a port
                        // class, then attempt to get the constant
                        // value of the port.
                        TypedIOPort port = (TypedIOPort)
                            getPortValue(method, (Local)r.getBase(),
                                    stmt, localDefs, localUses);
                        //     System.out.println("reference to port = " + port);

                        if (port == null) {
                            continue;
                        }

                        /** Don't do this for inside connections, to
                         * allow for properly defined ports of
                         * toplevel composites.
                         */
                     //    if (port instanceof Typeable) {
//                             PtolemyUtilities.inlineTypeableMethods(body,
//                                     stmt, box, r, (Typeable)port);

//                         }

                        // Inline namedObj methods on the attribute.
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getFullNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    port.getFullName()));
                        }
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    port.getName()));
                        }

                        String methodName = r.getMethod().getName();
                        //   if (port.getWidth() == 0 &&
                        //                                     (methodName.equals("hasToken") ||
                        //                                             methodName.equals("hasRoom") ||
                        //                                             methodName.equals("get") ||
                        //                                             methodName.equals("put"))) {
                        //                                 // NOTE: broadcast is legal on a zero
                        //                                 // width port.

                        //                                 // If we try to get on a port with
                        //                                 // zero width, then throw a runtime
                        //                                 // exception.
                        //                                 Local local = SootUtilities.createRuntimeException(body, stmt,
                        //                                         methodName + "() called on a port with zero width: " +
                        //                                         port.getFullName() + "!");
                        //                                 body.getUnits().insertBefore(Jimple.v().newThrowStmt(local),
                        //                                         stmt);
                        //                                 if (stmt instanceof DefinitionStmt) {
                        //                                     // be sure we replace with the
                        //                                     // right return type.
                        //                                     if (methodName.equals("hasToken") ||
                        //                                             methodName.equals("hasRoom")) {
                        //                                         box.setValue(IntConstant.v(0));
                        //                                     } else {
                        //                                         box.setValue(NullConstant.v());
                        //                                     }
                        //                                 } else {
                        //                                     body.getUnits().remove(stmt);
                        //                                 }
                        //                                 continue;
                        //                             }

                        if (r.getMethod().getName().equals("isInput")) {
                                // return true.
                            if (port.isInput()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("isOutput")) {
                                // return true.
                            if (port.isOutput()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("isMultiport")) {
                                // return true.
                            if (port.isMultiport()) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                box.setValue(IntConstant.v(0));
                            }
                        } else if (r.getMethod().getName().equals("getWidth")) {
                            // Reflect and invoke the same method on our port
                            Object object = SootUtilities.reflectAndInvokeMethod(
                                    port, r.getMethod(), argValues);
                            // System.out.println("method result  = " + constant);
                            Constant constant =
                                SootUtilities.convertArgumentToConstantValue(object);

                            // replace the method invocation.
                            box.setValue(constant);
                        } else if (r.getMethod().getName().equals("hasToken")) {
                            // return true.
                            if(debug) {
                                System.out.println("inlining hasToken at " 
                                        + stmt);
                            }   
                            box.setValue(IntConstant.v(1));
                        } else if (r.getMethod().getName().equals("hasRoom")) {
                            // return true.
                            if(debug) {
                                System.out.println("inlining hasRoom at " 
                                        + stmt);
                            }
                            box.setValue(IntConstant.v(1));
                        } else if (r.getMethod().getName().equals("getInside")) {
                            // Could be get that takes a channel and
                            // returns a token, or get that takes a
                            // channel and a count and returns an
                            // array of tokens.  In either case,
                            // replace the get with circular array
                            // ref.
                            if(debug) {
                                System.out.println("inlining getInside at " 
                                        + stmt);
                            }
                            inliner.inlineGetInside(body, stmt, box, r, port);

                        } else if (r.getMethod().getName().equals("sendInside")) {
                            // Could be send that takes a channel and
                            // returns a token, or send that takes a
                            // channel and an array of tokens.  In
                            // either case, replace the send with
                            // circular array ref.
                            
                            if(debug) {
                                System.out.println("inlining sendInside at " 
                                        + stmt);
                            }
                            inliner.inlineSendInside(body, stmt, r, port);
                            
                        }
                    }

                }
            }
        }
        return doneSomething;
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a variable type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise return null.
     */
    public static TypedIOPort getPortValue(SootMethod method, Local local,
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof Local) {
                return getPortValue(method, (Local)value,
                        stmt, localDefs, localUses);
            } else if (value instanceof CastExpr) {
                return getPortValue(method, (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                return _getFieldValueTag(field);
            } else if (value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while (pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if (pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt =
                            (DefinitionStmt)pair.getUnit();
                        if (useStmt.getLeftOp() instanceof FieldRef) {
                            SootField field =
                                ((FieldRef)useStmt.getLeftOp()).getField();
                            return _getFieldValueTag(field);
                        }
                    }
                }
            } else {
                System.out.println("InlinePortTransformer.getPortValue():" +
                        " Unknown value = " + value +
                        " searching for local " + local +
                        " in method " + method);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                System.out.println(i.next().toString());
            }
        }
        return null;
    }

    private static TypedIOPort _getFieldValueTag(SootField field) {
        ValueTag tag = (ValueTag)field.getTag("_CGValue");
        if (tag == null) {
            return null;
        } else {
            NamedObj object = (NamedObj) tag.getObject();
            if (object instanceof TypedIOPort) {
                return (TypedIOPort)object;
            } else {
                throw new RuntimeException("The object " +
                        object.getFullName() +
                        " was not a port.");
            }
        }
    }

    private static Map _modelToPortInliner = new HashMap();
    private CompositeActor _model;
    private boolean _debug;
    private Map _options;
    private String _phaseName;
}














