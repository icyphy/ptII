/* A class that replaces HS port methods.

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
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import soot.PrimType;
import soot.RefType;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.util.Chain;


//////////////////////////////////////////////////////////////////////////
//// HSPortInliner
/**
A class that inlines methods on ports for HS models.

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
public class HSPortInliner implements PortInliner {
    /** Construct a new transformer
     */
    public HSPortInliner(SootClass modelClass, CompositeActor model, Map options) {
        _modelClass = modelClass;
        _model = model;
        _options = options;
    }

    /** Initialize the inliner.  Create one place buffers for each relation.
     */
    public void initialize() {
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
        if (expr.getArgCount() != 1) {
            throw new RuntimeException("multirate not supported.");
        }

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

                SootField arrayField =
                    _modelClass.getFieldByName(
                            InlinePortTransformer.getBufferFieldName(relation,
                                    i, port.getType()));

                // assign the value.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newStaticFieldRef(arrayField),
                                expr.getArg(0)),
                        stmt);

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
        if (expr.getArgCount() != 1) {
            throw new RuntimeException("multirate not supported.");
        }

        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        SootField field = _getBufferField(_modelClass, port, port.getType(),
                channelValue, false);

        // assign the value.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(
                        returnLocal,
                        Jimple.v().newStaticFieldRef(field)),
                stmt);

        // We may be calling get without setting the return value
        // to anything.
        if (stmt instanceof DefinitionStmt) {
            // Replace the get() with an array read.
            box.setValue(returnLocal);
        } else {
            body.getUnits().remove(stmt);
        }
    }

    /** Replace the getInside invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineGetInside(JimpleBody body, Stmt stmt,
            ValueBox box, InvokeExpr expr, TypedIOPort port) {
        if (expr.getArgCount() != 1) {
            throw new RuntimeException("multirate not supported.");
        }

        Local returnLocal =
            Jimple.v().newLocal("return", PtolemyUtilities.tokenType);
        body.getLocals().add(returnLocal);

        Value channelValue = expr.getArg(0);

        SootField field = _getBufferField(_modelClass, port, port.getType(),
                channelValue, true);

        // assign the value.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(
                        returnLocal,
                        Jimple.v().newStaticFieldRef(field)),
                stmt);

        // We may be calling get without setting the return value
        // to anything.
        if (stmt instanceof DefinitionStmt) {
            // Replace the get() with an array read.
            box.setValue(returnLocal);
        } else {
            body.getUnits().remove(stmt);
        }
    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSend(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {
        if (expr.getArgCount() != 2) {
            throw new RuntimeException("multirate send not supported.");
        }

        Value channelValue = expr.getArg(0);

        SootField field = _getBufferField(_modelClass, port, port.getType(),
                channelValue, false);

        // assign the value.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(
                        Jimple.v().newStaticFieldRef(field),
                        expr.getArg(1)),
                stmt);
        body.getUnits().remove(stmt);
    }

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSendInside(JimpleBody body, Stmt stmt,
            InvokeExpr expr, TypedIOPort port) {
        if (expr.getArgCount() != 2) {
            throw new RuntimeException("multirate sendInside not supported on port "
                    + port.getFullName() + ".");
        }

        Value channelValue = expr.getArg(0);

        SootField field = _getBufferField(_modelClass, port, port.getType(),
                channelValue, true);

        // assign the value.
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(
                        Jimple.v().newStaticFieldRef(field),
                        expr.getArg(1)),
                stmt);
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

            for (Iterator types = typeMap.keySet().iterator();
                 types.hasNext();) {
                ptolemy.data.type.Type type =
                    (ptolemy.data.type.Type)typeMap.get(types.next());
                RefType tokenType =
                    PtolemyUtilities.getSootTypeForTokenType(type);

                String fieldName = relation.getName() + "_bufferLocal";
                Local arrayLocal =
                    Jimple.v().newLocal(fieldName, tokenType);
                clinitBody.getLocals().add(arrayLocal);

                for (int i = 0; i < relation.getWidth(); i++) {
                    SootField field = new SootField(
                            InlinePortTransformer.getBufferFieldName(relation, i, type),
                            tokenType,
                            Modifier.PUBLIC | Modifier.STATIC);
                    _modelClass.addField(field);
              
                    // Tag the field with the type.
                    field.addTag(new TypeTag(type));

                    // Note: reverse order!
                    clinitUnits.addFirst(Jimple.v().newAssignStmt(
                            Jimple.v().newStaticFieldRef(field),
                            NullConstant.v()));
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
     *  given channel of the given port, created in the given _model
     *  class.  The given local variable will refer to the buffer.  A
     *  value containing the size of the given buffer will be
     *  returned.
     */
    private static SootField _getBufferField(
            SootClass modelClass, TypedIOPort port,
            ptolemy.data.type.Type type,
            Value channelValue, boolean inside) {

        // Now get the appropriate buffer
        if (Evaluator.isValueConstantValued(channelValue)) {
            // If we know the channel, then refer directly to the buffer in the
            // _model
            int argChannel =
                ((IntConstant)Evaluator.getConstantValueOf(channelValue)).value;
            int channel = 0;
            List relationList;
            if (inside) {
                relationList = port.insideRelationList();
            } else {
                relationList = port.linkedRelationList();
            }
            for (Iterator relations = relationList.iterator();
                 relations.hasNext();) {
                TypedIORelation relation = (TypedIORelation)relations.next();

                for (int i = 0; i < relation.getWidth(); i++, channel++) {
                    if (channel == argChannel) {
                        SootField arrayField =
                            modelClass.getFieldByName(
                                    InlinePortTransformer.getBufferFieldName(relation,
                                            i, type));

                        return arrayField;
                    }
                }
            }
            throw new RuntimeException("Constant channel not found!");
        } else {
            throw new RuntimeException(
                    "Cannot handle channel that is not constant");
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

    private CompositeActor _model;
    private SootClass _modelClass;
    private Map _options;

    private Map _portToTypeNameToBufferField;
    private Map _portToIndexArrayField;
    private Map _portToTypeNameToInsideBufferField;
    private Map _portToInsideIndexArrayField;
}
