/* Replace method calls on parameter objects.

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
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import soot.HasPhaseOptions;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

//////////////////////////////////////////////////////////////////////////
//// InlineParameterTransformer
/**
A Transformer that is responsible for inlining the values of
parameters and settable attributes.  The values of the parameters are
taken from the model specified for this transformer.  This transformer
replaces a parameter with a field that points to a properly
initialized token that contains the value of the parameter.  Settable
attributes other than parameters are handled similarly and are
replaces with their expression.  This transformer also properly
inlines code from the attribute changed method that handles the change
of any parameter values.


@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class InlineParameterTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private InlineParameterTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlineParameterTransformer v(CompositeActor model) {
        return new InlineParameterTransformer(model);
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
        System.out.println("InlineParameterTransformer.internalTransform("
                + phaseName + ", " + options + ")");
        _phaseName = phaseName;
        _options = options;

        Map attributeToValueFieldMap = new HashMap();

        boolean debug = PhaseOptions.getBoolean(options, "debug");

        // Determine which parameters have a constant value.
        ConstVariableModelAnalysis constantAnalysis;
        try {
            constantAnalysis = new ConstVariableModelAnalysis(_model);
        } catch (KernelException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        // For every variable and settable attribute in the model, create a
        // field that has the value of that attributes.
        // If the variable is constant, then determine its constant value,
        // and tag the generated field with that value.  This will be used
        // later by the token inliner...
        _createTokenAndExpressionFields(
                ModelTransformer.getModelClass(), _model, _model,
                attributeToValueFieldMap, constantAnalysis, debug);

        // Replace the token calls... on ALL the classes.
        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            if (SootUtilities.derivesFrom(theClass,
                        PtolemyUtilities.inequalityTermClass)) continue;

            // Inline calls to parameter.getToken and getExpression
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // What about static methods?  They don't have a this
                // local
                if (method.isStatic()) {
                    continue;
                }
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                if (debug) System.out.println("method = " + method);

                boolean moreToDo = true;
                while (moreToDo) {
                    TypeAssigner.v().transform(body,
                            _phaseName + ".ta");
                    moreToDo = _inlineMethodCalls(theClass, method, body,
                            attributeToValueFieldMap, debug);
                    LocalNameStandardizer.v().transform(body,
                            _phaseName + ".lns");
                }
            }
        }
    }

    private boolean _inlineMethodCalls(SootClass theClass,
            SootMethod method,
            JimpleBody body, Map attributeToValueFieldMap, boolean debug) {
        boolean doneSomething = false;
        if (debug) System.out.println("Inlining method calls in method " +
                method);

        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
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
                if (debug) System.out.println("invoking = " + r.getMethod());
                if (r.getBase().getType() instanceof RefType) {
                    RefType type = (RefType)r.getBase().getType();
                    if (debug) System.out.println("baseType = " + type);
                    // Remove calls to validate().
                    if (r.getMethod().equals(PtolemyUtilities.validateMethod)) {
                        body.getUnits().remove(stmt);
                        continue;
                    } else {
                        // Inline calls to attribute changed.
                        if (r.getMethod().getSubSignature().equals(PtolemyUtilities.attributeChangedMethod.getSubSignature())) {
                            // If we are calling attribute changed on
                            // one of the classes we are generating
                            // code for, then inline it.
                            if (type.getSootClass().isApplicationClass()) {
                                SootMethod inlinee = null;
                                if (r instanceof VirtualInvokeExpr) {
                                    // Now inline the resulting call.
                                    List methodList =
                                        Scene.v().getActiveHierarchy().resolveAbstractDispatch(
                                                type.getSootClass(), PtolemyUtilities.attributeChangedMethod);
                                    if (methodList.size() == 1) {
                                        // inline the method.
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
                                // a class for the attribute too????
                                body.getUnits().remove(stmt);
                                doneSomething = true;
                            }
                        }
                    }

                    // Statically evaluate constant arguments.
                    Value argValues[] = new Value[r.getArgCount()];
                    int argCount = 0;
                    for (Iterator args = r.getArgs().iterator();
                         args.hasNext();) {
                        Value arg = (Value)args.next();
                        //  if (debug) System.out.println("arg = " + arg);
                        if (Evaluator.isValueConstantValued(arg)) {
                            argValues[argCount++] = Evaluator.getConstantValueOf(arg);
                            if (debug) System.out.println("argument = " + argValues[argCount-1]);
                        } else {
                            break;
                        }
                    }

                    if (SootUtilities.derivesFrom(type.getSootClass(),
                            PtolemyUtilities.settableClass)) {
                        // If we are invoking a method on a
                        // variable class, then attempt to get the
                        // constant value of the variable.
                        Attribute attribute =
                            getAttributeValue(method, (Local)r.getBase(), stmt, localDefs, localUses);
                        if (debug) System.out.println("Settable base = " + attribute);

                        // If the attribute resolves to null, then
                        // replace the invocation with an
                        // exception throw.
                        if (attribute == null) {
                            Local exceptionLocal =
                                SootUtilities.createRuntimeException(
                                        body, stmt,
                                        "NullPointerException: " + r);
                            body.getUnits().swapWith(stmt,
                                    Jimple.v().newThrowStmt(
                                            exceptionLocal));
                        }

                        // Inline getType, setTypeEquals, etc...
                        if (attribute instanceof Typeable) {
                            if(PtolemyUtilities.inlineTypeableMethods(body,
                                       stmt, box, r, (Typeable)attribute)) {
                                continue;
                            }
                        }

                        // Inline namedObj methods on the attribute.
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getFullNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    attribute.getFullName()));
                            continue;
                        }
                        if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getNameMethod.getSubSignature())) {
                            box.setValue(StringConstant.v(
                                    attribute.getName()));
                            continue;
                        }

                        if(r instanceof SpecialInvokeExpr) {
                            continue;
                        }

                        // Get some references to the container of the attribute.
                        Entity container =
                            FieldsForEntitiesTransformer.getEntityContainerOfObject(attribute);
                        Local thisLocal = body.getThisLocal();
                        
                        Local containerLocal = getAttributeContainerRef(
                                container, method, (Local)r.getBase(), stmt, localDefs, localUses);
                            // FieldsForEntitiesTransformer.getLocalReferenceForEntity(
//                                 container, theClass, thisLocal, body, stmt, _options);
                                              
                        // For Variables, we handle get/setToken,
                        // get/setExpression different from other
                        // settables
                        if (attribute instanceof Variable) {
                            // Deal with tricky methods separately.
                            
                            // Match the subsignature so we catch
                            // isomorphic subclasses as well...
                            if (r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.variableConstructorWithToken.getSubSignature())) {
                                SootClass variableClass =
                                    r.getMethod().getDeclaringClass();
                                SootMethod constructorWithoutToken =
                                    variableClass.getMethod(
                                            PtolemyUtilities.variableConstructorWithoutToken.getSubSignature());
                                // Replace the three-argument
                                // constructor with a two-argument
                                // constructor.  We do this for
                                // several reasons:

                                // 1) The assignment is
                                // redundant...  all parameters
                                // are initialized with the
                                // appropriate value.

                                // 2) The type of the token is
                                // often wrong for polymorphic
                                // actors.

                                // 3) Later on, when we inline all
                                // token constructors, there is no
                                // longer a token to pass to the
                                // constructor.  It is easier to
                                // just deal with it now...

                                // Create a new two-argument constructor.
                                box.setValue(Jimple.v().newSpecialInvokeExpr(
                                        (Local)r.getBase(), constructorWithoutToken,
                                        r.getArg(0), r.getArg(1)));

                                // Call setToken with the actual value of the parameter

                                Token token;
                                // First create a token with the given
                                // expression and then set the
                                // token to that value.
                                try {
                                    token = ((Variable)attribute).getToken();
                                } catch (Exception ex) {
                                    throw new RuntimeException("Illegal parameter value = "
                                            + argValues[0]);
                                }

                                String localName = "_CGTokenLocal";
                                Local tokenLocal =
                                    PtolemyUtilities.buildConstantTokenLocal(
                                            body, stmt, token, localName);

                                body.getUnits().insertAfter(
                                        Jimple.v().newInvokeStmt(
                                                Jimple.v().newVirtualInvokeExpr(
                                                        (Local)r.getBase(),
                                                        PtolemyUtilities.variableSetTokenMethod,
                                                        tokenLocal)),
                                        stmt);
                                doneSomething = true;

                            } else if (r.getMethod().getName().equals("getToken")) {
                                if (debug) System.out.println("Replacing getToken on Variable");
                                // replace the method call with a field ref.
                                SootField tokenField = (SootField)attributeToValueFieldMap.get(attribute);
                                if (tokenField == null) {
                                    throw new RuntimeException(
                                            "No tokenField found for attribute " + attribute);
                                }
                                if(stmt instanceof InvokeStmt) {
                                    body.getUnits().remove(stmt);
                                } else {
                                    box.setValue(Jimple.v().newInstanceFieldRef(containerLocal,tokenField));
                                }
                                doneSomething = true;
                            } else if (r.getMethod().getName().equals("setToken")) {
                                if (debug) System.out.println("Replacing setToken on Variable");
                                // replace the entire statement
                                // (which must be an invokeStmt anyway)
                                // with an assignment to the field of the first argument.
                                SootField tokenField = (SootField)attributeToValueFieldMap.get(attribute);
                                if (tokenField == null) {
                                    throw new RuntimeException("No tokenField found for attribute " + attribute);
                                }

                                Local tokenLocal = Jimple.v().newLocal("convertedToken",
                                        tokenField.getType());
                                body.getLocals().add(tokenLocal);
                                // Convert the token to the type of the variable.
                                Local typeLocal = PtolemyUtilities.buildConstantTypeLocal(body,
                                        stmt, ((Variable)attribute).getType());
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                tokenLocal,
                                                Jimple.v().newInterfaceInvokeExpr(
                                                        typeLocal,
                                                        PtolemyUtilities.typeConvertMethod,
                                                        r.getArg(0))),
                                        stmt);
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                tokenLocal,
                                                Jimple.v().newCastExpr(
                                                        tokenLocal,
                                                        tokenField.getType())),
                                        stmt);
                                // Assign the value field for the variable.
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                Jimple.v().newInstanceFieldRef(containerLocal,tokenField),
                                                tokenLocal),
                                        stmt);
                                // Invoke attributeChanged on the container of the variable.
                                PtolemyUtilities.callAttributeChanged(
                                        containerLocal, (Local)r.getBase(),
                                        theClass, method, body, stmt);

                                // remove the old call.
                                body.getUnits().remove(stmt);
                                doneSomething = true;
                            } else if (r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.getExpressionMethod.getSubSignature())) {
                                if (debug) System.out.println("Replacing getExpression on Variable");
                                // First get the token out of the field, and then insert a call
                                // to its toString method to get the expression.
                                SootField tokenField =
                                    (SootField)attributeToValueFieldMap.get(attribute);
                                if (tokenField == null) {
                                    throw new RuntimeException("No tokenField found for attribute " + attribute);
                                }
                                String localName = "_CGTokenLocal";
                                Local tokenLocal = Jimple.v().newLocal(localName,
                                        tokenField.getType());
                                body.getLocals().add(tokenLocal);
                                
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(tokenLocal,
                                                Jimple.v().newInstanceFieldRef(containerLocal, tokenField)),
                                        stmt);
                                box.setValue(Jimple.v().newVirtualInvokeExpr(tokenLocal,
                                        PtolemyUtilities.toStringMethod));
                                doneSomething = true;
                                // FIXME null result => ""
                            } else if (r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.setExpressionMethod.getSubSignature())) {
                                if (debug) System.out.println("Replacing setExpression on Variable");
                           
                                // Call attribute changed AFTER we set the token.
                                PtolemyUtilities.callAttributeChanged(
                                        containerLocal, (Local)r.getBase(),
                                        theClass, method, body,
                                        body.getUnits().getSuccOf(stmt));

                                Token token;
                                // First create a token with the given
                                // expression and then set the
                                // token to that value.
                                try {
                                    token = ((Variable)attribute).getToken();
                                } catch (Exception ex) {
                                    throw new RuntimeException("Illegal parameter value = "
                                            + argValues[0]);
                                }
                                // Create code to instantiate the token
                                SootField tokenField =
                                    (SootField)attributeToValueFieldMap.get(attribute);
                                if (tokenField == null) {
                                    throw new RuntimeException("No tokenField found for attribute " + attribute);
                                }
                                String localName = "_CGTokenLocal";
                                Local tokenLocal =
                                    PtolemyUtilities.buildConstantTokenLocal(
                                            body, stmt, token, localName);

                                body.getUnits().swapWith(stmt,
                                        Jimple.v().newAssignStmt(
                                                Jimple.v().newInstanceFieldRef(
                                                        containerLocal,
                                                        tokenField),
                                                tokenLocal));
                                doneSomething = true;
                            }
                        } else {// if(false) { //FIXME
                                // It's just settable, so handle get/setExpression
                            if (r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.getExpressionMethod.getSubSignature())) {
                                if (debug) System.out.println("Replacing getExpression on Settable");
                          
                                box.setValue(Jimple.v().newInstanceFieldRef(containerLocal,
                                        (SootField)attributeToValueFieldMap.get(attribute)));
                                doneSomething = true;
                            } else if (r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.setExpressionMethod.getSubSignature())) {
                                if (debug) System.out.println("Replacing setExpression on Settable");

                                // Call attribute changed AFTER we set the token.
                                PtolemyUtilities.callAttributeChanged(
                                        containerLocal, (Local)r.getBase(),
                                        theClass, method, body,
                                        body.getUnits().getSuccOf(stmt));
                                // replace the entire statement (which must be an invokeStmt anyway)
                                // with an assignment to the field of the first argument.
                                body.getUnits().swapWith(stmt,
                                        Jimple.v().newAssignStmt(
                                                Jimple.v().newInstanceFieldRef(containerLocal,
                                                        (SootField)
                                                        attributeToValueFieldMap.get(attribute)),
                                                r.getArg(0)));
                                doneSomething = true;
                            }
                        }


                        /*
                          // FIXME what about all the other methods???
                          // If we have a attribute and all the args are constant valued, then
                          if (argCount == r.getArgCount()) {
                          // reflect and invoke the same method on our token
                          Constant constant = SootUtilities.reflectAndInvokeMethod(attribute,
                          r.getMethod(), argValues);
                          System.out.println("method result  = " + constant);

                          // replace the method invocation.
                          box.setValue(constant);
                          }
                        */
                    }
                }
            }
        }

        return doneSomething;
    }

    public Local getAttributeContainerRef(
            Entity container, SootMethod method, Local local,
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof Local) {
                return getAttributeContainerRef(container, method,
                        (Local)value,
                        stmt, localDefs, localUses);
            } else if (value instanceof CastExpr) {
                return getAttributeContainerRef(container, method,
                        (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if (value instanceof InstanceFieldRef) {
                return (Local)((InstanceFieldRef)value).getBase();
            } else if (value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while (pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if (pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt = (DefinitionStmt)pair.getUnit();
                        if (useStmt.getLeftOp() instanceof InstanceFieldRef) {
                            InstanceFieldRef leftOp = 
                                ((InstanceFieldRef)useStmt.getLeftOp());
                            return (Local)leftOp.getBase();
                        }
                    }
                }
                throw new RuntimeException(
                        "Could not determine the static value of "
                        + local + " in " + method);
            } else if (value instanceof NullConstant) {
                // If we get to an assignment from null, then the
                // attribute statically evaluates to null.
                return null;
            } else if (value instanceof ThisRef) {
                JimpleBody body = (JimpleBody)method.getActiveBody();
                // Manufacture a reference.
                Local newLocal = FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                        container, method.getDeclaringClass(), body.getThisLocal(), 
                        body, location, _options);
                return newLocal;
            } else {
                throw new RuntimeException(
                        "Unknown type of value: " + value + " in " + method);
            }
        } else {
            String string = "More than one definition of = " + local + "\n";
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                string += "Definition = " + i.next().toString();
            }
            throw new RuntimeException(string);
        }
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a variable type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise throw an exception
     */
    public static Attribute getAttributeValue(SootMethod method, Local local,
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof Local) {
                return getAttributeValue(method,
                        (Local)value,
                        stmt, localDefs, localUses);
            } else if (value instanceof CastExpr) {
                return getAttributeValue(method,
                        (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if (tag == null) {
                    // return null;
                    throw new RuntimeException(
                            "Could not determine the static value of "
                            + local + " in " + method);
                } else {
                    return (Attribute)tag.getObject();
                }
            } else if (value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while (pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if (pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt = (DefinitionStmt)pair.getUnit();
                        if (useStmt.getLeftOp() instanceof FieldRef) {
                            SootField field = ((FieldRef)useStmt.getLeftOp()).getField();
                            ValueTag tag = (ValueTag)field.getTag("_CGValue");
                            if (tag == null) {
                                System.out.println("Failed usage: " + useStmt);
                                // We came to a field store that we
                                // did not create... hopefully there
                                // is one that we created.

                                // continue;
                                //
                                // throw new RuntimeException("Could
                                // not determine the static value of "
                                // + local + " in " + method); return
                                // null;
                            } else {
                                return (Attribute)tag.getObject();
                            }
                        }
                    }
                }
                throw new RuntimeException(
                        "Could not determine the static value of "
                        + local + " in " + method);
            } else if (value instanceof NullConstant) {
                // If we get to an assignment from null, then the
                // attribute statically evaluates to null.
                return null;
            } else if (value instanceof ThisRef) {
                // Resolve the thisRef for automatically generated
                // attributes.
                SootClass refClass = method.getDeclaringClass();
                return ModelTransformer.getAttributeForClass(refClass);
            } else {
                throw new RuntimeException(
                        "Unknown type of value: " + value + " in " + method);
            }
        } else {
            String string = "More than one definition of = " + local + "\n";
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                string += "Definition = " + i.next().toString();
            }
            throw new RuntimeException(string);
        }
    }

    // Create a static field in the given class for each attribute in
    // the given container that is a variable, with type Token.
    // and if only a settable, then the field will have type String.
    // In addition, add a tag to the field that contains the value of
    // the token or expression that that field contains.
    private void _createTokenAndExpressionFields(SootClass theClass,
            NamedObj context, NamedObj container,
            Map attributeToValueFieldMap,
            ConstVariableModelAnalysis constantAnalysis, boolean debug) {
        /*   SootClass tokenClass =
             Scene.v().loadClassAndSupport("ptolemy.data.Token");
             Type tokenType = RefType.v(tokenClass);*/
        if (debug) System.out.println("creating field for " +
                container + " in class " + theClass);

        SootClass stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");
        Type stringType = RefType.v(stringClass);
        for (Iterator attributes =
                 container.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
            if (attributeToValueFieldMap.get(attribute) != null) {
                throw new RuntimeException("already created field for attribute" + attribute);
            }
            if (attribute instanceof Settable) {
                Settable settable = (Settable)attribute;

                if (debug) System.out.println("creating field for " + settable);

                String fieldName =
                    StringUtilities.sanitizeName(attribute.getName(context));
                SootField field;
                // Create a field to contain the value of the attribute.
                if (settable instanceof Variable) {
                    Variable variable = (Variable)settable;
                    ptolemy.data.type.Type type = variable.getType();
                    Type tokenType =
                        PtolemyUtilities.getSootTypeForTokenType(type);

                    boolean isConstant = constantAnalysis.getConstVariables(
                            (Entity)context).contains(attribute);
              
                    int modifier;
                    if (isConstant) {
                        modifier = Modifier.PUBLIC | Modifier.FINAL;
                    } else {
                        modifier = Modifier.PUBLIC;
                    }

                    field = new SootField(
                            fieldName + "_CGToken",
                            tokenType,
                            modifier);
                    theClass.addField(field);
                    if (isConstant) {
                        try {
                            field.addTag(new ValueTag(variable.getToken()));
                        } catch (Exception ex) {
                        }
                        field.addTag(new TypeTag(type));
                    }
                } else {// if(false) { //FIXME
                    field = new SootField(
                            fieldName + "_CGExpression",
                            stringType,
                            Modifier.PUBLIC |
                            Modifier.FINAL);
                    theClass.addField(field);
                    String expression = settable.getExpression();
                    field.addTag(new ValueTag(expression));
                }//  else {
//                     field = null;
//                 }
                attributeToValueFieldMap.put(attribute, field);
            }
            _createTokenAndExpressionFields(theClass, context, attribute,
                    attributeToValueFieldMap, constantAnalysis, debug);
        }

        if (container instanceof ComponentEntity) {
            ComponentEntity entity = (ComponentEntity)container;
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                Port port = (Port)ports.next();
                _createTokenAndExpressionFields(
                        theClass, context, port,
                        attributeToValueFieldMap, constantAnalysis, debug);
            }
        }

        if (container instanceof CompositeEntity &&
                !(container instanceof FSMActor)) {
            CompositeEntity model = (CompositeEntity)container;
            // Loop over all the actor instance classes.
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                ComponentEntity entity = (ComponentEntity)entities.next();
                String className =
                    ModelTransformer.getInstanceClassName(entity, _options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);

                _createTokenAndExpressionFields(
                        entityClass, entity, entity,
                        attributeToValueFieldMap, constantAnalysis, debug);
            }
        }
    }

    public SootField _findAttributeField(SootClass entityClass, String name) {
        return entityClass.getFieldByName(name);
    }

    private CompositeActor _model;
    private Map _options;
    private String _phaseName;
}














