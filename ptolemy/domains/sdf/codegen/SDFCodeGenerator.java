/* A code generator for SDF.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.CompositeActorApplication;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.codegen.*;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFCodeGenerator
/**
   A code generator for SDF.  Command line arguments are specified as
   with the CompositeActorApplication class, except that the first
   argument can optionally be used to enable shallow loading of
   abstract syntax trees: if the first argument is "-shallowLoading"
   then shallow loading will be enabled. Shallow loading is an
   experimental feature that decreases the size and number of abstract
   syntax trees that have to be loaded during code generation. For
   details on shallow loading, see ptolemy.lang.java.ASTReflect.

@author Jeff Tsay, Christopher Hylands
@version $Id$
 */
public class SDFCodeGenerator extends CompositeActorApplication
        implements JavaStaticSemanticConstants {

    public void generateCode() throws IllegalActionException {

        // We print elapsed time statistics during code generation
        // Note that this is different than printing stats at runtime.
        long startTime = new Date().getTime();
        if (_outputDirectoryName == null) {
            throw new RuntimeException("output directory was not specified " +
                    "with the -outdir option");
        }

        // Assume exactly one model on the command line.
        _compositeActor = (TypedCompositeActor)
            _models.iterator().next();

        _sourceSystemClassName = _compositeActor.getClass().getName();

        if (_outputPackageName == null) {
            // Output Package name defaults to cg.Foo.
            _outputPackageName = "cg." +
                _sourceSystemClassName.substring(_sourceSystemClassName
                        .lastIndexOf('.') + 1);
        }

        _packageDirectoryName = _outputDirectoryName + File.separatorChar
            + _outputPackageName.replace('.', File.separatorChar)
            + File.separatorChar;

        if (!_makefileOnly) {
	    // Create the directory to put the output package in,
	    // creating subdirectories as needed.
	    // This must be done before the Java compiler classes are loaded so
	    // that they can find the output package.
	    // (this is a nasty hack)
	    System.out.println("packageDir = " + _packageDirectoryName);
	    new File(_packageDirectoryName).mkdirs();
	}

        try {
            _generateMakefile();
        } catch ( IOException ioe) {
            System.err.println("Error: failed to write makefile: " + ioe);
        }

        if (_makefileOnly) {
            System.out.println("generated the makefile"); 
            return;
        }

        // FIXME: this should not be CG_Main.
        //_systemName = _compositeActor.getName();

        // Write a dummy main file to the output package directory so
        // that a ClassDecl stub is placed in the package scope for it
        // FIXME: (this is a nasty hack)
        try {
            new File(_packageDirectoryName
		     + _systemName
		     + ".java").createNewFile();
        } catch (IOException ioe) {
            throw new RuntimeException("could not create output directory " +
                    _packageDirectoryName);
        }

        // Initialize static resolution
        StaticResolution.setup();

        try {
            // initialize the model to ensure type resolution and scheduling
            // are done
            _compositeActor.getManager().initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not initialize composite actor");
        }

        // get the schedule for the model
        _director = (SDFDirector) _compositeActor.getDirector();
        _scheduler = (SDFScheduler) _director.getScheduler();

        Iterator schedule = _scheduler.getSchedule().actorIterator();

        // build a mapping between each actor and the firing count

        TypedAtomicActor lastActor = null;

        // gather information about the actor
        // disjointAppearances is not actually used, but it may be in the
        // future.

        while (schedule.hasNext()) {

            TypedAtomicActor actor = (TypedAtomicActor) schedule.next();

            // see if this is the first appearance of this actor
            if (_actorSet.add(actor)) {
                SDFActorCodeGeneratorInfo actorInfo =
                    new SDFActorCodeGeneratorInfo();

                actorInfo.actor = actor;
                actorInfo.disjointAppearances = 1;
                actorInfo.totalFirings = _scheduler.getFiringCount(actor);

                _makeBufferInfo(actor, actorInfo);

                _actorInfoMap.put(actor, actorInfo);

                //System.out.println("actor " + actor + " fires " +
                //        actorInfo.totalFirings + " time(s).");

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

        // Now drive the 3 passes of transformation.

        Iterator actorItr = _actorSet.iterator();

	System.out.println("SDFCodeGenerator: Starting to accumulate "
			   + "class info --- "
			   + Manager.timeAndMemory(startTime));

        ActorCodeGenerator actorCodeGen =
            new ActorCodeGenerator(_codeGeneratorClassFactory,
                    _outputDirectoryName,
                    _outputPackageName);

        System.out.println("\nSDFCodeGenerator: "
			   + "Done accumulating class info ---"
			   + Manager.timeAndMemory(startTime));

        System.out.println("SDFCodeGenerator: calling System.gc()");
	System.gc();

        System.out.println("SDFCodeGenerator: pass1 ---"
			   + Manager.timeAndMemory(startTime));


        LinkedList renamedSourceList = new LinkedList();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();
            SDFActorCodeGeneratorInfo actorInfo =
                (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);

            _makeInputInfo(actor, actorInfo);

            String renamedSource = actorCodeGen.pass1(actorInfo);
            renamedSourceList.addLast(renamedSource);
        }

	if (_exitAfterPass1) {
	    System.out.println("\nSDFCodeGenerator: exiting after pass 1");
	    return;
	}

        System.out.println("\nSDFCodeGenerator: pass2 ---"
			   + Manager.timeAndMemory(startTime));

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

	if (_exitAfterPass2) {
	    System.out.println("\nSDFCodeGenerator: exiting after pass 2");
	    return;
	}

        System.out.println("\nSDFCodeGenerator: pass3 ---"
			   + Manager.timeAndMemory(startTime));

        renamedSourceItr = renamedSourceList.iterator();

        while (renamedSourceItr.hasNext()) {
            String renamedSource = (String) renamedSourceItr.next();

            actorCodeGen.pass3(renamedSource);
        }

        System.out.println("AST loading status: "
                + ASTReflect.getLoadingStatus());
        System.out.println("\nSDFCodeGenerator: done "
			   + Manager.timeAndMemory(startTime));
    }

    /** Return the canonical pathname of the output package, including the last
     *  file separator character.
     *  @return the pathname of the output package.
     */
    public String getPackageDirectoryName() {
	return _packageDirectoryName;
    }

    /** The top-level main() method. Create an SDF code generator using the
     *  input arguments as they would be used for CompositeActorApplication,
     *  and generate code for the system.
     */
    public static void main(String[] args) {
        SDFCodeGenerator codeGen = new SDFCodeGenerator();

        try {
            codeGen.processArgs(args);
            codeGen.generateCode();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }

    /** Set to true if statistics such as elapsed time are printed
     *  at runtime by the generated Java code
     */
    public void setGenerateStatistics(boolean generateStatistics) {
	_generateStatistics = generateStatistics;
    }

    /** Set the output directory.
     *  @param the output directory.
     */
    public void setOutputDirectoryName(String outputDirectoryName) {
	_outputDirectoryName = outputDirectoryName;
    }

    /** Set the output package.
     *  @param the output package.
     */
    public void setOutputPackageName(String outputPackageName) {
	_outputPackageName = outputPackageName;
    }

    /** Set the list of models.
     *  @param the list of models.
     */
    public void setModels(List models) {
	_models = models;
    }

    /** Override the base class to not run the model.
     *  @param model The model to not execute.
     */
    public synchronized void startRun(CompositeActor model) {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Generate the main class. */
    protected void _generateMainClass() throws IllegalActionException {

        LinkedList memberList = new LinkedList();

        memberList.addLast(_generateMainMethod());

        Iterator bufferItr = _bufferInfoMap.values().iterator();
        PtolemyTypeIdentifier typeID =
            _codeGeneratorClassFactory.createPtolemyTypeIdentifier();

        while (bufferItr.hasNext()) {
            BufferInfo bufferInfo = (BufferInfo) bufferItr.next();

            TypeNode dataTypeNode =
                typeID.encapsulatedDataType(bufferInfo.type);

            int bufferWidth = bufferInfo.width;
            int bufferDimension = (bufferWidth <= 1) ? 1 : 2;

            TypeNode typeNode = TypeUtility.makeArrayType(dataTypeNode,
                    bufferDimension);

            int bufferLength = bufferInfo.length;

            LinkedList dimExprList = TNLManip.addFirst(new IntLitNode(
                    String.valueOf(bufferLength)));

            if (bufferDimension > 1) {
                dimExprList.addFirst(new IntLitNode(
                        String.valueOf(bufferWidth)));
            }

            TypeNode dataBaseTypeNode =
                TypeUtility.arrayBaseType(dataTypeNode);
            int dataTypeDims = TypeUtility.arrayDimension(dataTypeNode);

            AllocateArrayNode allocateArrayNode = new AllocateArrayNode(
                    dataBaseTypeNode, dimExprList, dataTypeDims,
                    AbsentTreeNode.instance);

            FieldDeclNode fieldDeclNode = new FieldDeclNode(
                    PUBLIC_MOD | STATIC_MOD | FINAL_MOD, typeNode,
                    new NameNode(AbsentTreeNode.instance,
                            bufferInfo.codeGenName),
                    allocateArrayNode);

            memberList.add(fieldDeclNode);
        }

        ClassDeclNode classDeclNode = new ClassDeclNode(PUBLIC_MOD,
                new NameNode(AbsentTreeNode.instance, _systemName),
                new LinkedList(), memberList,
                (TypeNameNode) StaticResolution.OBJECT_TYPE.clone());

        // Bring in imports for Complex and FixPoint
        // (remove unnecessary ones later)
        LinkedList importList = new LinkedList();

        importList.add(new ImportNode((NameNode)
                StaticResolution.makeNameNode("ptolemy.math.Complex")));
        importList.add(new ImportNode((NameNode)
                StaticResolution.makeNameNode("ptolemy.math.FixPoint")));

	if (_generateStatistics) {
	    // For timing measurements.
	    importList.add(new ImportNode((NameNode)
                    StaticResolution.makeNameNode("java.util.Date")));
	    importList.add(new ImportNode((NameNode)
                    StaticResolution.makeNameNode("java.io.PrintStream")));
	}

        CompileUnitNode unitNode = new CompileUnitNode(
                StaticResolution.makeNameNode(_outputPackageName),
                importList, TNLManip.addFirst(classDeclNode));

        String outFileName = _packageDirectoryName
	    + _systemName
	    + ".java";

        // Remove extra imports in the source code before writing to a file,
        // namely those for Complex and FixPoint added previously.
        // The CompileUnitNode must first undergo pass 2.
        unitNode.setProperty(IDENT_KEY, _packageDirectoryName
			     + _systemName);
        unitNode = StaticResolution.loadCompileUnit(unitNode, 2);
        unitNode.accept(new FindExtraImportsVisitor(true, null), null);

        JavaCodeGenerator.writeCompileUnitNodeList(TNLManip.addFirst(unitNode),
                TNLManip.addFirst(outFileName));
    }

    /** Generate the main() method of the main class. */
    protected MethodDeclNode _generateMainMethod()
            throws IllegalActionException {
        // A map of actors to the ObjectNodes that represent the actors.
        HashMap actorToVariableMap = new HashMap();

        LinkedList statementList = new LinkedList();

        Iterator actorItr = _actorSet.iterator();

        // Generate actor allocation statements and
        // fill in actorToVariableMap..
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

            statementList.addLast(new LocalVarDeclNode(FINAL_MOD, actorTypeNode,
                    actorVarNameNode, allocateActorNode));

            actorToVariableMap.put(actor, new ObjectNode(
                    (NameNode) actorVarNameNode.clone()));
        }


	if (_generateStatistics) {
	    // Generate    "long startTime = new Date().getTime();"
	    // FIXME: we should be able to pass a string to something
	    // that will then generate the parse tree for us?
	    NameNode startTimeVarNameNode =
		new NameNode(AbsentTreeNode.instance,
                        "startTime");

	    TypeNode startTimeTypeNode = LongTypeNode.instance;

	    TypeNameNode dateTypeNode =
		new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                        "Date"));

	    AllocateNode allocateStartTimeNode =
		new AllocateNode(dateTypeNode,
                        new LinkedList(),
                        AbsentTreeNode.instance);

	    MethodCallNode getTimeMethodCallNode =
		new MethodCallNode(new ObjectFieldAccessNode(
                        new NameNode(AbsentTreeNode.instance,
                                "getTime"),
                        allocateStartTimeNode),
                        new LinkedList());

	    statementList.addLast(new LocalVarDeclNode(NO_MOD,
                    startTimeTypeNode,
                    startTimeVarNameNode,
                    getTimeMethodCallNode));
	}


        // Generate preinitialize statements.
        actorItr = _actorSet.iterator();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

            ObjectNode actorObjectNode = (ObjectNode)
                ((ObjectNode) actorToVariableMap.get(actor)).clone();


            MethodCallNode methodCallNode = new MethodCallNode(
                    new ObjectFieldAccessNode(
                            new NameNode(AbsentTreeNode.instance,
                                    "preinitialize"),
                            actorObjectNode),
                    new LinkedList());

            statementList.addLast(new ExprStmtNode(methodCallNode));
        }


        // Generate initialize statements.
        actorItr = _actorSet.iterator();

        while (actorItr.hasNext()) {
            TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();

            ObjectNode actorObjectNode = (ObjectNode)
                ((ObjectNode) actorToVariableMap.get(actor)).clone();

            MethodCallNode methodCallNode = new MethodCallNode(
                    new ObjectFieldAccessNode(
                            new NameNode(AbsentTreeNode.instance,
                                    "initialize"),
                            actorObjectNode),
                    new LinkedList());

            statementList.addLast(new ExprStmtNode(methodCallNode));
        }

        // Generate the iteration loop.
        LinkedList iterationStatementList = new LinkedList();

        Iterator schedule = null;

        TypedAtomicActor lastActor = null;
        int contiguousAppearances = 0;

        schedule = _scheduler.getSchedule().actorIterator();

        // Iterate through the schedule. We generate the prefire(), fire(), and
        // postfire() sequence for the previous after the next different actor
        // is encountered. In order to generate the sequence for the last actor
        // in the schedule, we need to go past the last actor when iterating.

        // make sure there is something in the schedule
        boolean endLoop = !schedule.hasNext();
        while (!endLoop) {

            TypedAtomicActor actor = null;

            if (schedule.hasNext()) {
                actor = (TypedAtomicActor) schedule.next();
            } else {
                actor = null;
                endLoop = true;
            }

            if (actor != lastActor) {
                if (lastActor != null) {
                    ObjectNode actorVarNode = (ObjectNode)
                        ((ObjectNode) actorToVariableMap.get(lastActor)).clone();

                    ExprStmtNode prefireCallStatementNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance,
                                            "prefire"),
                                    actorVarNode),
                                    new LinkedList()));

                    // Every node in the tree needs to be unique, so the
                    // actor node needs to be cloned here.
                    ExprStmtNode fireCallStatementNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance,
                                            "fire"),
                                    ((ObjectNode)actorVarNode.clone())),
                                    new LinkedList()));

                    ExprStmtNode postfireCallStatementNode = new ExprStmtNode(
                            new MethodCallNode(new ObjectFieldAccessNode(
                                    new NameNode(AbsentTreeNode.instance,
                                            "postfire"),
                                    ((ObjectNode)actorVarNode.clone())),
                                    new LinkedList()));

                    LinkedList iterationCalls = new LinkedList();
                    iterationCalls.add(prefireCallStatementNode);
                    iterationCalls.add(fireCallStatementNode);
                    iterationCalls.add(postfireCallStatementNode);

                    BlockNode iterationBlockNode =
                        new BlockNode(iterationCalls);

                    if (contiguousAppearances > 1) {

                        NameNode loopCounterNameNode =
                            new NameNode(AbsentTreeNode.instance, "fc");

                        ObjectNode loopCounterObjectNode =
                            new ObjectNode(loopCounterNameNode);

                        List forInitList = TNLManip.addFirst(
                                new LocalVarDeclNode(NO_MOD, IntTypeNode.instance,
                                        (NameNode) loopCounterNameNode.clone(),
                                        new IntLitNode("0")));

                        LTNode forTestExprNode = new LTNode(
                                loopCounterObjectNode,
                                new IntLitNode(String.valueOf(contiguousAppearances)));

                        List forUpdateList = TNLManip.addFirst(new PostIncrNode(
                                (ObjectNode) loopCounterObjectNode.clone()));

                        iterationStatementList.addLast(new ForNode(forInitList,
                                forTestExprNode,
                                forUpdateList,
                                iterationBlockNode));
                    }  else {
                        iterationStatementList.addLast(iterationBlockNode);
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
            // Just add all the iteration statements to the
            // list of all statements.
            statementList.addAll(iterationStatementList);
        } else {
            if (iterations == 0) {
                // iterate forever
                statementList.addLast(new LoopNode(new BlockNode(iterationStatementList),
                        new BoolLitNode("true"), AbsentTreeNode.instance));
            } else {

                NameNode loopCounterNameNode =
                    new NameNode(AbsentTreeNode.instance, "it");

                ObjectNode loopCounterObjectNode =
                    new ObjectNode(loopCounterNameNode);

                List forInitList = TNLManip.addFirst(
                        new LocalVarDeclNode(NO_MOD, IntTypeNode.instance,
                                (NameNode) loopCounterNameNode.clone(),
                                new IntLitNode("0")));

                LTNode forTestExprNode = new LTNode(
                        loopCounterObjectNode,
                        new IntLitNode(String.valueOf(iterations)));

                List forUpdateList = TNLManip.addFirst(new PostIncrNode(
                        (ObjectNode) loopCounterObjectNode.clone()));

                statementList.addLast(new ForNode(forInitList, forTestExprNode,
                        forUpdateList, new BlockNode(iterationStatementList)));
            }
        }

        // Generate wrapup statements, unless we are iterating forever.
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

                statementList.addLast(new ExprStmtNode(methodCallNode));
            }
        }

	if (_generateStatistics) {
	    // Generate "long endTime = new Date().getTime();"
	    // FIXME: we should be able to pass a string to something
	    // that will then generate the parse tree for us?

	    NameNode endTimeVarNameNode =
		new NameNode(AbsentTreeNode.instance, "endTime");

	    TypeNode endTimeTypeNode = LongTypeNode.instance;

	    TypeNameNode dateTypeNode =
		new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                        "Date"));

	    AllocateNode allocateEndTimeNode =
		new AllocateNode(dateTypeNode,
                        new LinkedList(),
                        AbsentTreeNode.instance);

	    MethodCallNode getTimeMethodCallNode =
		new MethodCallNode(new ObjectFieldAccessNode(
                        new NameNode(AbsentTreeNode.instance, "getTime"),
                        allocateEndTimeNode),
                        new LinkedList());

	    statementList.addLast(
                    new LocalVarDeclNode(NO_MOD, endTimeTypeNode,
                            endTimeVarNameNode,
                            getTimeMethodCallNode));

	    // Generate "PrintStream _stdOut = System.out;"
	    NameNode stdOutVarNameNode =
		new NameNode(AbsentTreeNode.instance, "stdOut");

	    TypeNameNode stdOutTypeNameNode =
		new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                        "PrintStream"));

	    TypeNameNode systemTypeNameNode =
		new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                        "System"));

	    TypeFieldAccessNode outTypeFieldAccessNode =
		new TypeFieldAccessNode(new NameNode(AbsentTreeNode.instance,
                        "out"),
                        systemTypeNameNode);

	    statementList.addLast(new LocalVarDeclNode(NO_MOD,
                    stdOutTypeNameNode,
                    stdOutVarNameNode,
                    outTypeFieldAccessNode));

	    // Generate
	    // _stdOut.println("elapsed time: " + (endTime - startTime)+ "ms");
	    ObjectNode stdOutObjectNode =
		new ObjectNode(new NameNode(AbsentTreeNode.instance,
                        "stdOut"));

	    LinkedList timingList = new LinkedList();
	    timingList.add(new PlusNode(
                    new PlusNode(
                            new StringLitNode("elapsed time:"),
                            new MinusNode(
                                    new ObjectNode(new NameNode(AbsentTreeNode.instance,
					    "endTime")),
                                    new ObjectNode(new NameNode(AbsentTreeNode.instance,
					    "startTime")))
                            ),
                    new StringLitNode("ms")));

	    MethodCallNode printlnMethodCallNode =
		new MethodCallNode(new ObjectFieldAccessNode(
                        new NameNode(AbsentTreeNode.instance,
                                "println"),
                        stdOutObjectNode),
                        timingList);
            statementList.addLast(new ExprStmtNode(printlnMethodCallNode));
	}


        return new MethodDeclNode(PUBLIC_MOD | STATIC_MOD,
                new NameNode(AbsentTreeNode.instance, "main"),
                TNLManip.addFirst(new ParameterNode(NO_MOD,
                        TypeUtility.makeArrayType(
                                (TypeNode) StaticResolution.STRING_TYPE.clone(), 1),
                        new NameNode(AbsentTreeNode.instance, "args"))),
                new LinkedList(), new BlockNode(statementList),
                VoidTypeNode.instance);
    }


    /** Generate a makefile.
     */
    protected void _generateMakefile() throws IOException {
        String makefileName;
        // If _makefileOnly is true, create a makefile in the
        // current directory that uses the name of the class that
        // defines the system
        if (_makefileOnly) {
            makefileName =
                _sourceSystemClassName.substring(_sourceSystemClassName
                        .lastIndexOf('.') + 1)
                + ".mk";
        } else {
            makefileName = _packageDirectoryName + "makefile";
        }

        System.out.println("SDFCodeGenerator: creating " + makefileName);
        FileOutputStream makefileStream =
            new FileOutputStream(makefileName);

        String iterations = new String("0");
        Attribute attribute =
            _compositeActor.getDirector().getAttribute("iterations");
        if (attribute instanceof Variable) {
            iterations = ((Variable)attribute).getExpression();
        }
        String makefileString = new String(
                "# This makefile is automatically generated by "
                + "SDFCodeGenerator\n"
                + "ME =	" + _outputPackageName + "\n"
                + "ROOT =                $(PTII)\n"
                + "CG_ROOT =             \"$(ROOT)\"\n"
                + "SOURCE_SYSTEM_CLASS = " + _sourceSystemClassName + "\n"
                + "ITERATIONS =          " + iterations + "\n"
                + "OUTPKG =              " + _outputPackageName + "\n" 
		/* _packageDirectoryName has a trailing File.separatorChar
		   which causes problems under Windows because the character
		   is a backslash, which will backquote the second " in
		   when we cd to the "$(OUTPKG_DIR)" directory.
		   (We have to use double quotes here to handle spaces in
		   file names.)
		 */  
                + "OUTPKG_DIR =          \""
		+ _packageDirectoryName.substring(0,
						  _packageDirectoryName
						  .length()-1)
		+ "\"\n"
                /* FIXME: need to figure out OUTPKG_ROOT */
                + "OUTPKG_ROOT =         " + "$(ROOT)" + "\n"
                + "OUTPKG_MAIN_CLASS =   " + _systemName + "\n"
                + "CODEGEN_MK =          $(ROOT)/mk/codegen.mk\n"
                + "include $(CODEGEN_MK)\n"
                );
        makefileStream.write(makefileString.getBytes());
        makefileStream.close();
    }

    /** Figure out which buffers are connected to each input port of a given
     *  TypedAtomicActor, and add the information to the instance of
     *  SDFActorCodeGeneratorInfo argument.
     */
    protected void _makeInputInfo(TypedAtomicActor actor,
            SDFActorCodeGeneratorInfo actorInfo)
            throws IllegalActionException {

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
                    // we only support one receiver per channel
                    Receiver receiver = receivers[channel][0];

                    // Find the output port for this channel and the channel
                    // number connecting it to this port.
                    // This is done by matching the receiver for this
                    // channel with a remoteReceiver of a connected output port

                    int outputChannel = -1;

                    // Search all output ports connected to this port
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

                            // Search all channels of the output port.
                            int ch = 0;
                            do {

                                Receiver[] remoteReceivers =
                                    remoteReceiversArray[ch];

                                int i = 0;

                                // Search all receivers in the same
                                // receiver group.
                                while ((i < remoteReceivers.length) &&
                                        (outputPort == null)) {

                                    Receiver remoteReceiver =
                                        remoteReceivers[i];

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


                //System.out.println("connected buffers for port " +
                //  port.getName() + " of actor " + actor.getName());
                for (int ch = 0; ch < inputWidth; ch++) {
                    //System.out.println("ch " + ch + ": " + bufferNames[ch]);
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
            SDFActorCodeGeneratorInfo actorInfo)
            throws IllegalActionException {

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
                    // For non-SDFIOPorts, the production rate is assumed
                    // to be 1, and the init production rate is assumed to be 0
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
        String returnValue = "_cg_" + name + "_" + _labelNumber;
        _labelNumber++;
        return returnValue;
    }

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-makefileOnly")) {
            _makefileOnly = true;
        } else if (arg.equals("-outdir")) {
            _expectingOutputDirectory = true;
        } else if (arg.equals("-outpkg")) {
            _expectingOutputPackage = true;
        } else if (arg.equals("-pass1")) {
            _exitAfterPass1 = true;
        } else if (arg.equals("-pass2")) {
            _exitAfterPass2 = true;
        } else if (arg.equals("-shallowLoading")) {
            StaticResolution.enableShallowLoading();
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
        result += " -makefileOnly    Just generate a makefile\n";
        result += " -pass1           Exit after pass1\n";
        result += " -pass2           Exit after pass2\n";
        result += " -shallowLoading  Load ASTs shallowly\n";

        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** A map containing instances of SDFActorCodeGeneratorInfo, using the
     *  corresponding Actors as keys.
     */
    protected HashMap _actorInfoMap = new HashMap();

    /** The set of all actors in the system. */
    protected HashSet _actorSet = new HashSet();


    /** A map containing instances of BufferInfo, using the corresponding
     *  ports as keys. This map contains all BufferInfos for all output
     *  ports in the CompositeActor.
     */
    protected HashMap _bufferInfoMap = new HashMap();

    /** The TypedCompositeActor containing the system for which to generate
     *  code.
     */
    protected TypedCompositeActor _compositeActor = null;

    /** The director for the SDF system. */
    protected SDFDirector _director = null;

    /** A flag indicating whether we generate statistics
     * such as elapsed time
     */
    protected boolean _generateStatistics = true;

    /** A flag indicated whether we should only generate a makefile
     *  If _makefileOnly is true, then create a makefile in the current
     *  directory using the name of the source system class.
     *  If _makefileOnly is false, then create a makefile in the package
     *  output directory.
     */    
    protected boolean _makefileOnly = false;

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

    /** The scheduler for the SDF system. */
    protected SDFScheduler _scheduler = null;

    /** The name of the system.
     *  FIXME: Currently, this defaults to CG_Main, we should
     *  get the name using _compositeActor.getName(), but
     *  sdf/codegen/SDFActorTransformer needs to be able to get the
     *  name too.
     */
    protected String _systemName = new String("CG_Main");

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    // SDF Code Generator Class Factory.
    protected CodeGeneratorClassFactory _codeGeneratorClassFactory =
            SDFCodeGeneratorClassFactory.getInstance();

    // If true, then exit after pass 1 is completed.  
    private boolean _exitAfterPass1 = false;

    // If true, then exit after pass 2 is completed.  
    private boolean _exitAfterPass2 = false;

    // A flag indicating that we are expecting an output directory as the
    //  next argument in the command-line.
    private boolean _expectingOutputDirectory = false;

    // A flag indicating that we are expecting an output package name as the
    //  next argument in the command-line.
    private boolean _expectingOutputPackage = false;

    // A non-decreasing number used for globally unique labeling.
    private int _labelNumber = 0;

    // Fully dot qualified name of the class we are generating code
    // for.  This class usually extends TypedCompositeActor
    // and is named using the -class command line argument
    private String _sourceSystemClassName;
}
