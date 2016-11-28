/* Code generator for JavaScript Accessors.

Copyright (c) 2009-2015 The Regents of the University of California.
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

import java.io.IOException;
import java.net.URI;

import ptolemy.cg.kernel.generic.RunnableCodeGenerator;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
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
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -language accessor $PTII/ptolemy/cg/kernel/generic/accessor/demo/TestComposite/TestComposite.xml; cat $PTII/org/terraswarm/accessor/accessors/web/hosts/node/TestComposite.js
 *  </pre>
 *  which is shorthand for:
 *  <pre>
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -generatorPackage ptolemy.cg.kernel.generic.accessor -generatorPackageList generic.accessor $PTII/ptolemy/cg/adapter/generic/accessor/adapters/org/test/auto/TestComposite.xml; cat ~/cg/TestComposite.js
 *  </pre>
 *
 *  <p>For more information, see <a href="https://www.terraswarm.org/accessors/wiki/Main/CapeCodeHost#in_browser">https://www.terraswarm.org/accessors/wiki/Main/CapeCodeHost#CodeGeneration</a>.</p>
 *
 *  @author Christopher Brooks.  Based on HTMLCodeGenerator by Man-Kit Leung, Bert Rodiers
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

        codeDirectory.setExpression("$PTII/org/terraswarm/accessor/accessors/web/hosts/node");

        // @codeDirectory@ and @modelName@ are set in
        // RunnableCodeGenerator._executeCommands().
        // Run the accessors for 2000 ms.
        runCommand.setExpression("node nodeHostInvoke.js -timeout @timeout@ hosts/node/@modelName@");

        generatorPackageList.setExpression("generic.accessor");
    }

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a Accessor-style comment, which
     *  begins with "//" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    @Override
    public String comment(String comment) {
        return "// " + comment + _eol;
    }

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
     *  This method is the main entry point to generate js.
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
        String PTII = StringUtilities.getProperty("ptolemy.ptII.dir");

        if (uri != null) {
            modelURI = uri.toString();
            System.out.println("AccessorCodeGenerator: PTII: " + PTII + " "
                    + modelURI.startsWith("file:/") + " " + modelURI.contains(PTII));
            if (modelURI.startsWith("file:/")
                    && modelURI.contains(PTII)) {
                modelURI = modelURI.substring(5).replace(PTII, "$PTII");
            }
        }

        try {
            code.append("exports.setup = function() {" + _eol
                + INDENT1 + comment(" This composite accessor was created by Cape Code.")
                + INDENT1 + comment(" To run the code, run: ")
                    + INDENT1 + comment(" (cd " + codeDirectory.asFile().getCanonicalPath().replace(PTII, "$PTII") + "; "
                                    + _runCommand() + ")")
                + INDENT1 + comment(" To regenerate this composite accessor, run:")
                + INDENT1 + comment(" java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -language accessor " + modelURI)
                + INDENT1 + comment(" to edit the model, run:")
                + INDENT1 + comment(" $PTII/bin/vergil -capecode " + modelURI)
                + ((AccessorCodeGeneratorAdapter) getAdapter(toplevel()))
                .generateAccessor()
                + "};" + _eol);
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
}
