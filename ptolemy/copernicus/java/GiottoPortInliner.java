/* A class that replaces Giotto port methods.

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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.util.StringUtilities;
import soot.ArrayType;
import soot.Body;
import soot.IntType;
import soot.RefType;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.DefinitionStmt;
import soot.jimple.Expr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// GiottoPortInliner
/**
A class that inlines methods on ports for Giotto models.

Ports of Giotto models are represented by an array of locations, one location for each channel.  This corresponds to a buffer of length 1.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class GiottoPortInliner implements PortInliner {
    /** Construct a new transformer
     */
    public GiottoPortInliner(SootClass modelClass,
            CompositeActor model, Map options) {
        _modelClass = modelClass;
        _model = model;
        _options = options;
        _debug = PhaseOptions.getBoolean(_options, "debug");
    }

    public SootField getBufferField(IOPort port, ptolemy.data.type.Type type) {
        Map typeNameToBufferField = (Map)
            _portToTypeNameToBufferField.get(port);
        SootField arrayField = (SootField)
            typeNameToBufferField.get(type.toString());
        if (arrayField == null) {
            throw new RuntimeException("arrayField for " + port + " and type " + type + " is null!");
        }
        return arrayField;
    }

    public SootField getInsideBufferField(IOPort port,
            ptolemy.data.type.Type type) {
        Map typeNameToBufferField = (Map)
            _portToTypeNameToInsideBufferField.get(port);
        SootField arrayField = (SootField)
            typeNameToBufferField.get(type.toString());
        if (arrayField == null) {
            throw new RuntimeException("arrayField null!");
        }
        return arrayField;
    }

    /** Initialize the inliner.  This inliner does nothing, since it
     * expects the GiottoDirectorInliner to have previously called the
     * CreateBuffers method.
     */
    public void initialize() {
    }

    /** Create communication places for the ports.  This method is
     * provided for the use of the GiottoDirectorInliner, since that
     * inliner and this one are closely coordinated, unlike most other
     * domains.
     */
    public void createBuffers() {
        // Some maps we use for storing the association between a port
        // and the fields that we are replacing it with.
        _portToTypeNameToBufferField = new HashMap();
        _portToTypeNameToInsideBufferField = new HashMap();

        _createBuffers();
    }

    /** Replace the broadcast invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineBroadcast(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);


        Value bufferSizeValue = null;
        // Refer directly to the buffer in the _model
        int channel = 0;
        for (Iterator relations = port.linkedRelationList().iterator();
             relations.hasNext();) {
            TypedIORelation relation = (TypedIORelation)relations.next();

            for (int i = 0;
                 i < relation.getWidth();
                 i++, channel++) {
                Value channelValue = IntConstant.v(channel);

                SootField arrayField =
                    _modelClass.getFieldByName(
                            InlinePortTransformer.getBufferFieldName(relation,
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
                                            channelValue), expr.getArg(0)),
                            stmt);
                } else {
                    // We must send an array of tokens.
                    // Ignore all but the last value.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    returnArrayLocal,
                                    expr.getArg(0)),
                            stmt);

                    Value argCount = expr.getArg(1);
                    // Get the value.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    returnLocal,
                                    Jimple.v().newArrayRef(returnArrayLocal,
                                            argCount)),
                            stmt);
                    // Store in the buffer array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(bufferLocal,
                                            channelValue),
                                    returnLocal),
                            stmt);
                }
            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);
    }

    /** Replace the get invocation in the given box
     *  at the given unit in the
     *  given body with an array reference.
     */
    public void inlineGet(JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port) {
        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        _getBuffer(_modelClass, body,
                stmt, port, port.getType(), bufferLocal,
                _portToTypeNameToBufferField);

        // If we are calling with just a channel, then read the value.
        if (expr.getArgCount() == 1) {
            // We may be calling get without setting the return value
            // to anything.
            if (stmt instanceof DefinitionStmt) {
                // Replace the get() with an array read.
                box.setValue(Jimple.v().newArrayRef(bufferLocal,
                        channelValue));
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
                                            channelValue)),
                            stmt);
                    // Store in the return array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(returnArrayLocal,
                                            IntConstant.v(k)),
                                    returnLocal),
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
                                        channelValue)));
                // Store in the return array.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newArrayRef(
                                        returnArrayLocal,
                                        counterLocal),
                                returnLocal));
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

                // Replace the get() call.
                box.setValue(returnArrayLocal);
            }
        }
    }

    /** Replace the getInside invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineGetInside(JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port) {
        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);
        Local returnArrayLocal =
            Jimple.v().newLocal("returnArray",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(returnArrayLocal);
        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        _getBuffer(_modelClass, body,
                stmt, port, port.getType(), bufferLocal,
                _portToTypeNameToInsideBufferField);

        // If we are calling with just a channel, then read the value.
        if (expr.getArgCount() == 1) {
            // We may be calling get without setting the return value
            // to anything.
            if (stmt instanceof DefinitionStmt) {
                // Replace the get() with an array read.
                box.setValue(Jimple.v().newArrayRef(bufferLocal,
                        channelValue));
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
                                            channelValue)),
                            stmt);
                    // Store in the return array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    Jimple.v().newArrayRef(returnArrayLocal,
                                            IntConstant.v(k)),
                                    returnLocal),
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
                                        channelValue)));
                // Store in the return array.
                bodyList.add(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newArrayRef(
                                        returnArrayLocal,
                                        counterLocal),
                                returnLocal));

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

                // Replace the get() call.
                box.setValue(returnArrayLocal);
            }
        }
    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSend(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);

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

            _getBuffer(_modelClass, body,
                    stmt, port, port.getType(), bufferLocal,
                    _portToTypeNameToBufferField);

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
                                bufferLocal, channelValue,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
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

                // Get the value.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                sendTokenLocal,
                                Jimple.v().newArrayRef(
                                        sendArrayLocal,
                                        countValue)),
                        stmt);
                // Store in the buffer array.
                body.getUnits().insertBefore(
                        _createBufferStoreInstructions(
                                bufferLocal, channelValue,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
                        stmt);

            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);

    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSendInside(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {

        Local bufferLocal =
            Jimple.v().newLocal("buffer",
                    ArrayType.v(PtolemyUtilities.tokenType, 1));
        body.getLocals().add(bufferLocal);

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

            _getBuffer(_modelClass, body,
                    stmt, port, port.getType(), bufferLocal,
                    _portToTypeNameToInsideBufferField);

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
                                bufferLocal, channelValue,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
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

                // Get the value.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                sendTokenLocal,
                                Jimple.v().newArrayRef(
                                        sendArrayLocal,
                                        countValue)),
                        stmt);
                // Store in the buffer array.
                body.getUnits().insertBefore(
                        _createBufferStoreInstructions(
                                bufferLocal, channelValue,
                                sendTokenLocal, typeLocal,
                                tokenLocal, outputTokenLocal),
                        stmt);
            }
        }
        // blow away the send.
        body.getUnits().remove(stmt);

    }

    // Create the communication buffers for communication between
    // actors in the model.
    private void _createBuffers() {
        // First create the circular buffers for communication.
        SootMethod clinitMethod;
        Body clinitBody;
        if (_modelClass.declaresMethodByName("<clinit>")) {
            clinitMethod = _modelClass.getMethodByName("<clinit>");
            clinitBody = clinitMethod.retrieveActiveBody();
        } else {
            clinitMethod = new SootMethod("<clinit>", Collections.EMPTY_LIST,
                    VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
            _modelClass.addMethod(clinitMethod);
            clinitBody = Jimple.v().newBody(clinitMethod);
            clinitMethod.setActiveBody(clinitBody);
            clinitBody.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        Chain clinitUnits = clinitBody.getUnits();

        // Loop over all the relations, creating buffers for each channel.
        for (Iterator relations = _model.relationList().iterator();
             relations.hasNext();) {
            TypedIORelation relation = (TypedIORelation)relations.next();

            // Determine the types that the relation is connected to.
            Map typeMap = new HashMap();
            List destinationPortList =
                relation.linkedDestinationPortList();
            for (Iterator destinationPorts = destinationPortList.iterator();
                 destinationPorts.hasNext();) {
                TypedIOPort port = (TypedIOPort)destinationPorts.next();
                ptolemy.data.type.Type type = port.getType();
                typeMap.put(type.toString(), type);
            }

        }

        // In each actor class, create an
        _createInsideBufferReferences();

        // Loop over all the _model instance classes.
        for (Iterator entities = _model.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);

            _createBufferReferences(entity, entityClass);
        }
    }

    // Create references in the given class to the appropriate Giotto
    // communication buffers for each port in the given entity.
    // This includes both the communication buffers and index arrays.
    private void _createBufferReferences(Entity entity, SootClass entityClass) {
        // Loop over all the ports of the actor.
        for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
            TypedIOPort port = (TypedIOPort)ports.next();

            Map typeNameToBufferField = new HashMap();
            _portToTypeNameToBufferField.put(port, typeNameToBufferField);

            //  System.out.println("port = " + port.getFullName() + " type = " + port.getType());

            // If the port is connected.
            if (true) {//port.getWidth() > 0) {
                // If the port is an input, then it references
                // the buffer of its own type.  If the port
                // is an output, then we might have to convert to
                // multiple types.  Create a reference to the
                // port for each type that the port may reference.
                if (port.isInput()) {
                    ptolemy.data.type.Type type =
                        (ptolemy.data.type.Type)port.getType();

                    _createPortBufferReference(entityClass,
                            port, type, typeNameToBufferField);
                } else if (port.isOutput()) {
                    Set typeSet = _getConnectedTypeList(port);
                    typeSet.add(port.getType());
                    for (Iterator types = typeSet.iterator();
                         types.hasNext();) {
                        ptolemy.data.type.Type type =
                            (ptolemy.data.type.Type)types.next();

                        _createPortBufferReference(entityClass,
                                port, type, typeNameToBufferField);
                    }
                }
            }
        }
    }

    // Create a reference to the correct buffer in the given
    // class for the given port and the given type.
    private void _createPortBufferReference(SootClass entityClass,
            TypedIOPort port, ptolemy.data.type.Type type,
            Map typeNameToBufferField) {
        if(_debug) {  
            System.out.println("creating  buffer reference for " + port 
                    + " type = " + type);
        }
        RefType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portbuffer_" +
                    StringUtilities.sanitizeName(port.getName()) + "_" +
                    StringUtilities.sanitizeName(type.toString()),
                    ArrayType.v(tokenType, 1), Modifier.PUBLIC);
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

            Local channelLocal = Jimple.v().newLocal("channel",
                    ArrayType.v(tokenType, 1));
            body.getLocals().add(channelLocal);

            // Create the array of port channels.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(channelLocal,
                            Jimple.v().newNewArrayExpr(
                                    tokenType,
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
        }
    }

    // Create references in the given class to the appropriate Giotto
    // communication buffers for each port in the given entity.
    // This includes both the communication buffers and index arrays.
    private void _createInsideBufferReferences() {
        if(_debug) {
            System.out.println("creating inside buffer references for "
                    + _model.getFullName());
        }
        // Loop over all the ports of the _model
        for (Iterator ports = _model.portList().iterator();
             ports.hasNext();) {
            TypedIOPort port = (TypedIOPort)ports.next();

            Map typeNameToInsideBufferField = new HashMap();
            _portToTypeNameToInsideBufferField.put(port,
                    typeNameToInsideBufferField);

            if(_debug) {
                System.out.println("port = " + port.getFullName() 
                        + " type = " + port.getType());
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

                    _createPortInsideBufferReference(_modelClass,
                            port, type, typeNameToInsideBufferField);
                }
            } else if (port.isOutput()) {
                ptolemy.data.type.Type type =
                    (ptolemy.data.type.Type)port.getType();

                _createPortInsideBufferReference(_modelClass,
                        port, type, typeNameToInsideBufferField);
            }
        }
    }

    // Create a reference to the correct inside buffer in the given
    // class for the given port and the given type.
    private void _createPortInsideBufferReference(
            SootClass _modelClass,
            TypedIOPort port, ptolemy.data.type.Type type,
            Map typeNameToBufferField) {
        //  System.out.println("creating inside buffer reference for " + port + " type = " + type);
        RefType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portinsidebuffer_" +
                    StringUtilities.sanitizeName(port.getName()) + "_" +
                    StringUtilities.sanitizeName(type.toString()),
                    ArrayType.v(tokenType, 1), Modifier.PUBLIC);
        _modelClass.addField(bufferField);

        // Store references to the new field.
        typeNameToBufferField.put(type.toString(), bufferField);

        // Tag the field we created with the type of its data.
        bufferField.addTag(new TypeTag(type));

        // Create references to the buffer for each port channel
        for (Iterator methods = _modelClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Object insertPoint = body.getUnits().getLast();
            // Insert code into all the init methods.
            if (!method.getName().equals("<init>")) {
                continue;
            }

            Local channelLocal = Jimple.v().newLocal("channel",
                    ArrayType.v(tokenType, 1));
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
        }
    }

    /** Insert code into the given body before the given unit that will
     *  retrieve the communication buffer associated with the given
     *  given port.  The given local variable will refer to the
     *  buffer.
     */
    private static void _getBuffer(
            SootClass modelClass, JimpleBody body,
            Unit unit, TypedIOPort port,
            ptolemy.data.type.Type type,
            Local bufferLocal,
            Map portToTypeNameToBufferField) {

        // If we don't know the channel, then use the port indexes.
        Map typeNameToBufferField = (Map)
            portToTypeNameToBufferField.get(port);
        SootField arrayField = (SootField)
            typeNameToBufferField.get(type.toString());
        if (arrayField == null) {
            throw new RuntimeException("arrayField null!");
        }
        // Load the array of port channels.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(bufferLocal,
                        Jimple.v().newInstanceFieldRef(
                                body.getThisLocal(),
                                arrayField)),
                unit);
    }

    // Create instructions to store the given inputToken into the given
    // buffer at the given index.  If the given typeLocal is not null,
    // then convert the given input token to the given type using the given
    // temporary variables.
    private static List _createBufferStoreInstructions(
            Local bufferLocal, Value indexLocal, Local inputTokenLocal,
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

    private boolean _debug;
    private CompositeActor _model;
    private SootClass _modelClass;
    private Map _options;

    private Map _portToTypeNameToBufferField;
    private Map _portToTypeNameToInsideBufferField;
}
