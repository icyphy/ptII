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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.cg.kernel.generic.CodeGeneratorUtilities;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
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
        TemplateParser templateParser = new TemplateParser();
        setTemplateParser(templateParser);
        templateParser.setCodeGenerator(codeGenerator);
        // FIXME: Is the next line necessary?
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
        ptolemy.actor.lib.string.StringFunction actor = (ptolemy.actor.lib.string.StringFunction) getComponent();
        // FIXME: handle parameters
        String fun = actor.function.getExpression();
        // FIXME: handle multiports
        String code = "try {\n"
            // FIXME: what about MirrorComposite?
            + "    $actorSymbol(container) = new TypedCompositeActor();\n"
            + "    $actorSymbol(actor) = new StringFunction($actorSymbol(container), \"$actorSymbol(actor)\");\n"
            + "    $actorSymbol(input) = new TypedIOPort($actorSymbol(container), \"input\", true, false);\n"
            + "    $actorSymbol(input).setTypeEquals(BaseType.STRING);\n"
            + "    $actorSymbol(output) = new TypedIOPort($actorSymbol(container), \"output\", false, true);\n"
            + "    $actorSymbol(output).setTypeEquals(BaseType.STRING);\n"
            + "    $actorSymbol(container).connect($actorSymbol(input), ((StringFunction)$actorSymbol(actor)).input);\n"
            + "    $actorSymbol(container).connect($actorSymbol(output), ((StringFunction)$actorSymbol(actor)).output);\n"

            + "    new ptolemy.actor.Director($actorSymbol(container), \"director\");\n"
    
            + "    $actorSymbol(container).preinitialize();\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to create $actorSymbol(actor))\", ex);\n"
            + "}\n"
            + "try {\n"
            + "    TypedCompositeActor.resolveTypes($actorSymbol(container));\n"
            + "    $actorSymbol(actor).initialize();\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to initalize $actorSymbol(actor))\", ex);\n"
            + "}\n"
            + "try {\n"
            + "    ptolemy.data.expr.Parameter function = ((StringFunction)$actorSymbol(actor)).function;\n"
            + "    function.setExpression(\"" + fun + "\");\n"
            + "    ((StringFunction)$actorSymbol(actor)).attributeChanged(function);\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to set function in $actorSymbol(actor) to " + fun +"\", ex);\n"
            + "}\n";
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
        String code = "TypedCompositeActor $actorSymbol(toplevel);\n"
            + "TypedCompositeActor $actorSymbol(container);\n"
            + "TypedAtomicActor $actorSymbol(actor);\n"
            + "TypedIOPort $actorSymbol(input);\n"
            + "TypedIOPort $actorSymbol(output);\n";
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
        // FIXME: I'm not sure if we need this method, but I like calliing something
        // that returns null if the associated actor cannot be found.
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
        // FIXME: The types will change depending on what types are used by the actor.
        files.add("ptolemy.data.StringToken;");

        files.add("ptolemy.data.type.BaseType;");
        files.add("ptolemy.actor.TypedAtomicActor;");
        files.add("ptolemy.actor.TypedCompositeActor;");
        files.add("ptolemy.actor.TypedIOPort;");

        // FIXME: The actor itself will change.
        files.add("ptolemy.actor.lib.string.StringFunction;");
        ((ProceduralCodeGenerator)getCodeGenerator()).addLibraryIfNecessary("$(PTII)");
        return files;
    }


    /**
     * Generate the fire code. 
     * <p>Generate code that creates tokens, sends them to the input(s) of inner
     * Ptolemy actor, calls fire() on the actor and reads the outputs.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generateFireCode() throws IllegalActionException {
        // FIXME: what if the inline parameter is set?
        return "try {\n"
            // FIXME: Don't forget about multiports
            // FIXME: get inputs depending on the actor
            + "    $actorSymbol(input).sendInside(0, new StringToken($get(input)));\n"
            + "    $actorSymbol(actor).fire();\n"
            // FIXME: Don't forget about multiports
            // FIXME: get outputs depending on the actor
            + "    $put(output, ((StringToken)($actorSymbol(output).getInside(0))).stringValue());\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to fire() $actorSymbol(actor))\" /*+ $actorSymbol(toplevel).exportMoML()*/, ex);\n"
            + " };\n";
    }

    /**
     * Generate execution code.
     * @param executionMethod One of "prefire", "postfire" or "wrapup".
     * @return The execution code for the corresponding method.
     * @exception IllegalActionException If illegal macro names are found.
     */
    private String _generateExecutionCode(String executionMethod) throws IllegalActionException {
        // Syntactic sugar, avoid code duplication
        String code = "try {\n"
            + "    $actorSymbol(actor)." + executionMethod + "();\n"
            + "} catch (Exception ex) {\n"
            + "    throw new RuntimeException(\"Failed to " + executionMethod  + "() $actorSymbol(actor))\", ex);\n"
            + "};\n";
        return processCode(code.toString());
    }
}