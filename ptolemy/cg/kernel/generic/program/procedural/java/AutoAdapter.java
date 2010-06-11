/* A code generator adapter that is auto generated and calls actor code.

 Copyright (c) 2010 The Regents of the University of California.
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

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
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
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
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
     * @param codeGenerator The code generator with which to associate the adapter.
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
        // Use the full class name so that we don't have to import the actor.  If we import
        // the actor, then we cannot have model names with the same name as the actor.
        String actorClassName = getComponent().getClass().getName();

        StringBuffer code = new StringBuffer(
                getCodeGenerator().comment("AutoAdapter._generateInitalizeCode() start")
                + "try {\n"
                + "    $actorSymbol(container) = new TypedCompositeActor();\n"
                + "    $actorSymbol(actor) = new " + actorClassName + "($actorSymbol(container), \"$actorSymbol(actor)\");\n");

        // Generate code that creates and connects each port.
        Iterator entityPorts = ((Entity)getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof IOPort) {
                IOPort castPort = (IOPort) insidePort;
                String name = castPort.getName();
                if (!castPort.isMultiport()) {
                    code.append(_generatePortInstantiation(name, name, castPort));
                } else {
                    // Multiports.
                    TypedIOPort actorPort = (TypedIOPort)(((Entity)getComponent()).getPort(name));

                    // Set the type of the port of the actor
                    code.append("    ((" + getComponent().getClass().getName()
                            + ")$actorSymbol(actor))." + name + ".setTypeEquals(BaseType."
                            + actorPort.getType().toString().toUpperCase() +");\n");

                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append(_generatePortInstantiation(name, name + "Source" + i, actorPort));
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append(_generatePortInstantiation(name, name + "Sink" + i, actorPort));
                    }
                }
            }
        }

        code.append("    new ptolemy.actor.Director($actorSymbol(container), \"director\");\n"
                + "    $actorSymbol(container).preinitialize();\n"
                + "} catch (Exception ex) {\n"
                + "    throw new RuntimeException(\"Failed to create $actorSymbol(actor))\", ex);\n"
                + "}\n"
                + "try {\n"
                + "    TypedCompositeActor.resolveTypes($actorSymbol(container));\n"
                + "    $actorSymbol(actor).initialize();\n"
                + "} catch (Exception ex) {\n"
                + "    throw new RuntimeException(\"Failed to initalize $actorSymbol(actor))\", ex);\n"
                + "}\n");


        // Handle parameters.
        Iterator parameters = getComponent().attributeList(Settable.class).iterator();
        while (parameters.hasNext()) {
            Settable parameter = (Settable) parameters.next();
            if (!ptolemy.actor.gui.Configurer.isVisible(getComponent(), parameter)) {
                continue;
            }

            String parameterName = parameter.getName();
            if (parameterName.equals("firingsPerIteration")) {
                continue;
            }

            // FIXME: handle multiline values
            String parameterValue = "";
            if (parameter instanceof Variable) {
                // Evaluate things like $PTII
                parameterValue = ((Variable)parameter).getToken().toString();
                System.out.println("AutoAdapter: " + parameter + " " + parameterValue);
            } else {
                parameterValue = parameter.getExpression();
            }

            // FIXME: do we want one try block per parameter?  It does
            // make for better error messages.
            code.append("try {\n"
                + "    ptolemy.data.expr.Parameter " + parameterName + " = ((" + actorClassName + ")$actorSymbol(actor))." + parameterName + ";\n"
                    + "    " + parameterName + ".setExpression(\"" + StringUtilities.escapeString(parameterValue) + "\");\n"
                + "    ((" + actorClassName + ")$actorSymbol(actor)).attributeChanged(" + parameterName + ");\n"
                + "} catch (Exception ex) {\n"
                    + "    throw new RuntimeException(\"Failed to set parameter \\\"" + parameterName
                    + "\\\" in $actorSymbol(actor) to \\\"" + StringUtilities.escapeString(parameterValue) + "\\\"\", ex);\n"
                + "}\n");
        }
        code.append(getCodeGenerator().comment("AutoAdapter._generateInitalizeCode() start"));

        return processCode(code.toString());
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
        StringBuffer code = new StringBuffer("TypedCompositeActor $actorSymbol(toplevel);\n"
                + "TypedCompositeActor $actorSymbol(container);\n"
                + "TypedAtomicActor $actorSymbol(actor);\n");

        // Handle inputs and outputs on a per-actor basis.
        Iterator entityPorts = ((Entity)getComponent()).portList().iterator();
        while (entityPorts.hasNext()) {
            ComponentPort insidePort = (ComponentPort) entityPorts.next();
            if (insidePort instanceof IOPort) {
                IOPort castPort = (IOPort) insidePort;
                String name = castPort.getName();
                if (!castPort.isMultiport()) {
                    code.append("TypedIOPort $actorSymbol(" + name + ");\n");
                } else {
                    // FIXME: We instantiate a separate external port for each channel
                    // of the multiport.  Could we just connect directly to the channels
                    // of the multiport?  The problem I had was that the receivers are
                    // not created if I connect directly to the channels.
                    IOPort actorPort = (IOPort)(((Entity)getComponent()).getPort(name));

                    int sources = actorPort.numberOfSources();
                    for (int i = 0; i < sources; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name + "Source" + i + ");\n");
                    }

                    int sinks = actorPort.numberOfSinks();
                    for (int i = 0; i < sinks; i++) {
                        code.append("TypedIOPort $actorSymbol(" + name + "Sink" + i + ");\n");
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
    public static AutoAdapter getAutoAdapter(GenericCodeGenerator codeGenerator, Object object) {
        // FIXME: I'm not sure if we need this method, but I like calling something
        // that returns null if the associated actor cannot be found or is of the wrong type.
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
	String javaClassPath = StringUtilities.getProperty("java.class.path");
	StringTokenizer tokenizer = new StringTokenizer(javaClassPath,
					       File.pathSeparator);
	while (tokenizer.hasMoreTokens()) {
	    ((ProceduralCodeGenerator)getCodeGenerator()).addLibraryIfNecessary(tokenizer.nextToken());
	}

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
        StringBuffer code = new StringBuffer("try {\n");

        code.append(_eol + getCodeGenerator().comment("AutoAdapter._generateFireCode() start"));
        // FIXME: it is odd that we are transferring data around in the fire code.
        // Shouldn't we do this in prefire() and posfire()?

        // Transfer data from the codegen variables to the actor input ports.
        Iterator inputPorts = ((Actor)getComponent()).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String name = inputPort.getName();
            Type type = inputPort.getType();

            if (!inputPort.isMultiport()) {
                code.append(_generateSendInside(name, name, type, 0));
            } else {
                // Multiports.

                // Generate code for the sources.  We don't use getWidth() here
                // because IOPort.getWidth() says not to.
                int sources = inputPort.numberOfSources();
                for (int i = 0; i < sources; i++) {
                    code.append(_generateSendInside(name, name + "Source" + i, type, i));
                }

                // Generate code for the sinks.
                int sinks = inputPort.numberOfSinks();
                for (int i = 0; i < sinks; i++) {
                    code.append(_generateSendInside(name, name + "Sink" + i, type, i));
                }
            }
        }

        // Fire the actor.
        code.append("    $actorSymbol(actor).fire();\n");

        // Transfer data from the actor output ports to the codegen variables.
        Iterator outputPorts = ((Actor)getComponent()).outputPortList().iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            String name = outputPort.getName();
            Type type = outputPort.getType();

            // Get data from the actor.
            if (!outputPort.isMultiport()) {
                code.append(_generateGetInside(name, name, type, 0));
            } else {
                // Multiports.
                int sources = outputPort.numberOfSources();
                for (int i = 0; i < sources; i++) {
                    code.append(_generateGetInside(name, name + "Source" + i, type, i));
                }

                int sinks = outputPort.numberOfSinks();
                for (int i = 0; i < sinks; i++) {
                    code.append(_generateGetInside(name, name + "Sink" + i, type, i));
                }
            }

        }

        code.append("} catch (Exception ex) {\n"
                + "    throw new RuntimeException(\"Failed to fire() $actorSymbol(actor))\" /*+ $actorSymbol(toplevel).exportMoML()*/, ex);\n"
                + " };\n");
        code.append(_eol + getCodeGenerator().comment("AutoAdapter._generateFireCode() end"));
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /**
     * Generate execution code for the actor execution methods.
     * @param executionMethod One of "prefire", "postfire" or "wrapup".
     * @return The execution code for the corresponding method.
     * @exception IllegalActionException If illegal macro names are found.
     */
    private String _generateExecutionCode(String executionMethod)
            throws IllegalActionException {
        // Syntactic sugar, avoid code duplication.
        String code = "try {\n"
            + "    $actorSymbol(actor)." + executionMethod + "();\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to "
            + executionMethod  + "() $actorSymbol(actor))\", ex);\n"
            + "};\n";
        return processCode(code.toString());
    }

    /** Return the code necessary to instantiate the port.
     *  @param actorPortName The name of the Actor port to be instantiated.
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

        PortParameter portParameter = (PortParameter)getComponent().getAttribute(actorPortName,
                PortParameter.class);

        StringBuffer code = new StringBuffer("    $actorSymbol(" + codegenPortName + ") = new TypedIOPort($actorSymbol(container)"
                + ", \"" + codegenPortName + "\", "
                + port.isInput()
                + ", " + port.isOutput() + ");\n"
                // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                + "    $actorSymbol(" + codegenPortName + ").setTypeEquals(BaseType."
                + ((TypedIOPort)port).getType().toString().toUpperCase() +");\n");

        try {
            // Make sure that there is a field with that name
            // $PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml
            // has a NonStrict actor with an output that is not connected.
            // If we don't check for the field, then the generated Java code
            // fails.
            getComponent().getClass().getField(actorPortName);

            String portOrParameter = "((" + getComponent().getClass().getName()
                + ")$actorSymbol(actor))." 
                + actorPortName + ( portParameter instanceof PortParameter 
                        ? ".getPort()" : "");
            
            code.append("    $actorSymbol(container).connect($actorSymbol(" + codegenPortName +"), "
                    + portOrParameter
                    + ");\n"
                    // Need to set the type for ptII/ptolemy/actor/lib/string/test/auto/StringCompare.xml
                    + "    " + portOrParameter + ".setTypeEquals(BaseType."
                    + ((TypedIOPort)port).getType().toString().toUpperCase() +");\n");

        } catch (NoSuchFieldException ex) {
            // Ignore.
        }
        return code.toString();

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
        return
            "$put(" + actorPortName
            // Refer to the token by the full class name and obviate the
            // need to manage imports.
            + ", ((" + type.getTokenClass().getName() + ")($actorSymbol("
            + codegenPortName
            + ").getInside(0"
            // For non-multiports "". For multiports, ", 0", ", 1" etc.
            + (channel == 0 ? "" : ", " + channel)
            + ")))." + type.toString().toLowerCase() + "Value());\n";
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
        return
            // Set the type.
            "    $actorSymbol(" + codegenPortName + ").setTypeEquals(BaseType."
                        + type.toString().toUpperCase() +");\n"
            // Send data to the actor.
            + "    $actorSymbol(" + codegenPortName + ").sendInside(0, new "
            // Refer to the token by the full class name and obviate the
            // need to manage imports.
            + type.getTokenClass().getName() + "($get(" + actorPortName
            // For non-multiports "". For multiports, #0, #1 etc.
            + (channel == 0 ? "" : "#" + channel)
            + ")));\n";
    }
}