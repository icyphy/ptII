/* Code generator for the Procedural languages.

Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.cg.kernel.generic.CodeGeneratorUtilities;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ProceduralCodeGenerator

/** Base class for Procedural code generator.
 *
 *  @author Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (rodiers)
 *  @Pt.AcceptedRating red (rodiers)
 */
public class ProceduralCodeGenerator extends ProgramCodeGenerator {

    /** Create a new instance of the ProceduralCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProceduralCodeGenerator.
     *  @param outputFileExtension The extension of the output file.
     *   (for example c in case of C and java in case of Java)
     *  @param templateExtension The extension of the template files.
     *   (for example c in case of C and j in case of Java).
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ProceduralCodeGenerator(NamedObj container, String name,
            String outputFileExtension, String templateExtension)
                    throws IllegalActionException, NameDuplicationException {
        super(container, name, outputFileExtension, templateExtension);

        compile = new Parameter(this, "compile");
        compile.setTypeEquals(BaseType.BOOLEAN);
        compile.setExpression("true");

        compileCommand = new StringParameter(this, "compileCommand");
        compileCommand.setTypeEquals(BaseType.STRING);
        // Set it to a default so that derived classes may override it.
        compileCommand.setExpression(_compileCommandDefault);

        generateEmbeddedCode = new Parameter(this, "generateEmbeddedCode");
        generateEmbeddedCode.setTypeEquals(BaseType.BOOLEAN);
        generateEmbeddedCode.setExpression("true");
        // Hide the embeddedCode parameter from the user.
        generateEmbeddedCode.setVisibility(Settable.EXPERT);

        generatorPackageList.setExpression("generic.program.procedural");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then compile the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter compile;

    /** The command to use to compile the generated code if the
     *  <i>useMake</i> parameter is false.  The initial default value
     *  is "make -f @modelName@.mk".  Various '@' delimited
     *  key/value pairs will be automatically substituted.  In the
     *  default case @modelName@ will be replaced with a sanitized
     *  (Java-safe) version of the model name.
     *
     *  <p>If the string "@help:all@" appears, then all the key/value
     *  pairs are echoed at run time, though this may not result in a
     *  syntactically correct command.</p>
     *
     *  <p>If <i>useMake</i> is true, then the value of this parameter
     *  is ignored.</p>
     */
    public StringParameter compileCommand;

    /** If true, then generate code for that uses the reflection for Java
     *  and JNI for C and is embedded within the model
     *  The default value is false and this parameter is not usually
     *  editable by the user.  This parameter is set to true when
     *  CompiledCompositeActor is run in an interpreted Ptolemy model.
     *  This parameter
     *  is set to false when a model contains one or more
     *  CompiledCompositeActors and C or Java code is being generated for the
     *  model.
     */
    public Parameter generateEmbeddedCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an include command line argument the compile command.
     *  @param includeCommand  The include command, for example
     *  "-I/usr/local/include".
     */
    public void addInclude(String includeCommand) {
        _includes.add(includeCommand);
    }

    /** Add a library command line argument the compile command.
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibraryIfNecessary(String)
     */
    public void addLibrary(String libraryCommand) {
        _libraries.add(libraryCommand);
    }

    /** If the compile command does not yet containe a library,
     *         add a library command line argument the compile command.
     *
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibrary(String)
     */
    public void addLibraryIfNecessary(String libraryCommand) {
        if (!_libraries.contains(libraryCommand)) {
            _libraries.add(libraryCommand);
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
        ProceduralCodeGenerator newObject = (ProceduralCodeGenerator) super
                .clone(workspace);
        newObject._includes = null;
        newObject._libraries = null;
        newObject._modifiedVariables = null;
        newObject._newTypesUsed = null;
        newObject._tokenFuncUsed = null;
        newObject._typeFuncUsed = null;

        return newObject;
    }

    /** Add called functions to the set of overloaded functions for
     *  later use.
     *  If the function starts with "Array_", add everything after the
     *  "Array_" is added to the set of token functions used.
     *  @param name The name of the function, for example "Double_equals"
     *  @param templateParser The corresponding templateParser that contains the
     *  codeBlock.
     *  @exception IllegalActionException If there is a problem adding
     *  a function to the set of overloaded functions.
     */
    public void markFunctionCalled(String name,
            ProceduralTemplateParser templateParser)
                    throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a Collection of Strings, return a string where each element of the
     *  Set is separated by a space.
     *  @param collection The Collection of Strings.
     *  @return A String that contains each element of the Set separated by
     *  a space.
     */
    protected String _concatenateElements(Collection<String> collection) {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (buffer.length() > 0) {
                buffer.append(" ");
            }
            buffer.append(iterator.next());
        }
        return buffer.toString();
    }

    /** Reset the code generator.
     *  @exception IllegalActionException Not thrown in this base
     *  class, thrown by the parent if the container of the model
     *  cannot be set to null.
     */
    @Override
    protected void _reset() throws IllegalActionException {
        super._reset();

        _includes.clear();
        _libraries.clear();
    }

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there are problems reading
     *  parameters or executing the commands.
     */
    @Override
    protected int _executeCommands() throws IllegalActionException {
        List<String> commands = new LinkedList<String>();

        // The compile command.
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            if (((BooleanToken) useMake.getToken()).booleanValue()) {
                commands.add("make -f " + _sanitizedModelName + ".mk ");
            } else {
                String command = CodeGeneratorUtilities
                        .substitute(((StringToken) compileCommand.getToken())
                                .stringValue(), _substituteMap);
                System.out.println("JavaCodeGenerator: compile command: "
                        + command);
                commands.add(command);
            }
        }

        // The run command.
        if (_isTopLevel()) {
            if (((BooleanToken) run.getToken()).booleanValue()) {
                if (((BooleanToken) useMake.getToken()).booleanValue()) {
                    commands.add("make -f " + _sanitizedModelName + ".mk run");
                } else {
                    String command = CodeGeneratorUtilities
                            .substitute(((StringToken) runCommand.getToken())
                                    .stringValue(), _substituteMap);
                    System.out.println("JavaCodeGenerator: run command: "
                            + command);
                    commands.add(command);
                }
            }
        }

        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
            // FIXME: need to put this output in to the UI, if any.
            _executeCommands.start();
        } catch (Throwable throwable) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator<?> allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + _eol);
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:" + _eol + errorMessage + _eol + throwable);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    /** Read in a template makefile, substitute variables and write
     *  the resulting makefile.
     *
     *  <p>If a <code>.mk.in</code> file with the name of the sanitized model
     *  name, then that file is used as a template.  For example, if the
     *  model name is <code>Foo</code> and the file <code>Foo.mk.in</code>
     *  exists, then the file <code>Foo.mk.in</code> is used as a makefile
     *  template.
     *
     *  <p>If no <code>.mk.in</code> file is found, then the makefile
     *  template can be found by looking up a resource name
     *  makefile.in in the package named by the
     *  <i>generatorPackage</i> parameter.  Thus, if the
     *  <i>generatorPackage</i> has the value "ptolemy.codegen.c",
     *  then we look for the resource "ptolemy.codegen.c.makefile.in", which
     *  is usually found as <code>$PTII/ptolemy/codegen/c/makefile.in</code>.
     *
     *  <p>The makefile is written to a directory named by the
     *  <i>codeDirectory</i> parameter, with a file name that is a
     *  sanitized version of the model name, and a ".mk" extension.
     *  Thus, for a model named "Foo", we might generate a makefile in
     *  "$HOME/codegen/Foo.mk".

     *  <p>Under Java under Windows, your <code>$HOME</code> variable
     *  is set to the value of the <code>user.home</code>System property,
     *  which is usually something like
     *  <code>C:\Documents and Settings\<i>yourlogin</i></code>, thus
     *  for user <code>mrptolemy</code> the makefile would be
     *  <code>C:\Documents and Settings\mrptolemy\codegen\Foo.mk</code>.
     *
     *  <p>See the parent class
     *  {@link ptolemy.cg.kernel.generic.program.ProgramCodeGenerator#_writeMakefile(CompositeEntity, String)}
     *  for variable that are substituted by the parent class.</p>
     *
     *  <p>This class substitutes the following variables:
     *  <dl>
     *  <dt><code>@PTCGIncludes@</code>
     *  <dd>The elements of the set of include command arguments that
     *  were added by calling {@link #addInclude(String)}, where each
     *  element is separated by a space.
     *  </dl>
     *  @param container The composite actor for which we generate the makefile
     *  @param currentDirectory The director in which the makefile is to be written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    @Override
    protected void _writeMakefile(CompositeEntity container,
            String currentDirectory) throws IllegalActionException {
        _substituteMap.put("@PTCGIncludes@", _concatenateElements(_includes));
        super._writeMakefile(container, currentDirectory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set of include command line arguments where each element is
     *  a string, for example "-I/usr/local/include".
     */
    protected Set<String> _includes = new HashSet<String>();

    /** List of library command line arguments where each element is
     *  a string, for example "-L/usr/local/lib".
     *  This variable is a list so as to preserve the order that the
     *  library commands were added to the list of libraries matters,
     *  see the manual page for the -L option of the ld command.
     */
    protected List<String> _libraries = new LinkedList<String>();

    /** The initial default value of the <i>compileCommand</i> parameter.
     *  The constructor of a derived class may compare the value of <i>compileCommand</i>
     *  and this variable and decide to override the value of the <i>compileCommand</i>
     *  parameter with a new value.
     */
    protected final static String _compileCommandDefault = "make -f @modelName@.mk";
}
