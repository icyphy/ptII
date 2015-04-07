/* Base class for code generators.

   Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.cg.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveClasses;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// GenericCodeGenerator

/** Base class for code generator.
 *  <p>Derived classes usually override {@link #generateCode(StringBuffer)}.
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributors: Christopher Brooks, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public abstract class GenericCodeGenerator extends Attribute implements
Decorator {
    // Note: If you add publicly settable parameters, update
    // _commandFlags or _commandOptions.

    /** Create a new instance of the code generator.  The file
     *  extension is set to the last package of the <i>generatePackage</i>
     *  parameter.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public GenericCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Note: If you add publicly settable parameters, update
        // _commandFlags or _commandOptions.

        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/cg/");

        // FIXME: This should not be necessary, but if we don't
        // do it, then getBaseDirectory() thinks we are in the current dir.
        codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

        generateInSubdirectory = new Parameter(this, "generateInSubdirectory");
        generateInSubdirectory.setTypeEquals(BaseType.BOOLEAN);
        generateInSubdirectory.setExpression("false");

        generatorPackage = new StringParameter(this, "generatorPackage");

        generatorPackageList = new StringParameter(this, "generatorPackageList");

        overwriteFiles = new Parameter(this, "overwriteFiles");
        overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
        overwriteFiles.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _outputFileExtension = _getOutputFileExtension();

        _model = (CompositeEntity) getContainer();

        _generatorPackageListParser._updateGeneratorPackageList();

        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");

    }

    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @param outputFileExtension The extension of the output file.
     *   (for example c in case of C and java in case of Java)
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public GenericCodeGenerator(NamedObj container, String name,
            String outputFileExtension) throws IllegalActionException,
            NameDuplicationException {
        this(container, name);
        _outputFileExtension = outputFileExtension;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is "<code>$HOME/cg/</code>".
     */
    public FileParameter codeDirectory;

    /** If true, then generate code in a subdirectory of <i>codeDirectory</i>.
     *  The default value is false, indicating that code should be
     *  generated in the directory named by codeDirectory.
     *  If the value is true, then code is generated in a <b>subdirectory</b>
     *  of the directory named by the <i>codeDirectory</i> parameter.
     *  This is useful for languages such as Java, which means that
     *  the code will be generated in a package named by the sanitized
     *  version of the model name.  The default value is a Boolean with
     *  the value "false".
     */
    public Parameter generateInSubdirectory;

    /** The name of the package in which to look for adapter class
     *  code generators. The default value of this parameter
     *  is the empty string.  Derived classes may set this parameter
     *  to values like <code>ptolemy.cg.kernel.generic.program.procedural.c</code>.
     *  <code>ptolemy.cg.kernel.generic.html</code>,
     *  or <code>ptolemy.cg.kernel.generic.program.procedural.java</code>.
     */
    public StringParameter generatorPackage;

    /** The name of the package(s) in which to look for adapter
     *  classes. The string can either be just one package, such as
     *  "generic.program.procedural.java" or a list of semicolon ';' ,
     *  colon ':', space ' ' or asterix '*' separated list of Java
     *  packages, such as
     *  <code>generic.program.procedural.java.target1:generic.program.procedural.java.target2</code>.
     *  The adapter is first searched in the first package.
     *  Adapters are looked up by class name, where the class name
     *  consists of "ptolemy.cg.adapter" + the package name
     *  from this list + "adapters.".  Thus, if generatorPackageList
     *  is set to <code>generic.program.procedural.java</code>, then the
     *  <code>ptolemy.cg.adapter.<b>generic.program.procedural.java</b>.adapters.</code>
     *  package will be searched.
     */
    public StringParameter generatorPackageList;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is the codeDirectory parameter, then set the
     *  base directory of the codeDirectory parameter.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == codeDirectory) {
            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            File directory = codeDirectory.asFile();
            if (directory != null) {
                codeDirectory.setBaseDirectory(directory.toURI());
            }
        } else if (attribute == generateInSubdirectory) {
            // FIXME: we should check to see if generateInSubdirectory is changing
            // before proceeding.
            _sanitizedModelName = CodeGeneratorAdapter.generateName(_model);
            if (((BooleanToken) generateInSubdirectory.getToken())
                    .booleanValue()) {
                _generateInSubdirectory = true;
                if (!codeDirectory.asFile().toString()
                        .endsWith(_sanitizedModelName)) {
                    // Add the sanitized model name as a directory
                    codeDirectory.setExpression(codeDirectory.asFile()
                            .toString() + "/" + _sanitizedModelName);
                    codeDirectory.setBaseDirectory(codeDirectory.asFile()
                            .toURI());
                }
            } else {
                _generateInSubdirectory = false;
                // Dragging a GenericCodeGenerator into an empty model
                // was resulting in a NullPointerException.
                // See test GenericCodeGenerator-1.1
                if (codeDirectory.asFile() != null
                        && codeDirectory.asFile().toString()
                        .endsWith(_sanitizedModelName)) {
                    // Remove the sanitized model name as a directory.
                    String path = codeDirectory.asFile().toString();
                    path = path.substring(0, path.indexOf(_sanitizedModelName));
                    // If path is the empty string, then codeDirectory.asFile() will
                    // return null.
                    codeDirectory.setExpression(path);
                    if (codeDirectory.asFile() != null) {
                        codeDirectory.setBaseDirectory(codeDirectory.asFile()
                                .toURI());
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GenericCodeGenerator newObject = (GenericCodeGenerator) super
                .clone(workspace);
        newObject._adapterStore = new HashMap<Object, CodeGeneratorAdapter>();
        newObject._generatorPackageListParser = new GeneratorPackageListParser();
        newObject._model = null;

        return newObject;
    }

    /** Return a formatted comment containing the
     *  specified string.  In this base class, the empty
     *  string is returned.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        return "";
    }

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return A list of decorated attributes for the target NamedObj,
     *   or null if there is no adapter for this object.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        CodeGeneratorAdapter adapter;
        try {
            adapter = _getAdapter(target);
        } catch (IllegalActionException e) {
            return null;
        }
        if (adapter == null) {
            return null;
        }
        return adapter.createDecoratorAttributes(target, this);
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    @Override
    public List<NamedObj> decoratedObjects() {
        CompositeEntity container = (CompositeEntity) getContainer();
        return container.deepEntityList();
    }

    /** Generate code and write it to the file specified by the
     *  <i>codeDirectory</i> parameter.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public int generateCode() throws KernelException {
        // FIXME: This doesn't make any sense.
        // It writes to a string buffer and then discards the
        // reference to that string buffer... (???)
        return generateCode(new StringBuffer());
    }

    /** Generate code.  This is the main entry point.
     *
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        _sanitizedModelName = CodeGeneratorAdapter.generateName(_model);

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (_isTopLevel()) {

            // If necessary, create a manager.
            Actor container = (Actor) getContainer();
            Manager manager = container.getManager();

            TypedCompositeActor toplevel = (TypedCompositeActor) ((NamedObj) container)
                    .toplevel();

            if (manager == null) {
                manager = new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
            }

            try {
                manager.preinitializeAndResolveTypes();
                //System.out.println(((CompositeActor)container).toplevel().exportMoML());
                //StringUtilities.exit(5);
                returnValue = _generateCode(code);
            } finally {
                // We call wrapup here so that the state gets set to idle.
                // This makes it difficult to test the Exit actor.
                try {
                    long startTime = new Date().getTime();
                    manager.wrapup();
                    _printTimeAndMemory(startTime, "CodeGenerator: "
                            + "wrapup consumed: ");
                } catch (RuntimeException ex) {
                    // The Exit actor causes Manager.wrapup() to throw this.
                    if (!manager.isExitingAfterWrapup()) {
                        throw ex;
                    }
                }
            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            returnValue = _generateCode(code);
        }
        return returnValue;
    }

    /** Generate code for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  Return -2 if no CodeGenerator was created.
     *  @exception Exception If any error occurs.
     */
    public static int generateCode(String[] args) throws Exception {
        URL modelURL = null;
        GenericCodeGenerator codeGenerator = null;
        try {
            if (args.length == 0) {
                System.err
                .println("Usage: java -classpath $PTII "
                        + "ptolemy.cg.kernel.generic.GenericCodeGenerator model.xml "
                        + "[model.xml . . .]"
                        + _eol
                        + "  The arguments name MoML files containing models."
                        + "  Use -help to get a full list of command line arguments.");
                return -1;
            }

            // See MoMLSimpleApplication for similar code
            Workspace workspace = new Workspace("GenericCodeGeneratorWorkspace");
            MoMLParser parser = new MoMLParser(workspace);
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters(),
                    workspace);
            // Don't remove graphical classes here, it means
            // we can't generate code for plotters etc using $PTII/bin/ptcg
            //MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

            // Add DocViewerFactory so that ptolemy/actor/lib/test/auto/Sinewave.xml,
            // ptolemy/actor/lib/test/auto/Sinewave2.xml,
            // ptolemy/actor/lib/hoc/test/auto/MultiInstanceComposite.xml all work.
            RemoveClasses removeClasses = new RemoveClasses();
            removeClasses.put("ptolemy.vergil.basic.DocViewerFactory", null);
            MoMLParser.addMoMLFilter(removeClasses);

            // Reset the list each time we parse a parameter set.
            // Otherwise two calls to this method will share params!
            _parameterNames = new LinkedList<String>();
            _parameterValues = new LinkedList<String>();
            for (int i = 0; i < args.length; i++) {
                if (parseArg(args[i])) {
                    continue;
                }
                if (args[i].trim().startsWith("-")) {
                    if (i >= args.length - 1) {
                        throw new IllegalActionException("t set "
                                + "parameter " + args[i] + " when no value is "
                                + "given.");
                    }

                    // Save in case this is a parameter name and value.
                    _parameterNames.add(args[i].substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                    continue;
                }
                // Note: the code below uses explicit try catch blocks
                // so we can provide very clear error messages about what
                // failed to the end user.  The alternative is to wrap the
                // entire body in one try/catch block and say
                // "Code generation failed for foo", which is not clear.

                try {
                    modelURL = new File(args[i]).toURI().toURL();
                } catch (Exception ex) {
                    throw new Exception("Could not open \"" + args[i] + "\"",
                            ex);
                }

                CompositeActor toplevel = null;

                try {
                    try {
                        // Reset the parser and reload so that if
                        // we run the model and then generate code,
                        // we get the same results when generating code.
                        // If we don't do this, then the nightly tests
                        // fail because the results don't match.
                        parser.reset();
                        MoMLParser.purgeModelRecord(modelURL);
                        toplevel = (CompositeActor) parser
                                .parse(null, modelURL);
                    } catch (Exception ex) {
                        throw new Exception("Failed to parse \"" + args[i]
                                + "\"", ex);
                    }

                    // Get all instances of this class contained in the model
                    List<GenericCodeGenerator> codeGenerators = toplevel
                            .attributeList(GenericCodeGenerator.class);

                    String generatorPackageValue = _getGeneratorPackageValue();
                    Class<?> generatorClass = _getCodeGeneratorClass(generatorPackageValue, _getGeneratorDialectValue());

                    if (codeGenerators.size() != 0) {
                        // Get the last CodeGenerator in the list, maybe
                        // it was added last?
                        for (Object object : codeGenerators) {
                            //if (object instanceof CCodeGenerator) {
                            if (generatorClass.isInstance(object)) {
                                codeGenerator = (GenericCodeGenerator) object;
                                break;
                            }
                        }
                    }

                    if (codeGenerators.size() == 0 || codeGenerator == null) {
                        // Add a codeGenerator
                        Constructor<?> codeGeneratorConstructor = generatorClass
                                .getConstructor(new Class[] { NamedObj.class,
                                        String.class });
                        codeGenerator = (GenericCodeGenerator) codeGeneratorConstructor
                                .newInstance(new Object[] { toplevel,
                                "CodeGenerator_AutoAdded" });
                    }

                    codeGenerator._updateParameters(toplevel);
                    // Pick up any new command line arguments from the codeGenerator.
                    _commandOptions = codeGenerator.updateCommandOptions();
                    codeGenerator.generatorPackage
                    .setExpression(generatorPackageValue);

                    Attribute generateEmbeddedCode = codeGenerator
                            .getAttribute("generateEmbeddedCode");
                    if (generateEmbeddedCode instanceof Parameter) {
                        ((Parameter) generateEmbeddedCode)
                        .setExpression("false");
                    }

                    try {
                        codeGenerator.generateCode();
                    } catch (KernelException ex) {
                        throw new Exception(
                                "Failed to generate code for \""
                                        + args[i]
                                                + "\""
                                                + "\ncodeDirectory:       "
                                                + codeGenerator.codeDirectory
                                                .stringValue()
                                                + "\ngeneratorPackage:    "
                                                + codeGenerator.generatorPackage
                                                .stringValue()
                                                + "\ngeneratePackageList: "
                                                + codeGenerator.generatorPackageList
                                                .stringValue(),
                                                ex);
                    }
                } finally {
                    // Destroy the top level so that we avoid
                    // problems with running the model after generating code
                    if (toplevel != null
                            && toplevel.getManager() != null
                            && toplevel.getManager().getState()
                            .equals(Manager.IDLE)) {
                        try {
                            // Only set the container to null if the
                            // Manager is IDLE.  If it is not IDLE,
                            // then we probably have thrown an
                            // exception.
                            toplevel.setContainer(null);
                            toplevel.setManager(null);
                        } catch (KernelException ex) {
                            throw new InternalErrorException(
                                    toplevel,
                                    null,
                                    "Failed to set the container of \""
                                            + toplevel.getFullName()
                                            + "\" to null?  This is done so as to avoid "
                                            + "problems with running the model after "
                                            + "generating code.  The Manager state was: "
                                            + toplevel.getManager().getState()
                                            + " Note that calling setContainer(null) "
                                            + "on a model with level crossing links can "
                                            + "result in problems if the Manager is not IDLE.");
                        }
                    }
                }
                parser.resetAll();
                MoMLParser.setMoMLFilters(null);
            }
            if (modelURL == null) {
                throw new IllegalArgumentException("No model was read?");
            }

            if (codeGenerator != null) {
                ExecuteCommands executeCommands = codeGenerator
                        .getExecuteCommands();
                if (executeCommands != null) {
                    return executeCommands.getLastSubprocessReturnCode();
                } else {
                    // Everything ok, but no commands were executed, perhaps
                    // only code was generated?
                    return 0;
                }
            }
            return -2;
        } catch (Throwable ex) {
            ConfigurationApplication.throwArgsException(ex, args);
        } finally {
            if (codeGenerator != null) {
                codeGenerator._resetAll();
                codeGenerator.setContainer(null);
            }
        }
        return -1;
    }

    /** Return the copyright for this code.
     *  In this base class, the empty string is returned.
     *  In derived classes, the standard Ptolemy copyright is
     *  returned within a comment.
     *  @return The copyright.
     */
    public String generateCopyright() {
        // FIXME: Why isn't this method static?
        // Why isn't it in CodegenUtilities?
        return comment("Generated by Ptolemy II (http://ptolemy.eecs.berkeley.edu)"
                + _eol
                + _eol
                + "Copyright (c) 2005-2014 The Regents of the University of California."
                + _eol
                + "All rights reserved."
                + _eol
                + "Permission is hereby granted, without written agreement and without"
                + _eol
                + "license or royalty fees, to use, copy, modify, and distribute this"
                + _eol
                + "software and its documentation for any purpose, provided that the above"
                + _eol
                + "copyright notice and the following two paragraphs appear in all copies"
                + _eol
                + "of this software."
                + _eol
                + ""
                + _eol
                + "IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY"
                + _eol
                + "FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES"
                + _eol
                + "ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF"
                + _eol
                + "THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF"
                + _eol
                + "SUCH DAMAGE."
                + _eol
                + ""
                + _eol
                + "THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,"
                + _eol
                + "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF"
                + _eol
                + "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE"
                + _eol
                + "PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF"
                + _eol
                + "CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,"
                + _eol + "ENHANCEMENTS, OR MODIFICATIONS." + _eol);
    }

    /** Get the code generator adapter associated with the given component.
     *  @param component The given component.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    final public Object getAdapter(Object component)
            throws IllegalActionException {
        return _getAdapter(component);
    }

    /** Return the name of the code file that was written, if any.
     *  If no file was written, then return null.
     *  @return The name of the file that was written.
     */
    final public String getCodeFileName() {
        return _codeFileName;
    }

    /** Return the associated component, which is always the container.
     *  @return The adapter to generate code.
     */
    public NamedObj getComponent() {
        return getContainer();
    }

    /** Get the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @return executeCommands The subprocess command executor.
     *  @see #setExecuteCommands(ExecuteCommands)
     */
    final public ExecuteCommands getExecuteCommands() {
        return _executeCommands;
    }

    /**
     * Return the visibility signature for run() and execute() (for Java).
     * @return The visibility signature, empty string for C.
     */
    public String getMethodVisibilityString() {
        return "";
    }

    /**
     * Return the exception signature (for Java).
     * @return The exception signature, empty string for C.
     */
    public String getMethodExceptionString() {
        return "";
    }

    /** Return false to indicate that this decorator should not
     *  decorate objects across opaque hierarchy boundaries.
     */
    @Override
    public boolean isGlobalDecorator() {
        return true;
    }

    /** Generate code for a model.
     *  <p>For example:
     *  <pre>
     *  java -classpath $PTII ptolemy.cg.kernel.generic.GenericCodeGenerator $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  or
     *  <pre>
     *  $PTII/bin/ptinvoke ptolemy.cg.kernel.generic.GenericCodeGenerator $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *
     *  <p>The HandSimDroid work in $PTII/ptserver uses dependency
     *  injection to determine which implementation actors such as
     *  Const and Display to use.  This method reads the
     *  ptolemy/actor/ActorModule.properties file.</p>
     *
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @exception Exception If any error occurs.
     */
    public static void main(String[] args) throws Exception {
        ActorModuleInitializer.initializeInjector();

        generateCode(args);
    }

    /** This method is used to set the code generator for a adapter class.
     *  Since this is not a adapter class for a component, this method does
     *  nothing.
     *  @param codeGenerator The given code generator.
     */
    public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeEntity.
     *  @exception NameDuplicationException If there already exists a
     *   container with the same name.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        if (container != null && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                            + " by CompositeEntity");
        }
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
    }

    /** Set the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @param executeCommands The subprocess command executor.
     *  @see #getExecuteCommands()
     */
    public void setExecuteCommands(ExecuteCommands executeCommands) {
        _executeCommands = executeCommands;
    }

    /** Return an updated array of command line options.
     *  @return An array of updated command line options.
     */
    public String[][] updateCommandOptions() {
        return _commandOptions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Copy a C (or h) file into the directory /src of the project.
     *  This is useful to copy the files pre-written in C.
     *  @param path The path to the source to be copied.  A relative
     *  path has $PTII prepended to it.
     *  @param directoryToCopy The destination directory, typically
     *  an absolute path determined by the value of the codeDirectory
     *  parameter.
     *  @param codeFileName the name of the file to copy.
     *  @exception IllegalActionException If thrown while copying
     *  a file.
     */
    protected void _copyCFileTosrc(String path, String directoryToCopy,
            String codeFileName) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        BufferedReader cFileReader = null;
        String cFileName = path + codeFileName;
        codeFileName = directoryToCopy + codeFileName;
        String referenceClassName = "ptolemy.util.FileUtilities";
        Class referenceClass;
        try {
            referenceClass = Class.forName(referenceClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Did not find the base class !\"");
        }
        ClassLoader classLoader = referenceClass.getClassLoader();
        URL url = classLoader.getResource(cFileName);
        if (url == null) {
            throw new NullPointerException("Could not find \"" + cFileName
                    + "\" in the classpath.");
        }
        //        if (codeFileName.endsWith(".h"))
        //            codeFileName = "includes/" + codeFileName;
        //        else if (codeFileName.endsWith(".c"))
        //            codeFileName = "src/" + codeFileName;
        //        else
        //            throw new IllegalActionException(this, "Only .c and .h files are allowed in _copyCFileTosrc");
        String inputLine = "";

        try {
            try {
                cFileReader = CodeGeneratorUtilities.openAsFileOrURL(url
                        .toString());
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "Failed to read \""
                        + cFileName + "\"");
            }
            if (cFileReader != null) {
                //                _executeCommands.stdout("Reading \"" + cFileName
                //                        + "\"," + _eol + "    writing \""
                //                        + codeFileName + "\"");
                while ((inputLine = cFileReader.readLine()) != null) {
                    code.append(inputLine + _eol);
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to read \"" + cFileName + "\" or write \""
                            + codeFileName + "\"");
        } finally {
            if (cFileReader != null) {
                try {
                    cFileReader.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to close \"" + cFileName + "\"");
                }
            }
        }

        String result = code.toString();
        code = new StringBuffer();
        code.append(result);

        _writeCodeFileName(code, codeFileName, true, false);
    }

    /** Copy files into the directory of the project.
     *  This is useful to copy the files pre-written in C.
     *  @param path The path to the source to be copied.  A relative
     *  path has $PTII prepended to it.
     *  @param directoryToCopy The destination directory, typically
     *  an absolute path determined by the value of the codeDirectory
     *  parameter.
     *  @param codeFileNames the name of the file to copy.
     *  @exception IllegalActionException If thrown while copying
     *  a file.
     */
    protected void _copyCFilesTosrc(String path, String directoryToCopy,
            String[] codeFileNames) throws IllegalActionException {
        for (int i = 0; i < codeFileNames.length; i++) {
            _copyCFileTosrc(path, directoryToCopy, codeFileNames[i]);
        }
    }

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory. In this base class, 0 is
     *  returned by default.
     *  @return The result of the execution.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected int _executeCommands() throws IllegalActionException {
        return 0;
    }

    /** Generate code and append it to the given string buffer.
     *  <p>Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code></p>
     *
     *  <p>This method is the main entry point, derived classes will
     *  override this method with code that traverses the model and
     *  generates code.</p>
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    protected int _generateCode(StringBuffer code) throws KernelException {
        _codeFileName = _writeCode(code);
        return 0;
    }

    /**
     * Get the code generator adapter associated with the given object.
     * @param object The given object.
     * @return The code generator adapter.
     * @exception IllegalActionException If the adapter class cannot be found.
     */
    final protected CodeGeneratorAdapter _getAdapter(Object object)
            throws IllegalActionException {
        //_debugging = true;
        if (_debugging) {
            _debug("GenerateCodeGenerator._getAdapter(" + object + ")");
        }

        Class<?> adapterClassFilter = _getAdapterClassFilter();
        if (_adapterStore == null) {
            // During instantiation of a CCodeGenerator, we eventually call _getAdapter()
            // before the reset of this class is instantiated.
            return null;
        }
        if (_adapterStore.containsKey(object)) {
            assert adapterClassFilter.isInstance(_adapterStore.get(object));
            return _adapterStore.get(object);
        }

        List<String> packageList = _generatorPackageListParser
                .generatorPackages();
        ArrayList<String> packages = new ArrayList<String>(packageList);
        if (packages.isEmpty()) {
            throw new IllegalActionException(
                    "Failed to generate the list of packages, "
                            + "the package list parser returned an empty list."
                            + " The value of the generatorPackageList parameter was: "
                            + generatorPackageList.stringValue());
        }
        //ArrayList<String> packagesWorkingSet = new ArrayList<String>(packagesToBacktrack);

        Class<?> componentClass = object.getClass();

        CodeGeneratorAdapter adapterObject = null;

        // We have 3 levels in which we need to seek.
        //      First the different packages
        //      Secondly the hierarchy of the object
        //      Lastly for each package the hierarchy of the package
        StringBuffer errorMessage = new StringBuffer("Searched for "
                + object.getClass() + "\n");
        while (adapterObject == null) {
            String className = componentClass.getName();

            if (packages.isEmpty()) {
                // Try to auto generate an adapter object.
                // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=342
                adapterObject = _getAutoGeneratedAdapter(this, object);
                if (adapterObject == null) {
                    throw new IllegalActionException(
                            "There is no "
                                    + "codegen adaptor for "
                                    + object.getClass()
                                    + ".  Searched the contents of the generatorPackageList parameter, "
                                    + "which was: "
                                    + generatorPackageList.stringValue()
                                    + "Search list was: \n" + errorMessage);
                }
                _adapterStore.put(object, adapterObject);
                return adapterObject;
            }

            if (!className.contains("ptolemy")) {
                componentClass = object.getClass();
                className = componentClass.getName();
                for (int i = 0; i < packages.size(); ++i) {
                    String packageName = packages.get(i);
                    if (packageName.indexOf('.') != -1) {
                        packageName = packageName.substring(0,
                                packageName.lastIndexOf('.'));
                        packages.set(i, packageName);
                    } else {
                        if (_debugging) {
                            _debug("Removing "
                                    + packageName
                                    + " from the list of packages to be searched because it does "
                                    + "not of a '.' in it");
                        }
                        packages.remove(i);
                        --i;
                    }
                }
            }

            for (int i = 0; i < packages.size(); ++i) {
                String packageName = packages.get(i);

                String adapterClassName = "ptolemy.cg.adapter." + packageName
                        + ".adapters." + className;

                try {
                    String message = "About to instantiate adapter: object: "
                            + object + " packageName: " + packageName
                            + " adapterClassName: " + adapterClassName;
                    errorMessage.append(i + ". " + message + "\n");
                    if (_debugging) {
                        _debug(message);
                    }
                    adapterObject = _instantiateAdapter(object, componentClass,
                            adapterClassName);
                    message = "Instantiated adapter: object: " + object
                            + " packageName: " + packageName
                            + " adapterClassName: " + adapterClassName;
                    errorMessage.append(i + ". " + message + "\n");
                    if (_debugging) {
                        _debug(message);
                    }
                } catch (IllegalActionException ex) {
                    String message = "Warning: Failed to instantiate adapter: object: "
                            + object
                            + " packageName: "
                            + packageName
                            + " adapterClassName: "
                            + adapterClassName
                            + " "
                            + KernelException.stackTraceToString(ex);
                    errorMessage.append(i + ". " + message + "\n");
                    if (_debugging) {
                        _debug(message);
                    }

                    // If adapter class cannot be found, get to next package
                    continue;
                }
            }
            if (adapterObject == null) {
                // If adapter class cannot be found, search the adapter class
                // for parent class instead.
                componentClass = componentClass.getSuperclass();
            }
        }
        _adapterStore.put(object, adapterObject);
        return adapterObject;
    }

    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.
     */
    protected Class<?> _getAdapterClassFilter() {
        return CodeGeneratorAdapter.class;
    }

    /**
     * Attempt to automatically generate an adapter.
     * In this base class, return null, indicating that an adapter cannot
     * be generated.  Derived classes could create an adapter
     * @param codeGenerator The code generator with which to associate the adapter.
     * @param object The given object.
     * @return The code generator adapter or null if no adapter can be
     * automatically generated.
     */
    protected CodeGeneratorAdapter _getAutoGeneratedAdapter(
            GenericCodeGenerator codeGenerator, Object object) {
        // See
        // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=342
        return null;
    }

    /**
     * Return the name of the output file.
     * @return The output file name.
     * @exception IllegalActionException If there is problem resolving
     *  the string value of the generatorPackage parameter.
     */
    protected String _getOutputFilename() throws IllegalActionException {
        if (_outputFileExtension == null || _outputFileExtension.length() == 0) {
            _outputFileExtension = _getOutputFileExtension();
        }
        return _sanitizedModelName + "." + _outputFileExtension;
    }

    /** Instantiate the given code generator adapter.
     *  @param component The given component.
     *  @param componentClass The class of the component to be instantiated.
     *  The constructor for class named by the adapterClassName argument
     *  must take an argument of the class componentClass.
     *  @param adapterClassName The dot separated name of the adapter.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    protected CodeGeneratorAdapter _instantiateAdapter(Object component,
            Class<?> componentClass, String adapterClassName)
                    throws IllegalActionException {

        Class<?> adapterClass = null;

        try {
            adapterClass = Class.forName(adapterClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Cannot find adapter class " + adapterClassName);
        }

        Constructor<?> constructor = null;

        try {
            constructor = adapterClass
                    .getConstructor(new Class[] { componentClass });

        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " + adapterClassName
                    + " which accepts an instance of "
                    + component.getClass().getName()
                    + " as the argument.");
        }

        CodeGeneratorAdapter adapterObject = null;

        try {
            adapterObject = (CodeGeneratorAdapter) constructor
                    .newInstance(new Object[] { component });
        } catch (ClassCastException ex0) {
            throw new InternalErrorException(
                    component instanceof NamedObj ? (NamedObj) component : null,
                            ex0,
                            "Problem casting a "
                                    + constructor
                                    + " to a CodeGeneratorAdapter. Perhaps "
                                    + adapterClass
                                    + " should extend "
                                    + CodeGeneratorAdapter.class.getName()
                                    + "? Thus we fail to create an adapter class code generator for "
                                    + adapterClassName + ".");
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create adapter class code generator for "
                            + adapterClassName + ".");
        }

        if (!_getAdapterClassFilter().isInstance(adapterObject)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: " + component
                    + ". Its adapter class " + adapterObject
                    + " does not" + " implement "
                    + _getAdapterClassFilter() + ".");
        }

        adapterObject.setCodeGenerator(this);

        return adapterObject;
    }

    /** Test if the containing actor is in the top level.
     *  @return true if the containing actor is in the top level.
     *  @exception IllegalActionException If thrown while getting the container.
     */
    protected boolean _isTopLevel() throws IllegalActionException {
        return getContainer().getContainer() == null;
    }

    /** Print the elapsed time since the specified startTime if
     *  the elapsed time is greater than 10 seconds. Otherwise,
     *  do nothing.
     *  @param startTime The start time.  Usually set to the value
     *   of <code>(new Date()).getTime()</code>.
     *  @param message A prefix to the printed message.
     *  @return The current time.
     */
    protected long _printTimeAndMemory(long startTime, String message) {
        long currentTime = new Date().getTime();
        if (currentTime - startTime > 10000) {
            // Findbugs does not like the explicit garbage collection ...
            //System.gc();
            System.out.println(message + Manager.timeAndMemory(startTime));
        }
        return currentTime;
    }

    /** Reset the code generator.
     *  @exception IllegalActionException If the container of the model
     *  cannot be set to null.
     */
    protected void _reset() throws IllegalActionException {
        // Reset the code file name so that getCodeFileName()
        // accurately reports whether code was generated.
        _codeFileName = null;

        _adapterStore.clear();
    }

    /** Reset the code generator, including setting the model to null.
     *  @exception IllegalActionException If the container of the model
     *  cannot be set to null.
     */
    protected void _resetAll() throws IllegalActionException {
        _reset();

        if (_model != null) {
            try {
                _model.setContainer(null);
            } catch (KernelException ex) {
                throw new IllegalActionException(_model, ex,
                        "Could not set the container of "
                                + _model.getFullName() + " to null.");
            }
            _model = null;
        }
    }

    /** Write the code to a directory named by the codeDirectory
     *  parameter, with a file name that is a sanitized version of the
     *  model name, and an extension that is the last package of
     *  the generatorPackage.
     *  @param code The StringBuffer containing the code.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected String _writeCode(StringBuffer code)
            throws IllegalActionException {
        String codeFileName = _getOutputFilename();

        // Check if needs to overwrite.
        boolean overwriteFile = ((BooleanToken) overwriteFiles.getToken())
                .booleanValue();

        if (_executeCommands != null) {
            _executeCommands.stdout("Writing "
                    // Findbugs says that codeFileName cannot be null,
                    // so no need to check.
                    + codeFileName
                    + " in "
                    + (codeDirectory == null ? "<codeDirectory was null?>"
                            : codeDirectory.getBaseDirectory()) + " ("
                            + (code == null ? 0 : code.length()) + " characters)");
        }

        return _writeCodeFileName(code, codeFileName, overwriteFile, false);
    }

    /** Write the code to a directory named by the codeDirectory
     *  parameter, with a file name that is a sanitized version of the
     *  model name, and an extension that is the last package of
     *  the generatorPackage.
     *  @param code The StringBuffer containing the code.
     *  @param codeFileName The name of the output file.
     *  @param overwriteFile The overwrite flag.
     *  @param dontShowDialog When true the confirmation dialog won't be shown.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected String _writeCodeFileName(StringBuffer code, String codeFileName,
            boolean overwriteFile, boolean dontShowDialog)
                    throws IllegalActionException {

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        try {

            if (!overwriteFile && codeDirectory.asFile().exists()) {
                // FIXME: It is totally bogus to ask a yes/no question
                // like this, since it makes it impossible to call
                // this method from a script.  If the question is
                // asked, the build will hang.
                if (!dontShowDialog
                        && !MessageHandler.yesNoQuestion(codeDirectory.asFile()
                                + " exists. OK to overwrite?")) {
                    /*
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                     */
                    return FileUtilities.nameToFile(codeFileName,
                            codeDirectory.getBaseDirectory())
                            .getCanonicalPath();
                }
            }

            File codeDirectoryFile = codeDirectory.asFile();
            if (codeDirectoryFile.isFile()) {
                throw new IllegalActionException(this, "Error: "
                        + codeDirectory.stringValue() + " is a file, "
                        + " it should be a directory.");
            }

            if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
                throw new IllegalActionException(this, "Failed to make the \""
                        + codeDirectory.stringValue() + "\" directory.");
            }
            // The base directory has been set, this might have happened before
            // the directory was created. At that moment java assumes it is a file
            // when you call asFile().toURI(), and java removes the last "/".
            // When you afterwards call FileUtilities.openForWriting, this will call
            // nameToFile, which will call URI.resolve. In the end java than parses
            // the base directory and since it was recorded without a slash, it assumes
            // it is a file and removes this from the path.
            // To fix this we explicitly call setBaseDirectory again.
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());

            Writer writer = null;
            try {
                writer = FileUtilities.openForWriting(codeFileName,
                        codeDirectory.getBaseDirectory(), false);
                writer.write(code.toString());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            String fileNameWritten = FileUtilities.nameToFile(codeFileName,
                    codeDirectory.getBaseDirectory()).getCanonicalPath();
            //System.out.println("Wrote " + fileNameWritten);
            return fileNameWritten;
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeFileName + "\" in "
                    + codeDirectory.getBaseDirectory());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the file extension for generated files.  The file
     *  extension is set to the last package of the
     *  <i>generatePackage</i> parameter.  For example, if
     *  <i>generatePackage</i> is set to
     *  ptolemy.cg.kernel.generic.html", then the extension will
     *  be ".html".  In this base class, the output file extension
     *  @return The file extension.
     *  @exception IllegalActionException If getting the value of the
     *  <i>generatorPackage</i> parameter throws it.
     */
    private String _getOutputFileExtension() throws IllegalActionException {
        String packageValue = generatorPackage.stringValue();

        if (packageValue.indexOf(".") == -1) {
            return "";
        }
        String outputFileExtension = packageValue.substring(packageValue
                .lastIndexOf("."));
        return outputFileExtension;
    }

    /** Set the parameters in the model stored in _parameterNames
     *  to the values given by _parameterValues. Those lists are
     *  populated by command line arguments.
     *  @param model The model in which to update parameters.
     */
    private void _updateParameters(NamedObj model) {
        // Check saved options to see whether any is setting an attribute.
        Iterator<String> names = _parameterNames.iterator();
        Iterator<String> values = _parameterValues.iterator();

        boolean ignoreInline = false;
        if (model instanceof CompositeActor) {
            ptolemy.actor.Director director = ((CompositeActor) model)
                    .getDirector();
            try {
                // Avoid compile-time dependency on DEDirector.
                Class deDirectorClass = Class
                        .forName("ptolemy.domains.de.kernel.DEDirector");
                // FIXME: this only works if the top level director is a DEDirector.
                if (deDirectorClass.isInstance(director)) {
                    ignoreInline = true;
                }
            } catch (Throwable throwable) {
                // Ignore, the DEDirector class is not present.
            }
        }

        while (names.hasNext() && values.hasNext()) {
            String name = names.next();
            String value = values.next();

            if (name.equals("inline") && value.equals("true") && ignoreInline) {
                System.out
                .println("Warning: '-inline true' is not relevant for a DE model, forcing the value to false.");
                value = "false";
            }

            Attribute attribute = model.getAttribute(name);

            if (attribute instanceof Settable) {
                // Use a MoMLChangeRequest so that visual rendition (if
                // any) is updated and listeners are notified.
                String moml = "<property name=\"" + name + "\" value=\""
                        + value + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, model,
                        moml);
                model.requestChange(request);
            } else {
                attribute = getAttribute(name);

                if (attribute instanceof Settable) {
                    // Use a MoMLChangeRequest so that visual rendition (if
                    // any) is updated and listeners are notified.
                    String moml = "<property name=\"" + name + "\" value=\""
                            + value + "\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            this, moml);
                    model.requestChange(request);
                }
            }

            if (model instanceof CompositeActor) {
                ptolemy.actor.Director director = ((CompositeActor) model)
                        .getDirector();

                if (director != null) {
                    attribute = director.getAttribute(name);

                    if (attribute instanceof Settable) {

                        // Use a MoMLChangeRequest so that
                        // visual rendition (if any) is
                        // updated and listeners are notified.
                        String moml = "<property name=\"" + name
                                + "\" value=\"" + value + "\"/>";
                        MoMLChangeRequest request = new MoMLChangeRequest(this,
                                director, moml);
                        director.requestChange(request);
                    }
                }
            }
        }
    }

    /** Parse a command-line argument. This method recognized -help
     *  and -version command-line arguments, and prints usage or
     *  version information. No other command-line arguments are
     *  recognized.
     *  @param arg The command-line argument to be parsed.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    private static boolean parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            System.out.println(_usage());

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } else if (arg.equals("-version")) {
            System.out
            .println("Version "
                    + VersionAttribute.CURRENT_VERSION.getExpression()
                    + ", Build $Id$");

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        }
        // Argument not recognized.
        return false;
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    private static String _usage() {
        try {
            // Instantiate a CodeGenerator so that we may get any CodeGenerator
            // specific arguments.  This is a hack, but it beats have -help
            // not tell you about the command line arguments.
            String generatorPackageValue = _getGeneratorPackageValue();
            Class<?> generatorClass = _getCodeGeneratorClass(generatorPackageValue, _getGeneratorDialectValue());
            Constructor<?> codeGeneratorConstructor = generatorClass
                    .getConstructor(new Class[] { NamedObj.class, String.class });
            CompositeActor toplevel = new CompositeActor();
            GenericCodeGenerator codeGenerator = (GenericCodeGenerator) codeGeneratorConstructor
                    .newInstance(new Object[] { toplevel, "CodeGenerator_help" });
            Method updateCommandOptions = generatorClass
                    .getMethod("updateCommandOptions");
            _commandOptions = (String[][]) updateCommandOptions
                    .invoke(codeGenerator);
        } catch (Throwable throwable) {
            System.err
            .println("Failed to get arguments from the generatorPackage: "
                    + throwable.getMessage());
            throwable.printStackTrace();
        }

        // Call the static method that generates the usage strings.
        return StringUtilities.usageString(_commandTemplate, _commandOptions,
                _commandFlags)
                + "\n To get additional help, try -language java -help";
    }

    /** Get the code generator associated with the generatorPackage parameter.
     *  @param generatorPackageValue  The value of the generatorPackage parameter.
     *  @return The CodeGenerator class that corresponds with the
     *  generatorPackage parameter.  For example, if generatorPackage
     *  is "ptolemy.cg.kernel.generic.program.procedural.c",
     *  "ptolemy.cg.kernel.generic.program.procedural.c.CCodeGenerator"
     *  is looked for.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    private static Class<?> _getCodeGeneratorClass(String generatorPackageValue, String generatorDialect)
            throws IllegalActionException {
        String language = generatorPackageValue.substring(generatorPackageValue
                .lastIndexOf("."));
        
        String capitalizedLanguage = language.substring(1, 2).toUpperCase(
                Locale.getDefault())
                + language.substring(2);
        
        String dialect = "";
        
        if (generatorDialect != null && generatorDialect != "") {
            dialect = generatorDialect.substring(0, 1).toUpperCase(
                    Locale.getDefault());
            if (generatorDialect.length() >= 2) {
                dialect += generatorDialect.substring(1);
            }
        }
        
        // Append dialect
        if (dialect != "") {
            capitalizedLanguage += dialect;
        }
        
        String codeGeneratorClassName = generatorPackageValue + "."
                + capitalizedLanguage + "CodeGenerator";

        Class<?> result = null;
        try {
            result = Class.forName(codeGeneratorClassName);
        } catch (Throwable throwable) {
            capitalizedLanguage = language.toUpperCase(Locale.getDefault());

            // Append dialect
            if (dialect != "") {
                capitalizedLanguage += dialect;
            }
            
            String oldCodeGeneratorClassName = codeGeneratorClassName;
            codeGeneratorClassName = generatorPackageValue
                    + capitalizedLanguage + "CodeGenerator";

            try {
                result = Class.forName(codeGeneratorClassName);
            } catch (Throwable throwable2) {
                throw new IllegalActionException(
                        null,
                        throwable2,
                        "Failed to find \""
                                + oldCodeGeneratorClassName
                                + "\" and \""
                                + codeGeneratorClassName
                                + "\", generatorPackage parameter was \""
                                + generatorPackageValue
                                + "\". "
                                + "Common values are ptolemy.cg.kernel.generic.program.procedural.c, "
                                + "ptolemy.cg.kernel.generic.html, or "
                                + "ptolemy.cg.kernel.generic.program.procedural.java");
            }
        }
        return result;
    }

    /** Return the value of the -generatorPackage or -language command
     *  line argument.
     *  @return The value of the generatorPackage argument
     */
    private static String _getGeneratorPackageValue() {
        // If the user called this with -generatorPackage
        // ptolemy.codegen.java, the process that
        // argument.  This is a bit hacky, but works.
        String generatorPackageValue = "ptolemy.cg.kernel.generic.program.procedural.c";
        int parameterIndex = -1;
        if ((parameterIndex = _parameterNames.indexOf("generatorPackage")) != -1) {
            generatorPackageValue = _parameterValues.get(parameterIndex);
        }

        if ((parameterIndex = _parameterNames.indexOf("language")) != -1) {
            String languageValue = _parameterValues.get(parameterIndex);

            boolean foundLanguage = false;
            // Use a two column table to make it easy to add languages
            for (String[] _language : _languages) {
                if (_language[0].equals(languageValue)) {
                    generatorPackageValue = _language[1];
                    foundLanguage = true;
                    break;
                }
            }
            if (!foundLanguage) {
                StringBuffer languageError = new StringBuffer();
                for (String[] _language : _languages) {
                    if (languageError.length() > 0) {
                        languageError.append(",");
                    }
                    languageError.append(_language[0]);
                }
                generatorPackageValue = "ptolemy.cg.kernel.generic.program.procedural."
                        + languageValue;
                System.out.println("Warning, -language was \"" + languageValue
                        + "\", defaulting to setting "
                        + "generatorPackage to \"" + generatorPackageValue
                        + "\".  " + "Acceptable languages are: "
                        + languageError.toString());
            }
        }
        return generatorPackageValue;
    }

    
    /** Return the value of the -generatorDialect or -dialect command
     *  line argument.
     *  @return The value of the generatorPackage argument
     */
    private static String _getGeneratorDialectValue() {
        
        int parameterIndex = -1;
        if ((parameterIndex = _parameterNames.indexOf("generatorDialect")) != -1) {
            return _parameterValues.get(parameterIndex);
        }
        
        if ((parameterIndex = _parameterNames.indexOf("dialect")) != -1) {
            return _parameterValues.get(parameterIndex);
        }
        
        return "";
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indent string for indent level 1.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    public static final String INDENT1 = StringUtilities.getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    public static final String INDENT2 = StringUtilities.getIndentPrefix(2);

    /** Indent string for indent level 3.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    public static final String INDENT3 = StringUtilities.getIndentPrefix(3);

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected static final String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** Execute commands to run the generated code.
     */
    protected ExecuteCommands _executeCommands;

    /** The value of the generateInSubdirectory parameter. */
    protected boolean _generateInSubdirectory;

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    /** The sanitized model name. */
    protected String _sanitizedModelName;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A map giving the code generator adapters for each actor. */
    private Map<Object, CodeGeneratorAdapter> _adapterStore = new HashMap<Object, CodeGeneratorAdapter>();

    /** The name of the file that was written.
     *  If no file was written, then the value is null.
     */
    protected String _codeFileName = null;

    /** The command-line options that are either present or not. */
    private static String[] _commandFlags = { "-help",
        "-generateInSubdirectory", "-version", };

    /** The command-line options that take arguments. */
    private static String[][] _commandOptions = {
        //{ "-allowDynamicMultiportReferences",
        //  "        true|false (default: false)" },
        {
            "-codeDirectory",
        "        <directory in which to put code (default: $HOME/cg/. Other values: $CWD, $HOME, $PTII, $TMPDIR)>" },
        //    { "-compileTarget",
        // "     <target to be run, defaults to empty string>" },
        { "-dialect", "              <Use to distinguish between different implementations in the same package>" },
        { "-generatorDialect", "     <Same as dialect. Class naming convention: <Package><Dialect>CodeGenerator>" },
        {
            "-generatorPackage",
        " <Java package of code generator, defaults to ptolemy.cg.kernel.generic.program.procedural.c>" },
        { "-generatorPackageList",
        " <Semicolon or * separated list of Java packages to be searched for adapters>" },
        { "-language", "             <c|java|html (default: c)>" },
        //{ "-overwriteFiles", "    true|false (default: true)" },
        //{ "-padBuffers", "        true|false (default: true)" },
        { "-<parameter name>", "     <parameter value>" } };

    //{ "-sourceLineBinding", " true|false (default: false)" },
    //{ "-target", "            <target name, defaults to false>" }}

    /** The form of the command line. */
    private static final String _commandTemplate = "ptcg [ options ] [file ...]";

    private static String[][] _languages = {
        { "c", "ptolemy.cg.kernel.generic.program.procedural.c" },
        { "fmima", "ptolemy.cg.kernel.generic.program.procedural.fmima" },
        { "html", "ptolemy.cg.kernel.generic.html" },
        { "java", "ptolemy.cg.kernel.generic.program.procedural.java" } };
    private GeneratorPackageListParser _generatorPackageListParser = new GeneratorPackageListParser();

    /** List of parameter names seen on the command line. */
    private static List<String> _parameterNames;

    /** List of parameter values seen on the command line. */
    private static List<String> _parameterValues;

    /** The extension of the output file.  (for example c in case of C
     *   and java in case of Java). The file extension is set to the
     *   last package of the <i>generatePackage</i> parameter.  For
     *   example, if <i>generatePackage</i> is set to
     *   ptolemy.cg.kernel.generic.html", then the extension will be
     *   ".html".
     */
    private String _outputFileExtension;

    /** Parse the generatorPackageList parameter.
     *  The <i>generatorPackageList</i> parameter is assumed to be
     *  a semicolon ';' or asterix '*' separated list of Java packages
     *  to be searched for packages.
     */
    class GeneratorPackageListParser {
        /** Create a package list parser. */
        public GeneratorPackageListParser() {
        }

        /** Return the list of generator packages.
         *  @return the list of generator packages.
	 *  @exception IllegalActionException If thrown while
	 *  updating the package list.
         */
        public List<String> generatorPackages() throws IllegalActionException {
            _updateGeneratorPackageList();
            return _generatorPackages;
        }

        /** Update the internal list of generator packages.
         */
        private void _updateGeneratorPackageList()
                throws IllegalActionException {
            if (generatorPackageList != null) {
                String packageList = generatorPackageList.stringValue();
                String[] packages = packageList.split(";: *");
                _generatorPackages = Arrays.asList(packages);
            }
        }

        /** The list of generator packages. */
        private List<String> _generatorPackages;
    }
}
