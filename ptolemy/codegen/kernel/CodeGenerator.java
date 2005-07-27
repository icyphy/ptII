/* Base class for code generators.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** Base class for code generator.
 *
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou,
 *   Contributors: Christopher Brooks
 *  @version $Id$ CodeGenerator.java,v 1.51 2005/07/13 14:07:20 cxh Exp $
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {
    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/codegen");
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");

        overwriteFiles = new Parameter(this, "overwriteFiles");
        overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
        overwriteFiles.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;

    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/". Subclasses may override this
     *  produce comments that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        return "/* " + comment + " */\n";
    }

    /** Generate the body code that lies between initialize and wrapup.
     *  In this base class, nothing is generated.
     *  @exception Not Thrown in this base class.
     *  @return The empty string.
     */
    public String generateBodyCode() throws IllegalActionException {
        return "";
    }

    /** Generate code and write it to the file specified by the
     *  <i>codeDirectory</i> parameter.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public void generateCode() throws KernelException {
        generateCode(new StringBuffer());
    }

    /** Generate code and append it to the given string buffer.
     *  Write the code to the file specified by the codeDirectory parameter.
     *  This is the main entry point.
     *  @param code The given string buffer.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public void generateCode(StringBuffer code) throws KernelException {
        // Get the code generator helper associated with the director of
        // the container first.
        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();
        ComponentCodeGenerator directorHelper = _getHelper((NamedObj) director);
        ((Director) directorHelper).setCodeGenerator(this);

        Set includingFiles = new HashSet();
        Iterator actors = ((CompositeActor) getContainer()).deepEntityList()
                .iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            actorHelper.setCodeGenerator(this);
            includingFiles.addAll(actorHelper.getIncludingFiles());
        }

        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
            code.append("#include " + file + "\n");
        }

        String preinitializeCode = generatePreinitializeCode();
        String initializeCode = generateInitializeCode();
        String bodyCode = generateBodyCode();
        generateVariableDeclarations(code);
        code.append("main(int argc, char *argv[]) {\n");
        code.append(preinitializeCode);
        code.append(initializeCode);
        code.append(bodyCode);
        generateWrapupCode(code);
        code.append("}\n");

        // Write the code to the file specified by codeDirectory.
        try {
            // Check if needs to overwrite.
            if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                    && codeDirectory.asFile().exists()) {
                // FIXME: It is totally bogus to ask a yes/no question
                // like this, since it makes it impossible to call
                // this method from a script.  If the question is
                // asked, the build will hang.
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }

            Writer writer = codeDirectory.openForWriting();
            writer.write(code.toString());
            codeDirectory.close();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeDirectory.asFile() + "\"");
        }
    }

    /** 
     * Return the code associated with initialization of the containing
     * composite actor. This method calls the generateInitializeCode()
     * method of the code generator helper associated with the model director.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If the helper class for the model
     *  director cannot be found or if an error occurs when the director
     *  helper generates initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Initialize " + getContainer().getFullName()));

        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();
        ComponentCodeGenerator directorHelper = _getHelper((NamedObj) director);
        code.append(directorHelper.generateInitializeCode());
        return code.toString();
    }

    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator helper associated with the model director
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();
        ComponentCodeGenerator directorHelper = _getHelper((NamedObj) director);
        code.append(((Director) directorHelper).generatePreinitializeCode());
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param code The given string buffer.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public void generateVariableDeclarations(StringBuffer code)
            throws IllegalActionException {
        code.append(comment("Variable Declarations "
                + getContainer().getFullName()));

        Iterator actors = ((CompositeActor) getContainer()).deepEntityList()
                .iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            // Generate variable declarations for referenced parameters.
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            HashSet parameterSet = actorHelper.getReferencedParameter();

            if (parameterSet != null) {
                Iterator parameters = parameterSet.iterator();

                while (parameters.hasNext()) {
                    Parameter parameter = (Parameter) parameters.next();
                    boolean isArrayType = _generateType(parameter, code);

                    if (isArrayType) {
                        code.append("[ ]");
                    }

                    code.append(" = ");
                    code.append(parameter.getToken().toString());
                    code.append(";\n");
                }
            }

            // Generate variable declarations for input ports.
            Iterator inputPorts = actor.inputPortList().iterator();

            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

                if (inputPort.getWidth() == 0) {
                    break;
                }

                // FIXME: What if port is ArrayType.
                _generateType(inputPort, code);

                if (inputPort.isMultiport()) {
                    code.append("[" + inputPort.getWidth() + "]");
                }

                int bufferSize = actorHelper.getBufferSize(inputPort);

                //int bufferSize = directorHelper.getBufferSize(inputPort);
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }

                code.append(";\n");
            }

            // Generate variable declarations for output ports.
            Iterator outputPorts = actor.outputPortList().iterator();

            while (outputPorts.hasNext()) {
                TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

                // Only generate declarations for those output ports with
                // port width zero.
                if (outputPort.getWidth() == 0) {
                    _generateType(outputPort, code);
                    code.append(";\n");
                }
            }
        }
    }

    /** Generate into the specified code stream the code associated with
     *  wrapping up the container composite actor. This method calls the
     *  generateWrapupCode() method of the code generator helper associated
     *  with the director of this container.
     *  @param code The code stream into which to generate the code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public void generateWrapupCode(StringBuffer code)
            throws IllegalActionException {
        code.append(comment("Wrapup " + getContainer().getFullName()));

        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();
        ComponentCodeGenerator directorHelper = _getHelper((NamedObj) director);
        directorHelper.generateWrapupCode(code);
    }

    /** Return the associated component, which is always the container.
     *  @return The helper to generate code.
     */
    public NamedObj getComponent() {
        return getContainer();
    }

    /** Generate code for a model.
     *  <p>For example:
     *  <pre>
     *  java -classpath $PTII ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  or
     *  <pre>
     *  $PTII/bin/ptinvoke ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @exception Exception If any error occurs.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.codegen.kernel.CodeGenerator model.xml "
                    + "[model.xml . . .]\n"
                    + "  The arguments name MoML files containing models");
        }

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        parser.setMoMLFilters(BackwardCompatibility.allFilters());
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {
            // Note: the code below uses explicit try catch blocks
            // so we can provide very clear error messages about what
            // failed to the end user.  The alternative is to wrap the
            // entire body in one try/catch block and say
            // "Code generation failed for foo", which is not clear.
            URL modelURL;

            try {
                modelURL = new File(args[i]).toURL();
            } catch (Exception ex) {
                throw new Exception("Could not open \"" + args[i] + "\"", ex);
            }

            CompositeActor toplevel = null;

            try {
                try {
                    toplevel = (CompositeActor) parser.parse(null, modelURL);
                } catch (Exception ex) {
                    throw new Exception("Failed to parse \"" + args[i] + "\"",
                            ex);
                }

                // Get all instances of this class contained in the model
                List codeGenerators = toplevel
                        .attributeList(CodeGenerator.class);

                CodeGenerator codeGenerator;

                if (codeGenerators.size() == 0) {
                    // Add a codeGenerator
                    codeGenerator = new CodeGenerator(toplevel,
                            "CodeGenerator_AutoAdded");
                } else {
                    // Get the last CodeGenerator in the list, maybe
                    // it was added last?
                    codeGenerator = (CodeGenerator) codeGenerators
                            .get(codeGenerators.size() - 1);
                }

                System.out.println("CodeGenerator: " + codeGenerator);

                try {
                    codeGenerator.generateCode();
                } catch (KernelException ex) {
                    throw new Exception("Failed to generate code for \""
                            + args[i] + "\"", ex);
                }
            } finally {
                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                }
            }
        }
    }

    /** Set the container of this object to be the given container.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeEntity.
     *  @exception NameDuplicationException If there already exists a
     *   container with the same name.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if ((container != null) && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                            + " by CompositeEntity");
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        if (_helperStore.containsKey(component)) {
            return (ComponentCodeGenerator) _helperStore.get(component);
        }

        String packageName = generatorPackage.stringValue();

        String componentClassName = component.getClass().getName();
        String helperClassName = componentClassName.replaceFirst("ptolemy",
                packageName);

        Class helperClass = null;

        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Cannot find helper class " + helperClassName);
        }

        Constructor constructor = null;

        try {
            constructor = helperClass.getConstructor(new Class[] { component
                    .getClass() });
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " + helperClassName
                            + " which accepts an instance of "
                            + componentClassName + " as the argument.");
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component });
        } catch (Exception e) {
            throw new IllegalActionException((NamedObj) component, e,
                    "Failed to create helper class code generator.");
        }

        if (!(helperObject instanceof ComponentCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: " + component
                            + ". Its helper class does not"
                            + " implement componentCodeGenerator.");
        }

        ComponentCodeGenerator castHelperObject = (ComponentCodeGenerator) helperObject;

        _helperStore.put(component, helperObject);

        return castHelperObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a port or parameter, append a string in the form
     *  "static <i>type</i> <i>objectName</i>" to the given string buffer.
     *  Return true if the type of the port or parameter is ArrayType. This
     *  method is only called in the generateVariableDeclarations() method.
     *  @param namedobj The port or parameter.
     *  @param code The string buffer that contains the generated code.
     *  @return True if the type the port or parameter is ArrayType.
     */
    private boolean _generateType(NamedObj namedobj, StringBuffer code) {
        String type = "";

        if (namedobj instanceof Parameter) {
            type = ((Parameter) namedobj).getType().toString();
        } else if (namedobj instanceof TypedIOPort) {
            type = ((TypedIOPort) namedobj).getType().toString();
        }

        boolean isArrayType = false;

        if (type.charAt(0) == '{') {
            // This is an ArrayType.
            StringTokenizer tokenizer = new StringTokenizer(type, "{}");
            type = tokenizer.nextToken();
            isArrayType = true;
        }

        if (type.equals("boolean")) {
            type = "unsigned char";
        }

        code.append("static ");
        code.append(type);
        code.append(" ");
        code.append(namedobj.getFullName().replace('.', '_'));
        return isArrayType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A hash map that stores the code generator helpers associated
    // with the actors.
    private HashMap _helperStore = new HashMap();
}
