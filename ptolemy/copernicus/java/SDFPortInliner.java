/* A class that replaces SDF port methods.

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.util.StringUtilities;
import soot.ArrayType;
import soot.RefType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
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
//// SDFPortInliner
/**
A class that inlines methods on ports for SDF models.

This class creates a set of appropriately sized circular buffers for
each channel in a particular composite actor.  These buffers are
referred to by static fields of the model.  Inside each actor in the
composite, an array of integer indexes into the circular buffer is
generated for each port.  Port method invocations where the channel
index can be statically determined are replaced with references to the
appropriate buffer in the model, and an index update instructions for
the appropriate index in the actor.

In cases where the channel cannot be statically determined for a given
invocation point, e.g. a for loop over all of the channels of a
trigger input port to read and discard the data, a second reference to
each buffer exists in the actors.  These references are in a array
that can be indexed by the channel of a port, and are called "buffer
references".

Additionally, index fields and buffer references are also created for
each port in the model for handling "inside" port methods...

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
public class SDFPortInliner implements PortInliner {
    /** Construct a new transformer
     */
    public SDFPortInliner(SootClass modelClass,
            CompositeActor model, Map options) {
        _modelClass = modelClass;
        _model = model;
        _options = options;
        _debug = PhaseOptions.getBoolean(_options, "debug");
    }

    /** Initialize the inliner.  Create communication buffers and index arrays.
     */
    public void initialize() {
        // Some maps we use for storing the association between a port
        // and the fields that we are replacing it with.
        _portToTypeNameToBufferField = new HashMap();
        _portToIndexArrayField = new HashMap();
        _portToTypeNameToInsideBufferField = new HashMap();
        _portToInsideIndexArrayField = new HashMap();

        _createBuffers();
    }

    /** Replace the broadcast invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineBroadcast(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {
        SootClass theClass = body.getMethod().getDeclaringClass();

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

        SootField indexArrayField = (SootField)_portToIndexArrayField.get(port);

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
        // Refer directly to the buffer in the _model
        int channel = 0;
        for (Iterator relations = port.linkedRelationList().iterator();
             relations.hasNext();) {
            TypedIORelation relation = (TypedIORelation)relations.next();

            int bufferSize = _getBufferSize(relation);

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
                    _modelClass.getFieldByName(
                            InlinePortTransformer.getBufferFieldName(relation,
                                    i, port.getType()));
                Local containerLocal = FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                        _model, theClass, body.getThisLocal(), body, stmt, _options);
                
                // load the buffer array.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(bufferLocal,
                                Jimple.v().newInstanceFieldRef(
                                        containerLocal,
                                        arrayField)),
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
    public void inlineGet(JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port) {
        SootClass theClass = body.getMethod().getDeclaringClass();

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

        Value bufferSizeValue = _getBufferAndSize(body,
                stmt, port, port.getType(), channelValue, bufferLocal,
                _portToTypeNameToBufferField, false);

        _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                channelValue, bufferSizeValue, _portToIndexArrayField);

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
    public void inlineGetInside(JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port) {
        SootClass theClass = body.getMethod().getDeclaringClass();
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

        Value bufferSizeValue = _getBufferAndSize(body,
                stmt, port, port.getType(), channelValue, bufferLocal,
                _portToTypeNameToInsideBufferField, true);

        _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                channelValue, bufferSizeValue, _portToInsideIndexArrayField);

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
    public void inlineSend(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {
        SootClass theClass = body.getMethod().getDeclaringClass();

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

            Value bufferSizeValue = _getBufferAndSize(body,
                    stmt, port, type, channelValue, bufferLocal,
                    _portToTypeNameToBufferField, false);

            _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                    channelValue, bufferSizeValue, _portToIndexArrayField);

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
    public void inlineSendInside(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {
        SootClass theClass = body.getMethod().getDeclaringClass();

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

            Value bufferSizeValue = _getBufferAndSize(body,
                    stmt, port, type, channelValue, bufferLocal,
                    _portToTypeNameToInsideBufferField, true);

            _getCorrectIndex(body, stmt, port, indexLocal, indexArrayLocal,
                    channelValue, bufferSizeValue, _portToInsideIndexArrayField);

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

    // Create the communication buffers for communication between
    // actors in the model.
    private void _createBuffers() {
        // First create the circular buffers for communication.
        // Loop over all the relations, creating buffers for each channel.
        for (Iterator relations = _model.relationList().iterator();
             relations.hasNext();) {
            TypedIORelation relation = (TypedIORelation)relations.next();

            int bufferSize = _getBufferSize(relation);

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

            for (Iterator types = typeMap.keySet().iterator();
                 types.hasNext();) {
                ptolemy.data.type.Type type =
                    (ptolemy.data.type.Type)typeMap.get(types.next());
                RefType tokenType =
                    PtolemyUtilities.getSootTypeForTokenType(type);
                Type arrayType = ArrayType.v(tokenType, 1);
                String fieldName = relation.getName() + "_bufferLocal";
               
                for (int i = 0; i < relation.getWidth(); i++) {
                    SootField field = new SootField(
                            InlinePortTransformer.getBufferFieldName(relation, i, type),
                            arrayType,
                            Modifier.PUBLIC);
                    _modelClass.addField(field);
                    if(_debug) {
                        System.out.println("creating field = " + field +
                                " of size " + bufferSize);
                    }
                    // Tag the field with the type.
                    field.addTag(new TypeTag(type));

                    // Add initialization code to each constructor
                    for(Iterator methods = _modelClass.getMethods().iterator();
                        methods.hasNext();) {
                        SootMethod initMethod = (SootMethod) methods.next();
                        
                        // Only look at constructors.
                        if(!initMethod.getName().equals("<init>")) {
                            continue;
                        }
                        JimpleBody initBody = (JimpleBody)initMethod.getActiveBody();
                        Chain initUnits = initBody.getUnits();
                        Local arrayLocal =
                            Jimple.v().newLocal(fieldName, arrayType);
                        initBody.getLocals().add(arrayLocal);
          
                        // Create the new buffer
                        Stmt insertPoint = (Stmt)initBody.getFirstNonIdentityStmt();
                        // This *should* be the statment after the constructor.
                        insertPoint = (Stmt)initUnits.getSuccOf(insertPoint);
                        Local containerLocal =
                            FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                                _model, _modelClass, initBody.getThisLocal(),
                                initBody, insertPoint, _options);
                        
                        initUnits.insertBefore(
                                Jimple.v().newAssignStmt(arrayLocal,
                                        Jimple.v().newNewArrayExpr(tokenType,
                                                IntConstant.v(bufferSize))),
                                insertPoint);
                        initUnits.insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newInstanceFieldRef(
                                                containerLocal, field),
                                        arrayLocal),
                                insertPoint);
                    }
                }
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

    // Create references in the given class to the appropriate SDF
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
            if (port.getWidth() > 0) {
                // Create a field for the indexes into the buffer for that field.
                SootField indexArrayField = new SootField("_index_" +
                        StringUtilities.sanitizeName(port.getName()),
                        ArrayType.v(IntType.v(), 1), Modifier.PUBLIC);
                entityClass.addField(indexArrayField);
                // Store references to the new field.
                _portToIndexArrayField.put(port, indexArrayField);

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

                    _createPortBufferReference(entityClass,
                            port, type, typeNameToBufferField);
                } else if (port.isOutput()) {
                    Set typeSet = _getConnectedTypeList(port);
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
            TypedIOPort port, ptolemy.data.type.Type type, Map typeNameToBufferField) {
        //  System.out.println("creating  buffer reference for " + port + " type = " + type);
        RefType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portbuffer_" +
                    StringUtilities.sanitizeName(port.getName()) + "_" +
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
            Stmt insertPoint = (Stmt) body.getUnits().getLast();
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
                        _modelClass.getFieldByName(
                                InlinePortTransformer.getBufferFieldName(relation,
                                        i, type));
               
                    Local containerLocal = FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                            _model, entityClass, body.getThisLocal(), body, insertPoint, _options);
               
                    // Load the buffer array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(bufferLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            containerLocal, arrayField)),
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

    // Create references in the given class to the appropriate SDF
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
                System.out.println("port = " + port.getFullName() +
                        " type = " + port.getType());
            }
            // If the port is connected.
            if (port.getWidthInside() > 0) {
                // Create a field for the indexes into the buffer for that field.
                SootField indexArrayField = new SootField("_indexInside_" +
                        StringUtilities.sanitizeName(port.getName()),
                        ArrayType.v(IntType.v(), 1), Modifier.PUBLIC);
                _modelClass.addField(indexArrayField);
                // Store references to the new field.
                _portToInsideIndexArrayField.put(port, indexArrayField);

                // Initialize the index fields.
                for (Iterator methods = _modelClass.getMethods().iterator();
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
    }

    // Create a reference to the correct inside buffer in the given
    // class for the given port and the given type.
    private void _createPortInsideBufferReference(
            SootClass modelClass,
            TypedIOPort port, ptolemy.data.type.Type type,
            Map typeNameToBufferField) {
        
        //  System.out.println("creating inside buffer reference for " + port + " type = " + type);
        RefType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
        // Create a field that refers to all the channels of that port.
        SootField bufferField =
            new SootField("_portinsidebuffer_" +
                    StringUtilities.sanitizeName(port.getName()) + "_" +
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
            Stmt insertPoint = (Stmt) body.getUnits().getLast();
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
                                            port.getWidthInside()))),
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
                                InlinePortTransformer.getBufferFieldName(relation,
                                        i, type));

                    Local containerLocal = FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                            _model, modelClass, body.getThisLocal(), body, insertPoint, _options);
               
                    // Load the buffer array.
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(bufferLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            containerLocal,
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
    private static List _createBufferStoreInstructions(
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

    // Create instructions to update the given index.
    private static List _createIndexUpdateInstructions(
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
    private Value _getBufferAndSize(JimpleBody body,
            Stmt stmt, TypedIOPort port,
            ptolemy.data.type.Type type,
            Value channelValue, Local bufferLocal,
            Map portToTypeNameToBufferField, boolean inside) {
        SootClass theClass = body.getMethod().getDeclaringClass();

        Value bufferSizeValue = null;
        // Now get the appropriate buffer
        if (Evaluator.isValueConstantValued(channelValue)) {
            // If we know the channel, then refer directly to the buffer in the
            // _model
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
                            _modelClass.getFieldByName(
                                    InlinePortTransformer.getBufferFieldName(relation,
                                            i, type));

                        Local containerLocal = FieldsForEntitiesTransformer.getLocalReferenceForEntity(
                                _model, theClass, body.getThisLocal(), body, stmt, _options);
                      
                        // load the buffer array.
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(bufferLocal,
                                        Jimple.v().newInstanceFieldRef(
                                                containerLocal,
                                                arrayField)),
                                stmt);
                        int bufferSize = _getBufferSize(relation);

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
                    stmt);
            // Load the buffer array.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(bufferLocal,
                            Jimple.v().newArrayRef(
                                    bufferArrayLocal,
                                    channelValue)),
                    stmt);
            // get the length of the buffer
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            bufferSizeLocal,
                            Jimple.v().newLengthExpr(bufferLocal)),
                    stmt);
            bufferSizeValue = bufferSizeLocal;

        }
        return bufferSizeValue;
    }

    /** Retrieve the correct index into the given channel of the given
     *  port into the given local variable.
     */
    private static void _getCorrectIndex(JimpleBody body, Stmt stmt,
            TypedIOPort port, Local indexLocal, Local indexArrayLocal,
            Value channelValue, Value bufferSizeValue,
            Map portToIndexArrayField) {

        if (bufferSizeValue.equals(IntConstant.v(1))) {
            // Load the correct index into indexLocal
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexLocal,
                            IntConstant.v(0)),
                    stmt);
        } else {
            SootField indexArrayField = (SootField)portToIndexArrayField.get(port);
            if (indexArrayField == null) {
                System.out.println(portToIndexArrayField.toString());
                throw new RuntimeException("indexArrayField is null for port " + port + "!");
            }
            // Load the array of indexes.
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexArrayLocal,
                            Jimple.v().newInstanceFieldRef(
                                    body.getThisLocal(),
                                    indexArrayField)),
                    stmt);
            // Load the correct index into indexLocal
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexLocal,
                            Jimple.v().newArrayRef(
                                    indexArrayLocal,
                                    channelValue)),
                    stmt);
        }
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

    // Return the value of the <i>bufferSize</i> variable in the given
    // relation.
    private static int _getBufferSize(Relation relation) {
        int bufferSize;
        try {
            Variable bufferSizeVariable =
                (Variable)relation.getAttribute("bufferSize");
            bufferSize =
                ((IntToken)bufferSizeVariable.getToken()).intValue();
        } catch (Exception ex) {
            // hack for HS
            System.out.println("No BufferSize parameter for " +
                    relation + "...  Assuming 1.");
            // continue;
            bufferSize = 1;
        }
        return bufferSize;
    }

    private boolean _debug;
    private CompositeActor _model;
    private SootClass _modelClass;
    private Map _options;

    private Map _portToTypeNameToBufferField;
    private Map _portToIndexArrayField;
    private Map _portToTypeNameToInsideBufferField;
    private Map _portToInsideIndexArrayField;
}
