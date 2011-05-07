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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
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
 *  inner Ptolemy actor. 
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
     *  @param codeGenerator The code generator with which to associate the adapter.
     *  @param component The associated component.
     */
    public AutoAdapter(ProgramCodeGenerator codeGenerator, TypedAtomicActor component) {
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
        // Use the full class name so that we don't have to import the
        // actor.  If we import the actor, then we cannot have model
        // names with the same name as the actor.
        String actorClassName = getComponent().getClass().getName();

        StringBuffer code = new StringBuffer();
        // Generate code that creates and connects each port.
        Iterator entityPorts = ((Entity)getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof IOPort) {

                IOPort castPort = (IOPort) insidePort;
                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    code.append(_generatePortInstantiation(name, castPort.getName(), castPort));
                } else {
                    // Multiports.  Not all multiports have port names
                    // that match the field name. See
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml

                    //TypedIOPort actorPort = (TypedIOPort)(((Entity)getComponent()).getPort(castPort.getName()));

                    TypedIOPort actorPort = null;
                    try {
                        Field foundPortField = _findFieldByPortName(castPort.getName());
                        actorPort = (TypedIOPort)foundPortField.get(getComponent());
                        code.append("    ((" + getComponent().getClass().getName()
                                + ")$actorSymbol(actor))." + foundPortField.getName() + ".setTypeEquals("
                                + _typeToBaseType(actorPort.getType()) + ");" + _eol);

                    } catch (Exception ex) {
                        //throw new IllegalActionException(castPort, ex,
                        //        "Could not find port " + castPort.getName());
                        actorPort = (TypedIOPort)((Entity)getComponent()).getPort(castPort.getName());
                        code.append("new TypedIOPort($actorSymbol(container), \""
                                + actorPort.getName().replace("\\", "\\\\") + "\", " 
                                + actorPort.isInput() + ", "
                                + actorPort.isOutput() + ").setMultiport(true);" + _eol);

                    }


                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append(_generatePortInstantiation(name, name + "Source" + i, actorPort));
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append(_generatePortInstantiation(name, name + "Sink" + i, actorPort));
                    }
                }

                List<TypeAttribute> typeAttributes = insidePort.attributeList(TypeAttribute.class);
                if (typeAttributes.size() > 0) {
                    if (typeAttributes.size() > 1) {
                        new Exception("Warning, " + insidePort.getFullName()
                                + " has more than one typeAttribute."
                                      ).printStackTrace();
                    }
                    // only get the first element of the list.
                    TypeAttribute typeAttribute = typeAttributes.get(0);
                    // The port has a type attribute, which means the
                    // type was set via the UI.
                    // This is needed by:
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/comm/test/auto/TrellisDecoder.xml 
                    code.append("{" + _eol
                            + "ptolemy.actor.TypeAttribute _type = "
                            + "new ptolemy.actor.TypeAttribute("

                            //+ "$actorSymbol("
                            //+ TemplateParser.escapePortName(insidePort.getName()) + "), \"inputType\");" + _eol
                            //+ "((" + actorClassName + ")$actorSymbol(actor))." 
                            //+ TemplateParser.escapePortName(insidePort.getName()) + ", \"inputType\");" + _eol

                            // Certain actors may create ports on the
                            // fly, so query the actor for its port.

                            + "(TypedIOPort)$actorSymbol(container).getPort(\"" 
                            + insidePort.getName().replace("\\", "\\\\") + "\"), \"inputType\");" + _eol
                            + "_type.setExpression(\""
                            + typeAttribute.getExpression()
                            + "\");" + _eol
                            + "}" + _eol);
                }
            }
        }

        String [] splitInitializeConnectionCode = getCodeGenerator()._splitBody("_AutoAdapterI_", code.toString());

        code = new StringBuffer();
        // Handle parameters.
        Iterator parameters = getComponent().attributeList(Settable.class).iterator();
        while (parameters.hasNext()) {
            Settable parameter = (Settable) parameters.next();
            if (!ptolemy.actor.gui.Configurer.isVisible(getComponent(), parameter)) {
                continue;
            }

            String parameterName = StringUtilities.sanitizeName(parameter.getName()).replaceAll("\\$", "Dollar");
            if (parameterName.equals("firingsPerIteration")) {
                continue;
            }
            // FIXME: handle multiline values
            String parameterValue = "";
            if (parameter instanceof Variable
                    && ((Variable)parameter).getToken() != null) {
                // Evaluate things like $PTII
                parameterValue = ((Variable)parameter).getToken().toString();
                if (((Variable)parameter).isStringMode()) {
                    if (parameterValue.startsWith("\"") && parameterValue.endsWith("\"")) {
                        // This is needed by
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/string/test/auto/StringFunction.xml 
                        parameterValue = parameterValue.substring(1, parameterValue.length() - 1);
                    }
                }
            } else {
                parameterValue = parameter.getExpression();
            }

            parameterValue = StringUtilities.escapeString(parameterValue);
            // FIXME: do we want one try block per parameter?  It does
            // make for better error messages.

            boolean privateParameter = false;
            try {
                 getComponent().getClass().getField(parameterName);
            } catch (NoSuchFieldException ex) {
                privateParameter = true;
                code.append("try{" + _eol
                        + "{" + _eol
                        + "// Accessing private field" + _eol
                        + "Object actor = $actorSymbol(actor);" + _eol
                        + "java.lang.reflect.Field fields[] = actor.getClass().getDeclaredFields();" + _eol
                        + "for (int i = 0; i < fields.length; i++){" + _eol
                        + "    if (fields[i].getName().equals(\"" + parameterName + "\")) {" + _eol
                        + "        fields[i].setAccessible(true);" + _eol 
                        + "        ptolemy.data.expr.Parameter parameter = (ptolemy.data.expr.Parameter)fields[i].get(actor);" + _eol
                        + "        parameter.setExpression(\"" + parameterValue + "\");" + _eol
                        + "       ((" + actorClassName + ")$actorSymbol(actor)).attributeChanged(parameter);" + _eol
                        + "        break;" + _eol
                        + "    }" + _eol
                        + "}" + _eol
                        + "}" + _eol);
            } catch (SecurityException ex2) {
                throw new IllegalActionException(getComponent(), ex2,
                        "Can't access " + parameterName + " field.");
            }

            if (!privateParameter) {
                String setParameter = "";
                if (parameter instanceof Parameter) {
                    setParameter = "    ptolemy.data.expr.Parameter " + parameterName + " = ((" + actorClassName + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                        + "    " + parameterName + ".setExpression(\""
                        + parameterValue
                        + "\");" + _eol;
                } else {
                    if (parameter instanceof ptolemy.kernel.util.StringAttribute) {
                        setParameter = "    ptolemy.kernel.util.StringAttribute " + parameterName + " = ((" + actorClassName + ")$actorSymbol(actor))." + parameterName + ";" + _eol
                            + "    " + parameterName + ".setExpression(\""
                            + parameterValue
                            + "\");" + _eol;
                    }
                } 
                code.append("try {" + _eol
                        + setParameter
                        + "    ((" + actorClassName + ")$actorSymbol(actor)).attributeChanged(" + parameterName + ");" + _eol);
            }
            code.append("} catch (Exception ex) {" + _eol
                    + "    throw new RuntimeException(\"Failed to set parameter \\\"" + parameterName
                    + "\\\" in $actorSymbol(actor) to \\\"" + StringUtilities.escapeString(parameterValue) + "\\\"\", ex);" + _eol
                + "}" + _eol);
        }
        //code.append(getCodeGenerator().comment("AutoAdapter._generateInitalizeCode() start"));

        String [] splitInitializeParameterCode = getCodeGenerator()._splitBody("_AutoAdapterP_", code.toString());

        String result = getCodeGenerator().comment("AutoAdapter._generateInitalizeCode() start")
            + "try {" + _eol
            //+ "    $actorSymbol(container) = new TypedCompositeActor();" +_eol
            + "    $actorSymbol(container) = new " + getComponent().getContainer().getClass().getName() + "();" +_eol
            // Some custom actors such as ElectricalOverlord
            // want to be in a container with a particular name.
            + "    $actorSymbol(container).setName(\""
            + getComponent().getContainer().getName()
            + "\");" + _eol
            + "    $actorSymbol(actor) = new " + actorClassName
            + "($actorSymbol(container), \"$actorSymbol(actor)\");" + _eol
            + splitInitializeConnectionCode[0]
            + splitInitializeConnectionCode[1]
            + "    new ptolemy.actor.Director($actorSymbol(container), \"director\");" + _eol
            + "    $actorSymbol(container).setManager(new ptolemy.actor.Manager(\"manager\"));" + _eol
            + "    $actorSymbol(container).preinitialize();" + _eol
            + "} catch (Exception ex) {" + _eol
                + "    throw new RuntimeException(\"Failed to create $actorSymbol(actor))\", ex);" + _eol
            + "}" + _eol
            + "try {" + _eol
            + "    TypedCompositeActor.resolveTypes($actorSymbol(container));" + _eol
            + "    $actorSymbol(actor).initialize();" + _eol
            + "} catch (Exception ex) {" + _eol
            + "    throw new RuntimeException(\"Failed to initalize $actorSymbol(actor))\", ex);" + _eol
            + "}" + _eol
            + "{" + _eol
            + splitInitializeParameterCode[0]
            + splitInitializeParameterCode[1]
            + "}" + _eol;

        return processCode(result);
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
        StringBuffer code = new StringBuffer("TypedCompositeActor $actorSymbol(toplevel);" + _eol
                + "TypedCompositeActor $actorSymbol(container);" + _eol
                + "TypedAtomicActor $actorSymbol(actor);" + _eol);

        // Handle inputs and outputs on a per-actor basis.
        Iterator entityPorts = ((Entity)getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof IOPort) {
                IOPort castPort = (IOPort) insidePort;
                String name = TemplateParser.escapePortName(castPort.getName());
                if (!castPort.isMultiport()) {
                    code.append("TypedIOPort $actorSymbol(" + name + ");" + _eol);
                } else {
                    // FIXME: We instantiate a separate external port for each channel
                    // of the multiport.  Could we just connect directly to the channels
                    // of the multiport?  The problem I had was that the receivers are
                    // not created if I connect directly to the channels.

                    // Use castPort.getName() and get the real name of the port. 
                    // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                    IOPort actorPort = (IOPort)(((Entity)getComponent()).getPort(castPort.getName()));

                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name + "Source" + i + ");" + _eol);
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name + "Sink" + i + ");" + _eol);
                    }
                }
            }
        }
        return processCode(code.toString());
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
     * @param codeGenerator The code generator with which to associate the adapter.
     * @param object The given object.
     * @return the AutoAdapter or null if object is not assignable
     * from TypedAtomicActor.
     */
    public static AutoAdapter getAutoAdapter(GenericCodeGenerator codeGenerator,
            Object object) {
        // FIXME: I'm not sure if we need this method, but I like
        // calling something that returns null if the associated actor
        // cannot be found or is of the wrong type.
        try {
            Class typedAtomicActor = Class.forName("ptolemy.actor.TypedAtomicActor");
            if (!typedAtomicActor.isAssignableFrom(object.getClass())) {
                return null;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }
        // FIXME: I don't like casting to ProgramCodeGenerator, but we need to set
        // the codeGenerator of the templateParser.
        return new AutoAdapter((ProgramCodeGenerator)codeGenerator, (TypedAtomicActor) object);
    }

    /** Get the files needed by the code generated for this actor.
     *  Add $(PTII) to the classpath of the generated code.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the Maximum actor.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("ptolemy.data.type.ArrayType;");
        // Need IntToken etc.
        files.add("ptolemy.data.*;");
        // FIXME: we should only import Complex if necessary.
        files.add("ptolemy.math.Complex;");
        files.add("ptolemy.data.type.BaseType;");
        files.add("ptolemy.actor.TypedAtomicActor;");
        files.add("ptolemy.actor.TypedCompositeActor;");
        files.add("ptolemy.actor.TypedIOPort;");

        // If the actor is imported, then we cannot have models with the same
        // name as the actor.
        //files.add(getComponent().getClass().getName() + ";");
        ((ProceduralCodeGenerator)getCodeGenerator()).addLibraryIfNecessary("$(PTII)");

	// Loop through the path elements in java.class.path and add
        // them as libraries.  We need this so that we can find the
        // JavaScope.zip code coverage file in the nightly build
        ((JavaCodeGenerator)getCodeGenerator())._addClassPathLibraries();

        return files;
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
        StringBuffer code = new StringBuffer(getCodeGenerator().comment("AutoAdapter._generateFireCode() start"));

        // FIXME: it is odd that we are transferring data around in the fire code.
        // Shouldn't we do this in prefire() and posfire()?

        // Transfer data from the codegen variables to the actor input ports.
        Iterator inputPorts = ((Actor)getComponent()).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String name = inputPort.getName();
            Type type = inputPort.getType();

            if (!inputPort.isMultiport() && ((inputPort instanceof ParameterPort) || inputPort.numLinks() > 0)) {
                // Only generate code if we have a ParameterPort or we are connected.
                code.append(_generateSendInside(name, name, type, 0));
            } else {
                // Multiports.

                // Generate code for the sources.  We don't use getWidth() here
                // because IOPort.getWidth() says not to.
                int sources = inputPort.numberOfSources();
                //code.append(_eol + getCodeGenerator().comment("AutoAdapter._generateFireCode() MultiPort name " + name + " type: " + type + " numberOfSources: " + inputPort.numberOfSources() + " inputPort: " + inputPort + " width: " + inputPort.getWidth() + " numberOfSinks: " + inputPort.numberOfSinks()));
                for (int i = 0; i < sources; i++) {
                    code.append(_generateSendInside(name, name + "Source" + i, type, i));
                }

                // Generate code for the sinks.
                int sinks = inputPort.numberOfSinks();
                int width = inputPort.getWidth();
                if (width < sinks) {
                    sinks = width;
                }
                for (int i = 0; i < sinks; i++) {
                    code.append(_generateSendInside(name, name + "Sink" + i, type, i));
                }
            }
        }

        // Fire the actor.
        
        code.append("$actorSymbol(actor).fire();" + _eol);


        // Transfer data from the actor output ports to the codegen variables.
        Iterator outputPorts = ((Actor)getComponent()).outputPortList().iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            String name = outputPort.getName();
            Type type = outputPort.getType();

            // Get data from the actor.
            if (!outputPort.isMultiport()) {
                if (outputPort.isOutsideConnected()) {
                    // Place the temporary variable inside a block so that
                    // if we split a long body, the declaration does not
                    // get separated from the use.
                    code.append("{" + _eol
                            // Create temporary variables for each port so that we don't
                            // read from empty mailbox.
                            + _generateGetInsideDeclarations(name, name, type, 0)
                            + _generateGetInside(name, name, type, 0)
                            + _eol + "}" + _eol);
                }
            } else {
                // Multiports.
                int sources = outputPort.numberOfSources();
                for (int i = 0; i < sources; i++) {
                    code.append("{" + _eol
                            + _generateGetInsideDeclarations(name, name + "Source" + i, type, i) 
                            + _generateGetInside(name, name + "Source" + i, type, i)
                            + _eol + "}" + _eol);
                }
                int sinks = outputPort.numberOfSinks();
                for (int i = 0; i < sinks; i++) {
                    code.append("{" + _eol
                            + _generateGetInsideDeclarations(name, name + "Sink" + i, type, i)
                            + _generateGetInside(name, name + "Sink" + i, type, i)
                            + _eol + "}" + _eol);
                }
            }

        }

        String [] splitFireCode = getCodeGenerator()._splitBody("_AutoAdapterF_", code.toString());

        return "try {" + _eol
            + splitFireCode[0] + _eol
            + splitFireCode[1] + _eol
            + "} catch (Exception ex) {" + _eol
            + "    throw new RuntimeException(\"Failed to fire() $actorSymbol(actor))\", ex);" + _eol
            + " }" + _eol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Look for a field in the actor by port name.  This method is
     *  necessary because some ports have different names than the name
     *  of the field.
     *  @param portName The escaped name of the port.
     *  @exception Exception Thrown if a field cannot be access, or if
     *  getComponent() fails.
     */
    private Field _findFieldByPortName(String portName) throws NoSuchFieldException {
        portName = TemplateParser.unescapePortName(portName);
        Field foundPortField = null;
        // Make sure that there is a field with that name
        // $PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml
        // has a NonStrict actor with an output that is not connected.
        // If we don't check for the field, then the generated Java code
        // fails.
        NamedObj component = null;
        try {
            foundPortField = getComponent().getClass().getField(portName);
        } catch (NoSuchFieldException ex) {
            StringBuffer portNames = new StringBuffer();
            try {
                component = getComponent();
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
            } catch (Exception ex2) {
                throw new NoSuchFieldException(component.getFullName()
                        + ": Failed to find the field that corresponds with " + portName
                        + " Fields: " + portNames
                        + ": " + ex);
            }
        }
        //System.out.println("AutoAdapter._find " + portName + " " + foundPortField);
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
        String code = "try {" + _eol
            + "    $actorSymbol(actor)." + executionMethod + "();" + _eol
            + "} catch (Exception ex) {" + _eol
            + "    throw new RuntimeException(\"Failed to "
            + executionMethod  + "() $actorSymbol(actor))\", ex);" + _eol
            + "}" + _eol;
        return processCode(code);
    }

    /** Return the code necessary to instantiate the port.
     *  @param actorPortName The escaped name of the Actor port to be instantiated.
     *  @param codegenPortName The name of the port on the codegen side.
     *  For non-multiports, actorPortName and codegenPortName are the same.
     *  For multiports, codegenPortName will vary according to channel number
     *  while actorPortName will remain the same.
     *  @param port The port of the actor.
     *  @exception IllegalActionException If there is a problem checking whether
     *  actorPortName is a PortParameter.
     */
    private String _generatePortInstantiation(String actorPortName,
            String codegenPortName, IOPort port) throws IllegalActionException {
        //String escapedActorPortName = TemplateParser.escapePortName(actorPortName);
        String unescapedActorPortName = TemplateParser.unescapePortName(actorPortName);
        String escapedCodegenPortName = TemplateParser.escapePortName(codegenPortName);
        PortParameter portParameter = (PortParameter)getComponent().getAttribute(actorPortName,
                PortParameter.class);
        // Multiport need to have different codegenPortNames, see
        // $PTII/bin/ptcg -language java  $PTII/ptolemy/actor/lib/test/auto/Gaussian1.xml
        StringBuffer code = new StringBuffer("    $actorSymbol(" + escapedCodegenPortName + ") = new TypedIOPort($actorSymbol(container)"
                // Need to deal with backslashes in port names, see
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithPortNameProblemTest.xml
                + ", \"" + codegenPortName.replace("\\", "\\\\") + "\", "
                + port.isInput() + ", "
                + port.isOutput() + ");" + _eol
                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                + "    $actorSymbol(" + escapedCodegenPortName + ").setTypeEquals("
                + _typeToBaseType(((TypedIOPort)port).getType())
                +");" + _eol);

        try {
            Field foundPortField = _findFieldByPortName(unescapedActorPortName);
                    

            if (foundPortField == null) {
                throw new NoSuchFieldException("Could not find port " + unescapedActorPortName);
            }

            String portOrParameter = "((" + getComponent().getClass().getName()
                + ")$actorSymbol(actor))." 
                + foundPortField.getName() + ( portParameter instanceof PortParameter 
                        ? ".getPort()" : "");
            
            code.append("    $actorSymbol(container).connect($actorSymbol(" + escapedCodegenPortName +"), "
                    + portOrParameter
                    + ");" + _eol);
            if (port.isOutput()) {
                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                code.append("    " + portOrParameter + ".setTypeEquals("
                    + _typeToBaseType(((TypedIOPort)port).getType())
                    +");" + _eol);
            }

        } catch (NoSuchFieldException ex) {
//              throw new IllegalActionException(getComponent(), ex,
//                      "Could not find field that corresponds with " + unescapedActorPortName);
            // The port is not a field
            code.append("new TypedIOPort($actorSymbol(actor), \""
                    + unescapedActorPortName.replace("\\", "\\\\") + "\", " 
                    + port.isInput() + ", "
                    + port.isOutput() + ");" + _eol);

            String portOrParameter = 
                "(TypedIOPort)$actorSymbol(actor).getPort(\"" 
                + unescapedActorPortName.replace("\\", "\\\\") + "\")";
            code.append("    $actorSymbol(container).connect($actorSymbol(" + escapedCodegenPortName +"), "
                    + portOrParameter
                    + ");" + _eol);
            if (port.isOutput()) {
                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                code.append("    (" + portOrParameter + ").setTypeEquals("
                    + _typeToBaseType(((TypedIOPort)port).getType())
                    +");" + _eol);
            }
        }
        return code.toString();
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
            ArrayType array = (ArrayType)type;

            String codeGenElementType = getCodeGenerator().codeGenType(array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(array.getDeclaredElementType());

            String ptolemyData = "$actorSymbol(" + actorPortName + "_ptolemyData)";
            return
                // Get the data from the Ptolemy port
                type.getTokenClass().getName() + " " + ptolemyData + " = (("
                + type.getTokenClass().getName() + ")($actorSymbol("
                + codegenPortName + ").getInside(0"
                // For non-multiports "". For multiports, ", 0", ", 1" etc.
                + (channel == 0 ? "" : ", " + channel)
                + ")));" + _eol
                // Create an array for the codegen data.
                + _eol + getCodeGenerator().comment("AutoAdapter: FIXME: This will leak. We should check to see if the token already has been allocated")
                + " Token $actorSymbol(" + portData + ") = $Array_new("
                + "((ptolemy.data.ArrayToken)" + ptolemyData + ").length(), 0);" + _eol

                // Copy from the Ptolemy data to the codegen data.
                + " for (int i = 0; i < ((ptolemy.data.ArrayToken)" + ptolemyData + ").length(); i++) {" + _eol
                + "   Array_set($actorSymbol(" + portData + "), i, "
                + getCodeGenerator().codeGenType(array.getDeclaredElementType())
                + "_new(((("
                + codeGenElementType + "Token)(" + ptolemyData + ".getElement(i)))."
                + targetElementType + "Value())));" + _eol
                + "  }" + _eol;
        } else if (type == BaseType.COMPLEX) {
            return "$targetType(" + actorPortName + ") $actorSymbol(" + portData + ");" + _eol
                + "Complex complex = (Complex)(((" + type.getTokenClass().getName() + ")"
                + "($actorSymbol(" + codegenPortName + ").getInside(0"
                + ")))." + type.toString().toLowerCase() + "Value());" + _eol
                + "double real = complex.real;" + _eol
                + "double imag = complex.imag;" + _eol
                + "$actorSymbol(" + portData + ") = $typeFunc(TYPE_Complex::new(real, imag));" + _eol;

                // For non-multiports "". For multiports, ", 0", ", 1" etc.
                //+ (channel == 0 ? "" : ", " + channel)
        } else {
            return "$targetType(" + actorPortName + ") $actorSymbol(" + portData + ");" + _eol
                + "$actorSymbol(" + portData + ") = "
                +  "((" + type.getTokenClass().getName() + ")"
                + "($actorSymbol(" + codegenPortName + ").getInside(0"
                + ")))." + type.toString().toLowerCase() + "Value();" + _eol;

                // For non-multiports "". For multiports, ", 0", ", 1" etc.
                //+ (channel == 0 ? "" : ", " + channel)
        }
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
            
            ArrayType array = (ArrayType)type;

            String codeGenElementType = getCodeGenerator().codeGenType(array.getDeclaredElementType()).replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(array.getDeclaredElementType());
            String ptolemyData = "$actorSymbol(" + actorPortName + "_ptolemyData)";
            return
                "{" + _eol
//                 // Get the data from the Ptolemy port
//                 + type.getTokenClass().getName() + " " + ptolemyData + "= (("
//                 + type.getTokenClass().getName() + ")($actorSymbol("
//                 + codegenPortName + ").getInside(0"
//                 // For non-multiports "". For multiports, ", 0", ", 1" etc.
//                 + (channel == 0 ? "" : ", " + channel)
//                 + ")));" + _eol

                // Create an array for the codegen data.
                + _eol + getCodeGenerator().comment("AutoAdapter: FIXME: This will leak. We should check to see if the token already has been allocated")
                + " Token codeGenData = $Array_new("
                + "((ptolemy.data.ArrayToken)" + ptolemyData + ").length() , 0);" + _eol

                // Copy from the Ptolemy data to the codegen data.
                + " for (int i = 0; i < ((ptolemy.data.ArrayToken)" + ptolemyData +").length(); i++) {" + _eol
                + "   Array_set(codeGenData, i, "
                + getCodeGenerator().codeGenType(array.getDeclaredElementType())
                + "_new(((("
                + codeGenElementType + "Token)(" + ptolemyData + ".getElement(i)))."
                + targetElementType + "Value())));" + _eol
                + "  }" + _eol

                // Output our newly constructed token
                + " $put(" + actorPortName
                + ", codeGenData);" + _eol
                + "}" + _eol;
        } else {
            String portData = actorPortName + "_portData"
                + (channel == 0 ? "" : channel);
            return
                "$put(" + actorPortName
                // Refer to the token by the full class name and obviate the
                // need to manage imports.
                // + ", ((" + type.getTokenClass().getName() + ")($actorSymbol("
                + ", ($actorSymbol("
                + portData + ")))";
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
     */
    private String _generateSendInside(String actorPortName,
            String codegenPortName, Type type, int channel) {
        actorPortName = TemplateParser.escapePortName(actorPortName);
        codegenPortName = TemplateParser.escapePortName(codegenPortName);
        if (type instanceof ArrayType) {
            
            ArrayType array = (ArrayType)type;

            String javaElementType = getCodeGenerator().codeGenType(array.getDeclaredElementType());
            String codeGenElementType = javaElementType.replace("Integer", "Int");
            String targetElementType = getCodeGenerator().targetType(array.getDeclaredElementType());
            String ptolemyData = "$actorSymbol(" + actorPortName + "_ptolemyData)";
            return
                "{" + _eol
                // Get the codegen data
                + " Token codeGenData = $get("
                + actorPortName
                // For non-multiports "". For multiports, #0, #1 etc.
                + (channel == 0 ? "" : "#" + channel)
                + ");" + _eol

                // Create a token to send
                + codeGenElementType + "Token [] " + ptolemyData + " = new "
                + codeGenElementType + "Token [((Array)codeGenData.getPayload()).size];" + _eol


                // Copy from the codegen data to the Ptolemy data
                + " for (int i = 0; i < ((Array)codeGenData.getPayload()).size; i++) {" + _eol
                + "   " + ptolemyData+ "[i] = new " + codeGenElementType + "Token((("
                + javaElementType
                + ")(Array_get(codeGenData, i).getPayload()))." + targetElementType + "Value());" + _eol
                + " }" + _eol
                

                // Set the type.
                + "    $actorSymbol(" + codegenPortName + ").setTypeEquals("
                + _typeToBaseType(type) +");" + _eol
                // Output our newly constructed token
                + " $actorSymbol(" + codegenPortName + ").sendInside(0, new ArrayToken("
                + ptolemyData + "));" + _eol
                + "}" + _eol;
        } else if (type == BaseType.COMPLEX) {
            return
                // Set the type.
                "    $actorSymbol(" + codegenPortName + ").setTypeEquals("
                + _typeToBaseType(type) +");" + _eol
                // Send data to the actor.
                + "    $actorSymbol(" + codegenPortName + ").sendInside(0, new "
                // Refer to the token by the full class name and obviate the
                // need to manage imports.
                + type.getTokenClass().getName()
                // Get the real portion of the Complex number.
                + "(new Complex(((ComplexCG)($get(" + actorPortName
                // For non-multiports "". For multiports, #0, #1 etc.
                + (channel == 0 ? "" : "#" + channel)
                + ")).payload).real,"
                // Get the imaginary portion of the Complex number.
                + "((ComplexCG)($get(" + actorPortName
                // For non-multiports "". For multiports, #0, #1 etc.
                + (channel == 0 ? "" : "#" + channel)
                + ")).payload).imag))"
                + ");" + _eol;
        } else {
            return
                // Set the type.
                "    $actorSymbol(" + codegenPortName + ").setTypeEquals("
                + _typeToBaseType(type) +");" + _eol
                // Send data to the actor.
                + "    $actorSymbol(" + codegenPortName + ").sendInside(0, new "
                // Refer to the token by the full class name and obviate the
                // need to manage imports.
                + type.getTokenClass().getName() + "($get(" + actorPortName
                // For non-multiports "". For multiports, #0, #1 etc.
                + (channel == 0 ? "" : "#" + channel)
                + ")));" + _eol;
        }
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
                + _typeToBaseType(((ArrayType)type).getDeclaredElementType())
                + ")";
        }
        return "BaseType." + type.toString().toUpperCase();
    }

}
