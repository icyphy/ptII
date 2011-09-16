/* A code generator adapter that is auto generated and calls actor code.

 Copyright (c) 2010-2011 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
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
 *  @since Ptolemy II 8.1
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
    public String generatePostfireCode() throws IllegalActionException {
        return _generateExecutionCode("postfire");
    }

    /**
     * Generate the prefire code.
     * @return Code that calls prefire() on the inner actor.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public String generatePrefireCode() throws IllegalActionException {
        return _generateExecutionCode("prefire");
    }

    /**
     * Generate the preinitialize code.
     * <p>Generate code that declares the container, actor and ports.
     * @return A string of the preinitialize code for the adapter.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "TypedAtomicActor $actorSymbol(actor);" + _eol);
        // Declare each container only once.
        if (!_containersDeclared.contains(getComponent().getContainer())) {
            _containersDeclared.add(getComponent().getContainer());
            code.append("TypedCompositeActor $containerSymbol();" + _eol);
        }
        // Handle inputs and outputs on a per-actor basis.
        // There is very similar code in generatePreinitializeMethodBodyCode()
        Iterator entityPorts = ((Entity) getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                if (!castPort.isOutsideConnected()) {
                    continue;
                }
                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    code.append("TypedIOPort $actorSymbol(" + name + ");"
                            + _eol);
                } else {
                    // FIXME: We instantiate a separate external port for each channel
                    // of the multiport.  Could we just connect directly to the channels
                    // of the multiport?  The problem I had was that the receivers are
                    // not created if I connect directly to the channels.

                    // Use castPort.getName() and get the real name of the port.
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                    IOPort actorPort = (IOPort) (((Entity) getComponent())
                            .getPort(castPort.getName()));

                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name
                                + "Source" + i + ");" + _eol);
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name + "Sink"
                                + i + ");" + _eol);
                    }
                }
            }
        }
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
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        // Use the full class name so that we don't have to import the
        // actor.  If we import the actor, then we cannot have model
        // names with the same name as the actor.
        String actorClassName = getComponent().getClass().getName();

        // Generate code that creates the hierarchy.

        StringBuffer containmentCode = new StringBuffer();

        //NamedObj child = getComponent();
        NamedObj parentContainer = getComponent().getContainer();
        NamedObj grandparentContainer = parentContainer.getContainer();

        if (grandparentContainer == null) {
            // The simple case, where the actor is in the top level and
            // we only need to create a TypedCompositeActor container.
            containmentCode.append("    $containerSymbol() = new "
                    + getComponent().getContainer().getClass().getName()
                    // Some custom actors such as ElectricalOverlord
                    // want to be in a container with a particular name.
                    + "(_toplevel, \"" + getComponent().getName() + "\");"
                    + _eol);
        } else {
            // This wacky.  What we do is move up the hierarchy and instantiate
            // TypedComposites as necessary and *insert* the appropriate code into
            // the StringBuffer.  When we get to the top, we *append* code that
            // inserts the hierarchy into the toplevel and that creates the container
            // for the actor.  At runtime, when we are generating the hierarchy,
            // we need to avoid generating duplicate entities (entities that
            // already exist in a container that has more than one actor handled
            // by AutoAdapter).

            //            while (grandparentContainer != null && grandparentContainer.getContainer() != null && grandparentContainer.getContainer().getContainer() != null) {
            while (parentContainer != null
                    && parentContainer.getContainer() != null /* && parentContainer.getContainer().getContainer() != null*/) {
                containmentCode.insert(0,
                //                         "temporaryContainer = (TypedCompositeActor)cgContainer.getEntity(\"" + grandparentContainer.getName() + "\");" + _eol
                //                         + "if (temporaryContainer == null) { " + _eol
                //                         + "    cgContainer = new "
                //                         // Use the actual class of the container, not TypedCompositeActor.
                //                         + grandparentContainer.getClass().getName()
                //                         + "(cgContainer, \"" + grandparentContainer.getName() + "\");" + _eol
                //                         + "} else {" + _eol
                //                         + "    cgContainer = temporaryContainer;" + _eol
                //                         + "}" + _eol);

                        "temporaryContainer = (TypedCompositeActor)cgContainer.getEntity(\""
                                + parentContainer.getName()
                                + "\");"
                                + _eol
                                + "if (temporaryContainer == null) { "
                                + _eol
                                + "    cgContainer = new "
                                // Use the actual class of the container, not TypedCompositeActor.
                                + parentContainer.getClass().getName()
                                + "(cgContainer, \""
                                + parentContainer.getName() + "\");" + _eol
                                + "} else {" + _eol
                                + "    cgContainer = temporaryContainer;"
                                + _eol + "}" + _eol);
                //child = parentContainer;
                parentContainer = parentContainer.getContainer();
                //parentContainer = grandparentContainer;
                //grandparentContainer = grandparentContainer.getContainer();
            }

            //NamedObj container = grandparentContainer;
            //if (container == null) {
            //    container = parentContainer;
            //}
            containmentCode.insert(
                    0,
                    "{"
                            + _eol
                            + getCodeGenerator().comment(
                                    getComponent().getFullName()) + _eol
                            + "TypedCompositeActor cgContainer = _toplevel;"
                            + _eol
                            + "TypedCompositeActor temporaryContainer = null;"
                            + _eol);
            //                     + "if ((cgContainer = (TypedCompositeActor)_toplevel.getEntity(\"" + container.getName() + "\")) == null) { " + _eol
            //                     + "   cgContainer = new "
            //                     + container.getClass().getName() + "(_toplevel, \""
            //                     + container.getName()  + "\");" + _eol
            //                     + "}" + _eol);

            containmentCode
                    .append("    if ((temporaryContainer = (TypedCompositeActor)cgContainer.getEntity(\""
                            + getComponent().getContainer().getName()
                            + "_container"
                            + "\")) == null) {"
                            + _eol
                            + "     $containerSymbol() = new "
                            + getComponent().getContainer().getClass()
                                    .getName()
                            // Some custom actors such as ElectricalOverlord
                            // want to be in a container with a particular name.
                            + "(cgContainer, \""
                            + getComponent().getContainer().getName()
                            + "_container" + "\");" + _eol
                            //+ "    } else {" + _eol
                            //+ "       $containerSymbol() = cgContainer;" + _eol
                            + "    }" + _eol + "}" + _eol);

            // Instantiate Variables for those actors that access parameters in their container.
            // $PTII/bin/ptcg -language java auto/ReadParametersInContainerTest.xml
            Iterator variables = getComponent().getContainer()
                    .attributeList(Variable.class).iterator();
            while (variables.hasNext()) {
                if (!_importedVariable) {
                    _importedVariable = true;
                    _headerFiles.add("ptolemy.data.expr.Variable;");
                }
                Variable variable = (Variable) variables.next();
                String variableName = StringUtilities.sanitizeName(
                        variable.getName()).replaceAll("\\$", "Dollar");
                if (variableName.charAt(0) == '_') {
                    if (variableName.equals("_windowProperties")
                            || variableName.startsWith("_vergil")) {
                        // No need to create _windowProperties,  _vergilSize,
                        // _vergilZoomFactor, or _vergilCenter variables.
                        continue;
                    }
                }
                // FIXME: optimize this by creating a method that only creates
                // the variable if it is not already set.  The reason we would
                // have duplicate variables is because we have two custom actors
                // in one container and the container has Parameters.
                containmentCode
                        .append("if ($containerSymbol().getAttribute(\""
                                + variable.getName() + "\") == null) {" + _eol
                                + "   new Variable($containerSymbol(), \""
                                + variable.getName() + "\").setExpression(\""
                                + variable.getExpression() + "\");" + _eol
                                + "}" + _eol);
            }

            // Whew.
        }

        StringBuffer code = new StringBuffer();
        // Generate code that creates and connects each port.
        // There is very similar code in generatePreinitializeCode();
        Iterator entityPorts = ((Entity) getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof TypedIOPort) {
                TypedIOPort castPort = (TypedIOPort) insidePort;
                if (!castPort.isOutsideConnected()) {
                    continue;
                }
                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    // Only instantiate ports that are outside connected and avoid
                    // "Cannot put a token in a full mailbox."  See
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/ActorWithPrivateParameterTest.xml
                    code.append(_generatePortInstantiation(name,
                            castPort.getName(), castPort, 0 /* channelNumber */));
                } else {
                    // Multiports.  Not all multiports have port names
                    // that match the field name. See
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml

                    //TypedIOPort actorPort = (TypedIOPort)(((Entity)getComponent()).getPort(castPort.getName()));

                    TypedIOPort actorPort = null;
                    try {
                        Field foundPortField = _findFieldByPortName(
                                getComponent(), castPort.getName());
                        actorPort = (TypedIOPort) foundPortField
                                .get(getComponent());
                        code.append("    (("
                                + getComponent().getClass().getName()
                                + ")$actorSymbol(actor))."
                                + foundPortField.getName() + ".setTypeEquals("
                                + _typeToBaseType(actorPort.getType()) + ");"
                                + _eol);

                    } catch (Throwable throwable) {
                        //throw new IllegalActionException(castPort, throwable,
                        //        "Could not find port " + castPort.getName());
                        actorPort = (TypedIOPort) ((Entity) getComponent())
                                .getPort(castPort.getName());
                        code.append("new TypedIOPort($containerSymbol(), \""
                                //+ actorPort.getName().replace("\\", "\\\\") + "\", "
                                + AutoAdapter._externalPortName(
                                        actorPort.getContainer(),
                                        actorPort.getName())
                                + "\", "
                                + actorPort.isInput() + ", "
                                + actorPort.isOutput()
                                + ").setMultiport(true);" + _eol);
                    }

                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        if (actorPort.isOutsideConnected()) {
                            code.append(_generatePortInstantiation(name, name
                                    + "Source" + i, actorPort, i));
                        }
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        if (actorPort.isOutsideConnected()) {
                            code.append(_generatePortInstantiation(name, name
                                    + "Sink" + i, actorPort, i));
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
                                        insidePort.getName())
                            + "\"), \"inputType\");" + _eol
                            + "_type.setExpression(\""
                            + typeAttribute.getExpression() + "\");" + _eol
                            + "}" + _eol);
                }
            }
        }

        code.append(generateParameterCode());

        String[] splitInitializeConnectionCode = getCodeGenerator()._splitBody(
                "_AutoAdapterI_", code.toString());

        // Stitch every thing together.  We do this last because of
        // the _splitBody() calls.
        String result = getCodeGenerator().comment(
                "AutoAdapter._generateInitalizeCode() start")
                + "try {"
                + _eol
                //+ "    $containerSymbol() = new TypedCompositeActor();" +_eol
                + "    instantiateToplevel(\""
                + getComponent().toplevel().getName()
                + "\");"
                + _eol
                + containmentCode
                // If there are two custom actors in one container, then
                // we may have already created the actor.
                + "    if ($actorSymbol(actor) == null) {"
                + _eol
                + "        $actorSymbol(actor) = new "
                + actorClassName
                + "($containerSymbol(), \"$actorSymbol(actor)\");"
                + _eol
                // Set the displayName so that actors that call getDisplayName() get the same value.
                // Actors that generate random numbers often call getFullName(), then should call getDisplayName()
                // instead.
                + "        $actorSymbol(actor).setDisplayName(\""
                + getComponent().getName()
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
                //+ "    $containerSymbol().setManager(new ptolemy.actor.Manager(\"manager\"));" + _eol
                //+ "    $containerSymbol().preinitialize();" + _eol
                //+ getCodeGenerator().comment("FIXME: Don't call _toplevel.preinitialize() for each AutoAdapter")
                //+ "    _toplevel.preinitialize();" + _eol
                + "} catch (Exception ex) {"
                + _eol
                + "    throw new RuntimeException(\"Failed to create $actorSymbol(actor))\", ex);"
                + _eol + "}" + _eol;
        return processCode(result);
    }

    /**
     * Generate the wrapup code.
     * @return Code that calls wrapup() on the inner actor.
    *  @exception IllegalActionException If illegal macro names are found.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return _generateExecutionCode("wrapup");
    }

    /**
     * Create a new adapter to a preexisting actor that presumably does
     * not have a code generation template.
     * <p>This method is the entry point for this class.  Typically,
     * {@link ptolemy.cg.kernel.generic.program.procedural.java#_getAutoGeneratedAdapter(GenericCodeGenerator, Object)}
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
            // If the static _isAutoAdaptered() method is called, then
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
    public Set<String> getSharedCode() throws IllegalActionException {
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
                        + _eol + "    }" + _eol + "}" + _eol
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
        return sharedCode;
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
    protected String _generateFireCode() throws IllegalActionException {
        // FIXME: what if the inline parameter is set?
        StringBuffer code = new StringBuffer(getCodeGenerator().comment(
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

            if (!_isAutoAdapteredRemotePort(inputPort)) {
                if (!inputPort.isMultiport()
                        && inputPort.isOutsideConnected()
                        && ((inputPort instanceof ParameterPort) || inputPort
                                .numLinks() > 0)) {
                    // Only generate code if we have a ParameterPort or we are connected.
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/AutoAdapterTwoActors.xml
                    code.append(_generateSendInside(name, name, type, 0));
                } else {
                    // Multiports.

                    // Generate code for the sources.  We don't use
                    // getWidth() here because IOPort.getWidth() says
                    // not to.
                    int sources = inputPort.numberOfSources();
                    //code.append(_eol + getCodeGenerator().comment("AutoAdapter._generateFireCode() MultiPort name " + name + " type: " + type + " numberOfSources: " + inputPort.numberOfSources() + " inputPort: " + inputPort + " width: " + inputPort.getWidth() + " numberOfSinks: " + inputPort.numberOfSinks()));
                    for (int i = 0; i < sources; i++) {
                        code.append(_generateSendInside(name, name + "Source"
                                + i, type, i));
                    }

                    // Generate code for the sinks.
                    int sinks = inputPort.numberOfSinks();
                    int width = inputPort.getWidth();
                    if (width < sinks) {
                        sinks = width;
                    }
                    for (int i = 0; i < sinks; i++) {
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

            if (!_isAutoAdapteredRemotePort(outputPort)) {
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
     *  @exception Exception If a field cannot be access, or if
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
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].get(component) instanceof Port) {
                        Port portField = (Port) fields[i].get(component);
                        String portFieldName = portField.getName();
                        portNames.append("<" + portFieldName + "> ");
                        if (portName.equals(portFieldName)) {
                            foundPortField = fields[i];
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
     * @param type The type of the port.
     * @param channel The channel number.
     * For non-multiports, the channel number will be 0.
     */
    private String _generateGetInside(String actorPortName,
            String codegenPortName, Type type, int channel) {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        //codegenPortName = TemplateParser.escapePortName(codegenPortName);
        if (type instanceof ArrayType) {

            ArrayType array = (ArrayType) type;

            String codeGenElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());
            String ptolemyData = "$actorSymbol(" + actorPortName
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
                    + ptolemyData
                    + ").length() , 0);"
                    + _eol

                    // Copy from the Ptolemy data to the codegen data.
                    + " for (int i = 0; i < ((ArrayToken)"
                    + ptolemyData
                    + ").length(); i++) {"
                    + _eol
                    + "   Array_set(codeGenData, i, "
                    + getCodeGenerator().codeGenType(
                            array.getDeclaredElementType()) + "_new(((("
                    + codeGenElementType + "Token)(" + ptolemyData
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
            // + ", ((" + type.getTokenClass().getName() + ")($actorSymbol("
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
     * @param type The type of the port.
     * @param channel The channel number.
     * For non-multiports, the channel number will be 0.
     */
    private String _generateGetInsideDeclarations(String actorPortName,
            String codegenPortName, Type type, int channel) {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        codegenPortName = TemplateParser.escapePortName(codegenPortName);
        // This method is needed by $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml
        String portData = actorPortName + "_portData"
                + (channel == 0 ? "" : channel);
        if (type instanceof ArrayType) {
            ArrayType array = (ArrayType) type;

            String codeGenElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());

            String ptolemyData = "$actorSymbol(" + actorPortName
                    + "_ptolemyData)";
            return
            // Get the data from the Ptolemy port
            type.getTokenClass().getName()
                    + " "
                    + ptolemyData
                    + " = (("
                    + type.getTokenClass().getName()
                    + ")($actorSymbol("
                    + codegenPortName
                    + ").getInside(0"
                    // For non-multiports "". For multiports, ", 0", ", 1" etc.
                    + (channel == 0 ? "" : ", " + channel)
                    + ")));"
                    + _eol
                    // Create an array for the codegen data.
                    + _eol
                    + getCodeGenerator()
                            .comment(
                                    "AutoAdapter: FIXME: This will leak. We should check to see if the token already has been allocated")
                    + " Token $actorSymbol("
                    + portData
                    + ") = $Array_new("
                    + "((ArrayToken)"
                    + ptolemyData
                    + ").length(), 0);"
                    + _eol

                    // Copy from the Ptolemy data to the codegen data.
                    + " for (int i = 0; i < ((ArrayToken)"
                    + ptolemyData
                    + ").length(); i++) {"
                    + _eol
                    + "   Array_set($actorSymbol("
                    + portData
                    + "), i, "
                    + getCodeGenerator().codeGenType(
                            array.getDeclaredElementType())
                    + "_new(((("
                    + codeGenElementType
                    + "Token)("
                    + ptolemyData
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
                    + "($actorSymbol("
                    + codegenPortName
                    + ").getInside(0"
                    + ")))."
                    + type.toString().toLowerCase()
                    + "Value());"
                    + _eol
                    + "double real = complex.real;"
                    + _eol
                    + "double imag = complex.imag;"
                    + _eol
                    + "$actorSymbol("
                    + portData
                    + ") = $typeFunc(TYPE_Complex::new(real, imag));"
                    + _eol
                    + _generateGetInside(actorPortName, codegenPortName, type,
                            channel);

            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //+ (channel == 0 ? "" : ", " + channel)
        } else {
            return "$targetType("
                    + actorPortName
                    + ") $actorSymbol("
                    + portData
                    + ");"
                    + _eol
                    + "if ($actorSymbol("
                    + codegenPortName
                    + ").hasTokenInside(0)) {"
                    + _eol
                    + "    $actorSymbol("
                    + portData
                    + ") = "
                    + "(("
                    + type.getTokenClass().getName()
                    + ")"
                    + "($actorSymbol("
                    + codegenPortName
                    + ").getInside(0"
                    + ")))."
                    + type.toString().toLowerCase()
                    + "Value();"
                    + _eol
                    + _generateGetInside(actorPortName, codegenPortName, type,
                            channel) + _eol + "}" + _eol;
            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            //+ (channel == 0 ? "" : ", " + channel)
        }
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
     *  @exception IllegalActionException If there is a problem checking whether
     *  actorPortName is a PortParameter.
     */
    private String _generatePortInstantiation(String actorPortName,
            String codegenPortName, TypedIOPort port, int channelNumber)
            throws IllegalActionException {
        //String escapedActorPortName = TemplateParser.escapePortName(actorPortName);
        String unescapedActorPortName = TemplateParser
                .unescapePortName(actorPortName);
        String escapedCodegenPortName = TemplateParser
                .escapePortName(codegenPortName);
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
        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/ReadParametersAcrossLinkTest.xml

        // First, determine if the port is an input port that is connected
        // to a TypedComposite that has parameters.  If it is, then generate
        // a composite that contains the parameters and connect our code generator
        // to its input.  If we CompositeA -> ActorB, then we generate CompositeC
        // and generate code that creates CompositeC -> ActorB.

        // True if we are reading remote parameters.
        boolean readingRemoteParameters = false;
        if (port.isInput() && port.isMultiport()) {
            // FIXME: We should annotate the very few ports that are
            // used by actors to read parameters in remote actors.

            List<Relation> linkedRelationList = port.linkedRelationList();
            for (Relation relation : linkedRelationList) {
                NamedObj container = ((TypedIOPort) relation.linkedPortList(
                        port).get(0)).getContainer();
                if (container instanceof TypedCompositeActor) {
                    List<Parameter> parameters = container
                            .attributeList(Parameter.class);
                    if (parameters.size() > 0) {
                        readingRemoteParameters = true;
                        break;
                    }
                }
            }
        }

        StringBuffer code = new StringBuffer("{" + _eol);
        if (readingRemoteParameters) {
            code.append("TypedCompositeActor c0 = new TypedCompositeActor($containerSymbol(), \"c0"
                    + codegenPortName + "\");" + _eol);

            // Iterate through all the parameters in the remote actor.
            List ports = port.connectedPortList();
            NamedObj remoteActor = ((IOPort) ports.get(channelNumber))
                    .getContainer();
            List<Parameter> parameters = remoteActor
                    .attributeList(Parameter.class);
            for (Parameter parameter : parameters) {
                code.append("new Parameter(c0, \"" + parameter.getName()
                        + "\").setExpression(\"" + parameter.getExpression()
                        + "\");" + _eol);
            }

            // Create the input and output ports and connect them.
            //code.append("    System.out.println(\"E1\");" + _eol);
            code.append("TypedIOPort c0PortA = new TypedIOPort(c0, \"c0PortA\", false, true);"
                    + _eol
                    + "TypedIOPort c0PortB = new TypedIOPort(c0, \"c0PortB\", true, false);"
                    + _eol + "c0.connect(c0PortB, c0PortA);" + _eol);
        }

        // If the remote port belongs to an actor that would
        // use an AutoAdapter (that is, an actor for which there
        // is no template), then connect directly to the remote port.
        // This allows us to put multiple custom actors in one composite.

        // We don't call _isAutoAdapteredRemotePort() here because we
        // want to use these variables anyway.
        boolean remoteIsAutoAdaptered = false;
        Relation relation = (Relation) port.linkedRelationList().get(0);
        TypedIOPort remotePort = (TypedIOPort) relation.linkedPortList(port)
                .get(0);
        NamedObj remoteActor = remotePort.getContainer();
        String remoteActorSymbol = "";
        boolean moreThanOneRelation = false;
        String relationSymbol = "";
        if (_isAutoAdaptered(remoteActor)) {
            remoteIsAutoAdaptered = true;
            int verbosityLevel = ((IntToken) getCodeGenerator().verbosity.getToken()).intValue();
            if (verbosityLevel > 2) {
                System.out.println(getComponent().getName() + " " + port.getName()
                        + "#" + channelNumber
                        + " is connected to remote actor " + remoteActor.getName()
                        + " " + remotePort.getName() + " via "
                        + relation.getName() + " "
                        + port.linkedRelationList().size() + " "
                        + relation.linkedPortList(port).size());
            }
            if (/*port.linkedRelationList().size() > 1
                  ||*/ relation.linkedPortList(port).size() > 1) {
                StringBuffer message = new StringBuffer(
                        "Warning: custom actors that are "
                                + "connected to more than one port at the same level. Msg #1\n");
                Iterator relations = port.linkedRelationList().iterator();
                while (relations.hasNext()) {
                    Relation r = (Relation) relations.next();
                    message.append(getComponent().getName() + " "
                            + port.getName() + " " + r + _eol);
                    Iterator ports = r.linkedPortList(port).iterator();
                    while (ports.hasNext()) {
                        Port p = (TypedIOPort) ports.next();
                        message.append("    " + p + _eol);
                        if (!_isAutoAdaptered(p.getContainer())) {
                            // If one of the remote actors is not auto
                            // adapatered, then mark this connection as
                            // not being autoadaptered.  This is probably
                            // a mistake, we should just handle this.
                            // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml
                            message.append("\nPort " + p.getFullName() + " is contained by an actor that is not an auto adapter.\n");
                            remoteIsAutoAdaptered = false;
                        }
                    }
                }
                moreThanOneRelation = true;
                relationSymbol = "$actorSymbol(" + relation.getName() + ")";
                if (verbosityLevel > 1) {
                    System.out.println(message);
                }
            }
            // If the remote actor has not yet been created, then create it.
            remoteActorSymbol = getCodeGenerator().generateVariableName(
                    remoteActor) + "_actor";

            if (!moreThanOneRelation) {
                code.append("if (" + remoteActorSymbol + " == null) {" + _eol
                        + remoteActorSymbol + " = new "
                        + remoteActor.getClass().getName()
                        + "($containerSymbol() , \""
                        + remoteActorSymbol
                        + "\");"
                        + _eol
                        // Set the displayName so that actors that call getDisplayName() get the same value.
                        // Actors that generate random numbers often call getFullName(), then should call getDisplayName()
                        // instead.
                        + "        " + remoteActorSymbol + ".setDisplayName(\""
                        + remoteActor.getName() + "\");" + _eol + "}" + _eol);
            }
        }

        boolean connectedAlready = false;
        if (!remoteIsAutoAdaptered) {
            code.append("$actorSymbol("
                    + escapedCodegenPortName
                    + ") = new TypedIOPort($containerSymbol()"
                    // Need to deal with backslashes in port names, see
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                    + ", \""
                    //+ codegenPortName.replace("\\", "\\\\")
                    + AutoAdapter._externalPortName(port.getContainer(),
                            codegenPortName)
                    + "\", "
                    + port.isInput() + ", "
                    + port.isOutput() + ");"
                    + _eol
                    // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                    + "    $actorSymbol(" + escapedCodegenPortName
                    + ").setTypeEquals(" + _typeToBaseType(port.getType())
                    + ");" + _eol);
        }
        String portOrParameter = "";
        try {
            Field foundPortField = _findFieldByPortName(getComponent(),
                    unescapedActorPortName);
            if (foundPortField == null) {
                throw new NoSuchFieldException("Could not find port "
                        + unescapedActorPortName);
            }

            portOrParameter = "((" + getComponent().getClass().getName()
                    + ")$actorSymbol(actor))." + foundPortField.getName()
                    + (portParameter != null ? ".getPort()" : "");

        } catch (NoSuchFieldException ex) {
            // The port is not a field, it might be a PortParameter
            // that whose name is not the same as the declared name.
            // We check before we create it.  To test, use:
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterActorTest.xml
            //String multiport = "";
            code.append("if ($actorSymbol(actor).getPort(\""
                    + unescapedActorPortName.replace("\\", "\\\\")
                    //+ AutoAdapter._externalPortName(port.getContainer(),
                    //        unescapedActorPortName)
                    + "\") == null) {" + _eol
                    + "$actorSymbol(" + escapedCodegenPortName + ") = new TypedIOPort($actorSymbol(actor), \""
                    + unescapedActorPortName.replace("\\", "\\\\")
                    //+ AutoAdapter._externalPortName(port.getContainer(),
                    //        unescapedActorPortName) 
                    + "\", " + port.isInput()
                    + ", " + port.isOutput() + ");" + _eol);
            if (remotePort.isMultiport()) {
                code.append("$actorSymbol(" + escapedCodegenPortName + ").setMultiport(true);" + _eol);
            }

            code.append("}" + _eol);

            portOrParameter = "(TypedIOPort)$actorSymbol(actor).getPort(\""
                + unescapedActorPortName.replace("\\", "\\\\")
                //+ AutoAdapter._externalPortName(port.getContainer(),
                //            unescapedActorPortName)
                + "\")";
            if (!readingRemoteParameters) {
                connectedAlready = false;
                //code.append("    $containerSymbol().connect($actorSymbol("
                //+ escapedCodegenPortName + "), " + portOrParameter
                //+ ");" + _eol);
            } else {
                connectedAlready = true;
                //code.append("    System.out.println(\"D1\");" + _eol);
                code.append("    $containerSymbol().connect(c0PortA,"
                        + portOrParameter + ");" + _eol
                        + "    $containerSymbol().connect($actorSymbol("
                        + escapedCodegenPortName + "), c0PortB);" + _eol);
            }
            if (port.isOutput()) {
                code.append("    (" + portOrParameter + ").setTypeEquals("
                        + _typeToBaseType(port.getType()) + ");" + _eol);
            }

        }
        if (remoteIsAutoAdaptered) {
            try {
                Field remoteFoundPortField = _findFieldByPortName(remoteActor,
                            remotePort.getName());

                if (remoteFoundPortField == null) {
                    throw new NoSuchFieldException("Could not find port "
                            + remotePort.getName());
                }
                if (port.isOutput()) {
                    String relationAssignment = "";
                    String relationSetWidth = "";
                    if (moreThanOneRelation) {
                        _headerFiles.add("ptolemy.actor.TypedIORelation;");
                        code.append("TypedIORelation " + relationSymbol 
                                + " = null;" + _eol
                                + portOrParameter + ".link(" + relationSymbol + ");" + _eol);
                        // FIXME: What about multiple relations?
                        Iterator multiplePorts = relation.linkedPortList(port).iterator();
                        while (multiplePorts.hasNext()) {
                            TypedIOPort multipleRemotePort = (TypedIOPort) multiplePorts.next();
                            NamedObj multipleRemoteActor = multipleRemotePort.getContainer();
                            String multipleRemoteActorSymbol = getCodeGenerator().generateVariableName(
                                    multipleRemoteActor) + "_actor";
                            Field multipleRemoteFoundPortField = _findFieldByPortName(multipleRemoteActor,
                                    multipleRemotePort.getName());                            
                            PortParameter multiplePortParameter = (PortParameter) multipleRemoteActor
                                .getAttribute(multipleRemotePort.getName(), PortParameter.class);
                            code.append("if (" + multipleRemoteActorSymbol + " == null) {" + _eol
                                    + multipleRemoteActorSymbol + " = new "
                                    + multipleRemoteActor.getClass().getName()
                                    + "($containerSymbol() , \""
                                    + multipleRemoteActorSymbol
                                    + "\");"
                                    + _eol
                                    // Set the displayName so that actors that call getDisplayName() get the same value.
                                    // Actors that generate random numbers often call getFullName(), then should call getDisplayName()
                                    // instead.
                                    + "        " + multipleRemoteActorSymbol + ".setDisplayName(\""
                                    + multipleRemoteActor.getName() + "\");" + _eol + "}" + _eol
                                    + "((" + multipleRemoteActor.getClass().getName()
                                    + ")" + multipleRemoteActorSymbol + ")."
                                    + multipleRemoteFoundPortField.getName()
                                    + (multiplePortParameter != null ? ".getPort()" : "")
                                    + ".link(" + relationSymbol + ");" + _eol);
                        } 
                    } else {
                        if (port.isMultiport()) {
                            // Needed for
                            // $PTII/bin/ptcg -language java  $PTII/ptolemy/actor/lib/test/auto/WallClockTime.xml
                            _headerFiles.add("ptolemy.actor.IORelation;");
                            relationAssignment = "IORelation relation = (IORelation)";
                            relationSetWidth = "relation.setWidth("
                                + port.getWidth() + "); " + _eol;
                        }

                        // It is the responsibility of the custom actor
                        // with the output port to connect to the input
                        // port of the other custom actor.  This obviates
                        // the need for checking for the connection at
                        // runtime.
                        //code.append("    System.out.println(\"C1\");" + _eol);
                        code.append(relationAssignment
                                + "$containerSymbol().connect(" + portOrParameter
                                + ", " + "((" + remoteActor.getClass().getName()
                                + ")" + remoteActorSymbol + ")."
                                + remoteFoundPortField.getName()
                                // FIXME: should portParameter be the remote port?
                                + (portParameter != null ? ".getPort()" : "")
                                + ");" + _eol + relationSetWidth);
                    }
                }
            } catch (NoSuchFieldException ex) {
                // The port is not a field, it might be a PortParameter
                // that whose name is not the same as the declared name.
                // We check before we create it.  To test, use:
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterActorTest.xml
                code.append("if (" + remoteActorSymbol + ".getPort(\""
                        + AutoAdapter._externalPortName(remotePort.getContainer(),
                                remotePort.getName())
                        + "\") == null) {"
                        + _eol
                        + "$actorSymbol(" + escapedCodegenPortName + ") = "
                        + " new TypedIOPort(" + remoteActorSymbol + ", \""
                        + AutoAdapter._externalPortName(remotePort.getContainer(),
                                remotePort.getName()) + "\", " + remotePort.isInput()
                        + ", " + remotePort.isOutput() + ");" + _eol);
                if (port.isMultiport()) {
                    code.append("$actorSymbol(" + escapedCodegenPortName + ").setMultiport(true);" + _eol);
                }
                code.append("}" + _eol);
                    
                portOrParameter = "(TypedIOPort)" + remoteActorSymbol + ".getPort(\""
                    + AutoAdapter._externalPortName(remotePort.getContainer(),
                            remotePort.getName()) + "\")";
                if (!readingRemoteParameters) {
                    //code.append("    System.out.println(\"B1\");" + _eol);
                    code.append("    $containerSymbol().connect($actorSymbol("
                            + escapedCodegenPortName + "), " + portOrParameter
                            + ");" + _eol);
                } else {
                    //code.append("    System.out.println(\"B2\");" + _eol);
                    code.append("    $containerSymbol().connect(c0PortA,"
                            + portOrParameter + ");" + _eol
                            + "    $containerSymbol().connect($actorSymbol("
                            + escapedCodegenPortName + "), c0PortB);" + _eol);
                }
                if (port.isOutput()) {
                    // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                    code.append("    (" + portOrParameter + ").setTypeEquals("
                            + _typeToBaseType(port.getType()) + ");" + _eol);
                }
            }
        } else {
            if (!readingRemoteParameters) {
                if (!connectedAlready) {
                    //code.append("    System.out.println(\"A1\");" + _eol);
                    code.append("    $containerSymbol().connect($actorSymbol("
                            + escapedCodegenPortName + "), " + portOrParameter
                            + ");" + _eol);
                }
            } else {
                if (!connectedAlready) {
                    //code.append("    System.out.println(\"A2\");" + _eol);
                    code.append("    $containerSymbol().connect(c0PortA,"
                            + portOrParameter + ");" + _eol
                            + "    $containerSymbol().connect($actorSymbol("
                            + escapedCodegenPortName + "), c0PortB);" + _eol);
                }
            }

            if (port.isOutput()) {
                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                code.append("    (" + portOrParameter + ").setTypeEquals("
                        + _typeToBaseType(port.getType()) + ");" + _eol);
            }
        }

        code.append("}" + _eol);

        return code.toString();
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
     */
    private String _generateSendInside(String actorPortName,
            String codegenPortName, Type type, int channel) {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        codegenPortName = TemplateParser.escapePortName(codegenPortName);
        if (type instanceof ArrayType) {

            ArrayType array = (ArrayType) type;

            String javaElementType = getCodeGenerator().codeGenType(
                    array.getDeclaredElementType());
            String codeGenElementType = javaElementType.replace("Integer",
                    "Int");
            String targetElementType = getCodeGenerator().targetType(
                    array.getDeclaredElementType());
            String ptolemyData = "$actorSymbol(" + actorPortName
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
                    + ptolemyData
                    + " = new "
                    + codeGenElementType
                    + "Token [((Array)codeGenData.getPayload()).size];"
                    + _eol

                    // Copy from the codegen data to the Ptolemy data
                    + " for (int i = 0; i < ((Array)codeGenData.getPayload()).size; i++) {"
                    + _eol + "   " + ptolemyData + "[i] = new "
                    + codeGenElementType + "Token(((" + javaElementType
                    + ")(Array_get(codeGenData, i).getPayload()))."
                    + targetElementType + "Value());" + _eol
                    + " }"
                    + _eol

                    // Set the type.
                    + "    $actorSymbol(" + codegenPortName
                    + ").setTypeEquals(" + _typeToBaseType(type) + ");"
                    + _eol
                    // Output our newly constructed token
                    + " $actorSymbol(" + codegenPortName
                    + ").sendInside(0, new ArrayToken(" + ptolemyData + "));"
                    + _eol + "}" + _eol;
        } else if (type == BaseType.COMPLEX) {
            return
            // Set the type.
            "    $actorSymbol(" + codegenPortName + ").setTypeEquals("
                    + _typeToBaseType(type) + ");"
                    + _eol
                    // Send data to the actor.
                    + "    $actorSymbol("
                    + codegenPortName
                    + ").sendInside(0, new "
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
        } else {
            return
            // Set the type.
            "    $actorSymbol(" + codegenPortName + ").setTypeEquals("
                    + _typeToBaseType(type) + ");" + _eol
                    // Send data to the actor.
                    + "    $actorSymbol(" + codegenPortName
                    + ").sendInside(0, new "
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
                    + parameterValue + "\");" + _eol;
        } else {
            if (parameter instanceof ptolemy.kernel.util.StringAttribute) {
                setParameter = "    ptolemy.kernel.util.StringAttribute "
                        + parameterName + " = ((" + actorClassName
                        + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                        + "    " + parameterName + ".setExpression(\""
                        + parameterValue + "\");" + _eol;
            }
        }
        return "{ " + _eol + setParameter + "    ((" + actorClassName
                + ")$actorSymbol(actor)).attributeChanged(" + parameterName
                + ");" + _eol + "}" + _eol;
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
    private boolean _isAutoAdaptered(NamedObj namedObj) {
        try {
            _checkingAutoAdapter = true;
            _wouldBeAutoAdapted = false;
            try {
                // The adapter might be cached.
                Object adapter = getCodeGenerator().getAdapter(namedObj);
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
    private boolean _isAutoAdapteredRemotePort(Port port) throws IllegalActionException {
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
            boolean remoteIsAutoAdaptered = _isAutoAdaptered(remoteActor);
            StringBuffer message = new StringBuffer(
                    "Warning: custom actors "
                            + " connected to more than one port the same level. Msg #2\n");
            Iterator relations = port.linkedRelationList().iterator();
            while (relations.hasNext()) {
                Relation r = (Relation) relations.next();
                message.append(getComponent().getName() + " " + port.getName()
                        + " " + r + _eol);
                Iterator ports = r.linkedPortList(port).iterator();
                while (ports.hasNext()) {
                    Port p = (TypedIOPort) ports.next();
                    message.append("    " + p + _eol);
                    if (!_isAutoAdaptered(p.getContainer())) {
                        // If one of the remote actors is not auto
                        // adapatered, then mark this connection as
                        // not being autoadaptered.  This is probably
                        // a mistake, we should just handle this.
                        // This test:
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml
                        // has a bunch of custom actors that share an input relation, but the input
                        // is a non-autoadapter actor.  We would like to preserve the connectivity.
                        message.append("\nPort " + p.getFullName() + " is contained by an actor that is not an auto adapter.\n");
                        remoteIsAutoAdaptered = false;
                    }
                }
            }
            int verbosityLevel = ((IntToken) getCodeGenerator().verbosity.getToken()).intValue();
            if (verbosityLevel > 0) {
                System.out.println(message);
            }
            return remoteIsAutoAdaptered;
        }
        return _isAutoAdaptered(remoteActor);
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
        }
        return "BaseType." + type.toString().toUpperCase();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** If {@link #_isAutoAdaptered(NamedObj)} is called, then
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

    /** True if the import for ptolemy.data.expr.Variable has been added
     *   to the set of header files.
     */
    private boolean _importedVariable = false;

    /** True if _autoAdapterSetPrivateParameter() should be declared. */
    private boolean _needAutoAdapterSetPrivateParameter = false;

    /** The toplevel for which we generated code to call resolveTypes().
     *  This is static, but changes for each model.
     */
    private static NamedObj _toplevelTypesResolved = null;

    /** If {@link #_isAutoAdaptered(NamedObj)} is called, then
     *  {@link #getAutoAdapter(GenericCodeGenerator, Object)} sets
     *  this variable to true if the Object would use an AutoAdapter
     *  for code generation.  This is used to put two or more
     *  custom actors in to the same container.
     */
    private static boolean _wouldBeAutoAdapted = false;
}
