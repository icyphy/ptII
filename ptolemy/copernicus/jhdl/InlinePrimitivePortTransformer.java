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


package ptolemy.copernicus.jhdl;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
//import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;

import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.Typeable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.java.*;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// InlinePrimitivePortTransformer
/**
A Transformer that is responsible for inlining the communication between ports.
The connections between the ports are taken from the model specified in the
constructor of this transformer.

FIXME: This is SDF specific and should get pulled out on its own.
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
public class InlinePrimitivePortTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlinePrimitivePortTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlinePrimitivePortTransformer v(CompositeActor model) {
        return new InlinePrimitivePortTransformer(model);
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

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlinePrimitivePortTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _phaseName = phaseName;
        _debug = PhaseOptions.getBoolean(options, "debug");

        // Some maps we use for storing the association between a port
        // and the fields that we are replacing it with.
        Map portToTypeNameToBufferField = new HashMap();
        Map portToIndexArrayField = new HashMap();
        Map portToTypeNameToInsideBufferField = new HashMap();
        Map portToInsideIndexArrayField = new HashMap();

        _inlinePortCalls(ModelTransformer.getModelClass(), _model,
                portToTypeNameToBufferField,
                portToIndexArrayField,
                portToTypeNameToInsideBufferField,
                portToInsideIndexArrayField);
    }

    // inline port calls at all levels of the model.
    private void _inlinePortCalls(SootClass modelClass, CompositeActor model,
            Map portToTypeNameToBufferField,
            Map portToIndexArrayField,
            Map portToTypeNameToInsideBufferField,
            Map portToInsideIndexArrayField) {

        // Loop over all the model instance classes.
        for (Iterator entities = model.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);

            // Loop through all the methods and inline calls on ports.
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                System.out.println("inline port body of " +
                        method + " = " + body);
                // System.out.println("method = " + method);

                boolean moreToDo = true;
                while (moreToDo) {
                    moreToDo = _inlineMethodCalls(
                            modelClass, entityClass, method, body,
                            portToTypeNameToBufferField,
                            portToIndexArrayField,
                            portToTypeNameToInsideBufferField,
                            portToInsideIndexArrayField, _debug);
                    LocalNameStandardizer.v().transform(body,
                            _phaseName + ".lns");
                }
            }

            // Recurse
            if (entity instanceof CompositeActor) {
                _inlinePortCalls(entityClass, (CompositeActor)entity,
                        portToTypeNameToBufferField,
                        portToIndexArrayField,
                        portToTypeNameToInsideBufferField,
                        portToInsideIndexArrayField);
            }
        }
    }

    private boolean _inlineMethodCalls(SootClass modelClass,
            SootClass theClass, SootMethod method,
            JimpleBody body, Map portToTypeNameToBufferField,
            Map portToIndexArrayField, Map portToTypeNameToInsideBufferField,
            Map portToInsideIndexArrayField, boolean debug) {
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
            if (stmt.containsInvokeExpr()) {
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
                                PtolemyUtilities.portClass)) {
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
                                            methodName.equals("hasRoom"))) {

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
                                box.setValue(IntConstant.v(1));
                            } else if (r.getMethod().getName().equals("hasRoom")) {
                                // return true.
                                box.setValue(IntConstant.v(1));
                            }
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
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if (tag == null) {
                    return null;
                } else {
                    return (TypedIOPort)tag.getObject();
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
                                return null;
                            } else {
                                return (TypedIOPort)tag.getObject();
                            }
                        }
                    }
                }
            } else {
                System.out.println("InlinePrimitivePortTransformer.getPortValue():" +
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

    // Return a set of ptolemy.data.type.Type objects representing the
    // types of ports that the given output port is connected to.
    private Set _getConnectedTypeList(TypedIOPort port) {
        if (!port.isOutput()) {
            throw new RuntimeException("Can only get the connected types for" +
                    " an output port!");
        }

        // Loop through all of the sink ports...
        // Note that we would like to just put the types in the
        // Map, but types don't implement hashCode properly.
        Map typeMap = new HashMap();
        // FIXME: This needs to be changed to handle hierarchy.
        List portList = port.sinkPortList();
        for (Iterator ports = portList.iterator();
             ports.hasNext();) {
            TypedIOPort remotePort = (TypedIOPort)ports.next();
            ptolemy.data.type.Type type = remotePort.getType();
            typeMap.put(type.toString(), type);
        }

        // Construct the set of types.
        HashSet set = new HashSet();
        for (Iterator types = typeMap.keySet().iterator();
             types.hasNext();) {
            set.add(typeMap.get(types.next()));
        }
        return set;
    }

    // Return a set of ptolemy.data.type.Type objects representing the
    // types of ports that the given input port is connected to.
    private Set _getConnectedTypeListInside(TypedIOPort port) {
        if (!port.isInput()) {
            throw new RuntimeException("Can only get the inside connected"
                    + " types for an input port!");
        }

        // Loop through all of the connected ports...
        // Note that we would like to just put the types in the
        // Map, but types don't implement hashCode properly.
        Map typeMap = new HashMap();
        // FIXME: This needs to be changed to handle hierarchy.
        List portList = port.insideSinkPortList();
        for (Iterator ports = portList.iterator();
             ports.hasNext();) {
            TypedIOPort remotePort = (TypedIOPort)ports.next();
            ptolemy.data.type.Type type = remotePort.getType();
            typeMap.put(type.toString(), type);
        }

        // Construct the set of types.
        HashSet set = new HashSet();
        for (Iterator types = typeMap.keySet().iterator();
             types.hasNext();) {
            set.add(typeMap.get(types.next()));
        }
        return set;
    }

    // Create references in the given class to the appropriate SDF
    // communication buffers for each port in the given entity.
    // This includes both the communication buffers and index arrays.
    private void _createBufferReferences(
            CompositeEntity model, SootClass modelClass,
            Entity entity, SootClass entityClass,
            Map portToTypeNameToBufferField, Map portToIndexArrayField) {
        // Loop over all the ports of the actor.
        for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
            TypedIOPort port = (TypedIOPort)ports.next();

            Map typeNameToBufferField = new HashMap();
            portToTypeNameToBufferField.put(port, typeNameToBufferField);

            //  System.out.println("port = " + port.getFullName() + " type = " + port.getType());

            // If the port is connected.
            if (port.getWidth() > 0) {
                // Create a field for the indexes into the buffer for that field.
                SootField indexArrayField = new SootField("_index_" + port.getName(),
                        ArrayType.v(IntType.v(), 1), Modifier.PUBLIC);
                entityClass.addField(indexArrayField);
                // Store references to the new field.
                portToIndexArrayField.put(port, indexArrayField);

                // Initialize the index fields.
                for (Iterator methods = entityClass.getMethods().iterator();
                     methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();
                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                    Object insertPoint = body.getUnits().getLast();
                    // Insert code into all the init methods.
                    if (!method.getName().equals("<init>")) {
                        continue;
                    }
                    Local indexesLocal =
                        Jimple.v().newLocal("indexes",
                                ArrayType.v(IntType.v(), 1));
                    body.getLocals().add(indexesLocal);

                    // Initialize the index array field to contain
                    // an array initialized to zero.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    indexesLocal,
                                    Jimple.v().newNewArrayExpr(
                                            IntType.v(),
                                            IntConstant.v(port.getWidth()))),
                            insertPoint);
                    // Set the index field to point to the new array
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newInstanceFieldRef(
                                            body.getThisLocal(),
                                            indexArrayField),
                                    indexesLocal),
                            insertPoint);
                }

                // If the port is an input, then it references
                // the buffer of its own type.  If the port
                // is an output, then we might have to convert to
                // multiple types.  Create a reference to the
                // port for each type that the port may reference.
                if (port.isInput()) {
                    ptolemy.data.type.Type type =
                        (ptolemy.data.type.Type)port.getType();

                    _createPortBufferReference(modelClass, entityClass,
                            port, type, typeNameToBufferField);
                } else if (port.isOutput()) {
                    Set typeSet = _getConnectedTypeList(port);
                    for (Iterator types = typeSet.iterator();
                         types.hasNext();) {
                        ptolemy.data.type.Type type =
                            (ptolemy.data.type.Type)types.next();

                        _createPortBufferReference(modelClass, entityClass,
                                port, type, typeNameToBufferField);
                    }
                }
            }
        }
    }

    // Create references in the given class to the appropriate SDF
    // communication buffers for each port in the given entity.
    // This includes both the communication buffers and index arrays.
    private void _createInsideBufferReferences(
            CompositeEntity model, SootClass modelClass,
            Map portToTypeNameToInsideBufferField,
            Map portToInsideIndexArrayField) {
        System.out.println("creating inside buffer references");
        // Loop over all the ports of the model
        for (Iterator ports = model.portList().iterator();
             ports.hasNext();) {
            TypedIOPort port = (TypedIOPort)ports.next();

            Map typeNameToInsideBufferField = new HashMap();
            portToTypeNameToInsideBufferField.put(port,
                    typeNameToInsideBufferField);

            System.out.println("port = " + port.getFullName() + " type = " + port.getType());

            // If the port is connected.
            if (port.getWidthInside() > 0) {
                // Create a field for the indexes into the buffer for that field.
                SootField indexArrayField = new SootField("_indexInside_" + port.getName(),
                        ArrayType.v(IntType.v(), 1), Modifier.PUBLIC);
                modelClass.addField(indexArrayField);
                // Store references to the new field.
                portToInsideIndexArrayField.put(port, indexArrayField);

                // Initialize the index fields.
                for (Iterator methods = modelClass.getMethods().iterator();
                     methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();
                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                    Object insertPoint = body.getUnits().getLast();
                    // Insert code into all the init methods.
                    if (!method.getName().equals("<init>")) {
                        continue;
                    }
                    Local indexesLocal =
                        Jimple.v().newLocal("indexes",
                                ArrayType.v(IntType.v(), 1));
                    body.getLocals().add(indexesLocal);

                    // Initialize the index array field to contain
                    // an array initialized to zero.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    indexesLocal,
                                    Jimple.v().newNewArrayExpr(
                                            IntType.v(),
                                            IntConstant.v(port.getWidth()))),
                            insertPoint);
                    // Set the index field to point to the new array
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newInstanceFieldRef(
                                            body.getThisLocal(),
                                            indexArrayField),
                                    indexesLocal),
                            insertPoint);
                }

                // If the port is an input, then it might have to
                // convert to multiple inside types.  If the port is
                // an output, then it references the buffer of its own
                // type.  Create a reference to the port for each type
                // that the port may reference.
                if (port.isInput()) {
                    Set typeSet = _getConnectedTypeListInside(port);
                    for (Iterator types = typeSet.iterator();
                         types.hasNext();) {
                        ptolemy.data.type.Type type =
                            (ptolemy.data.type.Type)types.next();

                        _createPortInsideBufferReference(modelClass,
                                port, type, typeNameToInsideBufferField);
                    }
                } else if (port.isOutput()) {
                    ptolemy.data.type.Type type =
                        (ptolemy.data.type.Type)port.getType();

                    _createPortInsideBufferReference(modelClass,
                            port, type, typeNameToInsideBufferField);
                }
            }
        }
    }

    // Create a reference to the correct buffer in the given
    // class for the given port and the given type.
    private void _createPortBufferReference(
            SootClass modelClass, SootClass entityClass,
            TypedIOPort port, ptolemy.data.type.Type type,
            Map typeNameToBufferField) {
        //  System.out.println("creating  buffer reference for " + port + " type = " + type);
        BaseType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portbuffer_" + port.getName() + "_" +
                    StringUtilities.sanitizeName(type.toString()),
                    ArrayType.v(tokenType, 2), Modifier.PUBLIC);
        entityClass.addField(bufferField);

        // Store references to the new field.
        typeNameToBufferField.put(type.toString(), bufferField);

        // Tag the field we created with the type of its data.
        bufferField.addTag(new TypeTag(type));

        // Create references to the buffer for each port channel
        for (Iterator methods = entityClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Object insertPoint = body.getUnits().getLast();
            // Insert code into all the init methods.
            if (!method.getName().equals("<init>")) {
                continue;
            }

            Local bufferLocal = Jimple.v().newLocal("buffer",
                    ArrayType.v(tokenType, 1));
            body.getLocals().add(bufferLocal);
            Local channelLocal = Jimple.v().newLocal("channel",
                    ArrayType.v(tokenType, 2));
            body.getLocals().add(channelLocal);

            // Create the array of port channels.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(channelLocal,
                            Jimple.v().newNewArrayExpr(
                                    ArrayType.v(tokenType, 1),
                                    IntConstant.v(
                                            port.getWidth()))),
                    insertPoint);
            // Set the field to point to the new array.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(
                                    body.getThisLocal(),
                                    bufferField),
                            channelLocal),
                    insertPoint);

            // For each channel of the port, make the buffer for that
            // channel point to the appropriate buffer of the relation.
            int channel = 0;
            for (Iterator relations = port.linkedRelationList().iterator();
                 relations.hasNext();) {
                TypedIORelation relation = (TypedIORelation)relations.next();
                for (int i = 0; i < relation.getWidth(); i++, channel++) {
                    // FIXME: buffersize is only one!
                    //  if (bufsize == 1) {
                    //  } else {
                    // Get the buffer associated with the channel.
                    SootField arrayField =
                        modelClass.getFieldByName(
                                getBufferFieldName(relation,
                                        i, type));
                    // Load the buffer array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(bufferLocal,
                                    Jimple.v().newStaticFieldRef(
                                            arrayField)),
                            insertPoint);
                    // Store to the port array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(
                                            channelLocal,
                                            IntConstant.v(channel)),
                                    bufferLocal),
                            insertPoint);
                }
            }
        }
    }

    // Create a reference to the correct inside buffer in the given
    // class for the given port and the given type.
    private void _createPortInsideBufferReference(
            SootClass modelClass,
            TypedIOPort port, ptolemy.data.type.Type type,
            Map typeNameToBufferField) {
        //  System.out.println("creating inside buffer reference for " + port + " type = " + type);
        BaseType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portinsidebuffer_" + port.getName() + "_" +
                    StringUtilities.sanitizeName(type.toString()),
                    ArrayType.v(tokenType, 2), Modifier.PUBLIC);
        modelClass.addField(bufferField);

        // Store references to the new field.
        typeNameToBufferField.put(type.toString(), bufferField);

        // Tag the field we created with the type of its data.
        bufferField.addTag(new TypeTag(type));

        // Create references to the buffer for each port channel
        for (Iterator methods = modelClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Object insertPoint = body.getUnits().getLast();
            // Insert code into all the init methods.
            if (!method.getName().equals("<init>")) {
                continue;
            }

            Local bufferLocal = Jimple.v().newLocal("buffer",
                    ArrayType.v(tokenType, 1));
            body.getLocals().add(bufferLocal);
            Local channelLocal = Jimple.v().newLocal("channel",
                    ArrayType.v(tokenType, 2));
            body.getLocals().add(channelLocal);

            // Create the array of port channels.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(channelLocal,
                            Jimple.v().newNewArrayExpr(
                                    ArrayType.v(tokenType, 1),
                                    IntConstant.v(
                                            port.getWidth()))),
                    insertPoint);
            // Set the field to point to the new array.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(
                                    body.getThisLocal(),
                                    bufferField),
                            channelLocal),
                    insertPoint);

            // For each channel of the port, make the buffer for that
            // channel point to the appropriate buffer of the relation.
            int channel = 0;
            for (Iterator relations = port.insideRelationList().iterator();
                 relations.hasNext();) {
                TypedIORelation relation = (TypedIORelation)relations.next();
                for (int i = 0; i < relation.getWidth(); i++, channel++) {
                    // FIXME: buffersize is only one!
                    //  if (bufsize == 1) {
                    //  } else {
                    // Get the buffer associated with the channel.
                    SootField arrayField =
                        modelClass.getFieldByName(
                                getBufferFieldName(relation,
                                        i, type));
                    // Load the buffer array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(bufferLocal,
                                    Jimple.v().newStaticFieldRef(
                                            arrayField)),
                            insertPoint);
                    // Store to the port array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(
                                            channelLocal,
                                            IntConstant.v(channel)),
                                    bufferLocal),
                            insertPoint);
                }
            }
        }
    }

    // Create instructions to store the given inputToken into the given
    // buffer at the given index.  If the given typeLocal is not null,
    // then convert the given input token to the given type using the given
    // temporary variables.
    private List _createBufferStoreInstructions(
            Local bufferLocal, Local indexLocal, Local inputTokenLocal,
            Local typeLocal, Local tokenLocal, Local outputTokenLocal) {
        List list = new LinkedList();
        // Convert the type, if we need to.
        if (typeLocal != null) {
            list.add(Jimple.v().newAssignStmt(
                    tokenLocal,
                    Jimple.v().newInterfaceInvokeExpr(
                            typeLocal,
                            PtolemyUtilities.typeConvertMethod,
                            inputTokenLocal)));


            list.add(Jimple.v().newAssignStmt(
                    outputTokenLocal,
                    Jimple.v().newCastExpr(
                            tokenLocal,
                            outputTokenLocal.getType())));
            // store the converted token.
            list.add(Jimple.v().newAssignStmt(
                    Jimple.v().newArrayRef(bufferLocal,
                            indexLocal),
                    outputTokenLocal));
        } else {
            list.add(Jimple.v().newAssignStmt(
                    Jimple.v().newArrayRef(bufferLocal,
                            indexLocal),
                    inputTokenLocal));
        }
        return list;
    }
    private List _createIndexUpdateInstructions(
            Local indexLocal, Local indexArrayLocal, Value channelValue,
            Value bufferSizeValue) {
        // Now update the index into the buffer.
        List list = new LinkedList();
        // If the buffer is size one, then the below code is a noop.
        if (bufferSizeValue.equals(IntConstant.v(1))) {
            return list;
        }
        // increment the position.
        list.add(Jimple.v().newAssignStmt(
                indexLocal,
                Jimple.v().newAddExpr(
                        indexLocal,
                        IntConstant.v(1))));

        // wrap around.
        list.add(Jimple.v().newAssignStmt(
                indexLocal,
                Jimple.v().newRemExpr(
                        indexLocal,
                        bufferSizeValue)));

        // store back.
        list.add(Jimple.v().newAssignStmt(
                Jimple.v().newArrayRef(indexArrayLocal,
                        channelValue),
                indexLocal));
        return list;
    }

    /** Insert code into the given body before the given unit that
     *  will retrieve the communication buffer associated with the
     *  given channel of the given port, created in the given model
     *  class.  The given local variable will refer to the buffer.  A
     *  value containing the size of the given buffer will be
     *  returned.
     */
    private Value _getBufferAndSize(
            SootClass modelClass, JimpleBody body,
            Unit unit, TypedIOPort port,
            ptolemy.data.type.Type type,
            Value channelValue, Local bufferLocal,
            Map portToTypeNameToBufferField, boolean inside) {

        Value bufferSizeValue = null;
        // Now get the appropriate buffer
        if (Evaluator.isValueConstantValued(channelValue)) {
            // If we know the channel, then refer directly to the buffer in the
            // model
            int argChannel =
                ((IntConstant)Evaluator.getConstantValueOf(channelValue)).value;
            int channel = 0;
            boolean found = false;
            List relationList;
            if (inside) {
                relationList = port.insideRelationList();
            } else {
                relationList = port.linkedRelationList();
            }
            for (Iterator relations = relationList.iterator();
                 !found && relations.hasNext();) {
                TypedIORelation relation = (TypedIORelation)relations.next();

                for (int i = 0;
                     !found && i < relation.getWidth();
                     i++, channel++) {
                    if (channel == argChannel) {
                        found = true;
                        SootField arrayField =
                            modelClass.getFieldByName(
                                    getBufferFieldName(relation,
                                            channel, type));

                        // load the buffer array.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(bufferLocal,
                                        Jimple.v().newStaticFieldRef(arrayField)),
                                unit);
                        int bufferSize;
                        try {
                            Variable bufferSizeVariable =
                                (Variable)relation.getAttribute("bufferSize");
                            bufferSize =
                                ((IntToken)bufferSizeVariable.getToken()).intValue();
                        } catch (Exception ex) {
                            System.out.println("No BufferSize parameter for " +
                                    relation);
                            continue;
                        }
                        // remember the size of the buffer.
                        bufferSizeValue = IntConstant.v(bufferSize);
                    }
                }
            }
            if (!found) {
                throw new RuntimeException("Constant channel not found!");
            }
        } else {
            // If we don't know the channel, then use the port indexes.
            Map typeNameToBufferField = (Map)
                portToTypeNameToBufferField.get(port);
            SootField arrayField = (SootField)
                typeNameToBufferField.get(type.toString());
            if (arrayField == null) {
                throw new RuntimeException("arrayField null!");
            }
            Local bufferArrayLocal =
                Jimple.v().newLocal("bufferArray",
                        ArrayType.v(PtolemyUtilities.tokenType, 2));
            body.getLocals().add(bufferArrayLocal);
            Local bufferSizeLocal =
                Jimple.v().newLocal("bufferSize",
                        IntType.v());
            body.getLocals().add(bufferSizeLocal);
            // Load the array of port channels.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(bufferArrayLocal,
                            Jimple.v().newInstanceFieldRef(
                                    body.getThisLocal(),
                                    arrayField)),
                    unit);
            // Load the buffer array.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(bufferLocal,
                            Jimple.v().newArrayRef(
                                    bufferArrayLocal,
                                    channelValue)),
                    unit);
            // get the length of the buffer
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            bufferSizeLocal,
                            Jimple.v().newLengthExpr(bufferLocal)),
                    unit);
            bufferSizeValue = bufferSizeLocal;

        }
        return bufferSizeValue;
    }

    /** Retrieve the correct index into the given channel of the given
     *  port into the given local variable.
     */
    private void _getCorrectIndex(JimpleBody body, Unit unit,
            TypedIOPort port, Local indexLocal, Local indexArrayLocal,
            Value channelValue, Value bufferSizeValue,
            Map portToIndexArrayField) {

        if (bufferSizeValue.equals(IntConstant.v(1))) {
            // Load the correct index into indexLocal
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexLocal,
                            IntConstant.v(0)),
                    unit);
        } else {
            SootField indexArrayField = (SootField)portToIndexArrayField.get(port);
            if (indexArrayField == null) {
                throw new RuntimeException("indexArrayField is null for port " + port + "!");
            }
            // Load the array of indexes.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexArrayLocal,
                            Jimple.v().newInstanceFieldRef(
                                    body.getThisLocal(),
                                    indexArrayField)),
                    unit);
            // Load the correct index into indexLocal
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexLocal,
                            Jimple.v().newArrayRef(
                                    indexArrayLocal,
                                    channelValue)),
                    unit);
        }
    }


    /** Replace the broadcast invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    private void _inlineBroadcast(SootClass modelClass,
            JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port,
            Map portToIndexArrayField, Map portToTypeNameToBufferField) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local indexLocal =
            Jimple.v().newLocal("index",
                    IntType.v());
        body.getLocals().add(indexLocal);
        Local indexArrayLocal =
            Jimple.v().newLocal("indexArray",
                    ArrayType.v(IntType.v(), 1));
        body.getLocals().add(indexArrayLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        SootField indexArrayField = (SootField)portToIndexArrayField.get(port);

        if (indexArrayField == null) {
            throw new RuntimeException("indexArrayField is null!");
        }
        // Load the array of indexes.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(indexArrayLocal,
                        Jimple.v().newInstanceFieldRef(
                                body.getThisLocal(),
                                indexArrayField)),
                stmt);


        Value bufferSizeValue = null;
        // Refer directly to the buffer in the model
        int channel = 0;
        for (Iterator relations = port.linkedRelationList().iterator();
             relations.hasNext();) {
            TypedIORelation relation = (TypedIORelation)relations.next();
            int bufferSize;
            try {
                Variable bufferSizeVariable =
                    (Variable)relation.getAttribute("bufferSize");
                bufferSize =
                    ((IntToken)bufferSizeVariable.getToken()).intValue();
            } catch (Exception ex) {
                System.out.println("No BufferSize parameter for " +
                        relation);
                continue;
            }
            // remember the size of the buffer.
            bufferSizeValue = IntConstant.v(bufferSize);

            for (int i = 0;
                 i < relation.getWidth();
                 i++, channel++) {
                Value channelValue = IntConstant.v(channel);

                // Load the correct index into indexLocal
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(indexLocal,
                                Jimple.v().newArrayRef(
                                        indexArrayLocal,
                                        channelValue)),
                        stmt);

                SootField arrayField =
                    modelClass.getFieldByName(
                            getBufferFieldName(relation,
                                    i, port.getType()));

                // load the buffer array.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(bufferLocal,
                                Jimple.v().newStaticFieldRef(arrayField)),
                        stmt);

                // If we are calling with just a token, then send the token.
                if (expr.getArgCount() == 1) {
                    // Write to the buffer.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(bufferLocal,
                                            indexLocal), expr.getArg(0)),
                            stmt);
                    // increment the position in the buffer.
                    body.getUnits().insertBefore(
                            _createIndexUpdateInstructions(
                                    indexLocal, indexArrayLocal,
                                    channelValue, bufferSizeValue),
                            stmt);
                } else {
                    // We must send an array of tokens.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    returnArrayLocal,
                                    expr.getArg(0)),
                            stmt);
                    // If the count is specified statically
                    if (Evaluator.isValueConstantValued(expr.getArg(1))) {
                        int argCount =
                            ((IntConstant)Evaluator.getConstantValueOf(
                                    expr.getArg(1))).value;
                        for (int k = 0; k < argCount; k++) {
                            // Get the value.
                            body.getUnits().insertBefore(
                                    Jimple.v().newAssignStmt(
                                            returnLocal,
                                            Jimple.v().newArrayRef(returnArrayLocal,
                                                    IntConstant.v(k))),
                                    stmt);
                            // Store in the buffer array.
                            body.getUnits().insertBefore(
                                    Jimple.v().newAssignStmt(
                                            Jimple.v().newArrayRef(bufferLocal,
                                                    indexLocal),
                                            returnLocal),
                                    stmt);
                            // increment the position in the buffer.
                            body.getUnits().insertBefore(
                                    _createIndexUpdateInstructions(
                                            indexLocal, indexArrayLocal,
                                            channelValue, bufferSizeValue),
                                    stmt);
                        }
                    } else {
                        // we don't know the size beforehand,
                        // so build a loop into the code.
                        // The loop counter
                        Local counterLocal =
                            Jimple.v().newLocal("counter",
                                    IntType.v());
                        body.getLocals().add(counterLocal);

                        // The list of initializer instructions.
                        List initializerList = new LinkedList();
                        initializerList.add(
                                Jimple.v().newAssignStmt(
                                        counterLocal,
                                        IntConstant.v(0)));

                        // The list of body instructions.
                        List bodyList = new LinkedList();
                        // Get the value.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        returnLocal,
                                        Jimple.v().newArrayRef(returnArrayLocal,
                                                counterLocal)),
                                stmt);
                        // Store in the buffer array.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newArrayRef(bufferLocal,
                                                indexLocal),
                                        returnLocal),
                                stmt);
                        // increment the position.
                        bodyList.add(
                                Jimple.v().newAssignStmt(
                                        indexLocal,
                                        Jimple.v().newAddExpr(
                                                indexLocal,
                                                IntConstant.v(1))));
                        // wrap around.
                        bodyList.add(
                                Jimple.v().newAssignStmt(
                                        indexLocal,
                                        Jimple.v().newRemExpr(
                                                indexLocal,
                                                bufferSizeValue)));
                        // Increment the counter.
                        bodyList.add(
                                Jimple.v().newAssignStmt(
                                        counterLocal,
                                        Jimple.v().newAddExpr(
                                                counterLocal,
                                                IntConstant.v(1))));

                        Expr conditionalExpr =
                            Jimple.v().newLtExpr(
                                    counterLocal,
                                    expr.getArg(1));
                        List loop = SootUtilities.createForLoopBefore(body,
                                stmt,
                                initializerList,
                                bodyList,
                                conditionalExpr);
                        body.getUnits().insertBefore(loop, stmt);

                        // store back.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newArrayRef(indexArrayLocal,
                                                channelValue),
                                        indexLocal),
                                stmt);
                    }
                }
            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);
    }

    /** Replace the get invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    private void _inlineGet(SootClass modelClass, JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port,
            Map portToIndexArrayField, Map portToTypeNameToBufferField) {
        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local indexLocal =
            Jimple.v().newLocal("index",
                    IntType.v());
        body.getLocals().add(indexLocal);
        Local indexArrayLocal =
            Jimple.v().newLocal("indexArray",
                    ArrayType.v(IntType.v(), 1));
        body.getLocals().add(indexArrayLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        Value bufferSizeValue = _getBufferAndSize(modelClass, body,
                stmt, port, port.getType(), channelValue, bufferLocal,
                portToTypeNameToBufferField, false);

        _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                channelValue, bufferSizeValue, portToIndexArrayField);

        System.out.println("inlining get at " + stmt);
        // If we are calling with just a channel, then read the value.
        if (expr.getArgCount() == 1) {
            body.getUnits().insertAfter(_createIndexUpdateInstructions(
                    indexLocal, indexArrayLocal, channelValue,
                    bufferSizeValue), stmt);

            // We may be calling get without setting the return value
            // to anything.
            if (stmt instanceof DefinitionStmt) {
                // Replace the get() with an array read.
                box.setValue(Jimple.v().newArrayRef(bufferLocal,
                        indexLocal));
            } else {
                body.getUnits().remove(stmt);
            }
        } else {
            // We must return an array of tokens.
            // Create an array of the appropriate length.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            returnArrayLocal,
                            Jimple.v().newNewArrayExpr(
                                    PtolemyUtilities.tokenType,
                                    expr.getArg(1))),
                    stmt);
            Value countValue = expr.getArg(1);

            // If the count is specified statically
            // FIXME: constant loop unroller should take care of this.
            if (Evaluator.isValueConstantValued(countValue)) {
                int argCount = ((IntConstant)
                        Evaluator.getConstantValueOf(countValue)).value;
                for (int k = 0; k < argCount; k++) {
                    // Get the value.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    returnLocal,
                                    Jimple.v().newArrayRef(bufferLocal,
                                            indexLocal)),
                            stmt);
                    // Store in the return array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(returnArrayLocal,
                                            IntConstant.v(k)),
                                    returnLocal),
                            stmt);
                    // increment the position in the buffer.
                    body.getUnits().insertBefore(
                            _createIndexUpdateInstructions(
                                    indexLocal, indexArrayLocal,
                                    channelValue, bufferSizeValue),
                            stmt);
                }
                // Replace the get() call.
                box.setValue(returnArrayLocal);
            } else {
                // we don't know the size beforehand,
                // so build a loop into the code.
                // The loop counter
                Local counterLocal =
                    Jimple.v().newLocal("counter",
                            IntType.v());
                body.getLocals().add(counterLocal);

                // The list of initializer instructions.
                List initializerList = new LinkedList();
                initializerList.add(
                        Jimple.v().newAssignStmt(
                                counterLocal,
                                IntConstant.v(0)));

                // The list of body instructions.
                List bodyList = new LinkedList();
                // Get the value.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                returnLocal,
                                Jimple.v().newArrayRef(
                                        bufferLocal,
                                        indexLocal)));
                // Store in the return array.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newArrayRef(
                                        returnArrayLocal,
                                        counterLocal),
                                returnLocal));
                if (!bufferSizeValue.equals(IntConstant.v(1))) {
                    // increment the position.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    // wrap around.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newRemExpr(
                                            indexLocal,
                                            bufferSizeValue)));
                }
                // Increment the counter.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                counterLocal,
                                Jimple.v().newAddExpr(
                                        counterLocal,
                                        IntConstant.v(1))));

                Expr conditionalExpr =
                    Jimple.v().newLtExpr(
                            counterLocal,
                            expr.getArg(1));
                List loop = SootUtilities.createForLoopBefore(body,
                        stmt,
                        initializerList,
                        bodyList,
                        conditionalExpr);
                body.getUnits().insertBefore(loop, stmt);

                if (!bufferSizeValue.equals(IntConstant.v(1))) {
                    // store back.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(indexArrayLocal,
                                            channelValue),
                                    indexLocal),
                            stmt);
                }
                // Replace the get() call.
                box.setValue(returnArrayLocal);
            }
        }
    }

    /** Replace the getInside invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    private void _inlineGetInside(
            SootClass modelClass, JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port,
            Map portToIndexArrayField, Map portToTypeNameToBufferField) {
        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local indexLocal =
            Jimple.v().newLocal("index",
                    IntType.v());
        body.getLocals().add(indexLocal);
        Local indexArrayLocal =
            Jimple.v().newLocal("indexArray",
                    ArrayType.v(IntType.v(), 1));
        body.getLocals().add(indexArrayLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        Value bufferSizeValue = _getBufferAndSize(modelClass, body,
                stmt, port, port.getType(), channelValue, bufferLocal,
                portToTypeNameToBufferField, true);

        _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                channelValue, bufferSizeValue, portToIndexArrayField);

        System.out.println("inlining getInside at " + stmt);
        // If we are calling with just a channel, then read the value.
        if (expr.getArgCount() == 1) {
            body.getUnits().insertAfter(_createIndexUpdateInstructions(
                    indexLocal, indexArrayLocal, channelValue,
                    bufferSizeValue), stmt);

            // We may be calling get without setting the return value
            // to anything.
            if (stmt instanceof DefinitionStmt) {
                // Replace the get() with an array read.
                box.setValue(Jimple.v().newArrayRef(bufferLocal,
                        indexLocal));
            } else {
                body.getUnits().remove(stmt);
            }
        } else {
            // We must return an array of tokens.
            // Create an array of the appropriate length.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            returnArrayLocal,
                            Jimple.v().newNewArrayExpr(
                                    PtolemyUtilities.tokenType,
                                    expr.getArg(1))),
                    stmt);
            Value countValue = expr.getArg(1);

            // If the count is specified statically
            // FIXME: constant loop unroller should take care of this.
            if (Evaluator.isValueConstantValued(countValue)) {
                int argCount = ((IntConstant)
                        Evaluator.getConstantValueOf(countValue)).value;
                for (int k = 0; k < argCount; k++) {
                    // Get the value.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    returnLocal,
                                    Jimple.v().newArrayRef(bufferLocal,
                                            indexLocal)),
                            stmt);
                    // Store in the return array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(returnArrayLocal,
                                            IntConstant.v(k)),
                                    returnLocal),
                            stmt);
                    // increment the position in the buffer.
                    body.getUnits().insertBefore(
                            _createIndexUpdateInstructions(
                                    indexLocal, indexArrayLocal,
                                    channelValue, bufferSizeValue),
                            stmt);
                }
                // Replace the get() call.
                box.setValue(returnArrayLocal);
            } else {
                // we don't know the size beforehand,
                // so build a loop into the code.
                // The loop counter
                Local counterLocal =
                    Jimple.v().newLocal("counter",
                            IntType.v());
                body.getLocals().add(counterLocal);

                // The list of initializer instructions.
                List initializerList = new LinkedList();
                initializerList.add(
                        Jimple.v().newAssignStmt(
                                counterLocal,
                                IntConstant.v(0)));

                // The list of body instructions.
                List bodyList = new LinkedList();
                // Get the value.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                returnLocal,
                                Jimple.v().newArrayRef(
                                        bufferLocal,
                                        indexLocal)));
                // Store in the return array.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newArrayRef(
                                        returnArrayLocal,
                                        counterLocal),
                                returnLocal));
                if (!bufferSizeValue.equals(IntConstant.v(1))) {
                    // increment the position.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    // wrap around.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newRemExpr(
                                            indexLocal,
                                            bufferSizeValue)));
                }
                // Increment the counter.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                counterLocal,
                                Jimple.v().newAddExpr(
                                        counterLocal,
                                        IntConstant.v(1))));

                Expr conditionalExpr =
                    Jimple.v().newLtExpr(
                            counterLocal,
                            expr.getArg(1));
                List loop = SootUtilities.createForLoopBefore(body,
                        stmt,
                        initializerList,
                        bodyList,
                        conditionalExpr);
                body.getUnits().insertBefore(loop, stmt);

                if (!bufferSizeValue.equals(IntConstant.v(1))) {
                    // store back.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(indexArrayLocal,
                                            channelValue),
                                    indexLocal),
                            stmt);
                }
                // Replace the get() call.
                box.setValue(returnArrayLocal);
            }
        }
    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    private void _inlineSend(SootClass modelClass, JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port,
            Map portToIndexArrayField, Map portToTypeNameToBufferField) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local indexLocal =
            Jimple.v().newLocal("index",
                    IntType.v());
        body.getLocals().add(indexLocal);
        Local indexArrayLocal =
            Jimple.v().newLocal("indexArray",
                    ArrayType.v(IntType.v(), 1));
        body.getLocals().add(indexArrayLocal);

        // The first argument is always the channel.
        Value channelValue = expr.getArg(0);

        // We have to repeat for all of the remote types.
        Set typeSet = _getConnectedTypeList(port);
        for (Iterator types = typeSet.iterator();
             types.hasNext();) {
            ptolemy.data.type.Type type =
                (ptolemy.data.type.Type)types.next();
            Local typeLocal = null;
            //   if (!port.getType().equals(type)) {
            typeLocal = PtolemyUtilities.buildConstantTypeLocal(body,
                    stmt, type);
            // }

            Value bufferSizeValue = _getBufferAndSize(modelClass, body,
                    stmt, port, type, channelValue, bufferLocal,
                    portToTypeNameToBufferField, false);

            _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                    channelValue, bufferSizeValue, portToIndexArrayField);

            // A local of type token
            Local tokenLocal =
                Jimple.v().newLocal("tokenLocal",
                        PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            // A local of the appropriate type to store in the
            // buffer.
            Local outputTokenLocal =
                Jimple.v().newLocal("outputTokenLocal",
                        PtolemyUtilities.getSootTypeForTokenType(type));
            body.getLocals().add(outputTokenLocal);

            // If we are calling with just a channel, then write the value.
            if (expr.getArgCount() == 2) {
                Local sendTokenLocal = (Local)expr.getArg(1);
                // Replace the put() with an array write.
                body.getUnits().insertBefore(
                        _createBufferStoreInstructions(
                                bufferLocal, indexLocal,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
                        stmt);
                // increment the position in the buffer.
                body.getUnits().insertBefore(
                        _createIndexUpdateInstructions(
                                indexLocal, indexArrayLocal,
                                channelValue, bufferSizeValue),
                        stmt);
            } else {
                Local sendArrayLocal = (Local) expr.getArg(1);
                /*                Jimple.v().newLocal("sendArray",
                                  ArrayType.v(PtolemyUtilities.tokenType, 1));
                                  body.getLocals().add(sendArrayLocal);

                                  // We must send an array of tokens.
                                  body.getUnits().insertBefore(
                                  Jimple.v().newAssignStmt(
                                  sendArrayLocal,
                                  expr.getArg(1)),
                                  stmt);*/

                Value countValue = expr.getArg(2);

                Local sendTokenLocal =
                    Jimple.v().newLocal("sendToken", PtolemyUtilities.tokenType);
                body.getLocals().add(sendTokenLocal);

                // If the count is specified statically
                if (Evaluator.isValueConstantValued(countValue)) {
                    int argCount =
                        ((IntConstant)Evaluator.getConstantValueOf(countValue)).value;
                    for (int k = 0; k < argCount; k++) {
                        // Get the value.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        sendTokenLocal,
                                        Jimple.v().newArrayRef(
                                                sendArrayLocal,
                                                IntConstant.v(k))),
                                stmt);
                        // Store in the buffer array.
                        body.getUnits().insertBefore(
                                _createBufferStoreInstructions(
                                        bufferLocal, indexLocal,
                                        sendTokenLocal, typeLocal,
                                        tokenLocal, outputTokenLocal),
                                stmt);
                        // increment the position in the buffer.
                        body.getUnits().insertBefore(
                                _createIndexUpdateInstructions(
                                        indexLocal, indexArrayLocal,
                                        channelValue, bufferSizeValue),
                                stmt);
                    }
                } else {
                    // we don't know the size beforehand,
                    // so build a loop into the code.
                    // The loop counter
                    Local counterLocal =
                        Jimple.v().newLocal("counter",
                                IntType.v());
                    body.getLocals().add(counterLocal);

                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    counterLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                    // Get the value.
                    bodyList.add(Jimple.v().newAssignStmt(
                            sendTokenLocal,
                            Jimple.v().newArrayRef(sendArrayLocal,
                                    counterLocal)));

                    // Store in the buffer array.
                    bodyList.addAll(_createBufferStoreInstructions(bufferLocal,
                            indexLocal, sendTokenLocal, typeLocal,
                            tokenLocal, outputTokenLocal));

                    // Note that we don't use createIndexUpdateInstructions
                    // because we would generate too many field stores.
                    if (!bufferSizeValue.equals(IntConstant.v(1))) {
                        // increment the position.
                        bodyList.add(Jimple.v().newAssignStmt(
                                indexLocal,
                                Jimple.v().newAddExpr(
                                        indexLocal,
                                        IntConstant.v(1))));
                        // wrap around.
                        bodyList.add(Jimple.v().newAssignStmt(
                                indexLocal,
                                Jimple.v().newRemExpr(
                                        indexLocal,
                                        bufferSizeValue)));
                    }
                    // Increment the counter.
                    bodyList.add(Jimple.v().newAssignStmt(
                            counterLocal,
                            Jimple.v().newAddExpr(
                                    counterLocal,
                                    IntConstant.v(1))));

                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(counterLocal, countValue);
                    List loop = SootUtilities.createForLoopBefore(body,
                            stmt,
                            initializerList,
                            bodyList,
                            conditionalExpr);
                    body.getUnits().insertBefore(loop, stmt);

                    if (!bufferSizeValue.equals(IntConstant.v(1))) {
                        // store back.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newArrayRef(indexArrayLocal,
                                                channelValue),
                                        indexLocal),
                                stmt);
                    }
                }
            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);

    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    private void _inlineSendInside(
            SootClass modelClass, JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port,
            Map portToIndexArrayField, Map portToTypeNameToBufferField) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local indexLocal =
            Jimple.v().newLocal("index",
                    IntType.v());
        body.getLocals().add(indexLocal);
        Local indexArrayLocal =
            Jimple.v().newLocal("indexArray",
                    ArrayType.v(IntType.v(), 1));
        body.getLocals().add(indexArrayLocal);

        // The first argument is always the channel.
        Value channelValue = expr.getArg(0);

        // We have to repeat for all of the remote types.
        Set typeSet = _getConnectedTypeListInside(port);
        for (Iterator types = typeSet.iterator();
             types.hasNext();) {
            ptolemy.data.type.Type type =
                (ptolemy.data.type.Type)types.next();
            Local typeLocal = null;
            //   if (!port.getType().equals(type)) {
            typeLocal = PtolemyUtilities.buildConstantTypeLocal(body,
                    stmt, type);
            // }

            Value bufferSizeValue = _getBufferAndSize(modelClass, body,
                    stmt, port, type, channelValue, bufferLocal,
                    portToTypeNameToBufferField, true);

            _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                    channelValue, bufferSizeValue, portToIndexArrayField);

            // A local of type token
            Local tokenLocal =
                Jimple.v().newLocal("tokenLocal",
                        PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            // A local of the appropriate type to store in the
            // buffer.
            Local outputTokenLocal =
                Jimple.v().newLocal("outputTokenLocal",
                        PtolemyUtilities.getSootTypeForTokenType(type));
            body.getLocals().add(outputTokenLocal);

            // If we are calling with just a channel, then write the value.
            if (expr.getArgCount() == 2) {
                Local sendTokenLocal = (Local)expr.getArg(1);
                // Replace the put() with an array write.
                body.getUnits().insertBefore(
                        _createBufferStoreInstructions(
                                bufferLocal, indexLocal,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
                        stmt);
                // increment the position in the buffer.
                body.getUnits().insertBefore(
                        _createIndexUpdateInstructions(
                                indexLocal, indexArrayLocal,
                                channelValue, bufferSizeValue),
                        stmt);
            } else {
                Local sendArrayLocal = (Local) expr.getArg(1);
                /*                Jimple.v().newLocal("sendArray",
                                  ArrayType.v(PtolemyUtilities.tokenType, 1));
                                  body.getLocals().add(sendArrayLocal);

                                  // We must send an array of tokens.
                                  body.getUnits().insertBefore(
                                  Jimple.v().newAssignStmt(
                                  sendArrayLocal,
                                  expr.getArg(1)),
                                  stmt);*/

                Value countValue = expr.getArg(2);

                Local sendTokenLocal =
                    Jimple.v().newLocal("sendToken", PtolemyUtilities.tokenType);
                body.getLocals().add(sendTokenLocal);

                // If the count is specified statically
                if (Evaluator.isValueConstantValued(countValue)) {
                    int argCount =
                        ((IntConstant)Evaluator.getConstantValueOf(countValue)).value;
                    for (int k = 0; k < argCount; k++) {
                        // Get the value.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        sendTokenLocal,
                                        Jimple.v().newArrayRef(
                                                sendArrayLocal,
                                                IntConstant.v(k))),
                                stmt);
                        // Store in the buffer array.
                        body.getUnits().insertBefore(
                                _createBufferStoreInstructions(
                                        bufferLocal, indexLocal,
                                        sendTokenLocal, typeLocal,
                                        tokenLocal, outputTokenLocal),
                                stmt);
                        // increment the position in the buffer.
                        body.getUnits().insertBefore(
                                _createIndexUpdateInstructions(
                                        indexLocal, indexArrayLocal,
                                        channelValue, bufferSizeValue),
                                stmt);
                    }
                } else {
                    // we don't know the size beforehand,
                    // so build a loop into the code.
                    // The loop counter
                    Local counterLocal =
                        Jimple.v().newLocal("counter",
                                IntType.v());
                    body.getLocals().add(counterLocal);

                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    counterLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                    // Get the value.
                    bodyList.add(Jimple.v().newAssignStmt(
                            sendTokenLocal,
                            Jimple.v().newArrayRef(sendArrayLocal,
                                    counterLocal)));

                    // Store in the buffer array.
                    bodyList.addAll(_createBufferStoreInstructions(bufferLocal,
                            indexLocal, sendTokenLocal, typeLocal,
                            tokenLocal, outputTokenLocal));

                    // Note that we don't use createIndexUpdateInstructions
                    // because we would generate too many field stores.
                    if (!bufferSizeValue.equals(IntConstant.v(1))) {
                        // increment the position.
                        bodyList.add(Jimple.v().newAssignStmt(
                                indexLocal,
                                Jimple.v().newAddExpr(
                                        indexLocal,
                                        IntConstant.v(1))));
                        // wrap around.
                        bodyList.add(Jimple.v().newAssignStmt(
                                indexLocal,
                                Jimple.v().newRemExpr(
                                        indexLocal,
                                        bufferSizeValue)));
                    }
                    // Increment the counter.
                    bodyList.add(Jimple.v().newAssignStmt(
                            counterLocal,
                            Jimple.v().newAddExpr(
                                    counterLocal,
                                    IntConstant.v(1))));

                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(counterLocal, countValue);
                    List loop = SootUtilities.createForLoopBefore(body,
                            stmt,
                            initializerList,
                            bodyList,
                            conditionalExpr);
                    body.getUnits().insertBefore(loop, stmt);

                    if (!bufferSizeValue.equals(IntConstant.v(1))) {
                        // store back.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newArrayRef(indexArrayLocal,
                                                channelValue),
                                        indexLocal),
                                stmt);
                    }
                }
            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);

    }

    private CompositeActor _model;
    private boolean _debug;
    private Map _options;
    private String _phaseName;
}














