/* A code generator for SDF.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.CompositeActorApplication;
import ptolemy.codegen.*;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFCodeGenerator
/** A code generator for SDF.
 *
 *  @author Jeff Tsay
 */
public class SDFCodeGenerator extends CompositeActorApplication
    implements JavaStaticSemanticConstants {

    public SDFCodeGenerator(String[] args) throws Exception {
        super(args, false);
    }

    public void generateCode() throws IllegalActionException {
        if (_outputDirectoryName == null) {
            throw new RuntimeException("output directory was not specified " +
                    "with the -outdir option");
        }

        if (_outputPackageName == null) {
            throw new RuntimeException("output package was not specified " +
                    "with the -outpkg option");
        }

        _packageDirectoryName = _outputDirectoryName + File.separatorChar +
            _outputPackageName.replace('.', File.separatorChar) +
            File.separatorChar;

        // Create the directory to put the output package in,
        // creating subdirectories as needed.
        // This must be done before the Java compiler classes are loaded so
        // that they can find the output package.
        // (this is a nasty hack)
        new File(_packageDirectoryName).mkdirs();

        // write a dummy CG_Main.java file to the output package directory so
        // that a ClassDecl stub is placed in the package environment for it
        // (this is a nasty hack)
        try {
            new File(_packageDirectoryName + "CG_Main.java").createNewFile();
        } catch (IOException ioe) {
            ApplicationUtility.error("could not create output directory " +
                    _packageDirectoryName);
        }

        // assume just one model on the command line

        _compositeActor = (TypedCompositeActor) _models.get(0);

        try {
            // initialize the model to ensure type resolution and scheduling
            // are done
            _compositeActor.getManager().initialize();
        } catch (Exception e) {
            ApplicationUtility.error("could not initialize composite actor");
        }

        // get the schedule for the model
        _director = (SDFDirector) _compositeActor.getDirector();
        _scheduler = (SDFScheduler) _director.getScheduler();

        Enumeration schedule = _scheduler.schedule();

        // build a mapping between each actor and the firing count

        TypedAtomicActor lastActor = null;

        // gather information about the actor
        // disjointAppearances is not actually used, but it may be in the
        // future.

        while (schedule.hasMoreElements()) {

            TypedAtomicActor actor = (TypedAtomicActor) schedule.nextElement();

            // see if this is the first appearance of this actor
            if (_actorSet.add(actor)) {
                SDFActorCodeGeneratorInfo actorInfo =
                    new SDFActorCodeGeneratorInfo();

                actorInfo.actor = actor;
                actorInfo.disjointAppearances = 1;
                actorInfo.totalFirings = _scheduler.getFiringCount(actor);

                _makeBufferInfo(actor, actorInfo);

                _actorInfoMap.put(actor, actorInfo);

                ApplicationUtility.trace("actor " + actor + " fires " +
                        actorInfo.totalFirings + " time(s).");

            }  else {
                if (actor != lastActor) {
                    // update the disjoint appearance count
                    SDFActorCodeGeneratorInfo actorInfo =
                        (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);

                    actorInfo.disjointAppearances++;
                }
            }
            lastActor = actor;
        }

        // now drive the 3 passes of transformation

        Iterator actorItr = _actorSet.iterator();

        ActorCodeGenerator actorCodeGen =
            new ActorCodeGenerator(_codeGenClassFactory, _outputDirectoryName,
                    _outputPackageName);

        LinkedList renamedSourceList = new LinkedList();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();
            SDFActorCodeGeneratorInfo actorInfo =
                (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);

            _makeInputInfo(actor, actorInfo);

            String renamedSource = actorCodeGen.pass1(actorInfo);
            renamedSourceList.addLast(renamedSource);
        }

        actorItr = _actorSet.iterator();
        Iterator renamedSourceItr = renamedSourceList.iterator();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();
            SDFActorCodeGeneratorInfo actorInfo =
                (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);

            String renamedSource = (String) renamedSourceItr.next();

            actorCodeGen.pass2(renamedSource, actorInfo);
        }

        _generateMainClass();

        renamedSourceItr = renamedSourceList.iterator();

        while (renamedSourceItr.hasNext()) {
            String renamedSource = (String) renamedSourceItr.next();

            actorCodeGen.pass3(renamedSource);
        }
    }

    /** The top-level main() method. Create an SDF code generator using the
     *  input arguments as they would be used for CompositeActorApplication,
     *  and generate code for the system.
     */
    public static void main(String[] args) {
        SDFCodeGenerator codeGen = null;

        try {
            codeGen = new SDFCodeGenerator(args);

            codeGen.generateCode();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }

    /** Generate the main class. */
    protected void _generateMainClass() throws IllegalActionException {

        LinkedList memberList = new LinkedList();

        memberList.addLast(_generateMainMethod());

        Iterator bufferItr = _bufferInfoMap.values().iterator();
        PtolemyTypeIdentifier typeID =
            _codeGenClassFactory.createPtolemyTypeIdentifier();

        while (bufferItr.hasNext()) {
            BufferInfo bufferInfo = (BufferInfo) bufferItr.next();

            TypeNode dataTypeNode =
                typeID.encapsulatedDataType(bufferInfo.type);

            int bufferWidth = bufferInfo.width;
            int bufferDimension = (bufferWidth <= 1) ? 1 : 2;

            TypeNode typeNode = TypeUtility.makeArrayType(dataTypeNode,
                    bufferDimension);

            int bufferLength = bufferInfo.length;

            LinkedList dimExprList = TNLManip.cons(new IntLitNode(
                    String.valueOf(bufferLength)));

            if (bufferDimension > 1) {
                dimExprList.addFirst(new IntLitNode(
                        String.valueOf(bufferWidth)));
            }

            TypeNode dataBaseTypeNode = TypeUtility.arrayBaseType(dataTypeNode);
            int dataTypeDims = TypeUtility.arrayDimension(dataTypeNode);

            AllocateArrayNode allocateArrayNode = new AllocateArrayNode(
                    dataBaseTypeNode, dimExprList, dataTypeDims,
                    AbsentTreeNode.instance);

            FieldDeclNode fieldDeclNode = new FieldDeclNode(
                    PUBLIC_MOD | STATIC_MOD | FINAL_MOD, typeNode,
                    new NameNode(AbsentTreeNode.instance, bufferInfo.codeGenName),
                    allocateArrayNode);

            memberList.add(fieldDeclNode);
        }

        ClassDeclNode classDeclNode = new ClassDeclNode(PUBLIC_MOD,
                new NameNode(AbsentTreeNode.instance, "CG_Main"),
                new LinkedList(), memberList,
                (TypeNameNode) StaticResolution.OBJECT_TYPE.clone());

        // bring in imports for Complex and FixPoint
        // (remove unnecessary ones later)
        LinkedList importList = new LinkedList();

        importList.add(new ImportNode((NameNode)
                StaticResolution.makeNameNode("ptolemy.math.Complex")));
        importList.add(new ImportNode((NameNode)
                StaticResolution.makeNameNode("ptolemy.math.FixPoint")));

        CompileUnitNode unitNode = new CompileUnitNode(
                new NameNode(AbsentTreeNode.instance, _outputPackageName),
                importList, TNLManip.cons(classDeclNode));

        String outFileName = _packageDirectoryName +  "CG_Main.java";

        JavaCodeGenerator.writeCompileUnitNodeList(TNLManip.cons(unitNode),
                TNLManip.cons(outFileName));
    }

    /** Generate the main() method of the main class. */
    protected MethodDeclNode _generateMainMethod()
            throws IllegalActionException {
        // a map of actors to the ObjectNodes that represent the actors
        HashMap actorToVariableMap = new HashMap();

        LinkedList stmtList = new LinkedList();

        Iterator actorItr = _actorSet.iterator();

        // generate actor allocation statments and fill in actorToVariableMap
        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

            String oldActorClassString =
                StringManip.unqualifiedPart(actor.getClass().toString());

            String actorName =  actor.getName();

            String actorClassString = "CG_" + oldActorClassString +
                "_" + actorName;

            NameNode actorVarNameNode = new NameNode(AbsentTreeNode.instance,
                    "cg_" + oldActorClassString + "_" + actorName);

            TypeNameNode actorTypeNode = new TypeNameNode(
                    new NameNode(AbsentTreeNode.instance, actorClassString));

            AllocateNode allocateActorNode = new AllocateNode(actorTypeNode,
                    new LinkedList(), AbsentTreeNode.instance);

            stmtList.addLast(new LocalVarDeclNode(FINAL_MOD, actorTypeNode,
                    actorVarNameNode, allocateActorNode));

            actorToVariableMap.put(actor, new ObjectNode(
                    (NameNode) actorVarNameNode.clone()));
        }

        // generate preinitialize statements
        actorItr = _actorSet.iterator();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

            ObjectNode actorObjectNode = (ObjectNode)
                ((ObjectNode) actorToVariableMap.get(actor)).clone();


            MethodCallNode methodCallNode = new MethodCallNode(
                    new ObjectFieldAccessNode(
                            new NameNode(AbsentTreeNode.instance, "preinitialize"),
                            actorObjectNode),
                    new LinkedList());

            stmtList.addLast(new ExprStmtNode(methodCallNode));
        }

        // generate initialize statements
        actorItr = _actorSet.iterator();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

            ObjectNode actorObjectNode = (ObjectNode)
                ((ObjectNode) actorToVariableMap.get(actor)).clone();

            MethodCallNode methodCallNode = new MethodCallNode(
                    new ObjectFieldAccessNode(
                            new NameNode(AbsentTreeNode.instance, "initialize"),
                            actorObjectNode),
                    new LinkedList());

            stmtList.addLast(new ExprStmtNode(methodCallNode));
        }

        // generate the iteration loop
        LinkedList iterationStmtList = new LinkedList();

        Enumeration schedule = null;

        TypedAtomicActor lastActor = null;
        int contiguousAppearances = 0;

        schedule = _scheduler.schedule();

        // Iterate through the schedule. We generate the prefire(), fire(), and
        // postfire() sequence for the previous after the next different actor
        // is encountered. In order to generate the sequence for the last actor
        // in the schedule, we need to go past the last actor when iterating.

        // make sure there is something in the schedule
        boolean endLoop = !schedule.hasMoreElements();
        while (!endLoop) {

            TypedAtomicActor actor = null;

            if (schedule.hasMoreElements()) {
                actor = (TypedAtomicActor) schedule.nextElement();
            } else {
                actor = null;
                endLoop = true;
            }

            if (actor != lastActor) {
                if (lastActor != null) {
                    ObjectNode actorVarNode = (ObjectNode)
                        ((ObjectNode) actorToVariableMap.get(lastActor)).clone();

                    ExprStmtNode prefireCallStmtNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance, "prefire"),
                                    actorVarNode),
                                    new LinkedList()));

                    // Every node in the tree needs to be unique, so the
                    // actor node needs to be cloned here.
                    ExprStmtNode fireCallStmtNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance, "fire"),
                                    ((ObjectNode)actorVarNode.clone())),
                                    new LinkedList()));

                    ExprStmtNode postfireCallStmtNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance, "postfire"),
                                    ((ObjectNode)actorVarNode.clone())),
                                    new LinkedList()));

                    LinkedList iterationCalls = new LinkedList();
                    iterationCalls.add(prefireCallStmtNode);
                    iterationCalls.add(fireCallStmtNode);
                    iterationCalls.add(postfireCallStmtNode);

                    BlockNode iterationBlockNode =
                        new BlockNode(iterationCalls);

                    if (contiguousAppearances > 1) {

                        NameNode loopCounterNameNode =
                            new NameNode(AbsentTreeNode.instance, "fc");

                        ObjectNode loopCounterObjectNode =
                            new ObjectNode(loopCounterNameNode);

                        List forInitList = TNLManip.cons(
                                new LocalVarDeclNode(NO_MOD, IntTypeNode.instance,
                                        (NameNode) loopCounterNameNode.clone(),
                                        new IntLitNode("0")));

                        LTNode forTestExprNode = new LTNode(
                                loopCounterObjectNode,
                                new IntLitNode(String.valueOf(contiguousAppearances)));

                        List forUpdateList = TNLManip.cons(new PostIncrNode(
                                (ObjectNode) loopCounterObjectNode.clone()));

                        iterationStmtList.addLast(new ForNode(forInitList,
                                forTestExprNode, forUpdateList, iterationBlockNode));
                    }  else {
                        iterationStmtList.addLast(iterationBlockNode);
                    }
                }
                lastActor = actor;
                contiguousAppearances = 1;
            } else {
                contiguousAppearances++;
            }
        }

        int iterations =
            ((IntToken) _director.iterations.getToken()).intValue();

        if (iterations == 1) {
            // just add all the iteration statements to the list of all statements
            stmtList.addAll(iterationStmtList);
        } else {
            if (iterations == 0) {
                // iterate forever
                stmtList.addLast(new LoopNode(new BlockNode(iterationStmtList),
                        new BoolLitNode("true"), AbsentTreeNode.instance));
            } else {

                NameNode loopCounterNameNode =
                    new NameNode(AbsentTreeNode.instance, "it");

                ObjectNode loopCounterObjectNode =
                    new ObjectNode(loopCounterNameNode);

                List forInitList = TNLManip.cons(
                        new LocalVarDeclNode(NO_MOD, IntTypeNode.instance,
                                (NameNode) loopCounterNameNode.clone(),
                                new IntLitNode("0")));

                LTNode forTestExprNode = new LTNode(
                        loopCounterObjectNode,
                        new IntLitNode(String.valueOf(iterations)));

                List forUpdateList = TNLManip.cons(new PostIncrNode(
                        (ObjectNode) loopCounterObjectNode.clone()));

                stmtList.addLast(new ForNode(forInitList, forTestExprNode,
                        forUpdateList, new BlockNode(iterationStmtList)));
            }
        }

        // generate wrapup statements, unless we are iterating forever.
        if (iterations != 0) {
            actorItr = _actorSet.iterator();

            while (actorItr.hasNext()) {
                TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

                ObjectNode actorObjectNode = (ObjectNode)
                    ((ObjectNode) actorToVariableMap.get(actor)).clone();

                MethodCallNode methodCallNode = new MethodCallNode(
                        new ObjectFieldAccessNode(
                                new NameNode(AbsentTreeNode.instance, "wrapup"),
                                actorObjectNode),
                        new LinkedList());

                stmtList.addLast(new ExprStmtNode(methodCallNode));
            }
        }

        return new MethodDeclNode(PUBLIC_MOD | STATIC_MOD,
                new NameNode(AbsentTreeNode.instance, "main"),
                TNLManip.cons(new ParameterNode(NO_MOD,
                        TypeUtility.makeArrayType(
                                (TypeNode) StaticResolution.STRING_TYPE.clone(), 1),
                        new NameNode(AbsentTreeNode.instance, "args"))),
                new LinkedList(), new BlockNode(stmtList), VoidTypeNode.instance);
    }


    /** Figure out which buffers are connected to each input port of a given
     *  TypedAtomicActor, and add the information to the instance of
     *  SDFActorCodeGeneratorInfo argument.
     */
    protected void _makeInputInfo(TypedAtomicActor actor,
            SDFActorCodeGeneratorInfo actorInfo) throws IllegalActionException {

        // iterate over the ports of this actor
        Iterator portItr = actor.portList().iterator();

        while (portItr.hasNext()) {
            TypedIOPort port = (TypedIOPort) portItr.next();

            // we are only concerned with input ports
            if (port.isInput()) {

                int inputWidth = port.getWidth();

                List connectedPortList = port.connectedPortList();

                String[] bufferNames = new String[inputWidth];
                int[] bufferLengths = new int[inputWidth];

                Receiver[][] receivers = port.getReceivers();

                for (int channel = 0; channel < inputWidth; channel++) {
                    // get the receiver for this channel
                    // we only support one reciever per channel
                    Receiver receiver = receivers[channel][0];

                    // Find the output port for this channel and the channel
                    // number connecting it to this port.
                    // This is done by matching the receiver for this
                    // channel with a remoteReceiver of a connected output port

                    int outputChannel = -1;

                    // search all output ports connected to this port
                    Iterator connectedPortItr = connectedPortList.iterator();

                    TypedIOPort outputPort = null;

                    while (connectedPortItr.hasNext() && (outputPort == null)) {
                        TypedIOPort connectedPort =
                            (TypedIOPort) connectedPortItr.next();

                        // search only output ports
                        if (connectedPort.isOutput()) {
                            int outputWidth = connectedPort.getWidth();

                            Receiver[][] remoteReceiversArray =
                                connectedPort.getRemoteReceivers();

                            // search all channels of the output port
                            int ch = 0;
                            do {

                                Receiver[] remoteReceivers =
                                    remoteReceiversArray[ch];

                                int i = 0;

                                // search all receivers in the same receiver group
                                while ((i < remoteReceivers.length) &&
                                        (outputPort == null)) {

                                    Receiver remoteReceiver = remoteReceivers[i];

                                    if (receiver == remoteReceiver) {
                                        outputPort = connectedPort;
                                        outputChannel = ch;
                                    }
                                    i++;
                                }
                                ch++;
                            } while ((ch < outputWidth) && (outputPort == null));
                        }
                    }

                    if (outputPort == null) {
                        throw new InternalError(
                                "could not find output port associated " +
                                "with channel " + channel + " for port " +
                                port.getName() + " of actor " + actor + '.');
                    }

                    BufferInfo bufferInfo =
                        (BufferInfo) _bufferInfoMap.get(outputPort);

                    bufferLengths[channel] = bufferInfo.length;

                    if (bufferInfo.width == 1) {
                        bufferNames[channel] = bufferInfo.codeGenName;
                    } else {
                        bufferNames[channel] =
                            bufferInfo.codeGenName + '[' + outputChannel + ']';
                    }

                } // for (int channel = 0; channel < inputWidth; channel++) ...


                ApplicationUtility.trace("connected buffers for port " + port.getName() +
                        " of actor " + actor.getName());
                for (int ch = 0; ch < inputWidth; ch++) {
                    ApplicationUtility.trace("ch " + ch + ": " + bufferNames[ch]);
                }

                actorInfo.inputBufferNameMap.put(port, bufferNames);
                actorInfo.inputBufferLengthMap.put(port, bufferLengths);


            } // if (port.isInput()) ...
        } // while (portItr.hasNext()) ...
    }

    /** Create instances of BufferInfo for each output port of the argument
     *  actor, and put them in the actor to buffer info map.
     */
    protected void _makeBufferInfo(TypedAtomicActor actor,
            SDFActorCodeGeneratorInfo actorInfo) throws IllegalActionException {

        int firings = actorInfo.totalFirings;

        Iterator portItr = actor.portList().iterator();

        while (portItr.hasNext()) {
            TypedIOPort port = (TypedIOPort) portItr.next();

            // allocate one buffer for each output port
            if (port.isOutput()) {

                BufferInfo bufferInfo = new BufferInfo();
                bufferInfo.name = port.getName();
                bufferInfo.codeGenName = _makeUniqueName(bufferInfo.name);
                bufferInfo.width = port.getWidth();

                // set length of buffer =
                // init token production +
                // number of firings * token production rate
                // (a worst case length)

                int productionRate;
                int initProduction;

                if (port instanceof SDFIOPort) {
                    SDFIOPort sdfIOPort = (SDFIOPort) port;
                    productionRate = sdfIOPort.getTokenProductionRate();
                    initProduction = sdfIOPort.getTokenInitProduction();
                } else {
                    // for non-SDFIOPorts, the production rate is assumed to be 1,
                    // and the init production rate is assumed to be 0
                    productionRate = 1;
                    initProduction = 0;
                }
                bufferInfo.length = initProduction + firings * productionRate;

                bufferInfo.type = port.getType();

                actorInfo.outputInfoMap.put(port, bufferInfo);

                _bufferInfoMap.put(port, bufferInfo);
            }
        }
    }

    /** Given the name of an object, return a globally unique Java identifier
     *  containing
     *  the name, in the format "_cg_NAME_#" where NAME is the argument value
     *  and # is a number.
     */
    protected String _makeUniqueName(String name) {
        String retval = "_cg_" + name + "_" + labelNum;
        labelNum++;
        return retval;
    }

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-outdir")) {
            _expectingOutputDirectory = true;
        } else if (arg.equals("-outpkg")) {
            _expectingOutputPackage = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version 1.0, Build $Id$");
            System.exit(0);
        } else {
            if (_expectingOutputDirectory) {
                _outputDirectoryName = new File(arg).getCanonicalPath();
                _expectingOutputDirectory = false;
            }  else if  (_expectingOutputPackage) {
                _outputPackageName = arg;
                _expectingOutputPackage = false;
            } else {
                return super._parseArg(arg);
            }
        }
        return true;
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + "SDFCodeGenerator [options]" + "\n\n"
            + "Options that take values:\n";

        int i;
        for(i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }
        result += " -outdir <output directory>\n";
        result += " -outpkg <output package>\n";

        result += "\nBoolean flags:\n";
        for(i = 0; i < _commandFlags.length; i++) {
            result += " " + _commandFlags[i];
        }
        return result;
    }



    /** The TypedCompositeActor containing the system for which to generate
     *  code.
     */
    protected TypedCompositeActor _compositeActor = null;

    /** The director for the SDF system. */
    protected SDFDirector _director = null;

    /** The scheduler for the SDF system. */
    protected SDFScheduler _scheduler = null;

    /** The set of all actors in the system. */
    protected HashSet _actorSet = new HashSet();

    /** A map containing instances of SDFActorCodeGeneratorInfo, using the
     *  corresponding Actors as keys.
     */
    protected HashMap _actorInfoMap = new HashMap();

    /** A map containing instances of BufferInfo, using the corresponding
     *  ports as keys. This map contains all BufferInfos for all output
     *  ports in the CompositeActor.
     */
    protected HashMap _bufferInfoMap = new HashMap();

    protected CodeGeneratorClassFactory _codeGenClassFactory =
    SDFCodeGeneratorClassFactory.getInstance();

    /** A non-decreasing number used for globally unique labeling. */
    protected int labelNum = 0;

    /** The canonical pathname of the directory in which to place the
     *  output package, not including the last file separator character.
     */
    protected String _outputDirectoryName; // no initializer here

    /** The name of the qualified package in which to put the generated
     *  code.
     */
    protected String _outputPackageName; // no initializer here

    /** The canonical pathname of the output package, including the last
     *  file separator character.
     */
    protected String _packageDirectoryName;

    /** A flag indicating that we are expecting an output directory as the
     *  next argument in the command-line.
     */
    protected boolean _expectingOutputDirectory = false;

    /** A flag indicating that we are expecting an output package name as the
     *  next argument in the command-line.
     */
    protected boolean _expectingOutputPackage = false;
