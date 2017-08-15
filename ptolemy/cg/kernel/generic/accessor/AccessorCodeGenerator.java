/* Code generator for JavaScript Accessors.

Copyright (c) 2009-2017 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.accessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorUtilities;
import ptolemy.cg.kernel.generic.RunnableCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////AccessorCodeGenerator

/** Generate a JavaScript Accessor for a  model.
 *
 *  <p>Accessors are a technology, developed by the
 *  <a href="http://www.terraswarm.org#in_browser" target="_top">TerraSwarm Research Center</a>,
 *  for composing heterogeneous devices and services in the
 *  Internet of Things (IoT).  For more information, see
 *  <a href="http://accessors.org#in_browser" target="_top">http://accessors.org</a>.</p>
 *
 *  <p>The model can only contain JavaScript and JSAccessor actors.</p>
 *
 *  <p>To generate an Accessor version of a model, use:</p>
 *  <pre>
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -language accessor $PTII/ptolemy/cg/kernel/generic/accessor/demo/TestComposite/TestComposite.xml; cat $PTII/org/terraswarm/accessor/accessors/web/cg/TestComposite.js
 *  </pre>
 *  which is shorthand for:
 *  <pre>
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -generatorPackage ptolemy.cg.kernel.generic.accessor -generatorPackageList generic.accessor $PTII/ptolemy/cg/adapter/generic/accessor/adapters/org/test/auto/TestComposite.xml; cat ~/cg/TestComposite.js
 *  </pre>
 *
 *  <p>For more information, see <a href="https://accessors.org/wiki/Main/CapeCodeHost#in_browserr">https://accessors.org/wiki/Main/CapeCodeHost#CodeGeneration</a>.</p>
 *
 *  @author Christopher Brooks.  Contributor: Edward A. Lee.  Based on HTMLCodeGenerator by Man-Kit Leung, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating red (cxh)
 *  @Pt.AcceptedRating red (cxh)
 */
public class AccessorCodeGenerator extends RunnableCodeGenerator {

    /** Create a new instance of the AccessorCodeGenerator.
     *  The value of the <i>generatorPackageList</i> parameter of the
     *  base class is set to <code>generic.accessor</code>
     *  @param container The container.
     *  @param name The name of the AccessorCodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public AccessorCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        // The output file extension is .js.
        super(container, name, "js");

        // Use accessors/web/cg because the code generated can be used by multiple accessor hosts, not just the node accessor host.
        codeDirectory.setExpression("$PTII/org/terraswarm/accessor/accessors/web/cg");

        generatorPackageList.setExpression("generic.accessor");

        // @codeDirectory@ and @modelName@ are set in
        // RunnableCodeGenerator._executeCommands().
        // Run the accessors for 2000 ms.
        runCommand.setExpression("node ../hosts/node/nodeHostInvoke.js cg/@modelName@");

        modules = new StringParameter(this, "modules");
        modules.setExpression("");

        npmInstall = new Parameter(this, "npmInstall");
        npmInstall.setTypeEquals(BaseType.BOOLEAN);
        npmInstall.setExpression("true");
    }

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a Accessor-style comment, which
     *  begins with "//" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r".o Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    @Override
    public String comment(String comment) {
        return "// " + comment + _eol;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** A comma separated list of modules to be installed
     *  To install the Global Data Plane module,
     *  use <code>@terraswarm/gdp</code>.
     */
    public StringParameter modules;

    /** If true, then search <i>codeDirectory</i> and its parent
     *  directories for a node_modules/ directory. If it is found,
     *  then search for the modules listed in the <i>modules</i>
     *  parameter in that directory.  If any module is not found, then
     *  install all the modules using npm install.  If all the modules
     *  are found, then don't bother installing.
     *
     *  <p>The reason to set this to false is if the host is not
     *  connected to the internet or if the host already has the
     *  modules installed.  Setting this to false means that the
     *  composite accessor will be deployed more quickly because
     *  <code>npm install</code> will not be run.  The default value
     *  is false, indicating that <code>npm install
     *  <i>modules</i></code> should not be run.</p>
     */
    public Parameter npmInstall;


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate Accessor code and append it to the given string buffer.
     *  Write the code to the directory specified by the <i>codeDirectory</i>
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME/cg</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.cg.kernel.generic.accessor</code>, then the file that is
     *  written will be <code>$HOME/cg/Foo.js</code>
     *
     *  <p>This method is the main entry point to generate js.</p>
     *  
     *  <p>This method invokes the top level generateAccessor(), which
     *  is typically defined in
     *  ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/lib/jjs/JavaScript.java
     *  </p>
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {
        URI uri = URIAttribute.getModelURI(toplevel());
        String modelURI = "";
        String PTII = StringUtilities.getProperty("ptolemy.ptII.dir").replace('\\', '/');

        try {
            AccessorCodeGenerator._setupAccessorsDirectory(PTII);
        } catch (IOException ex) {
            throw new IllegalActionException(_model, ex, "Failed to set up accessors directory.");
        }

        if (uri != null) {
            modelURI = uri.toString();
            // System.out.println("AccessorCodeGenerator: PTII: " + PTII + " "
            //        + modelURI.startsWith("file:/") + " " + modelURI.contains(PTII));
            if (modelURI.startsWith("file:/")
                    && modelURI.contains(PTII)) {
                modelURI = modelURI.substring(5).replace(PTII, "$PTII");
            }
        }

        try {
            String stopTime = ((CompositeActor) _model).getDirector().stopTime.getExpression();
            Double stopTimeValue = 0.0;
            if (stopTime.length() > 0) {
                try {
                    stopTimeValue = Double.parseDouble(stopTime) * 1000.0;
                } catch (NumberFormatException ex) {
                    System.out.println("Could not parse stop time \"" + stopTime + "\". The generated composite accessor will not call stopAt().");
                }
            }

            code.append("exports.setup = function () {" + _eol
                + INDENT1 + comment(" This composite accessor was created by Cape Code.")
                + INDENT1 + comment(" To run the code, run: ")
                + INDENT1 + comment(" (cd " + codeDirectory.asFile().getCanonicalPath().replace('\\', '/').replace(PTII.replace('\\', '/'), "$PTII") + "; "
                                    + _runCommand() + ")")
                + INDENT1 + comment(" To regenerate this composite accessor, run:")
                + INDENT1 + comment(" $PTII/bin/ptinvoke ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -language accessor " + modelURI)
                + INDENT1 + comment(" to edit the model, run:")
                + INDENT1 + comment(" $PTII/bin/capecode " + modelURI)

                // Here's where we generate the rest of the accessor code.
                // See generateAccessor() in ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/lib/jjs/JavaScript.java

                + ((AccessorCodeGeneratorAdapter) getAdapter(toplevel())).generateAccessor()
                        + "};" + _eol);

            if (stopTimeValue > 0.0)  {
                code.append(_eol
                            + comment("To update the initialize code below, modify")
                            + comment("  $PTII/ptolemy/cg/kernel/generic/accessor/AccessorCodeGenerator.java")
                            + "if (exports.initialize) {" + _eol
                            + "    var originalInitialize = exports.initialize;" + _eol
                            + "    exports.initialize = function() {" + _eol
                            + "        originalInitialize.call(this);" + _eol
                            + "        this.stopAt(" + stopTimeValue + ");" + _eol
                            + "    }" + _eol
                            + "} else {" + _eol
                            + "    exports.initialize = function() {" + _eol
                            + "        this.stopAt(" + stopTimeValue + ");" + _eol
                            + "    }" + _eol
                            + "}" + _eol);
            } else {
                code.append(_eol + comment("The stopTime parameter of the directory in the model was 0, so this.stopAt() is not being generated." + _eol));
            }

        } catch (IOException ex) {
            throw new IllegalActionException(_model, "Failed to get the canonical path of " + codeDirectory);
        }
        super._generateCode(code);
        return _executeCommands();
    }

    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.
     */
    @Override
    protected Class<?> _getAdapterClassFilter() {
        return AccessorCodeGeneratorAdapter.class;
    }

    /** Return a list of setup commands to be invoked before
     *  the run command.
     *  In this class, the "npm install" command is added
     *  if the value of the <i>npmInstall</i> parameter is true and
     *  the value of the <i>modules</i> parameter is not empty.
     *  @return The list of commands.
     *  @exception IllegalActionException If there is a problem getting
     *  the value of the <i>modules</i> parameter
     */
    protected List<String> _setupCommands() throws IllegalActionException {
        List<String> commands = super._setupCommands();

        String modulesValue = ((StringToken) modules.getToken()).stringValue().replace(',', ' ').trim();
        if (_checkForLocalModules
            && ((BooleanToken) npmInstall.getToken()).booleanValue()
            && modulesValue.length() > 0) {
            // Search from codeDirectory and up for a node_modules/ directory
            // If it is found, then search for the modules in that directory.
            // If any module is not found, then install all the modules.
            // If all the modules are found, then don't bother installing.
            boolean missingModules = false;
            File directory = codeDirectory.asFile();
            while (directory != null) {
                File nodeModules = new File(directory, "node_modules");
                if (nodeModules.isDirectory()) {
                    String[] splitModules = modulesValue.split("\\s+");
                    for (int i = 0; i < splitModules.length; i++) {
                        File modulesFile = new File(nodeModules, splitModules[i]);
                        if (!modulesFile.isDirectory()) {
                            missingModules = true;
                        }
                    }
                    if (!missingModules) {
                        System.out.println("AccessorCodeGenerator: Found the modules in "
                                           + nodeModules
                                           + ", so npm install will not be run.");
                        break;
                    }
                }
                directory = directory.getParentFile();
            }
            if (missingModules) {
                String command = CodeGeneratorUtilities
                    .substitute("npm install @modules@",
                                _substituteMap);
                commands.add(command);
            }
        }
        return commands;
    }

    /** Update the substitute map for the setup and run commands
     *  The base class adds @codeDirectory@, @modelName@
     *  and @PTII@ to the map.
     *
     *  @exception IllegalActionException If the @stopTime@ parameter
     *  cannot be parsed as a Double.
     */
    protected void _updateSubstituteMap()
        throws IllegalActionException {
        super._updateSubstituteMap();

        // The modules to be installed if the npmInstall parameter is true.
        String modulesValue = ((StringToken) modules.getToken()).stringValue();
        if (modulesValue.length() > 0) {
            // The @modules@ parameter may be comma separated.
            _substituteMap.put("@modules@", modulesValue.replace(',', ' '));
        } else {
            _substituteMap.put("@modules@", "");
        }

        // // If stopTime is set in the director, then multiply it by
        // // 1000 and use it as the timeout of the accessor.
        // if (_model instanceof CompositeActor) {
        //     String stopTime = ((CompositeActor) _model).getDirector().stopTime.getExpression();
        //     String timeoutFlagAndValue = "";
        //     if (stopTime.length() > 0) {
        //         try {
        //             timeoutFlagAndValue = "-timeout " + Double.toString(Double.parseDouble(stopTime) * 1000.0);
        //         } catch (NumberFormatException ex) {
        //             throw new IllegalActionException(_model, ex, "Could not parse " + stopTime);
        //         }
        //     }
        //     _substituteMap.put("@timeoutFlagAndValue@", timeoutFlagAndValue);
        // }

        // If @timeoutFlagAndValue is present, then substitute in the
        // empty string because we are invoking stopAt().
        _substituteMap.put("@timeoutFlagAndValue@", "");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** If true, the {@link _setupCommands()} will check for
     *  modules in the local directory.
     *  Derived classes like AccessorSSHCodeGenerator set this to false.
     */
    protected boolean _checkForLocalModules = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set up the accessors directory.
     *  @param PTII The home directory of PTII.
     */
    private static void _setupAccessorsDirectory(String PTII) throws IOException {
        // If necessary, unjar the accessors jar file.
        String nodeHostPath = "org/terraswarm/accessor/accessors/web/hosts/node/nodeHost.js";
        
        FileUtilities.extractJarFileIfNecessary(nodeHostPath, PTII);

        // Create the Path in a platform-indendent manner.
        Path newLink = Paths.get(PTII, "org", "terraswarm", "accessor", "accessors", "web", "node_modules", "@accessors-hosts");
        Path temporary = Paths.get(PTII, "org", "terraswarm", "accessor", "accessors", "web", "node_modules", "@accessors-hosts.AccessorCodeGenerator");
        Path target = Paths.get("..", "hosts");
        FileUtilities.createLink(newLink, temporary, target);

        newLink = Paths.get(PTII, "org", "terraswarm", "accessor", "accessors", "web", "hosts", "browser", "common");
        temporary = Paths.get(PTII, "org", "terraswarm", "accessor", "accessors", "web", "hosts", "browser", "common.AccessorCodeGenerator");
        target = Paths.get("..", "common");
        FileUtilities.createLink(newLink, temporary, target);
    }
}
