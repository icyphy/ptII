/* A code generator adapter that is auto generated and calls actor code.

   Copyright (c) 2010-2014 The Regents of the University of California.
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

 */
package ptolemy.cg.kernel.generic.program.procedural.java;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.StringConst;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AutoAdapter

/**
 *  A code generator adapter that is auto generated and calls actor code.
 *
 *  <p>This class provides a way to generate code for actors that do
 *  not have custom code generation templates.  The generated code
 *  requires the Ptolemy kernel, actor, data and other packages.</p>
 *
 *  <p>This class wraps a Ptolemy actor in a TypedCompositeActor
 *  container, makes connections from the code generated actors to the
 *  container and invokes the actor execution methods (preinitialize(),
 *  initialize(), prefire(), fire(), postfire() and wrapup()) of the
 *  inner Ptolemy actor.</p>
 *
 *  <p>The primary entry point for this class is
 *  {@link #getAutoAdapter(GenericCodeGenerator, Object)}</p>
 *
 *  @author Christopher Brooks, Contributor: Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (cxh)
 *  @Pt.AcceptedRating red (cxh)
 */
public class AutoAdapter extends NamedProgramCodeGeneratorAdapter {

    // See
    // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=342

    // FIXME: Rename this to AutoTypedAtomicActorAdapter?

    /** Construct the code generator adapter associated with the given
     *  component.
     *
     *  <p>The primary entry point for this class is
     *  {@link #getAutoAdapter(GenericCodeGenerator, Object)}, but this
     *  is left public for testing.</p>
     *
     *  @param codeGenerator The code generator with which to associate the adapter.
     *  @param component The associated component.
     */
    public AutoAdapter(ProgramCodeGenerator codeGenerator,
            TypedAtomicActor component) {
        super(component);
        TemplateParser templateParser = new JavaTemplateParser();
        setTemplateParser(templateParser);
        templateParser.setCodeGenerator(codeGenerator);
        setCodeGenerator(codeGenerator);
    }

    /**
     * Generate the initialize code.
     * <p>Generate code that creates the container, actor and ports.
     * <p>Generate code that connects the ports of the inner actor to
     * the ports of the outer actor.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //String actorClassName = getComponent().getClass().getName();

        //code = generateParameterCode();
        String[] splitInitializeParameterCode = getCodeGenerator()._splitBody(
                "_AutoAdapterP_", code.toString());

        // Stitch every thing together.  We do this last because of
        // the _splitBody() calls.

        String resolveToplevelTypes = "";
        if (_toplevelTypesResolved != getComponent().toplevel()) {
            // _toplevelTypesResolved is static, so we set it the the value
            // of the current toplevel in case we generate code for multiple
            // models.  The Tcl test suite replicates this by generating
            // code for the DotProduct model multiple times.  A short cut is the following:
            //
            // set args [java::new {String[]} 3 [list "-language" "java" "auto/DotProduct.xml"]]
            // java::call ptolemy.cg.kernel.generic.GenericCodeGenerator generateCode $args
            // java::call ptolemy.cg.kernel.generic.GenericCodeGenerator generateCode $args
            _toplevelTypesResolved = getComponent().toplevel();
            resolveToplevelTypes = "try {"
                    + _eol
                    //+ "    TypedCompositeActor.resolveTypes($containerSymbol());" + _eol
                    + "    TypedCompositeActor.resolveTypes(_toplevel);"
                    + _eol
                    + "} catch (Exception ex) {"
                    + _eol
                    + "    throw new RuntimeException(\"Failed to resolve types of the top level.\", ex);"
                    + _eol + "}" + _eol;
        }
        String splitInitializeParameterBlock = "";
        if (!splitInitializeParameterCode[0].isEmpty()
                || !splitInitializeParameterCode[1].isEmpty()) {
            splitInitializeParameterBlock = "{" + _eol
                    + splitInitializeParameterCode[0]
                            + splitInitializeParameterCode[1] + "}" + _eol;
        }
        String result = resolveToplevelTypes
                + splitInitializeParameterBlock
                + "try {"
                + _eol
                // Initialize after the parameters are set.
                + "    $actorSymbol(actor).initialize();"
                + _eol
                + "} catch (Exception ex) {"
                + _eol
                + "    throw new RuntimeException(\"Failed to initialize $actorSymbol(actor))\", ex);"
                + _eol + "}" + _eol;

        return processCode(result);
    }

    /** Generate code for the Parameters of the actor.
     *  @return The generated code
     *  @exception IllegalActionException If thrown while reading the parameters
     *  of the actor.
     */
    public String generateParameterCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        String actorClassName = getComponent().getClass().getName();
        // Handle parameters.
        Iterator parameters = getComponent().attributeList(Settable.class)
                .iterator();
        while (parameters.hasNext()) {
            Settable parameter = (Settable) parameters.next();
            if (!ptolemy.actor.gui.Configurer.isVisible(getComponent(),
                    parameter)) {
                continue;
            }

            String parameterName = StringUtilities.sanitizeName(
                    parameter.getName()).replaceAll("\\$", "Dollar");
            if (parameterName.equals("firingsPerIteration")) {
                continue;
            }

            String parameterValue = _sanitizeParameterValue(parameter);

            // FIXME: do we want one try block per parameter?  It does
            // make for better error messages.

            boolean privateParameter = false;
            try {
                getComponent().getClass().getField(parameterName);
            } catch (NoSuchFieldException ex) {
                privateParameter = true;
                _needAutoAdapterSetPrivateParameter = true;
                // Call the method that sets the parameter.  We use a method
                // here so as to save space.
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/ActorWithPrivateParameterTest.xml
                code.append("_autoAdapterSetPrivateParameter($actorSymbol(actor), "
                        + "\""
                        + parameter.getName()
                        + "\", "
                        + "\""
                        + parameterName
                        + "\", "
                        + "\""
                        + parameterValue
                        + "\");" + _eol);
            } catch (SecurityException ex2) {
                throw new IllegalActionException(getComponent(), ex2,
                        "Can't access " + parameterName + " field.");
            }

            if (!privateParameter) {
                code.append(_generateSetParameter(parameter, parameterName,
                        actorClassName, parameterValue));
                //                 String setParameter = "";
                //                 if (parameter instanceof Parameter) {
                //                     setParameter = "    Parameter " + parameterName + " = ((" + actorClassName + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                //                         + "    " + parameterName + ".setExpression(\""
                //                         + parameterValue
                //                         + "\");" + _eol;
                //                 } else {
                //                     if (parameter instanceof ptolemy.kernel.util.StringAttribute) {
                //                         setParameter = "    ptolemy.kernel.util.StringAttribute " + parameterName + " = ((" + actorClassName + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                //                             + "    " + parameterName + ".setExpression(\""
                //                             + parameterValue
                //                             + "\");" + _eol;
                //                     }
                //                 }
                //                 code.append(//"try {" + _eol
                //                         "{ " + _eol
                //                         + setParameter
                //                         + "    ((" + actorClassName + ")$actorSymbol(actor)).attributeChanged(" + parameterName + ");" + _eol
                //                         + "}" + _eol);
            }
            // Exclude the catch code because it bulks up the code too much for large models.
            //             code.append("} catch (Exception ex) {" + _eol
            //                     + "    throw new RuntimeException(\"Failed to set parameter \\\"" + parameterName
            //                     + "\\\" in $actorSymbol(actor) to \\\"" + StringUtilities.escapeString(parameterValue) + "\\\"\", ex);" + _eol
            //                 + "}" + _eol);
        }
        //code.append(getCodeGenerator().comment("AutoAdapter._generateInitalizeCode() start"));
        return code.toString();
    }

    /**
     * Generate the postfire code.
     * @return Code that calls postfire() on the inner actor.
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        return _generateExecutionCode("postfire");
    }

    /**
     * Generate the prefire code.
     * @return Code that calls prefire() on the inner actor.
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generatePrefireCode() throws IllegalActionException {
        return _generateExecutionCode("prefire");
    }

    /**
     * Generate the preinitialize code that declares the ports.
     * <p>Generate code that declares the container, actor and ports.
     * @return A string of the preinitialize code for the adapter.
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode()
                + _eol + "TypedAtomicActor $actorSymbol(actor);" + _eol);
        if (!((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            // Declare each container only once.
            NamedObj container = getComponent().getContainer();
            while (container != null) {
                if (!_containersDeclared.contains(container)) {
                    _containersDeclared.add(container);
                    String containerSymbol = getCodeGenerator()
                            .generateVariableName(container);
                    code.append("TypedCompositeActor " + containerSymbol + ";"
                            + _eol);
                }
                container = container.getContainer();
            }
        }
        code.append(_generatePortDeclarations((Entity) getComponent()));

        return processCode(code.toString());
    }

    /** Generate the preinitialization method body.
     *
     *  <p>Typically, the preinitialize code consists of variable
     *   declarations.  However, AutoAdapter generates method calls
     *   that instantiate wrapper TypedCompositeActors, so we need
     *   to invoke those method calls.</p>
     *
     *  @return a string for the preinitialization method body.  In
     *  this base class, return the empty string.
     *  @exception IllegalActionException If there is a problem
     *  accessing the component, its ports or the remote components.
     */
    @Override
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        NamedObj component = getComponent();
        String actorClassName = component.getClass().getName();
        String containmentCode = _generateContainmentCode(component);

        String code = generatePreinitializeMethodBodyCode(component);
        String[] splitInitializeConnectionCode = getCodeGenerator()._splitBody(
                "_AutoAdapterI_", code);

        String toplevelSymbol = _generatePtTypedCompositeActorName(
                component.toplevel(), component.toplevel().getName());
        // Stitch every thing together.  We do this last because of
        // the _splitBody() calls.
        String result = getCodeGenerator().comment(
                "AutoAdapter._generatePreinitalizeCode("
                        + component.getFullName() + ") start")
                        + "try {"
                        + _eol
                        //+ "    $containerSymbol() = new TypedCompositeActor();" +_eol
                        + "    instantiateToplevel(\""
                        + component.toplevel().getName()
                        + "\");"
                        + _eol
                        // FIXME: set this just once
                        //+ getCodeGenerator().generateVariableName(component.toplevel()) + " = _toplevel;" + _eol
                        + toplevelSymbol
                        + " = _toplevel;"
                        + _eol
                        + containmentCode
                        // If there are two custom actors in one container, then
                        // we may have already created the actor.
                        + "    if ($actorSymbol(actor) == null) {"
                        + _eol
                        + "            $actorSymbol(actor) = new "
                        + actorClassName
                        //+ "($containerSymbol(), \"$actorSymbol(actor)\");"
                        + "($containerSymbol(), \""
                        + component.getName()
                        + "\");"
                        + _eol
                        // Set the displayName so that actors that call
                        // getDisplayName() get the same value.  Actors that
                        // generate random numbers often call getFullName(),
                        // then should call getDisplayName() instead.
                        + "        $actorSymbol(actor).setDisplayName(\""
                        + component.getName()
                        + "\");"
                        + _eol
                        + "    }"
                        + _eol
                        + splitInitializeConnectionCode[0]
                                + splitInitializeConnectionCode[1]
                                        + "    if ($containerSymbol().getAttribute(\"director\") == null) {"
                                        + _eol
                                        + "        new ptolemy.actor.Director($containerSymbol(), \"director\");"
                                        + _eol
                                        + "    }"
                                        + _eol
                                        + "} catch (Exception ex) {"
                                        + _eol
                                        + "    throw new RuntimeException(\"Failed to create $actorSymbol(actor))\", ex);"
                                        + _eol + "}" + _eol;
        return processCode(result);

    }

    /** Generate the preinitialization method body.
     *
     *  <p>Typically, the preinitialize code consists of variable
     *   declarations.  However, AutoAdapter generates method calls
     *   that instantiate wrapper TypedCompositeActors, so we need
     *   to invoke those method calls.</p>
     *
     *  @param component The component for which the preinitialization
     *  method is to be created.
     *  @return a string for the preinitialization method body.  In
     *  this base class, return the empty string.
     *  @exception IllegalActionException If there is a problem
     *  accessing the component, its ports or the remote components.
     */
    public String generatePreinitializeMethodBodyCode(NamedObj component)
            throws IllegalActionException {
        // Use the full class name so that we don't have to import the
        // actor.  If we import the actor, then we cannot have model
        // names with the same name as the actor.
        //String actorClassName = component.getClass().getName();

        //String containmentCode = _generateContainmentCode(component);

        StringBuffer code = new StringBuffer();
        // Generate code that creates and connects each port.
        // There is very similar code in _generatePortDeclarations()
        Iterator entityPorts = ((Entity) component).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                if (!castPort.isOutsideConnected()) {
                    continue;
                }

                // True if the port has a relation whose name starts with "autoConnector".
                boolean hasAutoConnectorRelation = _hasAutoConnectorRelation(castPort);

                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    // Only instantiate ports that are outside connected and avoid
                    // "Cannot put a token in a full mailbox."  See
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/ActorWithPrivateParameterTest.xml
                    code.append(_generatePortInstantiation(name,
                            castPort.getName(), castPort,
                            0 /* channelNumber */, castPort.sourcePortList()));
                } else {
                    // Multiports.  Not all multiports have port names
                    // that match the field name. See
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml

                    TypedIOPort actorPort = null;
                    try {
                        Field foundPortField = _findFieldByPortName(component,
                                castPort.getName());
                        actorPort = (TypedIOPort) foundPortField.get(component);
                        if (!hasAutoConnectorRelation) {
                            code.append("    (("
                                    + component.getClass().getName()
                                    + ")$actorSymbol(actor))."
                                    + foundPortField.getName()
                                    + ".setTypeEquals("
                                    + _typeToBaseType(actorPort.getType())
                                    + ");" + _eol);
                        }

                    } catch (Throwable throwable) {
                        //new IllegalActionException(castPort, throwable,
                        //        "Could not find port " + castPort.getName() + " " + throwable).printStackTrace();
                        actorPort = (TypedIOPort) ((Entity) component)
                                .getPort(castPort.getName());
                        if (!hasAutoConnectorRelation) {
                            code.append("new TypedIOPort($containerSymbol(), \""
                                    //+ actorPort.getName().replace("\\", "\\\\") + "\", "
                                    + AutoAdapter._externalPortName(
                                            actorPort.getContainer(),
                                            actorPort.getName()).replace("$",
                                                    "\\u0024")
                                                    + "\", "
                                                    + actorPort.isInput()
                                                    + ", "
                                                    + actorPort.isOutput()
                                                    + ").setMultiport(true);" + _eol);
                        }
                    }

                    // If we have a multiport connected to a composite that has a multiport with two inputs, then
                    // we want to use sourcePortList() and not numberOfSources()
                    // $PTII/bin/ptcg -language java ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport.xml
                    List sourcePortList = actorPort.sourcePortList();
                    for (int i = 0; i < sourcePortList.size(); i++) {
                        // If a multiport input has one channel that isAutoAdaptered and one channel that is not autoAdaptered,
                        // then skip generating the sendInside if the channel is autoAdaptered.  The test is:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
                        if (actorPort.isOutsideConnected()
                                && !isAutoAdaptered(((IOPort) sourcePortList
                                        .get(i)).getContainer())) {
                            code.append(_generatePortInstantiation(name, name
                                    + "Source" + i, actorPort, i,
                                    sourcePortList));
                        }
                    }

                    List sinkPortList = actorPort.sinkPortList();
                    for (int i = 0; i < sinkPortList.size(); i++) {
                        // If a multiport input has one channel that isAutoAdaptered and one channel that is not autoAdaptered,
                        // then skip generating the sendInside if the channel is autoAdaptered.  The test is:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
                        if (actorPort.isOutsideConnected()
                                && !isAutoAdaptered(((IOPort) sinkPortList
                                        .get(i)).getContainer())) {
                            code.append(_generatePortInstantiation(name, name
                                    + "Sink" + i, actorPort, i, sinkPortList));
                        }
                    }
                }

                List<TypeAttribute> typeAttributes = insidePort
                        .attributeList(TypeAttribute.class);
                if (typeAttributes.size() > 0) {
                    _headerFiles.add("ptolemy.actor.TypeAttribute;");
                    if (typeAttributes.size() > 1) {
                        new Exception("Warning, " + insidePort.getFullName()
                                + " has more than one typeAttribute.")
                        .printStackTrace();
                    }
                    // only get the first element of the list.
                    TypeAttribute typeAttribute = typeAttributes.get(0);
                    // The port has a type attribute, which means the
                    // type was set via the UI.
                    // This is needed by:
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/TrellisDecoder.xml
                    code.append("{"
                            + _eol
                            + "TypeAttribute _type = "
                            + "new TypeAttribute("

                            //+ "$actorSymbol("
                            //+ TemplateParser.escapePortName(insidePort.getName()) + "), \"inputType\");" + _eol
                            //+ "((" + actorClassName + ")$actorSymbol(actor))."
                            //+ TemplateParser.escapePortName(insidePort.getName()) + ", \"inputType\");" + _eol

                            // Certain actors may create ports on the
                            // fly, so query the actor for its port.

                            // Set the port of the actor, not the container.  See
                            // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/TrellisDecoder.xml
                            // However, if we don't set the port of the container, then this fails:
                            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                            + "(TypedIOPort)$containerSymbol().getPort(\""
                            //+ insidePort.getName().replace("\\", "\\\\")
                            + AutoAdapter._externalPortName(
                                    insidePort.getContainer(),
                                    insidePort.getName()).replace("$",
                                            "\\u0024") + "\"), \"inputType\");" + _eol
                                            + "_type.setExpression(\""
                                            + typeAttribute.getExpression() + "\");" + _eol
                                            + "}" + _eol);
                }
            }
        }

        code.append(generateParameterCode());
        return code.toString();
    }

    /**
     * Generate the wrapup code.
     * @return Code that calls wrapup() on the inner actor.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        return _generateExecutionCode("wrapup");
    }

    /**
     * Create a new adapter to a preexisting actor that presumably does
     * not have a code generation template.
     * <p>This method is the entry point for this class.  Typically,
     * {@link ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator#_getAutoGeneratedAdapter(GenericCodeGenerator, Object)}
     * calls this method.</p>
     *
     * @param codeGenerator The code generator with which to associate the adapter.
     * @param object The given object.
     * @return the AutoAdapter or null if object is not assignable
     * from TypedAtomicActor.
     */
    public static AutoAdapter getAutoAdapter(
            GenericCodeGenerator codeGenerator, Object object) {

        // FIXME: I'm not sure if we need this method, but I like
        // calling something that returns null if the associated actor
        // cannot be found or is of the wrong type.
        try {
            Class typedAtomicActor = Class
                    .forName("ptolemy.actor.TypedAtomicActor");
            if (!typedAtomicActor.isAssignableFrom(object.getClass())) {
                return null;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }

        if (_checkingAutoAdapter) {
            // If the static isAutoAdaptered() method is called, then
            // we set _checkingAutoAdapter to true and call
            // GenericCodeGenerator.getAdapter(), which eventually
            // calls this method if the Object has no adapter.  This
            // is a hack, but it means we don't need to modify
            // GenericCodeGenerator.
            _wouldBeAutoAdapted = true;
            return null;
        }
        // FIXME: I don't like casting to ProgramCodeGenerator, but we need to set
        // the codeGenerator of the templateParser.
        return new AutoAdapter((ProgramCodeGenerator) codeGenerator,
                (TypedAtomicActor) object);
    }

    /** Get the files needed by the code generated for this actor.
     *  Add $(PTII) to the classpath of the generated code.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the Maximum actor.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.addAll(_headerFiles);
        files.add("ptolemy.actor.Director;");
        files.add("ptolemy.actor.Manager;");
        files.add("ptolemy.data.expr.Parameter;");
        files.add("ptolemy.data.type.ArrayType;");
        // Need IntToken etc.
        files.add("ptolemy.data.*;");
        files.add("ptolemy.data.type.BaseType;");
        files.add("ptolemy.actor.TypedAtomicActor;");
        files.add("ptolemy.actor.TypedCompositeActor;");
        files.add("ptolemy.actor.TypedIOPort;");

        // If the actor is imported, then we cannot have models with the same
        // name as the actor.
        //files.add(getComponent().getClass().getName() + ";");
        ((ProceduralCodeGenerator) getCodeGenerator())
        .addLibraryIfNecessary("$(PTII)");

        // Loop through the path elements in java.class.path and add
        // them as libraries.  We need this so that we can find the
        // JavaScope.zip code coverage file in the nightly build
        ((JavaCodeGenerator) getCodeGenerator())._addClassPathLibraries();

        return files;
    }

    /**
     * Generate shared code that includes the declaration of the toplevel
     * composite.  If necessary a method that sets a private parameter is
     * added.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Set<String> getSharedCode() throws IllegalActionException {
        StringBuffer variableCode = new StringBuffer();
        Iterator composites = ((TypedCompositeActor) getComponent().toplevel())
                .entityList(TypedCompositeActor.class).iterator();
        while (composites.hasNext()) {
            String containerSymbol = "_toplevel";
            TypedCompositeActor composite = (TypedCompositeActor) composites
                    .next();
            // FIXME: we could just search entities that end in "Parameters"
            String subparametersCode = "";
            TypedCompositeActor subparameters = (TypedCompositeActor) composite
                    .getEntity("EconomicsParameters");
            if (subparameters != null) {
                subparametersCode = "{"
                        + _eol
                        + "TypedCompositeActor subParameters = (TypedCompositeActor)"
                        + "subComposite"
                        + ".getEntity(\""
                        + subparameters.getName()
                        + "\");"
                        + _eol
                        + "if (subParameters == null) {"
                        + _eol
                        + "    subParameters = new "
                        + subparameters.getClass().getName()
                        + "("
                        + "subComposite"
                        + ", \""
                        + subparameters.getName()
                        + "\");"
                        + _eol
                        + _generateContainedVariables(subparameters,
                                "subParameters") + "}" + _eol + "}" + _eol;
            }
            variableCode
            .append("{"
                    + _eol
                    + "TypedCompositeActor subComposite = (TypedCompositeActor)"
                    + containerSymbol
                    + ".getEntity(\""
                    + composite.getName()
                    + "\");"
                    + _eol
                    + "if (subComposite == null) {"
                    + _eol
                    + "    subComposite = new "
                    + composite.getClass().getName()
                    + "("
                    + containerSymbol
                    + ", \""
                    + composite.getName()
                    + "\");"
                    + _eol
                    + _generateContainedVariables(composite,
                            "subComposite") + subparametersCode + "}"
                            + _eol + "}" + _eol);
        }

        Iterator parameters = ((TypedCompositeActor) getComponent().toplevel())
                .attributeList(Parameter.class).iterator();
        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();
            if (!_skipVariable(parameter.getName())) {
                String parameterClassName = parameter.getClass().getName();
                variableCode.append("{" + _eol + parameterClassName
                        + " parameter = new " + parameterClassName
                        + "(_toplevel, \"" + parameter.getName() + "\");"
                        + _eol + "parameter.setExpression(\""
                        + parameter.getExpression().replace("\"", "\\\"")
                        + "\");" + "}" + _eol);
            }
        }

        Set<String> sharedCode = super.getSharedCode();
        sharedCode
        .add("static TypedCompositeActor _toplevel = null;"
                + _eol
                + getCodeGenerator()
                .comment(
                        "If necessary, create a top level for actors"
                                + "that do not have adapters that are handled by AutoAdapter.")
                                + "static void instantiateToplevel(String name) throws Exception {"
                                + _eol
                                + "    if (_toplevel == null) { "
                                + _eol
                                + "        _toplevel = new TypedCompositeActor();"
                                + _eol
                                + "        _toplevel.setName(name);"
                                + _eol
                                + "        new Director(_toplevel, \"director\");"
                                + _eol
                                + "        _toplevel.setManager(new Manager(\"manager\"));"
                                + _eol + variableCode.toString() + _eol + "    }"
                                + _eol + "}" + _eol
                                //                + getCodeGenerator().comment("Instantiate the containment hierarchy and return the container.")
                                //                 + "static ptolemy.kernel.CompositeEntity getContainer(ptolemy.kernel.util.NamedObj namedObj) {" + _eol
                                //                 + "    NamedObj child = namedObj;" + _eol
                                //                 + "    NamedObj container = child.getContainer();" + _eol
                                //                 + "    while (container != null) {" + _eol
                                //                 + "        " + _eol
                                //                 + "        container = child.getContainer();" + _eol
                                //                 + "    }" + _eol
                                //                 + "}" + _eol
                );

        if (_needAutoAdapterSetPrivateParameter) {
            sharedCode
            .add("// Search the fields of the class for a parameter by name."
                    + _eol
                    + "static void _autoAdapterSetPrivateParameter(Object actor, "
                    + _eol
                    + "String parameterName, String parameterSanitizedName, String parameterValue) throws Exception {"
                    + _eol
                    + "// Accessing private field."
                    + _eol
                    + "// Use getDeclaredFields() so that we get private fields."
                    + _eol
                    + "java.lang.reflect.Field declaredFields[] = actor.getClass().getDeclaredFields();"
                    + _eol
                    + "// Use getFields() instead of getDeclaredFields() so that we get fields in parent classes."
                    + _eol
                    + "java.lang.reflect.Field fields[] = actor.getClass().getFields();"
                    + _eol
                    + "// Note that there is overlap between the two arrays."
                    + _eol
                    + "java.lang.reflect.Field allFields[] = java.util.Arrays.copyOf(declaredFields, declaredFields.length + fields.length);"
                    + _eol
                    + "System.arraycopy(fields, 0, allFields, declaredFields.length, fields.length);"
                    + _eol
                    + "for (int i = 0; i < allFields.length; i++){"
                    + _eol
                    + "    ptolemy.data.expr.Parameter parameter = null;"
                    + _eol
                    + "    allFields[i].setAccessible(true);"
                    + _eol
                    + "    if (allFields[i].getName().equals(parameterSanitizedName)) {"
                    + _eol
                    + "        parameter = (ptolemy.data.expr.Parameter)allFields[i].get(actor);"
                    + _eol
                    + "    // If the field is a StringParameter, then we want to to assign to it."
                    + _eol
                    + "    } else if (ptolemy.data.expr.Parameter.class.isAssignableFrom(allFields[i].getType())) {"
                    + _eol
                    + "        parameter = (ptolemy.data.expr.Parameter)allFields[i].get(actor);"
                    + _eol
                    + "// Check for private parameters that have setName() different than the name of the field."
                    + _eol
                    + "// $PTII/bin/ptcg -language java ~/ptII/ptolemy/cg/kernel/generic/program/procedural/java/test/ActorWithPrivateParameterTest.xml"
                    + _eol
                    + "// Uninitialized parameters may be null."
                    + _eol
                    + "        if (parameter != null && !parameter.getName().equals(parameterName)) {"
                    + _eol
                    + "            parameter = null;"
                    + _eol
                    + "        }"
                    + _eol
                    + "    }"
                    + _eol
                    + "    if (parameter != null) {"
                    + _eol
                    + "        parameter.setExpression(parameterValue );"
                    + _eol
                    + "       ((ptolemy.kernel.util.NamedObj)actor).attributeChanged(parameter);"
                    + _eol
                    + "        break;"
                    + _eol
                    + "    }"
                    + _eol
                    + "}" + _eol + "}" + _eol);
        }
        _headerFiles.add("ptolemy.kernel.util.NamedObj;");
        for (Map.Entry<String, String> entry : _actorInstantiationMethods
                .entrySet()) {
            String methodName = entry.getKey();
            String methodBody = entry.getValue();
            sharedCode.add("NamedObj " + methodName + "() throws Exception {"
                    + _eol + methodBody + _eol + "}" + _eol);
        }
        if (((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            // The array of TypedIOPorts used if the code generator variablesAsArrays is true.
            sharedCode
            .add("TypedIOPort [] _ioPortMap = new TypedIOPort["
                    + getCodeGenerator().generatePtIOPortSize()
                    + "];"
                    + _eol
                    + "TypedCompositeActor [] _compositeMap = new TypedCompositeActor["
                    + getCodeGenerator()
                    .generatePtTypedCompositeActorSize() + "];"
                    + _eol);
        }
        return sharedCode;
    }

    /** Return true if the argument would be generated using
     *  an AutoAdapter.
     *
     *  <p>This is used to put two or more custom actors in to the
     *  same container.</p>
     *  @param namedObj The NamedObj to check.
     *  @return True if the argument would be generated using
     *  an auto adapter.
     */
    public boolean isAutoAdaptered(NamedObj namedObj) {
        return AutoAdapter.isAutoAdaptered(getCodeGenerator(), namedObj);
    }

    /** Return true if the argument would be generated using
     *  an AutoAdapter.
     *
     *  <p>This is used to put two or more custom actors in to the
     *  same container.</p>
     *  @param codeGenerator The codegenerator.
     *  @param namedObj The NamedObj to check.
     *  @return True if the argument would be generated using
     *  an auto adapter.
     */
    public static boolean isAutoAdaptered(ProgramCodeGenerator codeGenerator,
            NamedObj namedObj) {
        try {
            _checkingAutoAdapter = true;
            _wouldBeAutoAdapted = false;
            try {
                // The adapter might be cached.
                Object adapter = codeGenerator.getAdapter(namedObj);
                if (adapter instanceof AutoAdapter) {
                    return true;
                }
            } catch (IllegalActionException ex) {
                // getAdapter() will throw an exception if
                // getAutoAdapter() is called and _checkingAutoAdapter
                // is null.  So, we ignore this exception
            }
            if (_wouldBeAutoAdapted) {
                return true;
            }
        } finally {
            _checkingAutoAdapter = false;
            _wouldBeAutoAdapted = false;
        }

        return false;
    }

    /** Return true if the port connects to a remote port that would
     *  code generated using an AutoAdapter.
     *
     *  <p>This is used to put two or more custom actors in to the
     *  same container.</p>
     *  @param port The port to check.
     *  @return True if the remote port would be generated using
     *  an auto adapter.
     *  @exception IllegalActionException If the CodeGenerator verbosity parameter
     *  cannot be read.
     */
    public boolean isAutoAdapteredRemotePort(Port port)
            throws IllegalActionException {
        return isAutoAdapteredRemotePort(getCodeGenerator(), port);
    }

    /** Return true if the port connects to a remote port that would
     *  code generated using an AutoAdapter.
     *
     *  <p>This is used to put two or more custom actors in to the
     *  same container.</p>
     *  @param port The port to check.
     *  @param codeGenerator The codegenerator.
     *  @return True if the remote port would be generated using
     *  an auto adapter.
     *  @exception IllegalActionException If the CodeGenerator verbosity parameter
     *  cannot be read.
     */
    public static boolean isAutoAdapteredRemotePort(
            ProgramCodeGenerator codeGenerator, Port port)
                    throws IllegalActionException {
        List linkedRelationList = port.linkedRelationList();
        if (linkedRelationList.size() == 0) {
            return false;
        }
        Relation relation = (Relation) linkedRelationList.get(0);
        TypedIOPort remotePort = (TypedIOPort) relation.linkedPortList(port)
                .get(0);
        NamedObj remoteActor = remotePort.getContainer();
        if (/*port.linkedRelationList().size() > 1
              || */relation.linkedPortList(port).size() > 1) {
            // FIXME: this might be superflous, since we loop through below.
            boolean remoteIsAutoAdaptered = isAutoAdaptered(codeGenerator,
                    remoteActor);
            StringBuffer message = new StringBuffer(
                    "Warning: custom actors "
                            + "connected to more than one port the same level. Msg #2\n");
            Iterator relations = port.linkedRelationList().iterator();
            while (relations.hasNext()) {
                Relation r = (Relation) relations.next();
                message.append("Component: " + port.getContainer().getName()
                        + ", port: " + port.getName() + ", relation: " + r
                        + " is connected to:" + _eol);
                int i = 0;
                Iterator ports = r.linkedPortList(port).iterator();
                while (ports.hasNext()) {
                    Port p = (TypedIOPort) ports.next();
                    i++;
                    message.append("     " + i + " " + p.getFullName() + _eol);
                    if (!isAutoAdaptered(codeGenerator, p.getContainer())) {
                        // If one of the remote actors is not auto
                        // adapatered, then mark this connection as
                        // not being autoadaptered.  This is probably
                        // a mistake, we should just handle this.
                        // This test:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml
                        // has a bunch of custom actors that share an input relation, but the input
                        // is a non-autoadapter actor.  We would like to preserve the connectivity.
                        message.append("       which is contained by an actor that is not an auto adapter.\n");
                        remoteIsAutoAdaptered = false;
                    }
                }
            }
            int verbosityLevel = ((IntToken) codeGenerator.verbosity.getToken())
                    .intValue();
            if (verbosityLevel > 0) {
                System.out.println("AutoAdapter: " + message);
            }
            return remoteIsAutoAdaptered;
        }
        return isAutoAdaptered(codeGenerator, remoteActor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Generate the fire code.
     * <p>Generate code that creates tokens, sends them to the input(s) of inner
     * Ptolemy actor, calls fire() on the actor and reads the outputs.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        // FIXME: what if the inline parameter is set?
        StringBuffer code = new StringBuffer(super._generateFireCode()
                + _eol
                + getCodeGenerator().comment(
                        "AutoAdapter._generateFireCode() start"));

        // FIXME: it is odd that we are transferring data around in the fire code.
        // Shouldn't we do this in prefire() and posfire()?

        // Transfer data from the codegen variables to the actor input ports.
        Iterator inputPorts = ((Actor) getComponent()).inputPortList()
                .iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String name = inputPort.getName();
            Type type = inputPort.getType();

            if (!isAutoAdapteredRemotePort(inputPort)) {
                if (!inputPort.isMultiport()
                        && inputPort.isOutsideConnected()
                        && (inputPort instanceof ParameterPort || inputPort
                                .numLinks() > 0)) {
                    // Only generate code if we have a ParameterPort or we are connected.
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterTwoActors.xml
                    code.append(_generateSendInside(name, name, type, 0));
                } else {
                    // Multiports.

                    // Generate code for the sources.  We don't use
                    // getWidth() here because IOPort.getWidth() says
                    // not to.
                    // FIXME: Shouldn't we use sourcePortList() and not numberOfSources()?  See generatePreinitializeMethodBodyCode().
                    //int sources = inputPort.numberOfSources();
                    List sourcePortList = inputPort.sourcePortList();
                    int sources = sourcePortList.size();
                    //code.append(_eol + getCodeGenerator().comment("AutoAdapter._generateFireCode() MultiPort name " + name + " type: " + type + " numberOfSources: " + inputPort.numberOfSources() + " inputPort: " + inputPort + " width: " + inputPort.getWidth() + " numberOfSinks: " + inputPort.numberOfSinks()));
                    for (int i = 0; i < sources; i++) {
                        // If a multiport input has one channel that isAutoAdaptered and one channel that is not autoAdaptered,
                        // then skip generating the sendInside if the channel is autoAdaptered.  The test is:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
                        if (isAutoAdaptered(((IOPort) sourcePortList.get(i))
                                .getContainer())) {
                            continue;
                        }
                        code.append(_generateSendInside(name, name + "Source"
                                + i, type, i));

                        if (_isReadingRemoteParameters(inputPort, i,
                                sourcePortList)) {
                            // Sigh.

                            // If we have a custom actor A that is
                            // connected to a composite that contains a
                            // custom actor B, but A and B are connected by
                            // a non-custom actor, then we need to
                            // transfer the token by hand.  For example:
                            // B--> AddSubtract --> A
                            // Test case
                            // $PTII/bin/ptcg -language java ~/ptII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/knownFailedTests/ReadPMultiport2AutoD.xml

                            NamedObj remoteActor = ((IOPort) sourcePortList
                                    .get(i)).getContainer();
                            NamedObj remoteActorContainer = remoteActor
                                    .getContainer();

                            //String remoteActorContainerSymbol = getCodeGenerator().generateVariableName(remoteActorContainer);
                            String remoteActorContainerSymbol = _generatePtTypedCompositeActorName(
                                    remoteActorContainer,
                                    remoteActorContainer.getName());
                            // If this code is a problem, it could be
                            // because isReadingRemoteParameters is
                            // falsely returning true.
                            code.append("{"
                                    + _eol
                                    + "TypedCompositeActor c0 = (TypedCompositeActor) "
                                    + remoteActorContainerSymbol
                                    + ";"
                                    + _eol
                                    + "TypedIOPort c0PortA = (TypedIOPort)c0.getPort(\"c0PortA\");"
                                    + _eol
                                    + "TypedIOPort c0PortB = (TypedIOPort)c0.getPort(\"c0PortB\");"
                                    + _eol
                                    + "if ( c0PortA == null) {"
                                    + _eol
                                    + "c0PortA = new TypedIOPort(c0, \"c0PortA\", false, true);"
                                    + _eol
                                    + "} else {"
                                    + _eol
                                    + "c0PortA.setMultiport(true);"
                                    + _eol
                                    + "}"
                                    + _eol
                                    + "if ( c0PortB == null) {"
                                    + _eol
                                    + "c0PortB = new TypedIOPort(c0, \"c0PortB\", true, false);"
                                    + _eol
                                    // If c0PortB does not exist, then connect it.
                                    + "c0.connect(c0PortB, c0PortA);"
                                    + _eol
                                    + "} else {"
                                    + _eol
                                    + "c0PortB.setMultiport(true);"
                                    + _eol
                                    + "}"
                                    + _eol
                                    + "c0PortA.setTypeEquals("
                                    + _typeToBaseType(inputPort.getType())
                                    + ");"
                                    + _eol

                                    //                                     + "if (!c0PortA.isDeeplyConnected(" + portOrParameter + ")) {" + _eol
                                    //                                     + "    $containerSymbol().connect(" + outputPortName + ","
                                    //                                     + portOrParameter + ");" + _eol
                                    //                                     + "}" + _eol
                                    //                                     // Connect c0PortB if necessary.  See.
                                    //                                     // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport7.xml
                                    //                                     // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport.xml
                                    //                                     + "if (!c0PortB.isDeeplyConnected(" + escapedCodegenPortNameSymbol + ")) {" + _eol
                                    //                                     + "    $containerSymbol().connect("
                                    //                                     + escapedCodegenPortNameSymbol + ", c0PortB);" + _eol
                                    //                                     + "}" + _eol

                                    + "c0PortA.send(0, c0PortB.get(0));" + _eol
                                    + "}" + _eol);
                        }
                    }

                    // Generate code for the sinks.
                    int sinks = inputPort.numberOfSinks();
                    int width = inputPort.getWidth();
                    if (width < sinks) {
                        sinks = width;
                    }
                    // FIXME: Shouldn't we use sinkPortList() and not numberOfSinks()?  See generatePreinitializeMethodBodyCode().
                    for (int i = 0; i < sinks; i++) {
                        // If a multiport input has one channel that isAutoAdaptered and one channel that is not autoAdaptered,
                        // then skip generating the sendInside if the channel is autoAdaptered.  The test is:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
                        if (isAutoAdaptered(inputPort.sinkPortList().get(i)
                                .getContainer())) {
                            continue;
                        }
                        // FIXME: it seems out that we are calling send inside on a sink?
                        code.append(_generateSendInside(name,
                                name + "Sink" + i, type, i));
                    }
                }
            }
        }

        // Fire the actor.

        code.append("$actorSymbol(actor).fire();" + _eol);

        // Transfer data from the actor output ports to the codegen variables.
        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            String name = outputPort.getName();
            Type type = outputPort.getType();

            if (!isAutoAdapteredRemotePort(outputPort)) {
                // Get data from the actor.
                if (!outputPort.isMultiport()) {
                    if (outputPort.isOutsideConnected()) {
                        // Place the temporary variable inside a block so that
                        // if we split a long body, the declaration does not
                        // get separated from the use.
                        code.append("{" + _eol
                                // Create temporary variables for each port so that we don't
                                // read from empty mailbox.
                                + _generateGetInsideDeclarations(name, name,
                                        type, 0) + _eol + "}" + _eol);
                    }
                } else {
                    // Multiports.
                    int sources = outputPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append("{"
                                + _eol
                                + _generateGetInsideDeclarations(name, name
                                        + "Source" + i, type, i) + _eol + "}"
                                        + _eol);
                    }
                    int sinks = outputPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append("{"
                                + _eol
                                + _generateGetInsideDeclarations(name, name
                                        + "Sink" + i, type, i) + _eol + "}"
                                        + _eol);
                    }
                }
            }
        }

        String[] splitFireCode = getCodeGenerator()._splitBody(
                "_AutoAdapterF_", code.toString());

        return //"try {" + _eol
                "{" + _eol + splitFireCode[0] + _eol + splitFireCode[1] + _eol + "}"
                + _eol;
        //+ "} catch (Exception ex) {" + _eol
        //+ "    throw new RuntimeException(\"Failed to fire() $actorSymbol(actor))\", ex);" + _eol
        //+ " }" + _eol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a port name and actor name, return the name of
     *  the external port.  This method is necessary because if there
     *  are two custom actors at the same level that have the same
     *  port name that is connected to non-custom actors, then we need
     *  to differentiate between them.  In addition, a little magic
     *  concerning backslashed must occur.
     *  @param container The container of the port, which is almost always
     *  an Actor.
     *  @param portName The unescaped port name
     *  @return a combination of the port name and the actor name with
     *  backslashes substituted.
     */
    private static String _externalPortName(NamedObj container, String portName) {
        // Need to deal with backslashes in port names, see
        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
        return container.getName() + "_" + portName.replace("\\", "\\\\");
    }

    /** Look for a field in the actor by port name.  This method is
     *  necessary because some ports have different names than the name
     *  of the field.
     *  @param component The component that contains the port.
     *  Typically this parameter is calle with getComponent() as
     *  the first argument.
     *  @param portName The escaped name of the port.
     *  @return The field.
     *  @exception NoSuchFieldException If a field cannot be access, or if
     *  getComponent() fails.
     */
    private static Field _findFieldByPortName(NamedObj component,
            String portName) throws NoSuchFieldException {
        portName = TemplateParser.unescapePortName(portName);
        Field foundPortField = null;
        // Make sure that there is a field with that name
        // $PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml
        // has a NonStrict actor with an output that is not connected.
        // If we don't check for the field, then the generated Java code
        // fails.
        try {
            foundPortField = component.getClass().getField(portName);
        } catch (NoSuchFieldException ex) {
            StringBuffer portNames = new StringBuffer();
            try {
                // It could be that the name of the port and the variable name
                // do not match.
                Field[] fields = component.getClass().getFields();
                for (Field field : fields) {
                    if (field.get(component) instanceof Port) {
                        Port portField = (Port) field.get(component);
                        String portFieldName = portField.getName();
                        portNames.append("<" + portFieldName + "> ");
                        if (portName.equals(portFieldName)) {
                            foundPortField = field;
                            break;
                        }
                    }
                }
                if (foundPortField == null) {
                    throw new NoSuchFieldException(component.getFullName()
                            + "Could not find field that corresponds with "
                            + portName + " Fields: " + portNames);
                }
            } catch (Throwable throwable2) {
                throw new NoSuchFieldException(component.getFullName()
                        + ": Failed to find the field that corresponds with "
                        + portName + " Fields: " + portNames + ": " + ex);
            }
        }
        return foundPortField;
    }

    /** Generate code that instantiates an actor.  Each actor gets a method
     *  that is added to the set of shared methods.
     *  @param actor The actor
     *  @param actorSymbol The code generated variable name that
     *  refers to the actor.
     *  @param containerSymbol The code generated variable name that
     *  refers to the container of the symbol.
     *  @param generateContainmentCode True if the code that
     *  instantiates the containers should be generated.  Usually,
     *  this parameter is true because when true, the only side
     *  effects of the containment code is to increase code size and
     *  execution time slightly.
     *  @param generateContainedVariables True code that instantiates
     *  the parameters should be generated.  Usually, this false when
     *  actor is a NamedObj and true when actor is a
     *  TypedCompositeActor.
     *  @param generateElectricityConnections If true, invoke
     *  _generateElectricityConnections().
     *  @return code that calls the shared method that instantiates the actor
     *  @exception IllegalActionException If thrown while generating
     *  the containment code.
     */
    private String _generateActorInstantiation(NamedObj actor,
            String actorSymbol, String containerSymbol,
            boolean generateContainmentCode,
            boolean generateContainedVariables,
            boolean generateElectricityConnections)
                    throws IllegalActionException {
        //String actorSymbol = getCodeGenerator().generateVariableName(
        //            actor) + "_actor";
        //String containerSymbol = getCodeGenerator().generateVariableName((((NamedObj) actor).getContainer()));

        String code = "if ("
                + actorSymbol
                + " == null) {"
                + _eol
                + "if ("
                + containerSymbol
                + " == null) {"
                + _eol
                // FIXME: we should define a method that creates the containment hierarchy.
                + (generateContainmentCode ? _generateContainmentCode(actor)
                        : " ")
                        + "}"
                        + _eol
                        + actorSymbol
                        + " = ("
                        + actor.getClass().getName()
                        + ")"
                        + containerSymbol
                        + ".getEntity(\""
                        + actor.getName()
                        + "\");"
                        + _eol
                        + "if ("
                        + actorSymbol
                        + " == null) {"
                        + _eol
                        + actorSymbol
                        + " = new "
                        + actor.getClass().getName()
                        + "("
                        + containerSymbol
                        + ", \""
                        + actor.getName()
                        + "\");"
                        + _eol
                        + _generateStringConsts(actor, actorSymbol, containerSymbol)
                        + (generateElectricityConnections ? _generateElectricityConnections(
                                actor, actorSymbol, containerSymbol) : "")
                                + "}"
                                + _eol
                                + (generateContainedVariables ? _generateContainedVariables(
                                        actor, actorSymbol) : " ")
                                        // Set the displayName so that actors that
                                        // call getDisplayName() get the same value.
                                        // Actors that generate random numbers often
                                        // call getFullName(), then should call
                                        // getDisplayName() instead.
                                        + "        " + actorSymbol + ".setDisplayName(\""
                                        + actor.getName() + "\");" + _eol + "}" + _eol + "return "
                                        + actorSymbol + ";" + _eol;
        String nonArrayActorSymbol = "";
        if (actor instanceof TypedCompositeActor) {
            nonArrayActorSymbol = getCodeGenerator()
                    .generateVariableName(actor);
        } else {
            nonArrayActorSymbol = actorSymbol;
        }
        String methodName = "_instantiate"
                + (generateContainmentCode ? "Containment" : "")
                + (generateContainedVariables ? "Variables" : "")
                + (generateElectricityConnections ? "ECons" : "")
                + processCode(nonArrayActorSymbol);
        _actorInstantiationMethods.put(methodName, processCode(code));
        return methodName + "();" + _eol;
    }

    /** Generate code that creates the hierarchy.
     *  @param component The component
     *  @return code that creates the hierarchy
     *  @exception IllegalActionException If thrown while generate code that
     *  connects the ports of the containers.
     */
    private String _generateContainmentCode(NamedObj component)
            throws IllegalActionException {
        StringBuffer containmentCode = new StringBuffer();

        // The symbol of the container of the component, similar to $containerSymbol.
        //String containerSymbol = getCodeGenerator().generateVariableName((((NamedObj) component).getContainer()));
        NamedObj container = component.getContainer();
        String containerSymbol = _generatePtTypedCompositeActorName(container,
                container.getName());

        NamedObj parentContainer = component.getContainer();
        NamedObj grandparentContainer = parentContainer.getContainer();

        if (grandparentContainer == null) {
            // The simple case, where the actor is in the top level and
            // we only need to create a TypedCompositeActor container.
            // Put the actor into the toplevel so that getFullName() returns the same value.
            // This is important for actors that use random numbers, as it is common to
            // set the seed to seed + getFullName().hashCode().
            containmentCode.append(containerSymbol + " = _toplevel;" + _eol);
        } else {
            // This wacky.  What we do is move up the hierarchy and instantiate
            // TypedComposites as necessary and *insert* the appropriate code into
            // the StringBuffer.  When we get to the top, we *append* code that
            // inserts the hierarchy into the toplevel and that creates the container
            // for the actor.  At runtime, when we are generating the hierarchy,
            // we need to avoid generating duplicate entities (entities that
            // already exist in a container that has more than one actor handled
            // by AutoAdapter).

            while (parentContainer != null
                    && parentContainer.getContainer() != null) {
                //                 containmentCode.insert(0,
                //                         "temporaryContainer = (TypedCompositeActor)cgContainer.getEntity(\""
                //                         + parentContainer.getName()
                //                         + "\");"
                //                         + _eol
                //                         + "if (temporaryContainer == null) { "
                //                         + _eol
                //                         + "    temporaryContainer = new "
                //                         // Use the actual class of the container, not TypedCompositeActor.
                //                         + parentContainer.getClass().getName()
                //                         + "(cgContainer, \""
                //                         + parentContainer.getName() + "\");" + _eol
                //                         + _generateStringConsts(parentContainer)
                //                         + _generateContainedVariables(parentContainer, "temporaryContainer")
                //                         //+ "{" + _eol
                //                         //+ generatePreinitializeMethodBodyCode(parentContainer)
                //                         //+ "}" + _eol
                //                         + "}" + _eol
                //                         + "cgContainer = temporaryContainer;" + _eol);
                //String parentContainerSymbol = getCodeGenerator().generateVariableName(parentContainer);
                String parentContainerSymbol = _generatePtTypedCompositeActorName(
                        parentContainer, parentContainer.getName());
                //String parentContainerContainerSymbol = getCodeGenerator().generateVariableName(parentContainer.getContainer());
                NamedObj parentContainerContainer = parentContainer
                        .getContainer();
                String parentContainerContainerSymbol = _generatePtTypedCompositeActorName(
                        parentContainerContainer,
                        parentContainerContainer.getName());
                containmentCode.insert(
                        0,
                        "cgContainer = (TypedCompositeActor)"
                                + _generateActorInstantiation(parentContainer,
                                        parentContainerSymbol,
                                        parentContainerContainerSymbol, false,
                                        true, true));
                parentContainer = parentContainer.getContainer();
            }

            containmentCode.insert(
                    0,
                    "{"
                            + _eol
                            + getCodeGenerator().comment(
                                    component.getFullName()) + _eol
                                    + "TypedCompositeActor cgContainer = _toplevel;"
                                    + _eol
                                    + "TypedCompositeActor temporaryContainer = null;"
                                    + _eol);
            containmentCode.append(containerSymbol + " = cgContainer;" + _eol
                    + "}" + _eol);

            containmentCode.append(_generateContainedVariables(
                    component.getContainer(), containerSymbol));
            // Whew.
        }
        return containmentCode.toString();
    }

    /** Instantiate Variables and locations for those actors that
     *  access parameters in their container.
     *  @param container The container in which we should look for variables and locations
     *  @param containerSymbol The java variable present in the generated code that refers
     *  to the container.
     *  @return code that instantiates any variables in the container.
     */
    private String _generateContainedVariables(NamedObj container,
            String containerSymbol) {
        StringBuffer variableCode = new StringBuffer();
        // Instantiate Variables for those actors that access parameters in their container.
        // $PTII/bin/ptcg -language java auto/ReadParametersInContainerTest.xml
        Iterator variables = container.attributeList(Variable.class).iterator();
        while (variables.hasNext()) {
            //if (!_importedVariable) {
            //   _importedVariable = true;

            //}
            Variable variable = (Variable) variables.next();
            String variableName = StringUtilities.sanitizeName(
                    variable.getName()).replaceAll("\\$", "Dollar");
            if (_skipVariable(variableName)) {
                // No need to create _windowProperties,  _vergilSize,
                // _vergilZoomFactor, or _vergilCenter variables.
                continue;
            }
            String variableClassName = variable.getClass().getName();
            String variableClassShortName = variableClassName
                    .substring(variableClassName.lastIndexOf(".") + 1);
            _headerFiles.add(variableClassName + ";");

            // FIXME: optimize this by creating a method that only creates
            // the variable if it is not already set.  The reason we would
            // have duplicate variables is because we have two custom actors
            // in one container and the container has Parameters.
            variableCode.append("if (" + containerSymbol + ".getAttribute(\""
                    + variable.getName() + "\") == null) {" + _eol + "   new "
                    + variableClassShortName + "(" + containerSymbol + ", \""
                    + variable.getName() + "\").setExpression(\""
                    + variable.getExpression().replace("$", "\\u0024") + "\");"
                    + _eol + "}" + _eol);
        }

        Iterator locations = container.attributeList(Location.class).iterator();
        while (locations.hasNext()) {
            Location location = (Location) locations.next();
            String locationName = StringUtilities.sanitizeName(
                    location.getName()).replaceAll("\\$", "Dollar");
            if (_skipVariable(locationName)) {
                continue;
            }
            String locationClassName = location.getClass().getName();
            String locationClassShortName = locationClassName
                    .substring(locationClassName.lastIndexOf(".") + 1);
            _headerFiles.add(locationClassName + ";");

            // FIXME: optimize this by creating a method that only creates
            // the location if it is not already set.  The reason we would
            // have duplicate locations is because we have two custom actors
            // in one container and the container has Parameters.
            variableCode.append("if (" + containerSymbol + ".getAttribute(\""
                    + location.getName() + "\") == null) {" + _eol + "   new "
                    + locationClassShortName + "(" + containerSymbol + ", \""
                    + location.getName() + "\").setExpression(\""
                    + location.getExpression() + "\");" + _eol + "}" + _eol);
        }

        // Include the variables of any TypedComposites that have only parameters and not actors.
        // By rights, these should be ScopeExtendingAttributes . . .

        if (container instanceof TypedCompositeActor) {
            Iterator composites = ((TypedCompositeActor) container)
                    .deepCompositeEntityList().iterator();
            while (composites.hasNext()) {
                TypedCompositeActor composite = (TypedCompositeActor) composites
                        .next();
                // Composites that are named "Generator" are handled specially.
                // FIXME: we could have an attribute if set will generates variables.
                if (composite.getName().equals("Generator")) {
                    variableCode
                    .append("{"
                            + _eol
                            //+ "System.out.println(\"GeneratorHack: \");" + _eol
                            + "TypedCompositeActor genComposite = (TypedCompositeActor)"
                            + containerSymbol
                            + ".getEntity(\""
                            + composite.getName()
                            + "\");"
                            + _eol
                            + "if (genComposite == null) {"
                            + _eol
                            + "    genComposite = new "
                            + composite.getClass().getName()
                            + "("
                            + containerSymbol
                            + ", \""
                            + composite.getName()
                            + "\");"
                            + _eol
                            + _generateContainedVariables(composite,
                                    "genComposite") + "}" + _eol + "}"
                                    + _eol);
                }
            }
        }
        return variableCode.toString();
    }

    /**
     * Generate execution code for the actor execution methods.
     * @param executionMethod One of "prefire", "postfire" or "wrapup".
     * @return The execution code for the corresponding method.
     * @exception IllegalActionException If illegal macro names are found.
     */
    private String _generateExecutionCode(String executionMethod)
            throws IllegalActionException {
        // Syntactic sugar, avoid code duplication.
        String code = "try {" + _eol + "    $actorSymbol(actor)."
                + executionMethod + "();" + _eol + "} catch (Exception ex) {"
                + _eol + "    throw new RuntimeException(\"Failed to "
                + executionMethod + "() $actorSymbol(actor))\", ex);" + _eol
                + "}" + _eol;
        return processCode(code);
    }

    /**
     * Return the code that gets data from the actor port and sends
     * it to the codegen port
     *  @param actorPortName The name of the Actor port from which
     *  data will be read.
     *  @param codegenPortName The name of the port on the codegen side.
     *  For non-multiports, actorPortName and codegenPortName are the same.
     *  For multiports, codegenPortName will vary according to channel number
     *  while actorPortName will remain the same.
     *  @param type The type of the port.
     *  @param channel The channel number.
     *  For non-multiports, the channel number will be 0.
     *  @return The code
     *  @exception IllegalActionException If thrown while reading the
     *  variablesAsArrays parameter of the code generator.
     */
    private String _generateGetInside(String actorPortName,
            String codegenPortName, Type type, int channel)
                    throws IllegalActionException {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        //codegenPortName = TemplateParser.escapePortName(codegenPortName);
        if (type instanceof ArrayType) {

            ArrayType array = (ArrayType) type;

            String codeGenElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());
            // FIXME: do we really need a separate symbol here?  This is inside a block.
            //String ptolemyDataSymbol = _generatePtIOPortName(getComponent(), actorPortName + "_ptolemyData");
            String ptolemyDataSymbol = "$actorSymbol(" + actorPortName
                    + "_ptolemyData)";
            return "{"
            + _eol
            //                 // Get the data from the Ptolemy port
            //                 + type.getTokenClass().getName() + " " + ptolemyData + "= (("
            //                 + type.getTokenClass().getName() + ")($actorSymbol("
            //                 + codegenPortName + ").getInside(0"
            //                 // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //                 + (channel == 0 ? "" : ", " + channel)
            //                 + ")));" + _eol

            // Create an array for the codegen data.
            + _eol
            + getCodeGenerator()
            .comment(
                    "AutoAdapter: FIXME: This will leak. We should check to see if the token already has been allocated")
                    + " Token codeGenData = $Array_new("
                    + "((ArrayToken)"
                    + ptolemyDataSymbol
                    + ").length() , 0);"
                    + _eol

                    // Copy from the Ptolemy data to the codegen data.
                    + " for (int i = 0; i < ((ArrayToken)"
                    + ptolemyDataSymbol
                    + ").length(); i++) {"
                    + _eol
                    + "   Array_set(codeGenData, i, "
                    + getCodeGenerator().codeGenType(
                            array.getDeclaredElementType()) + "_new(((("
                            + codeGenElementType + "Token)(" + ptolemyDataSymbol
                            + ".getElement(i)))." + targetElementType + "Value())));"
                            + _eol + "  }" + _eol

                            // Output our newly constructed token
                            + " $put(" + actorPortName + ", codeGenData);" + _eol + "}"
                            + _eol;
        } else {
            String portData = actorPortName + "_portData"
                    + (channel == 0 ? "" : channel);
            return "$put(" + actorPortName
                    // Refer to the token by the full class name and obviate the
                    // need to manage imports.
                    //+ ", (" + _generatePtIOPortName(getComponent(), portData) + "))";
                    + ", ($actorSymbol(" + portData + ")))";

        }
    }

    /**
     *  Return the code that creates temporary variables that hold the
     *  values to be read.  We need to do this so as to avoid
     *  reading from the same Ptolemy receiver twice, which would happen
     *  if we have an automatically generated actor with a regular
     *  non-multiport that feeds its output to two actors.
     *  @param actorPortName The name of the Actor port from which
     *  data will be read.
     *  @param codegenPortName The name of the port on the codegen side.
     *  For non-multiports, actorPortName and codegenPortName are the same.
     *  For multiports, codegenPortName will vary according to channel number
     *  while actorPortName will remain the same.
     *  @param type The type of the port.
     *  @param channel The channel number.
     *  For non-multiports, the channel number will be 0.
     *  @return The inside declarations
     *  @exception IllegalActionException If thrown while generating code to to
     *  gets data from the actor port and sends it to the codegen port.
     */
    private String _generateGetInsideDeclarations(String actorPortName,
            String codegenPortName, Type type, int channel)
                    throws IllegalActionException {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        codegenPortName = TemplateParser.escapePortName(codegenPortName);
        String codegenPortNameSymbol = _generatePtIOPortName(getComponent(),
                codegenPortName);
        // This method is needed by $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
        String portData = actorPortName + "_portData"
                + (channel == 0 ? "" : channel);
        if (type instanceof ArrayType) {
            ArrayType array = (ArrayType) type;

            String codeGenElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());

            // FIXME: do we really need a separate symbol here?  This is inside a block.
            //String ptolemyDataSymbol = _generatePtIOPortName(getComponent(), actorPortName + "_ptolemyData");
            String ptolemyDataSymbol = "$actorSymbol(" + actorPortName
                    + "_ptolemyData)";

            return
                    // Get the data from the Ptolemy port
                    type.getTokenClass().getName()
                    + " "
                    + ptolemyDataSymbol
                    + " = (("
                    + type.getTokenClass().getName()
                    + ")("
                    + codegenPortNameSymbol
                    + ".getInside(0"
                    // For non-multiports "". For multiports, ", 0", ", 1" etc.
                    + (channel == 0 ? "" : ", " + channel)
                    + ")));"
                    + _eol
                    // Create an array for the codegen data.
                    + _eol
                    + getCodeGenerator()
                    .comment(
                            "AutoAdapter: FIXME: This will leak. We should check to see if the token already has been allocated")
                            + " Token "
                            + " $actorSymbol("
                            + portData
                            + ")"
                            + " = $Array_new("
                            + "((ArrayToken)"
                            + ptolemyDataSymbol
                            + ").length(), 0);"
                            + _eol

                            // Copy from the Ptolemy data to the codegen data.
                            + " for (int i = 0; i < ((ArrayToken)"
                            + ptolemyDataSymbol
                            + ").length(); i++) {"
                            + _eol
                            + "   Array_set("
                            + "$actorSymbol("
                            + portData
                            + ")"
                            + ", i, "
                            + getCodeGenerator().codeGenType(
                                    array.getDeclaredElementType())
                                    + "_new(((("
                                    + codeGenElementType
                                    + "Token)("
                                    + ptolemyDataSymbol
                                    + ".getElement(i)))."
                                    + targetElementType
                                    + "Value())));"
                                    + _eol
                                    + "  }"
                                    + _eol
                                    + _generateGetInside(actorPortName, codegenPortName, type,
                                            channel);
        } else if (type == BaseType.COMPLEX) {
            _headerFiles.add("ptolemy.math.Complex;");
            return "$targetType("
            + actorPortName
            + ") $actorSymbol("
            + portData
            + ");"
            + _eol
            + "Complex complex = (Complex)((("
            + type.getTokenClass().getName()
            + ")"
            + "("
            + codegenPortNameSymbol
            + ".getInside(0"
            + ")))."
            + _valueMethodName(type)
            + ");"
            + _eol
            + "double real = complex.real;"
            + _eol
            + "double imag = complex.imag;"
            + _eol
            + "$actorSymbol("
            + portData
            + ")"
            + " = $typeFunc(TYPE_Complex::new(real, imag));"
            + _eol
            + _generateGetInside(actorPortName, codegenPortName, type,
                    channel);

            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //+ (channel == 0 ? "" : ", " + channel)
        } else if (type.equals(BaseType.OBJECT)) {
            //_headerFiles.add("ptolemy.math.Complex;");
            return "$targetType("
            + actorPortName
            + ") $actorSymbol("
            + portData
            + ");"
            + _eol
            + "Object object = (Object)((("
            + type.getTokenClass().getName()
            + ")"
            + "("
            + codegenPortNameSymbol
            + ".getInside(0"
            + ")))."
            + _valueMethodName(type)
            + ");"
            + _eol
            + "$actorSymbol("
            + portData
            + ")"
            + " = $typeFunc(TYPE_Object::new(object));"
            + _eol
            + _generateGetInside(actorPortName, codegenPortName, type,
                    channel);

            //             return "$targetType("
            //                 + actorPortName
            //                 + ") $actorSymbol("
            //                 + portData
            //                 + ");"
            //                 + _eol
            //                 + "ObjectToken objectToken = (ObjectToken)"
            //                 + codegenPortNameSymbol
            //                 + ".getInside(0);" + _eol
            //                 + "$actorSymbol(" + portData + ") = objectToken.getValue();"
            //                 + _eol
            //                 + _generateGetInside(actorPortName, codegenPortName, type,
            //                         channel);

            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //+ (channel == 0 ? "" : ", " + channel)

        } else {
            return "// type: "
                    + type
                    + "\n$targetType("
                    + actorPortName
                    + ") $actorSymbol("
                    + portData
                    + ");"
                    + _eol
                    + "if ("
                    + codegenPortNameSymbol
                    + ".hasTokenInside(0)) {"
                    + _eol
                    + "    $actorSymbol("
                    + portData
                    + ") = "
                    + "(("
                    + type.getTokenClass().getName()
                    + ")"
                    + "("
                    + codegenPortNameSymbol
                    + ".getInside(0"
                    + ")))."
                    + _valueMethodName(type)
                    + ";"
                    + _eol
                    + _generateGetInside(actorPortName, codegenPortName, type,
                            channel) + _eol + "}" + _eol;
            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //+ (channel == 0 ? "" : ", " + channel)
        }
    }

    /** Return the code that is used to connect ports for situations
     *  where an actor may read parameters from a remote container.
     *  @param readingReoteParametersDepth The depth at which remote
     *  parameters are being read.
     *  @param portOrParameter The port or parameter.
     *  @param escapedCodegenPortName The escaped port name.
     *  @return The code
     *  @exception IllegalActionException If thrown while reading the
     *  variablesAsArrays parameter of the code generator.
     */
    private String _generateRemoteParameterConnections(
            int readingRemoteParametersDepth, String portOrParameter,
            String escapedCodegenPortName) throws IllegalActionException {
        String outputPortName = "c" + readingRemoteParametersDepth + "PortA";
        String escapedCodegenPortNameSymbol = _generatePtIOPortName(
                getComponent(), escapedCodegenPortName);
        return "if (!c0PortA.isDeeplyConnected(" + portOrParameter + ")) {"
        + _eol + "    $containerSymbol().connect(" + outputPortName
        + "," + portOrParameter + ");" + _eol
        + "    $containerSymbol().connect("
        + escapedCodegenPortNameSymbol + ", c0PortB);" + _eol + "}"
        + _eol;
    }

    /** Generate port declaration code for a composite or atomic entity.
     *  @return the port declaration code.
     *  @exception IllegalActionException If the variablesAsArrays parameter
     *  of the code generator cannot be read.
     */
    private StringBuffer _generatePortDeclarations(Entity entity)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // Handle inputs and outputs on a per-actor basis.
        // There is very similar code in generatePreinitializeMethodBodyCode()
        Iterator entityPorts = entity.portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                if (!castPort.isOutsideConnected()) {
                    continue;
                }
                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    if (!((BooleanToken) getCodeGenerator().variablesAsArrays
                            .getToken()).booleanValue()) {
                        code.append("TypedIOPort $actorSymbol(" + name + ");"
                                + _eol);
                    }
                } else {
                    // FIXME: We instantiate a separate external port for each channel
                    // of the multiport.  Could we just connect directly to the channels
                    // of the multiport?  The problem I had was that the receivers are
                    // not created if I connect directly to the channels.

                    // Use castPort.getName() and get the real name of the port.
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                    IOPort actorPort = (IOPort) ((Entity) getComponent())
                            .getPort(castPort.getName());

                    if (actorPort == null) {
                        Entity container = (Entity) getComponent()
                                .getContainer();
                        if (container != null) {
                            // Needed by
                            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadParamIC2.xml
                            actorPort = (IOPort) container.getPort(castPort
                                    .getName());
                        }
                        if (actorPort == null) {
                            System.out.println(getComponent().getContainer()
                                    .exportMoML());
                            throw new NullPointerException(
                                    "Could not find port \""
                                            + castPort.getName()
                                            + "\" in "
                                            + getComponent().getFullName()
                                            + (container != null ? " or "
                                                    + container.getFullName()
                                                    : ""));
                        }

                    }

                    List sourcePortList = castPort.sourcePortList();
                    for (int i = 0; i < sourcePortList.size(); i++) {
                        if (!((BooleanToken) getCodeGenerator().variablesAsArrays
                                .getToken()).booleanValue()) {
                            code.append("TypedIOPort $actorSymbol(" + name
                                    + "Source" + i + ");" + _eol);
                        }

                        // True if the port is an input multiport connected to an
                        // actor in a container that has parameters.
                        if (_isReadingRemoteParameters(castPort, i,
                                sourcePortList)) {
                            NamedObj remoteActorContainer = ((IOPort) sourcePortList
                                    .get(i)).getContainer().getContainer();
                            if (remoteActorContainer != null
                                    && !((BooleanToken) getCodeGenerator().variablesAsArrays
                                            .getToken()).booleanValue()) {
                                if (!_containersDeclared
                                        .contains(remoteActorContainer)) {

                                    _containersDeclared
                                    .add(remoteActorContainer);
                                    //String remoteActorContainerSymbol = getCodeGenerator().generateVariableName(remoteActorContainer);
                                    String remoteActorContainerSymbol = _generatePtTypedCompositeActorName(
                                            remoteActorContainer,
                                            remoteActorContainer.getName());
                                    code.append("TypedCompositeActor "
                                            + remoteActorContainerSymbol + ";"
                                            + _eol);
                                }
                                NamedObj remoteActorContainerContainer = ((IOPort) sourcePortList
                                        .get(i)).getContainer().getContainer()
                                        .getContainer();
                                if (remoteActorContainerContainer != null
                                        && !_containersDeclared
                                        .contains(remoteActorContainerContainer)) {
                                    _containersDeclared
                                    .add(remoteActorContainerContainer);
                                    //String remoteActorContainerContainerSymbol = getCodeGenerator().generateVariableName(remoteActorContainerContainer);
                                    String remoteActorContainerContainerSymbol = _generatePtTypedCompositeActorName(
                                            remoteActorContainerContainer,
                                            remoteActorContainerContainer
                                            .getName());
                                    code.append("TypedCompositeActor "
                                            + remoteActorContainerContainerSymbol
                                            + ";" + _eol);
                                }
                            }
                        }
                    }
                    if (!((BooleanToken) getCodeGenerator().variablesAsArrays
                            .getToken()).booleanValue()) {
                        List sinkPortList = castPort.sinkPortList();
                        for (int i = 0; i < sinkPortList.size(); i++) {
                            code.append("TypedIOPort $actorSymbol(" + name
                                    + "Sink" + i + ");" + _eol);
                        }
                    }
                }
            }
        }
        return code;
    }

    /** Return the code necessary to instantiate the port.
     *  @param actorPortName The escaped name of the Actor port to be instantiated.
     *  @param codegenPortName The name of the port on the codegen side.
     *  For non-multiports, actorPortName and codegenPortName are the same.
     *  For multiports, codegenPortName will vary according to channel number
     *  while actorPortName will remain the same.
     *  @param port The port of the actor.
     *  @param channelNumber The number of the channel.  For
     *  singlePorts, the channelNumber will be 0.  For multiports, the
     *  channelNumber will range from 0 to the number of sinks or
     *  sources.
     *  @param sourceOrSinkPorts The source or sink ports connected to the port.
     *  @exception IllegalActionException If there is a problem checking whether
     *  actorPortName is a PortParameter.
     */
    private String _generatePortInstantiation(String actorPortName,
            String codegenPortName, TypedIOPort port, int channelNumber,
            List sourceOrSinkPorts) throws IllegalActionException {
        //String escapedActorPortName = TemplateParser.escapePortName(actorPortName);
        String unescapedActorPortName = TemplateParser
                .unescapePortName(actorPortName);
        String escapedCodegenPortName = TemplateParser
                .escapePortName(codegenPortName);
        String escapedCodegenPortNameSymbol = _generatePtIOPortName(
                getComponent(), escapedCodegenPortName);
        PortParameter portParameter = (PortParameter) getComponent()
                .getAttribute(actorPortName, PortParameter.class);
        // Multiport need to have different codegenPortNames, see
        // $PTII/bin/ptcg -language java  $PTII/ptolemy/actor/lib/test/auto/Gaussian1.xml

        // There are some custom actors that reach across their links and read
        // parameters from the actor on the other side:
        //
        // If we have a model CompositeA -> ActorB
        // has a parameter named "remoteParameter" and ActorB is a
        // ptolemy/cg/kernel/generic/program/procedural/java/test/ReadParametersAcrossLink.java
        // then that actor reads the value of the "remoteParameter"
        // parameter in CompositeA.
        //
        // To test this:
        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/ReadPsIC.xml

        // First, determine if the port is an input port that is connected
        // to a TypedComposite that has parameters.  If it is, then generate
        // a composite that contains the parameters and connect our code generator
        // to its input.  If we CompositeA -> ActorB, then we generate CompositeC
        // and generate code that creates CompositeC -> ActorB.

        // True if the port is an input multiport connected to an
        // actor in a container that has parameters.
        boolean readingRemoteParameters = _isReadingRemoteParameters(port,
                channelNumber, sourceOrSinkPorts);
        int verbosityLevel = ((IntToken) getCodeGenerator().verbosity
                .getToken()).intValue();
        StringBuffer code = new StringBuffer("{" + _eol);
        int readingRemoteParametersDepth = 0;
        if (readingRemoteParameters) {
            NamedObj remoteActor = ((IOPort) sourceOrSinkPorts
                    .get(channelNumber)).getContainer();
            // Check to see if the container of the container of the
            // remote actor is null.  Needed for:
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortWithStarsInName2.xml
            if (remoteActor.getContainer().getContainer() == null) {
                if (verbosityLevel > 4) {
                    System.out
                    .println(getComponent().getFullName()
                            + " remote actor "
                            + remoteActor.getFullName()
                            + " is close to the top, setting readingRemoteParameters to false");
                }
                readingRemoteParameters = false;
            } else {
                //String remoteActorSymbol = getCodeGenerator().generateVariableName(
                //        remoteActor) + "_actor";
                //String remoteActorContainerSymbol = getCodeGenerator().generateVariableName((((NamedObj) remoteActor).getContainer()));
                NamedObj remoteActorContainer = remoteActor.getContainer();
                String remoteActorContainerSymbol = _generatePtTypedCompositeActorName(
                        remoteActorContainer, remoteActorContainer.getName());
                // FIXME: remoteActor.getContainer().getContainer() be null?
                //String remoteActorContainerContainerSymbol = getCodeGenerator().generateVariableName((((NamedObj) remoteActor).getContainer().getContainer()));
                NamedObj remoteActorContainerContainer = remoteActorContainer
                        .getContainer();
                String remoteActorContainerContainerSymbol = _generatePtTypedCompositeActorName(
                        remoteActorContainerContainer,
                        remoteActorContainerContainer.getName());
                if (verbosityLevel > 4) {
                    System.out
                    .println(getComponent().getFullName()
                            + " remote actor "
                            + remoteActor.getFullName()
                            + "Setting readingRemoteParameters "
                            + (remoteActor.getContainer()
                                    .getContainer() != port
                                    .getContainer().getContainer())
                                    + " "
                                    + remoteActor.getContainer().getContainer()
                                    .getContainer() != null);
                }
                if (remoteActor.getContainer().getContainer() != port
                        .getContainer().getContainer()
                        && remoteActor.getContainer().getContainer()
                        .getContainer() != null) {
                    // The remoteActor could be contained by a composite, so we create connections
                    // all the way up.
                    // FIXME: In theory, we should have a loop here to deal with arbitrary depth
                    readingRemoteParametersDepth = 1;
                    if (verbosityLevel > 4) {
                        System.out.println(getComponent().getFullName()
                                + " remote actor " + remoteActor.getFullName()
                                + "Setting readingRemoteParametersDepth "
                                + readingRemoteParametersDepth);
                    }
                    //String remoteActorC3Symbol = getCodeGenerator().generateVariableName((((NamedObj) remoteActor).getContainer().getContainer().getContainer()));
                    NamedObj remoteActorC3 = remoteActor.getContainer()
                            .getContainer().getContainer();
                    String remoteActorC3Symbol = _generatePtTypedCompositeActorName(
                            remoteActorC3, remoteActorC3.getName());
                    code.append("TypedCompositeActor c1 = (TypedCompositeActor)"
                            + _generateActorInstantiation(remoteActor
                                    .getContainer().getContainer(),
                                    remoteActorContainerContainerSymbol,
                                    remoteActorC3Symbol, true, true, true));
                    code.append("TypedIOPort c1PortA = (TypedIOPort)c1.getPort(\"c1PortA\");"
                            + _eol
                            + "if ( c1PortA == null) {"
                            + _eol
                            + "c1PortA = new TypedIOPort(c1, \"c1PortA\", false, true);"
                            + _eol
                            + "}"
                            + _eol
                            + "TypedIOPort c1PortB = (TypedIOPort)c1.getPort(\"c1PortB\");"
                            + _eol
                            + "if ( c1PortB == null) {"
                            + _eol
                            + "c1PortB = new TypedIOPort(c1, \"c1PortB\", true, false);"
                            + _eol
                            // If c1PortB does not exist, then connect it.
                            //+ "c1.connect(c1PortB, c1PortA);" + _eol
                            + "}" + _eol);
                }
                code.append("TypedCompositeActor c0 = (TypedCompositeActor)"
                        + _generateActorInstantiation(
                                remoteActor.getContainer(),
                                remoteActorContainerSymbol,
                                remoteActorContainerContainerSymbol, true,
                                true, true));
                // Create the input and output ports and connect them.
                if (verbosityLevel > 3) {
                    code.append("    System.out.println(\"E1 " + actorPortName
                            + " " + port.getFullName() + " " + channelNumber
                            + " " + remoteActor.getFullName() + "\");" + _eol);
                }
                // We set the port as a multiport if necessary, see:
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport7.xml
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport.xml
                code.append("TypedIOPort c0PortA = (TypedIOPort)c0.getPort(\"c0PortA\");"
                        + _eol
                        + "if ( c0PortA == null) {"
                        + _eol
                        + "c0PortA = new TypedIOPort(c0, \"c0PortA\", false, true);"
                        + _eol
                        + "} else {"
                        + _eol
                        + "c0PortA.setMultiport(true);"
                        + _eol
                        + "}"
                        + _eol
                        + "TypedIOPort c0PortB = (TypedIOPort)c0.getPort(\"c0PortB\");"
                        + _eol
                        + "if ( c0PortB == null) {"
                        + _eol
                        + "c0PortB = new TypedIOPort(c0, \"c0PortB\", true, false);"
                        + _eol
                        // If c0PortB does not exist, then connect it.
                        + "c0.connect(c0PortB, c0PortA);"
                        + _eol
                        + "} else {"
                        + _eol
                        + "c0PortB.setMultiport(true);"
                        + _eol
                        + "}"
                        + _eol);
            }
        }

        // If the remote port belongs to an actor that would
        // use an AutoAdapter (that is, an actor for which there
        // is no template), then connect directly to the remote port.
        // This allows us to put multiple custom actors in one composite.

        // remoteIsAutoAdaptered is true if the remote actor is a
        // custom actor.  The idea is that the layout of the custom
        // actors in the code generated MoML fragment should match the
        // original layout.  Custom actors may communicate with each
        // other via backdoor methods, so we keep custom actors in the
        // same container.

        // We don't call isAutoAdapteredRemotePort() here because we
        // want to use these variables anyway.
        boolean remoteIsAutoAdaptered = false;

        Set<String> instantiatedPort = new HashSet<String>();
        HashMap<String, String> connectedPorts = new HashMap<String, String>();

        Iterator linkedRelations = port.linkedRelationList().iterator();
        while (linkedRelations.hasNext()) {
            Relation relation = (Relation) linkedRelations.next();

            Iterator linkedPorts = relation.linkedPortList(port).iterator();

            while (linkedPorts.hasNext()) {
                //TypedIOPort remotePort = (TypedIOPort) linkedPorts.get(0);
                TypedIOPort remotePort = (TypedIOPort) linkedPorts.next();

                if (verbosityLevel > 3) {
                    //code.append("System.out.println(\"AA1: " + port.getFullName().replace("$", "\\u0024") + " --> " + relation.getName() + " --> " + remotePort.getFullName().replace("$", "\\u0024") + "\");" + _eol);
                }
                NamedObj remoteActor = remotePort.getContainer();
                String remoteActorSymbol = "";
                boolean moreThanOneRelation = false;
                String relationSymbol = "";
                boolean hasAutoConnectorRelation = _hasAutoConnectorRelation(port);
                if (isAutoAdaptered(remoteActor)) {
                    // The remote actor is a custom actor (aka AutoAdaptered)
                    remoteIsAutoAdaptered = true;
                    if (verbosityLevel > 2) {
                        System.out.println("AutoAdapter: "
                                + getComponent().getName() + " "
                                + port.getName() + "#" + channelNumber
                                + " is connected to remote actor "
                                + remoteActor.getName() + " "
                                + remotePort.getName() + " via "
                                + relation.getName() + " "
                                + port.linkedRelationList().size() + " "
                                + relation.linkedPortList(port).size());
                    }

                    // Warn if relations that have more than one port.
                    if (/*port.linkedRelationList().size() > 1
                          ||*/relation.linkedPortList(port).size() > 1) {
                        StringBuffer message = new StringBuffer(
                                "Warning: custom actors that are "
                                        + "connected to more than one port at the same level. Msg #1\n");
                        Iterator relations = port.linkedRelationList()
                                .iterator();
                        while (relations.hasNext()) {
                            Relation r = (Relation) relations.next();
                            message.append(getComponent().getName() + " "
                                    + port.getName() + " " + r + _eol);
                            Iterator ports = r.linkedPortList(port).iterator();
                            while (ports.hasNext()) {
                                Port p = (TypedIOPort) ports.next();
                                message.append("    " + p + _eol);
                                if (!isAutoAdaptered(p.getContainer())) {
                                    // If one of the remote actors is not auto
                                    // adapatered, then mark this connection as
                                    // not being autoadaptered.  This is probably
                                    // a mistake, we should just handle this.
                                    // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml
                                    message.append("\nPort "
                                            + p.getFullName()
                                            + " is contained by an actor that is not an auto adapter.\n");
                                    remoteIsAutoAdaptered = false;
                                }
                            }
                        }
                        moreThanOneRelation = true;
                        relationSymbol = "$actorSymbol(" + relation.getName()
                                + ")";
                        if (verbosityLevel > 1) {
                            System.out.println("AutoAdapter: " + message);
                        }
                    }

                    remoteActorSymbol = getCodeGenerator()
                            .generateVariableName(remoteActor) + "_actor";
                    //String remoteActorContainerSymbol = getCodeGenerator().generateVariableName((((NamedObj) remoteActor).getContainer()));
                    NamedObj remoteActorContainer = remoteActor.getContainer();
                    String remoteActorContainerSymbol = _generatePtTypedCompositeActorName(
                            remoteActorContainer,
                            remoteActorContainer.getName());

                    // Create the remote actor if necessary.
                    if (!moreThanOneRelation) {
                        // Test this block with
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterTwoActors.xml
                        if (verbosityLevel > 1) {
                            code.append("System.out.println(\"Create remote actor: "
                                    + remoteActor.getName() + "\");" + _eol);
                        }
                        code.append(_generateActorInstantiation(remoteActor,
                                remoteActorSymbol, remoteActorContainerSymbol,
                                true, false, true));
                    }
                }

                // Instantiate the port and set its type.
                if (!instantiatedPort.contains(escapedCodegenPortNameSymbol)
                        && !remoteIsAutoAdaptered /*&& !hasAutoConnectorRelation*/) {
                    instantiatedPort.add(escapedCodegenPortNameSymbol);
                    // Ports that have a relation whose name starts with "autoConnector" will be created specially?
                    // FIXME: maybe this should only be input or output ports that are autoConnector ports?
                    if (verbosityLevel > 3) {
                        code.append("System.out.println(\"I1\");" + _eol);
                    }
                    code.append("if ($containerSymbol().getPort(\""
                            + AutoAdapter._externalPortName(
                                    port.getContainer(), codegenPortName)
                                    .replace("$", "\\u0024")
                                    + "\") == null) {"
                                    + _eol
                                    + escapedCodegenPortNameSymbol
                                    + " = new TypedIOPort($containerSymbol()"
                                    // Need to deal with backslashes in port names, see
                                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                                    + ", \""
                                    //+ codegenPortName.replace("\\", "\\\\")
                                    + AutoAdapter._externalPortName(
                                            port.getContainer(), codegenPortName)
                                            .replace("$", "\\u0024") + "\", "
                                            + port.isInput() + ", "
                                            + port.isOutput()
                                            + ");"
                                            + _eol
                                            // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                                            + "    " + escapedCodegenPortNameSymbol
                                            + ".setTypeEquals("
                                            + _typeToBaseType(port.getType()) + ");" + _eol
                                            + "}" + _eol);

                }

                boolean connectedAlready = false;
                String portOrParameter = "";

                // Try to get the field by port name.  Unfortunately, some field names
                // do not match their port names.
                try {
                    Field foundPortField = _findFieldByPortName(getComponent(),
                            unescapedActorPortName);
                    if (foundPortField == null) {
                        throw new NoSuchFieldException("Could not find port "
                                + unescapedActorPortName);
                    }

                    portOrParameter = "(("
                            + getComponent().getClass().getName()
                            + ")$actorSymbol(actor))."
                            + foundPortField.getName()
                            + (portParameter != null ? ".getPort()" : "");

                } catch (NoSuchFieldException ex) {
                    // The port is not a field, it might be a PortParameter
                    // that whose name is not the same as the declared name.
                    // We check before we create it.  To test, use:
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterActorTest.xml
                    //String multiport = "";
                    code.append("if ($actorSymbol(actor).getPort(\""
                            + unescapedActorPortName.replace("\\", "\\\\")
                            .replace("$", "\\uu0024")
                            //+ AutoAdapter._externalPortName(port.getContainer(),
                            //        unescapedActorPortName)
                            + "\") == null) {"
                            + _eol
                            //+ escapedCodegenPortNameSymbol
                            + "TypedIOPort port"
                            + " = new TypedIOPort($actorSymbol(actor), \""
                            + unescapedActorPortName.replace("\\", "\\\\")
                            .replace("$", "\\u0024")
                            //+ AutoAdapter._externalPortName(port.getContainer(),
                            //        unescapedActorPortName)
                            + "\", " + port.isInput() + ", " + port.isOutput()
                            + ");" + _eol);

                    // Instantiate any attributes contained by the port
                    StringBuffer attributeCode = new StringBuffer();
                    Iterator portAttributes = port.attributeList().iterator();
                    while (portAttributes.hasNext()) {
                        Attribute attribute = (Attribute) portAttributes.next();
                        if (attribute instanceof Parameter) {
                            Parameter parameter = (Parameter) attribute;
                            String className = parameter.getClassName();
                            attributeCode
                            .append("parameter = new " + className
                                    + "(port, \"" + parameter.getName()
                                    + "\");" + _eol
                                    + "parameter.setExpression(\""
                                    + parameter.getExpression()
                                    + "\");" + _eol);
                        }
                    }
                    if (attributeCode.length() > 0) {
                        code.append("{" + _eol + "Parameter parameter = null;"
                                + _eol + attributeCode + "}" + _eol);
                    }

                    if (remotePort.isMultiport()) {
                        if (verbosityLevel > 3) {
                            code.append("System.out.println(\"MP1\");" + _eol);
                        }
                        code.append(// escapedCodegenPortNameSymbol
                                "port" + ".setMultiport(true);" + _eol);
                    }

                    code.append("}" + _eol);

                    // Need to replace $ for
                    // $PTII/bin/ptcg -language java  $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortWithStarsInName2.xml
                    portOrParameter = "(TypedIOPort)$actorSymbol(actor).getPort(\""
                            + unescapedActorPortName.replace("\\", "\\\\")
                            .replace("$", "\\uu0024")

                            //+ AutoAdapter._externalPortName(port.getContainer(),
                            //            unescapedActorPortName)
                            + "\")";
                    if (!readingRemoteParameters) {
                        connectedAlready = false;
                        //code.append("    $containerSymbol().connect("
                        //+ escapedCodegenPortNameSymbol + ", " + portOrParameter
                        //+ ");" + _eol);
                    } else {
                        connectedAlready = true;
                        if (verbosityLevel > 3) {
                            code.append("    System.out.println(\"D1\");"
                                    + _eol);
                        }
                        code.append(_generateRemoteParameterConnections(
                                readingRemoteParametersDepth, portOrParameter,
                                escapedCodegenPortName));
                    }
                    if (port.isOutput()) {
                        code.append("    (" + portOrParameter
                                + ").setTypeEquals("
                                + _typeToBaseType(port.getType()) + ");" + _eol);
                    }

                }
                if (remoteIsAutoAdaptered /* foo */) {
                    // Look up the remote port as a field in the remote actor.
                    // If the remote actor does not have a field that matches
                    // the port by name, then it could be a PortParameter
                    try {
                        Field remoteFoundPortField = _findFieldByPortName(
                                remoteActor, remotePort.getName());

                        if (remoteFoundPortField == null) {
                            throw new NoSuchFieldException(
                                    "Could not find port "
                                            + remotePort.getName());
                        }
                        if (port.isOutput()) {
                            String relationAssignment = "";
                            String relationSetWidth = "";
                            if (moreThanOneRelation) {
                                _headerFiles
                                .add("ptolemy.actor.TypedIORelation;");
                                code.append("{" + _eol + "TypedIORelation "
                                        + relationSymbol + " = null;" + _eol
                                        + portOrParameter + ".link("
                                        + relationSymbol + ");" + _eol);
                                // FIXME: What about multiple relations?
                                Iterator multiplePorts = relation
                                        .linkedPortList(port).iterator();
                                while (multiplePorts.hasNext()) {
                                    TypedIOPort multipleRemotePort = (TypedIOPort) multiplePorts
                                            .next();
                                    NamedObj multipleRemoteActor = multipleRemotePort
                                            .getContainer();
                                    String multipleRemoteActorSymbol = getCodeGenerator()
                                            .generateVariableName(
                                                    multipleRemoteActor)
                                                    + "_actor";
                                    Field multipleRemoteFoundPortField = _findFieldByPortName(
                                            multipleRemoteActor,
                                            multipleRemotePort.getName());
                                    PortParameter multiplePortParameter = (PortParameter) multipleRemoteActor
                                            .getAttribute(multipleRemotePort
                                                    .getName(),
                                                    PortParameter.class);
                                    code.append("if ("
                                            + multipleRemoteActorSymbol
                                            + " == null) {"
                                            + _eol
                                            + multipleRemoteActorSymbol
                                            + " = new "
                                            + multipleRemoteActor.getClass()
                                            .getName()
                                            + "($containerSymbol() , \""
                                            //+ multipleRemoteActorSymbol
                                            + multipleRemoteActor.getName()
                                            + "\");"
                                            + _eol
                                            // Set the displayName so that actors that call getDisplayName() get the same value.
                                            // Actors that generate random numbers often call getFullName(), then should call getDisplayName()
                                            // instead.
                                            + "        "
                                            + multipleRemoteActorSymbol
                                            + ".setDisplayName(\""
                                            + multipleRemoteActor.getName()
                                            + "\");"
                                            + _eol
                                            + "}"
                                            + _eol
                                            + "(("
                                            + multipleRemoteActor.getClass()
                                            .getName()
                                            + ")"
                                            + multipleRemoteActorSymbol
                                            + ")."
                                            + multipleRemoteFoundPortField
                                            .getName()
                                            + (multiplePortParameter != null ? ".getPort()"
                                                    : "") + ".link("
                                                    + relationSymbol + ");" + _eol);
                                }
                                code.append("}" + _eol);
                            } else {
                                if (port.isMultiport()) {
                                    // Needed for
                                    // $PTII/bin/ptcg -language java  $PTII/ptolemy/actor/lib/test/auto/WallClockTime.xml
                                    _headerFiles
                                    .add("ptolemy.actor.IORelation;");
                                    relationAssignment = "IORelation relation = (IORelation)";
                                    relationSetWidth = "relation.setWidth("
                                            + port.getWidth() + "); " + _eol;
                                } else if (remotePort.isMultiport()) {
                                    _headerFiles
                                    .add("ptolemy.actor.IORelation;");
                                    relationAssignment = "IORelation relation2 = (IORelation)";
                                    // Don't set the width to the width of the remote port if the remote port is a multiport. See
                                    // $PTII/bin/ptcg -language java  $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
                                    // Set the with to the width of the *port*, not the *remotePort*.
                                    relationSetWidth = "relation2.setWidth("
                                            + port.getWidth() + "); " + _eol;
                                }

                                // It is the responsibility of the custom actor
                                // with the output port to connect to the input
                                // port of the other custom actor.  This obviates
                                // the need for checking for the connection at
                                // runtime.

                                // At runtime, check to see that the remote port is not null.
                                // Some actors do magic in preinitialize() and before that time,
                                // the port is null.
                                // FIXME: could this conditional be moved up further so as to avoid
                                // unnecessary work?
                                code.append("{" + _eol + "if ((("
                                        + remoteActor.getClass().getName()
                                        + ")" + remoteActorSymbol + ")."
                                        + remoteFoundPortField.getName()
                                        + " != null) {" + _eol);
                                if (verbosityLevel > 3) {
                                    code.append("    System.out.println(\"C1 port: "
                                            + port.getFullName()
                                            + " remotePort: "
                                            + remotePort.getName()
                                            + " found: "
                                            + remoteFoundPortField.getName()
                                            + " "
                                            + port.isMultiport()
                                            + "\");"
                                            + _eol);
                                }
                                code.append(relationAssignment
                                        + "$containerSymbol().connect("
                                        + portOrParameter
                                        + ", "
                                        + "(("
                                        + remoteActor.getClass().getName()
                                        + ")"
                                        + remoteActorSymbol
                                        + ")."
                                        + remoteFoundPortField.getName()
                                        // FIXME: should portParameter be the remote port?
                                        + (portParameter != null ? ".getPort()"
                                                : "") + ");" + _eol
                                                + relationSetWidth + _eol + "}" + _eol
                                                + "}" + _eol);
                                //                         code.append("((" + remoteActor.getClass().getName()
                                //                                       + ")" + remoteActorSymbol + ")."
                                //                                 + remoteFoundPortField.getName()
                                //                                 // FIXME: should portParameter be the remote port?
                                //                                 + (portParameter != null ? ".getPort()" : "")
                                //                                 + ".setName(\"" + remotePort.getName() + "\");" + _eol);
                            }
                        }
                    } catch (NoSuchFieldException ex) {
                        if (!hasAutoConnectorRelation) {
                            // The port is not a field, it might be a PortParameter
                            // that whose name is not the same as the declared name.
                            // We check before we create it.  To test, use:
                            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterActorTest.xml
                            code.append("if ("
                                    + remoteActorSymbol
                                    + ".getPort(\""
                                    + AutoAdapter._externalPortName(
                                            remotePort.getContainer(),
                                            remotePort.getName())
                                            + "\") == null) {"
                                            + _eol
                                            + escapedCodegenPortNameSymbol
                                            + " = "
                                            + " new TypedIOPort("
                                            + remoteActorSymbol
                                            + ", \""
                                            + AutoAdapter._externalPortName(
                                                    remotePort.getContainer(),
                                                    remotePort.getName()).replace("$",
                                                            "\\u0024") + "\", "
                                                            + remotePort.isInput() + ", "
                                                            + remotePort.isOutput() + ");" + _eol);
                            if (port.isMultiport()) {
                                code.append(escapedCodegenPortNameSymbol
                                        + ".setMultiport(true);" + _eol);
                            }
                            code.append("}" + _eol);

                            portOrParameter = "(TypedIOPort)"
                                    + remoteActorSymbol
                                    + ".getPort(\""
                                    + AutoAdapter._externalPortName(
                                            remotePort.getContainer(),
                                            remotePort.getName()).replace("$",
                                                    "\\uu0024") + "\")";
                            if (!readingRemoteParameters) {
                                if (verbosityLevel > 3) {
                                    code.append("    System.out.println(\"B1\");"
                                            + _eol);
                                }
                                code.append("    if ("
                                        + escapedCodegenPortNameSymbol
                                        + " == null) {"
                                        + _eol
                                        + "        "
                                        + escapedCodegenPortNameSymbol
                                        + " = (TypedIOPort) "
                                        + remoteActorSymbol
                                        + ".getPort(\""
                                        + AutoAdapter._externalPortName(
                                                remotePort.getContainer(),
                                                remotePort.getName()) + "\");"
                                                + _eol + "        }" + _eol
                                                + "    if (!" + escapedCodegenPortName
                                                + ".equals(" + portOrParameter + ")) {"
                                                + _eol
                                                + "        $containerSymbol().connect("
                                                + escapedCodegenPortName + ", "
                                                + portOrParameter + ");" + _eol
                                                + "    }" + _eol);
                            } else {
                                if (verbosityLevel > 3) {
                                    code.append("    System.out.println(\"B2\");"
                                            + _eol);
                                }
                                code.append(_generateRemoteParameterConnections(
                                        readingRemoteParametersDepth,
                                        portOrParameter, escapedCodegenPortName));
                            }
                            if (port.isOutput()) {
                                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                                code.append("    (" + portOrParameter
                                        + ").setTypeEquals("
                                        + _typeToBaseType(port.getType())
                                        + ");" + _eol);
                            }
                        }
                    }
                } else {
                    // !remoteIsAutoAdaptered
                    if (!readingRemoteParameters) {
                        if (!connectedAlready
                                && connectedPorts
                                .get(escapedCodegenPortNameSymbol) == null) {
                            connectedPorts.put(escapedCodegenPortNameSymbol,
                                    portOrParameter);
                            if (verbosityLevel > 3) {
                                code.append("    System.out.println(\"A1\");"
                                        + _eol);
                            }
                            // FIXME: This does not handle ports that have multiple connections
                            // to the same port.
                            // FIXME: We should not have to test connectivity, we should know
                            // from the model whether we should call connect().
                            code.append(/*"if (!" + escapedCodegenPortNameSymbol + ".isDeeplyConnected("
                                          + portParameter + ")) {" + _eol
                                          + */
                                    "    $containerSymbol().connect("
                                    + escapedCodegenPortNameSymbol + ", "
                                    + portOrParameter + ");" + _eol
                                    /*+ "}" + _eol*/);
                        }
                    } else {
                        String outputPortName = "c"
                                + readingRemoteParametersDepth + "PortA";
                        if (!connectedAlready
                                && connectedPorts.get(outputPortName) == null) {
                            connectedPorts.put(outputPortName, portOrParameter);
                            if (verbosityLevel > 3) {
                                code.append("    System.out.println(\"A2\");"
                                        + _eol);
                            }
                            //code.append(_generateRemoteParameterConnections(readingRemoteParametersDepth,
                            //                portOrParameter,
                            //                escapedCodegenPortName));

                            //String escapedCodegenPortNameSymbol = _generatePtIOPortName(getComponent(), escapedCodegenPortName);
                            // FIXME: we might be able to get rid of this check now that we use connectedPorts
                            code.append("if (!c0PortA.isDeeplyConnected("
                                    + portOrParameter
                                    + ")) {"
                                    + _eol
                                    + "    $containerSymbol().connect("
                                    + outputPortName
                                    + ","
                                    + portOrParameter
                                    + ");"
                                    + _eol
                                    + "}"
                                    + _eol
                                    // Connect c0PortB if necessary.  See.
                                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport7.xml
                                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport.xml
                                    + "if (!c0PortB.isDeeplyConnected("
                                    + escapedCodegenPortNameSymbol + ")) {"
                                    + _eol + "    $containerSymbol().connect("
                                    + escapedCodegenPortNameSymbol
                                    + ", c0PortB);" + _eol + "}" + _eol);

                            if (readingRemoteParametersDepth == 1) {
                                code.append("c1.connect(c0PortA,c1PortA);"
                                        + _eol);
                            }
                        }
                    }

                    if (port.isOutput()) {
                        // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                        code.append("    (" + portOrParameter
                                + ").setTypeEquals("
                                + _typeToBaseType(port.getType()) + ");" + _eol);
                    }
                }
            }
        }

        code.append("}" + _eol);

        return code.toString();
    }

    /** Generate sanitized name for the given port.
     * This method is used when the
     * {@link ptolemy.cg.kernel.generic.program.ProgramCodeGenerator#variablesAsArrays}
     * parameter is true.
     * @param portName The sanitized name of the port
     * @return The name of the port as an array element.
     *  @exception IllegalActionException If the variablesAsArrays parameter
     *  of the code generator cannot be read.
     */
    private String _generatePtIOPortName(NamedObj container, String portName)
            throws IllegalActionException {
        // See also ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/actor/sched/StaticSchedulingDirector.java generatePortName()
        if (!((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            if (container != null) {
                return getCodeGenerator().generateVariableName(container) + "_"
                        + portName;
            } else {
                return "$actorSymbol(" + portName + ")";
            }
        } else {
            return getCodeGenerator().generatePtIOPortName(container, portName);
        }
    }

    /** Generate a name for the given actor.
     * This method is used when the
     * {@link ptolemy.cg.kernel.generic.program.ProgramCodeGenerator#variablesAsArrays}
     * parameter is true.
     * @param container The container of the TypedCompositeActor
     * @param actorName The sanitized name of the typedCompositeActor
     * @return The name of the TypedCompositeActor as an array element.
     *  @exception IllegalActionException If the variablesAsArrays parameter
     *  of the code generator cannot be read.
     */
    private String _generatePtTypedCompositeActorName(NamedObj container,
            String actorName) throws IllegalActionException {
        // See also ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/actor/sched/StaticSchedulingDirector.java generatePortName()
        if (!((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            if (container != null) {
                return getCodeGenerator().generateVariableName(container); //+ "_" + actorName;
            } else {
                return "$actorSymbol(" + actorName + ")";
            }
        } else {
            return getCodeGenerator().generatePtTypedCompositeActorName(
                    container, actorName);
        }
    }

    /**
     * Return the code that sends data from the codegen variable to
     * the actor port.
     *  @param actorPortName The name of the Actor port to which data
     *  will be sent.
     *  @param codegenPortName The name of the port on the codegen side.
     *  For non-multiports, actorPortName and codegenPortName are the same.
     *  For multiports, codegenPortName will vary according to channel number
     *  while actorPortName will remain the same.
     * @param type The type of the port.
     * @param channel The channel number.
     * For non-multiports, the channel number will be 0.
     * @exception IllegalActionException If thrown while reading the
     * variablesAsArrays parameter of the code generator.
     */
    private String _generateSendInside(String actorPortName,
            String codegenPortName, Type type, int channel)
                    throws IllegalActionException {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        codegenPortName = TemplateParser.escapePortName(codegenPortName);
        String codegenPortNameSymbol = _generatePtIOPortName(getComponent(),
                codegenPortName);
        if (type instanceof ArrayType) {

            ArrayType array = (ArrayType) type;

            String javaElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType());
            String codeGenElementType = javaElementType.replace("Integer",
                    "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());
            // FIXME: do we really need a separate symbol here?  This is inside a block.
            String ptolemyDataSymbol = "$actorSymbol(" + actorPortName
                    + "_ptolemyData)";
            return "{"
            + _eol
            // Get the codegen data
            + " Token codeGenData = $get("
            + actorPortName
            // For non-multiports "". For multiports, #0, #1 etc.
            + (channel == 0 ? "" : "#" + channel)
            + ");"
            + _eol

            // Create a token to send
            + codeGenElementType
            + "Token [] "
            + ptolemyDataSymbol
            + " = new "
            + codeGenElementType
            + "Token [((Array)codeGenData.getPayload()).size];"
            + _eol

            // Copy from the codegen data to the Ptolemy data
            + " for (int i = 0; i < ((Array)codeGenData.getPayload()).size; i++) {"
            + _eol + "   " + ptolemyDataSymbol + "[i] = new "
            + codeGenElementType + "Token(((" + javaElementType
            + ")(Array_get(codeGenData, i).getPayload()))."
            + targetElementType + "Value());" + _eol + " }"
            + _eol

            // Set the type.
            + "   " + codegenPortNameSymbol + ".setTypeEquals("
            + _typeToBaseType(type) + ");" + _eol
            // Output our newly constructed token
            + codegenPortNameSymbol + ".sendInside(0, new ArrayToken("
            + ptolemyDataSymbol + "));" + _eol + "}" + _eol;
        } else if (type == BaseType.COMPLEX) {
            return
                    // Set the type.
                    "   " + codegenPortNameSymbol + ".setTypeEquals("
                    + _typeToBaseType(type) + ");" + _eol
                    // Send data to the actor.
                    + "    " + codegenPortNameSymbol
                    + ".sendInside(0, new "
                    // Refer to the token by the full class name and obviate the
                    // need to manage imports.
                    + type.getTokenClass().getName()
                    // Get the real portion of the Complex number.
                    + "(new Complex(((ComplexCG)($get(" + actorPortName
                    // For non-multiports "". For multiports, #0, #1 etc.
                    + (channel == 0 ? "" : "#" + channel) + ")).payload).real,"
                    // Get the imaginary portion of the Complex number.
                    + "((ComplexCG)($get(" + actorPortName
                    // For non-multiports "". For multiports, #0, #1 etc.
                    + (channel == 0 ? "" : "#" + channel)
                    + ")).payload).imag))" + ");" + _eol;
        } else if (type.equals(BaseType.OBJECT)) {
            _headerFiles.add("ptolemy.data.ObjectToken;");
            return "{"
            + _eol
            // Set the type.
            + codegenPortNameSymbol
            + ".setTypeEquals("
            + _typeToBaseType(type)
            + ");"
            + _eol
            + "Token cgToken = $get("
            + actorPortName
            + ")"
            // For non-multiports "". For multiports, #0, #1 etc.
            + (channel == 0 ? "" : "#" + channel)
            + ";"
            + _eol
            + "ObjectToken objectToken = null;"
            + _eol
            + "if (cgToken == null) {"
            + _eol
            + "   objectToken = new ObjectToken(cgToken);"
            + _eol
            + "} else {"
            + _eol
            + "   objectToken = new ObjectToken(((ObjectCG)cgToken.payload).object);"
            + _eol + "}" + _eol
            // Send data to the actor.
            + "    " + codegenPortNameSymbol
            + ".sendInside(0, objectToken);" + _eol + "}" + _eol;
        } else {
            return
                    // Set the type.
                    codegenPortNameSymbol + ".setTypeEquals(" + _typeToBaseType(type)
                    + ");" + _eol
                    // Send data to the actor.
                    + "    " + codegenPortNameSymbol + ".sendInside(0, new "
                    // Refer to the token by the full class name and obviate the
                    // need to manage imports.
                    + type.getTokenClass().getName() + "($get(" + actorPortName
                    // For non-multiports "". For multiports, #0, #1 etc.
                    + (channel == 0 ? "" : "#" + channel) + ")));" + _eol;
        }
    }

    /** Return the code to set a parameter.
     *  @param parameter The parameter
     *  @param parameterName The sanitized parameter name that has
     *  been processed by StringUtilities.sanitizeName() an had $
     *  replaced with Dollar
     *  @param actorClassName The name of the actor class.
     *  @param parameterValue The value of the parameter returned by
     *  _sanitizeParameterValue()
     */
    private String _generateSetParameter(Settable parameter,
            String parameterName, String actorClassName, String parameterValue) {
        String setParameter = "";
        if (parameter instanceof Parameter) {
            setParameter = "    Parameter " + parameterName + " = (("
                    + actorClassName + ")$actorSymbol(actor))." + parameterName
                    + ";" + _eol + "    " + parameterName + ".setExpression(\""
                    // Handle custom actors that are in non-toplevel containers that refer to variables that have dollar signs in their names.
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterStringParameter.xml
                    + parameterValue.replace("$", "\\u0024") + "\");" + _eol;
        } else {
            if (parameter instanceof ptolemy.kernel.util.StringAttribute) {
                setParameter = "    ptolemy.kernel.util.StringAttribute "
                        + parameterName + " = ((" + actorClassName
                        + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                        + "    " + parameterName + ".setExpression(\""
                        + parameterValue.replace("$", "\\u0024") + "\");"
                        + _eol;
            }
        }
        return "{ " + _eol + setParameter + "    ((" + actorClassName
                + ")$actorSymbol(actor)).attributeChanged(" + parameterName
                + ");" + _eol + "}" + _eol;
    }

    /** If the name of the container is "Electricity", then
     *  find any StringConsts that are attached to the parent
     *  and generate code.
     *  @param composite
     *  @return Code that creates any StringConsts
     */
    private String _generateStringConsts(NamedObj composite,
            String actorSymbol, String containerSymbol)
                    throws IllegalActionException {
        if (!composite.getContainer().getName().equals("Electricity")) {
            return "";
        }
        StringBuffer code = new StringBuffer();
        Iterator entityPorts = ((Entity) composite).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                StringBuffer stringConstantCode = new StringBuffer();
                Iterator remotePorts = castPort.connectedPortList().iterator();
                while (remotePorts.hasNext()) {
                    TypedIOPort remotePort = (TypedIOPort) remotePorts.next();
                    if (remotePort.getContainer() instanceof StringConst) {
                        StringConst stringConstant = (StringConst) remotePort
                                .getContainer();
                        if (stringConstantCode.length() == 0) {
                            _headerFiles.add("ptolemy.actor.lib.StringConst;");
                            stringConstantCode.append("{" + _eol
                                    + "StringConst stringConst = null;" + _eol
                                    + "TypedIOPort port = null;" + _eol);
                        }
                        stringConstantCode
                        .append("stringConst = new StringConst("
                                + containerSymbol + ", \""
                                + stringConstant.getName() + "\");"
                                + _eol
                                + "stringConst.value.setExpression(\""
                                + stringConstant.value.getExpression()
                                + "\");" + _eol
                                + "port = new TypedIOPort("
                                + actorSymbol + ", \""
                                + castPort.getName() + "\", "
                                + castPort.isInput() + ", "
                                + castPort.isOutput() + ");" + _eol
                                + containerSymbol
                                + ".connect(port, stringConst."
                                + remotePort.getName() + ");" + _eol);
                    }
                }
                if (stringConstantCode.length() != 0) {
                    stringConstantCode.append("}" + _eol);
                    code.append(stringConstantCode);
                }
            }

        }
        return code.toString();
    }

    /** If the name of the container is "Electricity", then
     *  find any StringConsts that are attached to the parent
     *  and generate code.
     *  @param composite
     *  @return Code that creates any StringConsts
     */
    private String _generateElectricityConnections(NamedObj composite,
            String actorSymbol, String containerSymbol)
                    throws IllegalActionException {
        if (!composite.getContainer().getName().equals("Electricity")) {
            return "";
        }
        StringBuffer code = new StringBuffer();
        Iterator entityPorts = ((Entity) composite).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                StringBuffer connectionCode = new StringBuffer();
                Iterator remotePorts = castPort.connectedPortList().iterator();
                while (remotePorts.hasNext()) {
                    TypedIOPort remotePort = (TypedIOPort) remotePorts.next();
                    if (remotePort.getContainer() instanceof TypedCompositeActor) {
                        // Create connections to any other TypedCompositeActors in the Electrical composite
                        TypedCompositeActor remoteActor = (TypedCompositeActor) remotePort
                                .getContainer();
                        String remoteActorSymbol = getCodeGenerator()
                                .generateVariableName(remoteActor);
                        TypedCompositeActor remoteActorContainer = (TypedCompositeActor) remoteActor
                                .getContainer();
                        //String remoteActorContainerSymbol = getCodeGenerator().generateVariableName(remoteActorContainer);
                        String remoteActorContainerSymbol = _generatePtTypedCompositeActorName(
                                remoteActorContainer,
                                remoteActorContainer.getName());
                        connectionCode
                        .append("{"
                                + _eol
                                + "TypedCompositeActor e0 = (TypedCompositeActor)"
                                + _generateActorInstantiation(
                                        remoteActor, remoteActorSymbol,
                                        remoteActorContainerSymbol,
                                        false, true, false)
                                        + "TypedIOPort e0PortA = (TypedIOPort)e0.getPort(\""
                                        + remotePort.getName()
                                        + "\");"
                                        + _eol
                                        + "if (e0PortA == null) {"
                                        + _eol
                                        + "e0PortA = new TypedIOPort(e0, \""
                                        + remotePort.getName()
                                        + "\", "
                                        + remotePort.isInput()
                                        + ", "
                                        + remotePort.isOutput()
                                        + ");"
                                        + _eol
                                        + "}"
                                        + _eol

                                        + "TypedIOPort e0PortB = (TypedIOPort)"
                                        + actorSymbol
                                        + ".getPort(\""
                                        + castPort.getName()
                                        + "\");"
                                        + _eol
                                        + "if (e0PortB == null) {"
                                        + _eol
                                        + "e0PortB = new TypedIOPort("
                                        + actorSymbol
                                        + ", \""
                                        + castPort.getName()
                                        + "\", "
                                        + castPort.isInput()
                                        + ", "
                                        + castPort.isOutput()
                                        + ");"
                                        + _eol
                                        + "}"
                                        + _eol

                                        + "if (!e0PortA.isDeeplyConnected(e0PortB)) {"
                                        + _eol
                                        + "((TypedCompositeActor)e0.getContainer()).connect(e0PortA, e0PortB);"
                                        + _eol + "}" + _eol);
                    }
                }
                if (connectionCode.length() != 0) {
                    connectionCode.append("}" + _eol);
                    code.append(connectionCode);
                }
            }

        }
        return code.toString();
    }

    //     /** If the name of the container is "Electricity", then
    //      *  find any StringConsts that are attached to the parent
    //      *  and generate code.
    //      *  @param composite
    //      *  @return Code that creates any StringConsts
    //      */
    //     private String _generateStringConstDeclarations(NamedObj composite) {
    //         if (!composite.getContainer().getName().equals("Electricity")) {
    //             return "";
    //         }
    //         StringBuffer code = new StringBuffer();
    //         Iterator entityPorts = ((Entity)composite).portList().iterator();
    //         while (entityPorts.hasNext()) {
    //             ComponentPort insidePort = (ComponentPort) entityPorts.next();
    //             if (insidePort instanceof TypedIOPort) {
    //                 TypedIOPort castPort = (TypedIOPort) insidePort;
    //                 StringBuffer stringConstantCode = new StringBuffer();
    //                 Iterator remotePorts = castPort.connectedPortList().iterator();
    //                 while (remotePorts.hasNext()) {
    //                     TypedIOPort remotePort = (TypedIOPort)remotePorts.next();
    //                     if (remotePort.getContainer() instanceof StringConst) {
    //                         if (!_containersDeclared.contains(remotePort.getContainer())) {
    //                             _containersDeclared.add(remotePort.getContainer());
    //                         }
    //                     }
    //                 }
    //             }

    //         }
    //         return code.toString();
    //     }

    /** Return true if the port has a linked relation whose name starts
     *  with "autoConnector".
     *  @param port The port to be checked.
     *  @return true if the port has a relation that starts with the string
     *  "autoConnector".
     *  @exception IllegalActionException If the verbosity parameter
     *  of the code generator cannot be read.
     */
    private boolean _hasAutoConnectorRelation(TypedIOPort port)
            throws IllegalActionException {
        Iterator relations = port.linkedRelationList().iterator();
        while (relations.hasNext()) {
            Relation relation = (Relation) relations.next();
            if (relation.getName().startsWith("autoConnector")) {
                int verbosityLevel = ((IntToken) getCodeGenerator().verbosity
                        .getToken()).intValue();
                if (verbosityLevel > 0) {
                    System.out
                    .println("Partially skipping "
                            + port
                            + " because it has a relation that starts with 'autoConnector'");
                }
                return true;
            }
        }
        return false;
    }

    /** Return true if the port is connected to a TypedCompositeActor that has Parameters.
     *  Only ports that are input multiports are checked.
     *  @param port The port.
     *  @param channelNumber The channel number of the port to be checked.
     *  @param sourceOrSinkPorts The source or sink ports connected to the port.
     *  @return True if the port is connected to a TypedCompositeActor that has Parameters.
     */
    private boolean _isReadingRemoteParameters(TypedIOPort port,
            int channelNumber, List sourceOrSinkPorts)
                    throws IllegalActionException {
        int verbosityLevel = ((IntToken) getCodeGenerator().verbosity
                .getToken()).intValue();

        if (port.isInput() && port.isMultiport()) {

            // FIXME: We should annotate the very few ports that are
            // used by actors to read parameters in remote actors.

            NamedObj remoteActor = null;
            NamedObj remoteContainer = null;
            try {
                remoteActor = ((NamedObj) sourceOrSinkPorts.get(channelNumber))
                        .getContainer();
                remoteContainer = remoteActor.getContainer();
            } catch (Exception ex) {
                System.out.println(port.getContainer().getContainer()
                        .exportMoML());
                throw new IllegalActionException(port, "Failed to get channel "
                        + channelNumber + " of sourcePorts "
                        + sourceOrSinkPorts.size() + " width: "
                        + port.getWidth());
            }
            if (verbosityLevel > 14) {
                System.out.println("_isReadingRemoteParameters: 0 "
                        + getComponent().getFullName() + " "
                        + port.getFullName() + " " + channelNumber + " width: "
                        + port.getWidth() + " " + port.sourcePortList().size()
                        + " " + remoteActor.getFullName() + " "
                        + remoteContainer.getFullName());
            }
            // If the custom actor is connected to a CompositeActorA
            // inside a CompositeActorB, then we want to check
            // CompositeActorB for Parameters.
            while (remoteContainer != null) {
                if (remoteContainer instanceof TypedCompositeActor) {
                    // If the remoteContainer contains any actors that would be AutoAdaptered,
                    // then we need not do anything special, the parameters will be
                    // created for us.  Thus, this method returns false
                    boolean foundAutoAdapteredUpstreamActor = false;

                    if (!isAutoAdaptered(remoteActor)) {
                        // If the remote actor is not auto adaptered,
                        // but it is connected to an auto adaptered
                        // actor in the same container, then consider
                        // it be reading remote parameters and return
                        // true.

                        // FIXME: This only checks one upstream actor,
                        // we should check all upstream actors in the
                        // container of the remote actor.

                        // Tests:
                        //$PTII/bin/ptcg -language java  $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterStringParameter.xml
                        //$PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ReadPMultiport2AutoD.xml
                        Iterator remoteActorSourcePorts = ((Actor) remoteActor)
                                .inputPortList().iterator();
                        done: while (remoteActorSourcePorts.hasNext()) {
                            IOPort remotePort = (IOPort) remoteActorSourcePorts
                                    .next();
                            Iterator remotePorts = remotePort.sourcePortList()
                                    .iterator();
                            while (remotePorts.hasNext()) {
                                NamedObj upstreamActor = ((NamedObj) remotePorts
                                        .next()).getContainer();
                                if (verbosityLevel > 14) {
                                    System.out
                                    .println("_isReadingRemoteParameters: upstream actor: "
                                            + upstreamActor
                                            .getFullName());
                                }
                                if (upstreamActor.getContainer().equals(
                                        remoteActor.getContainer())
                                        && isAutoAdaptered(upstreamActor)) {
                                    foundAutoAdapteredUpstreamActor = true;
                                    // We found one, so stop searching
                                    break done;
                                }
                            }
                        }
                    }
                    if (!foundAutoAdapteredUpstreamActor) {
                        // Oddly, if we found an upstream auto
                        // adaptered actor and there are parameters in
                        // the container.  However, if the remote
                        // actor does not have an upstream auto
                        // adaptered actor, then return false because
                        // we will catch these parameters elsewhere.
                        Iterator entities = ((TypedCompositeActor) remoteContainer)
                                .allAtomicEntityList().iterator();
                        while (entities.hasNext()) {
                            NamedObj namedObj = (NamedObj) entities.next();
                            if (isAutoAdaptered(namedObj)) {
                                if (namedObj.getClass().getName()
                                        .endsWith("SigmoidalActivation")) {
                                    if (verbosityLevel > 14) {
                                        System.out
                                        .println("_isReadingRemoteParameters: "
                                                + namedObj.getClass()
                                                .getName()
                                                + " "
                                                + namedObj
                                                .getFullName()
                                                + " is autoadaptered, NOT returning false");
                                    }
                                } else {
                                    if (verbosityLevel > 14) {
                                        System.out
                                        .println("_isReadingRemoteParameters: "
                                                + namedObj.getClass()
                                                .getName()
                                                + " "
                                                + namedObj
                                                .getFullName()
                                                + " is autoadaptered, returning false");
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                    // FIXME: we could be smarter about *which* parameters matter here.
                    List<Parameter> parameters = remoteContainer
                            .attributeList(Parameter.class);
                    if (parameters.size() > 0) {
                        // Check for common parameters.
                        int count = parameters.size();
                        for (Parameter parameter : parameters) {
                            String name = parameter.getName();
                            if (_skipVariable(name)) {
                                count--;
                            }
                        }
                        if (count != 0) {
                            // We have parameters in the container, so return true.
                            if (verbosityLevel > 14) {
                                System.out
                                .println("_isReadingRemoteParameters: "
                                        + remoteContainer.getFullName()
                                        + " return True, parameters: "
                                        + parameters);
                            }
                            return true;
                        }
                    }
                }
                if (remoteContainer.getContainer() == getComponent()
                        .getContainer()) {
                    // Stop because we are at the same level as the container of the component.
                    break;
                }
                remoteContainer = remoteContainer.getContainer();
            }

        }
        if (verbosityLevel > 14) {
            System.out.println("_isReadingRemoteParameters:"
                    + getComponent().getFullName() + " " + port.getFullName()
                    + " " + channelNumber + " returning FALSE");
        }
        return false;
    }

    /** Return a sanitized version of the value of the parameter.
     *  @param parameter The parameter.
     *  @return If the parameter is a Variable, do substitution for
     *  variables like $PTII and handle double quotes specially.
     *  @exception IllegalActionException If thrown while reading the value
     *  of the parameter.
     */
    private String _sanitizeParameterValue(Settable parameter)
            throws IllegalActionException {
        // FIXME: handle multiline values
        String parameterValue = "";
        if (parameter instanceof Variable
                && ((Variable) parameter).getToken() != null) {
            // Evaluate things like $PTII
            parameterValue = ((Variable) parameter).getToken().toString();
            if (((Variable) parameter).isStringMode()) {
                if (parameterValue.startsWith("\"")
                        && parameterValue.endsWith("\"")) {
                    // This is needed by
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/string/test/auto/StringFunction.xml
                    parameterValue = parameterValue.substring(1,
                            parameterValue.length() - 1);
                }
            }
        } else {
            parameterValue = parameter.getExpression();
        }
        // Don't escape strings here, otherwise StringMatch patterns fail because \\D gets converted
        // to \D.  We need a literal string parameter for use with patterns.  See
        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/string/test/auto/StringMatches2.xml
        //parameterValue = StringUtilities.escapeString(parameterValue);

        // Instead, we escape double quotes, which is needed by
        //$PTII/bin/ptcg -language java  $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterTwoActors.xml
        parameterValue = parameterValue.replaceAll("\"", "\\\\\"");

        return parameterValue;
    }

    /** Return true if the variable should be skipped.
     *  @param variableName The variable name.
     *  @return true if the variable should be skipped.
     */
    private boolean _skipVariable(String variableName) {
        if (variableName.equals("_windowProperties")
                || variableName.startsWith("_vergil")
                || variableName.equals("disableBackwardTypeInference")
                || variableName.equals("enableBackwardTypeInference")) {
            return true;
        }
        return false;
    }

    /**
     * Given a type, generate the Java code that represents that type.
     * Arrays of ints are converted into new ArrayType(BaseType.INT).
     * Convert a simple type (int) into the corresponding BaseType
     * static variable (INT)
     */
    private String _typeToBaseType(Type type) {
        if (type instanceof ArrayType) {
            return "new ArrayType("
                    + _typeToBaseType(((ArrayType) type)
                            .getDeclaredElementType()) + ")";
        } else if (type instanceof ObjectType) {
            return "BaseType.OBJECT";
        }
        return "BaseType." + type.toString().toUpperCase(Locale.getDefault());
    }

    /** Return the name of the xxxValue() method, such as doubleValue().
     *  @return the name of the xxxValue() method
     */
    private String _valueMethodName(Type type) {
        String typeName = type.toString().toLowerCase(Locale.getDefault());
        if (typeName.equals("object(null)")) {
            typeName = "get";
        }
        return typeName + "Value()";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** A map of methods that instantiate actors. The key is the
     *  method name, the value is the method body.
     */
    private Map<String, String> _actorInstantiationMethods = new HashMap<String, String>();

    /** If {@link #isAutoAdaptered(NamedObj)} is called, then
     *  {@link #getAutoAdapter(GenericCodeGenerator, Object)} sets
     *  checks this variable to determine whether to actually
     *  create the AutoAdapter.  When _isAutoAdapter() is called,
     *  getAutoAdapter() will not actually create the AutoAdapter
     *  and will instead set _wouldBeAutoAdapted and return null.
     */
    private static boolean _checkingAutoAdapter = false;

    /** The set of containers that have been declared.
     *  The custom actors are put into a common container so that
     *  if the custom actors have connections, they are connected directly.
     */
    private static Set<NamedObj> _containersDeclared = new HashSet<NamedObj>();

    /** Set of imports, where each element is a dot separate string,
     *  that ends in a semicolon, such as ""ptolemy.actor.TypeAttribute;".
     */
    private Set<String> _headerFiles = new HashSet<String>();

    /** True if _autoAdapterSetPrivateParameter() should be declared. */
    private boolean _needAutoAdapterSetPrivateParameter = false;

    /** The toplevel for which we generated code to call resolveTypes().
     *  This is static, but changes for each model.
     */
    private static NamedObj _toplevelTypesResolved = null;

    /** If {@link #isAutoAdaptered(NamedObj)} is called, then
     *  {@link #getAutoAdapter(GenericCodeGenerator, Object)} sets
     *  this variable to true if the Object would use an AutoAdapter
     *  for code generation.  This is used to put two or more
     *  custom actors in to the same container.
     */
    private static boolean _wouldBeAutoAdapted = false;

}
