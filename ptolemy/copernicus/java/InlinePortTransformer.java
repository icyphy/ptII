

package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
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
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining the values of parameters.
The values of the parameters are taken from the model specified for this 
transformer.
FIXME: This is SDF specific and should get pulled out on its own.
FIXME: currently we try to speed things up if the buffersize is only
one by removing the index update overhead.  Note that there are other optimizations
that can be made here (for instance, if we can statically determine all the channel
references (which is trivially true if there is only one channel), then there 
is no need to have the index or portbuffer arrays.
*/
public class InlinePortTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlinePortTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static InlinePortTransformer v(CompositeActor model) { 
        return new InlinePortTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlinePortTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        SootClass objectClass = 
            Scene.v().loadClassAndSupport("java.lang.Object");
        SootClass namedObjClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootClass kernelEntityClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.Entity");
        SootMethod getPortMethod = kernelEntityClass.getMethod(
              "ptolemy.kernel.Port getPort(java.lang.String)");
        SootClass portClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.Port");
        Type portType = RefType.v(portClass);
        
        // FIXME toplevel ports?
        // Loop over all the actor instance classes.
        for(Iterator entities = _model.entityList().iterator();
            entities.hasNext();) {
            Entity entity = (Entity)entities.next();
            String className = Options.getString(options, "targetPackage")
                + "." + entity.getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            
            // Loop over all the ports of the actor.
            for(Iterator ports = 
                    entity.portList().iterator();
                ports.hasNext();) {
                TypedIOPort port = (TypedIOPort)ports.next();

                ptolemy.data.type.Type type = port.getType();
                BaseType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
                System.out.println("port = " + port.getFullName() + " type = " + type);
                
                // If the port is connected.
                if(port.getWidth() > 0) {
                    // Create a field for the indexes into the buffer for that field.
                    SootField indexArrayField = new SootField("_index_" + port.getName(),
                            ArrayType.v(IntType.v(), 1), Modifier.PUBLIC);
                    entityClass.addField(indexArrayField);
                    // Create a field that refers to all the channels of that port.
                    SootField bufferField = new SootField("_portbuffer_" + port.getName(),
                            ArrayType.v(tokenType, 2), Modifier.PUBLIC);
                    entityClass.addField(bufferField);

                    // Tag the field we created with the type of its data.
                    bufferField.addTag(new TypeTag(type));
                    
                    // Create references to the buffer for each port channel
                    for(Iterator methods = entityClass.getMethods().iterator();
                        methods.hasNext();) {
                        SootMethod method = (SootMethod)methods.next();
                        JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                        Object insertPoint = body.getUnits().getLast();
                        // Insert code into all the init methods.
                        if(!method.getName().equals("<init>")) {
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
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newInstanceFieldRef(
                                                body.getThisLocal(),
                                                indexArrayField),
                                        indexesLocal),
                                insertPoint);
                        
                        Local bufferLocal = 
                            Jimple.v().newLocal("buffer", 
                                    ArrayType.v(tokenType, 1));
                        body.getLocals().add(bufferLocal);
                        Local channelLocal = 
                            Jimple.v().newLocal("channel", 
                                    ArrayType.v(tokenType, 2));
                        body.getLocals().add(channelLocal);
                        
                        // Create the array of port channels
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        channelLocal,
                                        Jimple.v().newNewArrayExpr(
                                                ArrayType.v(tokenType, 1),
                                                IntConstant.v(
                                                        port.getWidth()))),
                                insertPoint);;
                        // store it to the field.
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
                        for(Iterator relations = port.linkedRelationList().iterator();
                            relations.hasNext();) {
                            IORelation relation = (IORelation)relations.next();
                            for(int i = 0; 
                                i < relation.getWidth();
                                i++, channel++) {
                                // FIXME: buffersize is only one!
                                //  if(bufsize == 1) {
                                //  } else {
                                // This is the buffer associated with that channel.
                                SootField arrayField = 
                                    Scene.v().getMainClass().getFieldByName(
                                            "_" + relation.getName() + "_" + i);
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
            }

            // Loop through all the methods and inline calls on ports.
            for(Iterator methods = entityClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                
                // System.out.println("method = " + method);
                
                // Locals for buffer reading and writing.
                Local indexLocal =
                    Jimple.v().newLocal("index",
                            IntType.v());
                body.getLocals().add(indexLocal);
                Local indexArrayLocal =
                Jimple.v().newLocal("indexArray",
                        ArrayType.v(IntType.v(),1));
                body.getLocals().add(indexArrayLocal);             
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
                    
                                                                                                       
                CompleteUnitGraph unitGraph = 
                    new CompleteUnitGraph(body);
                // this will help us figure out where locals are defined.
                SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
                SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while(boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                            if(r.getBase().getType() instanceof RefType) {
                                RefType type = (RefType)r.getBase().getType();
                              
                                // Statically evaluate constant arguments.
                                Value argValues[] = new Value[r.getArgCount()];
                                int constantArgCount = 0;
                                for(Iterator args = r.getArgs().iterator();
                                    args.hasNext();) {
                                    Value arg = (Value)args.next();
                                    //System.out.println("arg = " + arg);
                                    if(Evaluator.isValueConstantValued(arg)) {
                                        argValues[constantArgCount++] = Evaluator.getConstantValueOf(arg);
                                        // System.out.println("argument = " + argValues[argCount-1]);
                                    } else {
                                        break;
                                    }
                                }
                                boolean allArgsAreConstant = (r.getArgCount() == constantArgCount);
                                
                                if(SootUtilities.derivesFrom(type.getSootClass(), portClass)) {
                                    // if we are invoking a method on a port class, then
                                    // attempt to get the constant value of the port.
                                    TypedIOPort port = (TypedIOPort)
                                        getPortValue(method, (Local)r.getBase(), 
                                                unit, localDefs, localUses);
                                    //     System.out.println("reference to port = " + port);
                                         
                                    if(port == null) {
                                        continue;
                                    }

                                    if(port instanceof Typeable) {
                                        PtolemyUtilities.inlineTypeableMethods(body, 
                                                unit, box, r, (Typeable)port);
                                       
                                    }
                                    
                                    String methodName = r.getMethod().getName();
                                    if(port.getWidth() == 0 &&
                                           (methodName.equals("hasToken") ||
                                                   methodName.equals("getToken") ||
                                                   methodName.equals("get") ||
                                                   methodName.equals("put") ||
                                                   methodName.equals("broadcast"))) {
                                        // If we try to get on a port with zero width, then 
                                        // throw a runtime exception.
                                        Local local = SootUtilities.createRuntimeException(body, unit, 
                                                methodName + "() called on a port with zero width: " + 
                                                port.getFullName() + "!");
                                        body.getUnits().insertBefore(Jimple.v().newThrowStmt(local),
                                                unit);
                                        if(unit instanceof DefinitionStmt) {
                                            box.setValue(NullConstant.v());
                                        } else {
                                            body.getUnits().remove(unit);
                                        }
                                        continue;
                                    }
                                                                            
                                    if(r.getMethod().getName().equals("hasToken")) {
                                       
                                        // return true.
                                        box.setValue(IntConstant.v(1));
                                    } else if(r.getMethod().getName().equals("hasRoom")) {
                                        // return true.
                                        box.setValue(IntConstant.v(1));
                                    } else if(r.getMethod().getName().equals("getWidth")) {
                                        // reflect and invoke the same method on our token
                                        Constant constant = SootUtilities.reflectAndInvokeMethod(
                                                port, r.getMethod(), argValues);
                                        // System.out.println("method result  = " + constant);
                                        
                                        // replace the method invocation.
                                        box.setValue(constant);
                                    } else if(r.getMethod().getName().equals("get")) {
                                        // Could be get that takes a channel and returns a token,
                                        // or get that takes a channel and a count and returns
                                        // an array of tokens.          
                                        // In either case, replace the get with circular array ref.
                                        Value channelValue = r.getArg(0);
                                       
                                        Value bufferSizeValue = _getBufferAndSize(entityClass, body, 
                                                unit, port, channelValue, bufferLocal);
                                    
                                        _getCorrectIndex(entityClass, body, 
                                                unit, port, indexLocal, indexArrayLocal, 
                                                channelValue, bufferSizeValue);
                                       
                                        // If we are calling with just a channel, then read the value.
                                        if(r.getArgCount() == 1) {
                                            body.getUnits().insertAfter(_createIndexUpdateInstructions( 
                                                    indexLocal, indexArrayLocal, channelValue, bufferSizeValue),
                                                    unit);
                                        
                                           
                                            // We may be calling get without setting the return value to anything.
                                            if(unit instanceof DefinitionStmt) {
                                                // Replace the get() with an array read.
                                                box.setValue(Jimple.v().newArrayRef(bufferLocal,
                                                        indexLocal));
                                            } else {
                                                body.getUnits().remove(unit);
                                            }
                                        } else {
                                            // We must return an array of tokens.
                                            // Create an array of the appropriate length.
                                            body.getUnits().insertBefore(
                                                    Jimple.v().newAssignStmt(
                                                            returnArrayLocal, 
                                                            Jimple.v().newNewArrayExpr(
                                                                    PtolemyUtilities.tokenType, r.getArg(1))),
                                                    unit);
                                            // If the count is specified statically
                                            // FIXME: constant loop unroller should take care of this.
                                            if(constantArgCount > 1) {
                                                int argCount = ((IntConstant)argValues[0]).value;
                                                for(int k = 0; k < argCount; k++) {
                                                    // Get the value.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    returnLocal,
                                                                    Jimple.v().newArrayRef(bufferLocal,
                                                                            indexLocal)),
                                                            unit);
                                                    // Store in the return array.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    Jimple.v().newArrayRef(returnArrayLocal,
                                                                            IntConstant.v(k)),
                                                                    returnLocal),
                                                            unit);
                                                    // increment the position in the buffer.
                                                    body.getUnits().insertBefore(
                                                            _createIndexUpdateInstructions( 
                                                                    indexLocal, indexArrayLocal, 
                                                                    channelValue, bufferSizeValue),
                                                            unit);
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
                                                if(!bufferSizeValue.equals(IntConstant.v(1))) {
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
                                                            r.getArg(1));
                                                List loop = SootUtilities.createForLoopBefore(body,
                                                        unit,
                                                        initializerList,
                                                        bodyList, 
                                                        conditionalExpr);
                                                body.getUnits().insertBefore(loop, unit);

                                                if(!bufferSizeValue.equals(IntConstant.v(1))) {
                                                    // store back.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    Jimple.v().newArrayRef(indexArrayLocal, 
                                                                            channelValue),
                                                                    indexLocal),
                                                            unit);
                                                }
                                                // Replace the get() call.
                                                box.setValue(returnArrayLocal);
                                            }
                                        } 
                                    } else if(r.getMethod().getName().equals("send")) {
                                        // Could be send that takes a channel and returns a token,
                                        // or send that takes a channel and an array of tokens.          
                                        // In either case, replace the send with circular array ref.
                                        Value channelValue = r.getArg(0);

                                        Value bufferSizeValue = _getBufferAndSize(entityClass, body, 
                                                unit, port, channelValue, bufferLocal);
                                        
                                        _getCorrectIndex(entityClass, body, 
                                                unit, port, indexLocal, indexArrayLocal, 
                                                channelValue, bufferSizeValue);
                                        
                                        // If we are calling with just a channel, then read the value.
                                        if(r.getArgCount() == 2) {
                                            // increment the position in the buffer.
                                            body.getUnits().insertAfter(
                                                    _createIndexUpdateInstructions( 
                                                            indexLocal, indexArrayLocal, 
                                                            channelValue, bufferSizeValue),
                                                    unit);
                                            // Replace the put() with an array write.
                                            body.getUnits().swapWith(unit,
                                                    Jimple.v().newAssignStmt(
                                                            Jimple.v().newArrayRef(bufferLocal, 
                                                                    indexLocal), r.getArg(1))); 
                                        } else {
                                            // We must send an array of tokens.
                                            body.getUnits().insertBefore(
                                                    Jimple.v().newAssignStmt(
                                                            returnArrayLocal, 
                                                            r.getArg(1)),
                                                    unit);
                                            // If the count is specified statically
                                            if(Evaluator.isValueConstantValued(r.getArg(2))) {
                                                int argCount =
                                                    ((IntConstant)Evaluator.getConstantValueOf(r.getArg(2))).value;
                                                for(int k = 0; k < argCount; k++) {
                                                    // Get the value.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    returnLocal,
                                                                    Jimple.v().newArrayRef(returnArrayLocal,
                                                                            IntConstant.v(k))),
                                                            unit);
                                                    // Store in the buffer array.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    Jimple.v().newArrayRef(bufferLocal,
                                                                            indexLocal),
                                                                    returnLocal),
                                                            unit);
                                                    // increment the position in the buffer.
                                                    body.getUnits().insertBefore(
                                                            _createIndexUpdateInstructions( 
                                                                    indexLocal, indexArrayLocal, 
                                                                    channelValue, bufferSizeValue),
                                                            unit);
                                                }
                                                // blow away the send.
                                                body.getUnits().remove(unit);
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
                                                                returnLocal,
                                                                Jimple.v().newArrayRef(returnArrayLocal,
                                                                        counterLocal)));
                                                       
                                                // Store in the buffer array.
                                                bodyList.add(Jimple.v().newAssignStmt(
                                                                Jimple.v().newArrayRef(bufferLocal,
                                                                        indexLocal),
                                                                returnLocal)); 
                                                if(!bufferSizeValue.equals(IntConstant.v(1))) {
                                                    // increment the position.
                                                    bodyList.add(Jimple.v().newAssignStmt(
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
                                                            r.getArg(2));
                                                List loop = SootUtilities.createForLoopBefore(body,
                                                        unit,
                                                        initializerList,
                                                        bodyList, 
                                                        conditionalExpr);
                                                body.getUnits().insertBefore(loop, unit);

                                                if(!bufferSizeValue.equals(IntConstant.v(1))) {
                                                    // store back.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    Jimple.v().newArrayRef(indexArrayLocal, 
                                                                            channelValue),
                                                                    indexLocal),
                                                            unit);
                                                }
                                                // blow away the send.
                                                body.getUnits().remove(unit);
                                            }
                                        }
                                    } else if(r.getMethod().getName().equals("broadcast")) {
                                        // Could be broadcast that takes a token,
                                        // or broadcaste that takes an array of tokens.          
                                        // In either case, replace the broadcast with circular array ref.
                                        
                                        SootField indexArrayField = 
                                            entityClass.getFieldByName("_index_" + port.getName());
                                                      
                                        // Load the array of indexes.
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(indexArrayLocal,
                                                        Jimple.v().newInstanceFieldRef(
                                                                body.getThisLocal(),
                                                                indexArrayField)), 
                                                unit);
                                        
                                        
                                        Value bufferSizeValue = null;
                                        // Refer directly to the buffer in the 
                                        // model
                                        int channel = 0;
                                        for(Iterator relations = port.linkedRelationList().iterator();
                                            relations.hasNext();) {
                                            IORelation relation = (IORelation)relations.next();
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
                                                
                                            for(int i = 0; 
                                                i < relation.getWidth();
                                                i++, channel++) {
                                                Value channelValue = IntConstant.v(channel);
                                                
                                                // Load the correct index into indexLocal
                                                body.getUnits().insertBefore(
                                                        Jimple.v().newAssignStmt(indexLocal,
                                                                Jimple.v().newArrayRef(
                                                                        indexArrayLocal, 
                                                                        channelValue)),
                                                        unit); 
                                                        
                                                SootField arrayField = 
                                                    Scene.v().getMainClass().getFieldByName(
                                                            "_" + relation.getName() + "_" + i);
                                                        
                                                // load the buffer array.
                                                body.getUnits().insertBefore(
                                                        Jimple.v().newAssignStmt(bufferLocal,
                                                                Jimple.v().newStaticFieldRef(arrayField)),
                                                        unit);

                                                // If we are calling with just a token, then send the token.
                                                if(r.getArgCount() == 1) {
                                                    // Write to the buffer.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    Jimple.v().newArrayRef(bufferLocal, 
                                                                            indexLocal), r.getArg(0)),
                                                            unit); 
                                                    // increment the position in the buffer.
                                                    body.getUnits().insertBefore(
                                                            _createIndexUpdateInstructions( 
                                                                    indexLocal, indexArrayLocal, 
                                                                    channelValue, bufferSizeValue),
                                                            unit);
                                                    // blow away the send.
                                                    body.getUnits().remove(unit);
                                                } else {
                                                    // We must send an array of tokens.
                                                    body.getUnits().insertBefore(
                                                            Jimple.v().newAssignStmt(
                                                                    returnArrayLocal, 
                                                                    r.getArg(0)),
                                                            unit);
                                                    // If the count is specified statically
                                                    if(Evaluator.isValueConstantValued(r.getArg(1))) {
                                                        int argCount =
                                                            ((IntConstant)Evaluator.getConstantValueOf(
                                                                    r.getArg(1))).value;
                                                        for(int k = 0; k < argCount; k++) {
                                                            // Get the value.
                                                            body.getUnits().insertBefore(
                                                                    Jimple.v().newAssignStmt(
                                                                            returnLocal,
                                                                            Jimple.v().newArrayRef(returnArrayLocal,
                                                                                    IntConstant.v(k))),
                                                                    unit);
                                                            // Store in the buffer array.
                                                            body.getUnits().insertBefore(
                                                                    Jimple.v().newAssignStmt(
                                                                            Jimple.v().newArrayRef(bufferLocal,
                                                                                    indexLocal),
                                                                            returnLocal),
                                                                    unit);
                                                            // increment the position in the buffer.
                                                            body.getUnits().insertBefore(
                                                                    _createIndexUpdateInstructions( 
                                                                            indexLocal, indexArrayLocal, 
                                                                            channelValue, bufferSizeValue),
                                                                    unit);
                                                        }
                                                        // blow away the send.
                                                        body.getUnits().remove(unit);
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
                                                                unit);
                                                        // Store in the buffer array.
                                                        body.getUnits().insertBefore(
                                                                Jimple.v().newAssignStmt(
                                                                        Jimple.v().newArrayRef(bufferLocal,
                                                                                indexLocal),
                                                                        returnLocal),
                                                                unit); 
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
                                                                    r.getArg(1));
                                                        List loop = SootUtilities.createForLoopBefore(body,
                                                                unit,
                                                                initializerList,
                                                                bodyList, 
                                                                conditionalExpr);
                                                        body.getUnits().insertBefore(loop, unit);
                                                                
                                                                // store back.
                                                        body.getUnits().insertBefore(
                                                                Jimple.v().newAssignStmt(
                                                                        Jimple.v().newArrayRef(indexArrayLocal, 
                                                                                channelValue),
                                                                        indexLocal),
                                                                unit);
                                                        // blow away the send.
                                                        body.getUnits().remove(unit);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                    LocalNameStandardizer.v().transform(body, phaseName + ".lns", "");
                }
            }            
        }
    }

    /** Attempt to determine the constant value of the 
     *  given local, which is assumed to have a variable
     *  type.  Walk backwards through all the possible 
     *  places that the local may have been defined and
     *  try to symbolically evaluate the value of the 
     *  variable. If the value can be determined, 
     *  then return it, otherwise return null.
     */ 
    public static Port getPortValue(SootMethod method, Local local, 
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof CastExpr) {
                return getPortValue(method, (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if(value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if(tag == null) {
                    return null;
                } else {
                    return (Port)tag.getObject();
                }
           
            } else if(value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while(pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if(pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt = (DefinitionStmt)pair.getUnit();
                        if(useStmt.getLeftOp() instanceof FieldRef) {
                            SootField field = ((FieldRef)useStmt.getLeftOp()).getField();
                            ValueTag tag = (ValueTag)field.getTag("_CGValue");
                            if(tag == null) {
                                return null;
                            } else {
                                return (Port)tag.getObject();
                            }
                        }
                    }
                }
            } else {
                System.out.println("InlinePortTransformer.getAttributeValue(): Unknown value = " 
                        + value + " searching for local " + local + " in method " + method);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for(Iterator i = definitionList.iterator();
                i.hasNext();) {
                System.out.println(i.next().toString());
            }
        }
        return null;
    }

    private List _createIndexUpdateInstructions(
            Local indexLocal, Local indexArrayLocal, Value channelValue, 
            Value bufferSizeValue) {
        // Now update the index into the buffer.
        List list = new LinkedList();
        // If the buffer is size one, then the below code is a noop.
        if(bufferSizeValue.equals(IntConstant.v(1))) {
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

    /** Insert code into the given body before the given unit that will retrieve
     *  the communication buffer associated with the given channel of the given 
     *  port.  The given local variable will refer to the buffer.  A value
     *  containing the size of the given buffer will be returned.
     */
    private Value _getBufferAndSize(SootClass entityClass, JimpleBody body, 
            Unit unit, Port port, Value channelValue, Local bufferLocal) {
        
        Value bufferSizeValue = null;
        // Now get the appropriate buffer
        if(Evaluator.isValueConstantValued(channelValue)) {
            // If we know the channel, then refer directly to the buffer in the 
            // model
            int argChannel = ((IntConstant)Evaluator.getConstantValueOf(channelValue)).value;
            int channel = 0;
            boolean found = false;
            for(Iterator relations = port.linkedRelationList().iterator();
                !found && relations.hasNext();) {
                IORelation relation = (IORelation)relations.next();
                
                for(int i = 0; 
                    !found && i < relation.getWidth();
                    i++, channel++) {
                    if(channel == argChannel) {
                        found = true;
                        SootField arrayField = 
                            Scene.v().getMainClass().getFieldByName(
                                    "_" + relation.getName() + "_" + i);
                                                        
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
            if(!found) {
                throw new RuntimeException("Constant channel not found!");
            }
        } else {
            // If we don't know the channel, then use the port indexes.
            SootField arrayField = 
                entityClass.getFieldByName("_portbuffer_" + port.getName());
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

    /** Retrieve the correct index into the given channel of the given port into the 
     *  given local variable.  
     */
    private static void _getCorrectIndex(SootClass entityClass, JimpleBody body, Unit unit, 
            TypedIOPort port, Local indexLocal, Local indexArrayLocal, Value channelValue, Value bufferSizeValue) {
   
        if(bufferSizeValue.equals(IntConstant.v(1))) {
            // Load the correct index into indexLocal
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(indexLocal,
                            IntConstant.v(0)),
                    unit); 
        } else {             
            SootField indexArrayField = 
                entityClass.getFieldByName("_index_" + port.getName());
            
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

    private CompositeActor _model;
}














