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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
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
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
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
     *  @exception IllegalActionException Not thrown in this base class.  
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
 
        String includeFiles = generateIncludeFiles();
        String sharedCode = generateSharedCode();
        String preinitializeCode = generatePreinitializeCode();
        String initializeCode = generateInitializeCode();
        String bodyCode = generateBodyCode();
        generateVariableDeclarations(code);
        
        code.append(includeFiles);
        code.append(sharedCode);
        code.append(preinitializeCode);
        code.append("main(int argc, char *argv[]) {\n");
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
    
    /** Generate include files.   
     *  @return The include files.
     *  @throws IllegalActionException If the helper class for some actor cannot 
     *  be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
        
        StringBuffer code = new StringBuffer();
      
        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
            code.append("#include " + file + "\n");
        }

        return code.toString();
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

        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateInitializeCode());
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
        
        CompositeEntity model = (CompositeEntity) getContainer();
        
        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + model.getName() + " does not have a director.");
        }
        
        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generatePreinitializeCode());
             
        Attribute iterations = director.getAttribute("iterations");
        if (iterations != null) {
            int iterationCount = 
                ((IntToken) ((Variable) iterations).getToken()).intValue();
            if (iterationCount > 0) {
                code.append("int iteration = 0;\n");
            }
        }
               
        return code.toString();
    }

    /**
     * Generate code shared by helper actors, including globally defined
     * data struct types and static methods or variables shared by multiple
     * instances of the same helper actor type.   
     * @return The shared code of the containing composite actor.
     * @throws IllegalActionException If an error ocurrs when generating
     *  the globally shared code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the shared code. 
     */
    public String generateSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Generate shared code for " + 
                getContainer().getFullName()));
        
        CodeStream tmpStream = new CodeStream("$CLASSPATH/ptolemy/codegen/kernel/SharedCode.c");
        tmpStream.appendCodeBlock("globalBlock");
        code.append(tmpStream.toString());   
        
        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        
        Set sharedCodeBlocks = compositeActorHelper.generateSharedCode();
  
        Iterator blocks = sharedCodeBlocks.iterator();
        while (blocks.hasNext()) {
            String block = (String) blocks.next();
            code.append(block);
        }
        
        code.append(comment("Finished generate shared code for " + 
                getContainer().getFullName()));

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

        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        compositeActorHelper.generateVariableDeclaration(code);
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

        TypedCompositeActor compositeActorHelper 
                = (TypedCompositeActor) _getHelper(getContainer());
        compositeActorHelper.generateWrapupCode(code);
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
    
    /** Th method is used to set the code generator for a helper class.
     *  Since this is not a helper class for a component, this method does 
     *  nothing.
     *  @param codeGenerator
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
            
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

        ComponentCodeGenerator castHelperObject = 
            (ComponentCodeGenerator) helperObject;
        
        castHelperObject.setCodeGenerator(this);

        _helperStore.put(component, helperObject);

        return castHelperObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A hash map that stores the code generator helpers associated
    // with the actors.
    private HashMap _helperStore = new HashMap();
}
