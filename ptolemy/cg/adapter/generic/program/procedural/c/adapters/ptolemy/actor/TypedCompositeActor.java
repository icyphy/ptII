/*
 @Copyright (c) 2005-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * A C adapter class for ptolemy.actor.lib.TypedCompositeActor.
 *
 * @author William Lucas
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */
public class TypedCompositeActor
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor {

    /**
     * Constructor method for the CompositeActor adapter.
     * @param actor the associated actor
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor actor) {
        super(actor);
    }

    /** Generate The fire function code. This method is called when
     *  the firing code of each actor is not inlined. Each actor's
     *  firing code is in a function with the same name as that of the
     *  actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //CompositeActor compositeActor = (CompositeActor) getComponent();
        //if (!(compositeActor instanceof CompiledCompositeActor)) {
        // Generate the code for the TypedComposite before generating code for the director.
        // Needed by:
        // $PTII/bin/ptcg -language java  -inline false  -variablesAsArrays false $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/ActorOrientedClass.xml
        // See generateFireFunctionCode() in ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/domains/sdf/kernel/SDFDirector.java
        String fireCode = _generateFireCode();
        // Append _fireFunction_ to the class names so as to
        // differentiate from the inner classes that are generated for
        // the first few Composites when inline is false.
        String[] splitFireCode = getCodeGenerator()._splitBody(
                "_fireFunction_"
                        + CodeGeneratorAdapter.generateName(getComponent())
                        + "_", fireCode);
        code.append(splitFireCode[0]);
        ProgramCodeGenerator codeGenerator = getCodeGenerator();
        code.append(_eol + "void "
                + codeGenerator.generateFireFunctionMethodName(getComponent())
                + "() {" + _eol);
        // code.append(_generateFireCode());
        code.append(splitFireCode[1]);
        code.append("}" + _eol);
        //}
        //        else {
        //            ptolemy.actor.Director director = compositeActor.getDirector();
        //            ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getCodeGenerator().getAdapter(
        //                    director);
        //            code.append(directorAdapter.generateFireFunctionCode());
        //        }

        return processCode(code.toString());
    }

    /**
     * Generate the initialize code for this composite actor.
     *
     * @return The generated initialize code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList<String> args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);

        // args.add(sanitizedContainerName + ".director");
        args.add("(" + sanitizedContainerName + "->getDirector("
                + sanitizedContainerName + "))");
        args.add(sanitizedContainerName);
        codeStream.appendCodeBlock("initializeBlock", args);

        return processCode(codeStream.toString());
    }

    /**
     * Generate the postfire code for this composite actor.
     *
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList<String> args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);

        args.add(sanitizedContainerName + ".director");
        codeStream.appendCodeBlock("postFireBlock", args);

        return processCode(codeStream.toString());
    }

    /**
     * Generate the prefire code for this composite actor.
     *
     * @return The generated prefire code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePrefireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList<String> args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);

        args.add(sanitizedContainerName + ".director");
        codeStream.appendCodeBlock("prefireBlock", args);

        return processCode(codeStream.toString());
    }

    /**
     * Generate the preinitialize code. We do not call the super
     * method, because we have arguments to add here
     * This code contains the variable declarations
     *
     * @return The generated preinitialize code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();

        codeStream.appendCodeBlock("variableDeclareBlock");

        // Here we declare the contained actors
        List actorList = TopActor.deepEntityList();
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            if (actor instanceof CompositeActor || actor instanceof AtomicActor
                    || actor instanceof FSMActor) {
                if (actor instanceof ModalController) {
                    continue;
                }
                String actorName = CodeGeneratorAdapter.generateName(actor);
                codeStream.append("$include(\"" + actorName + ".h\")");
            }
        }

        // After the actors we declare the receivers of this container
        Director director = TopActor.getDirector();
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getAdapter(director);
        String directorName = CodeGeneratorAdapter
                .generateName(directorAdapter);
        codeStream.append("$include(\"" + directorName + ".h\")");

        // Appends the enum definition for the ports
        if (_enumPortNumbersDefinition != null) {
            codeStream.append(_enumPortNumbersDefinition);
        }

        return processCode(codeStream.toString());
    }

    /**
     * Generate the preinitialize code. We do not call the super
     * method, because we have arguments to add here
     * This code contains the different constructions and initializations.
     *
     * @return The generated preinitialize Method code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);

        if (TopActor.getContainer() == null
                || TopActor instanceof ptolemy.cg.lib.CompiledCompositeActor) {
            // Appending the construction of the actors
            codeStream.append(_eol + sanitizedContainerName
                    + "_constructorActors();");
            // Appending the construction of the ports
            codeStream.append(_eol + sanitizedContainerName
                    + "_constructorPorts();");
            // Appending the construction of the receivers
            codeStream.append(_eol + sanitizedContainerName
                    + "_constructorReceivers();" + _eol);
        }
        // We initialize the corresponding receivers and call the director method
        args.clear();
        args.add(sanitizedContainerName + ".director");
        codeStream.appendCodeBlock("preinitializeBlock", args);

        return processCode(codeStream.toString());
    }

    /** Set up adapters contained by the composite actor.
     *  This method is run very early in the code generation sequence,
     *  so adapters that need to set up code generation-time variables
     *  may have setupAdapter() methods that need to be invoked.
     *  Variables and shared code that are to be generated should be
     *  in generateSharedCode() or other methods, not this method.
     *  @exception IllegalActionException If the adapter associated with
     *  an actor throws it while being set up.
     */
    @Override
    public void setupAdapter() throws IllegalActionException {
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.actor.CompositeActor) getComponent())
                        .getDirector());
        directorAdapter.setupAdapter();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating variable declarations for
     *   the actor.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {

        return "";
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList<String> args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);

        args.add(sanitizedContainerName + ".director");
        codeStream.appendCodeBlock("WrapupBlock", args);

        return processCode(codeStream.toString());
    }

    /** Return a set of parameters that will be modified during the
     *  execution of the model. These parameters are those returned by
     *  getModifiedVariables() method of directors or actors that
     *  implement ExplicitChangeContext interface.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the adapter associated with an actor
     *   or director throws it while getting modified variables.
     */
    @Override
    public Set<Parameter> getModifiedVariables() throws IllegalActionException {
        Set<Parameter> set = new HashSet<Parameter>();

        return set;
    }

    /**
     * Generate the fire code of a Composite Actor.
     * @return The generated code.
     * @exception IllegalActionException If thrown while appending the
     * fireBlock or processing the code.
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList<String> args = new LinkedList();

        ptolemy.actor.CompositeActor TopActor = (ptolemy.actor.CompositeActor) getComponent();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(TopActor);
        args.add(sanitizedContainerName + ".director");
        args.add(sanitizedContainerName);
        codeStream.appendCodeBlock("fireBlock", args);

        return processCode(codeStream.toString());
    }

    private String _enumPortNumbersDefinition;
}
