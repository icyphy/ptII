/* Base class for a html generator adapter.

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
package ptolemy.cg.kernel.generic.html;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////HTMLCodeGeneratorAdapter

/**
 * Base class for a html code generator adapter.
 *
 * <p>Subclasses should override generateHTML().
 *
 * <p>Subclasses should be sure to properly indent the code by
 * either using the code block functionality in methods like
 * _generateBlockCode(String) or by calling
 * {@link ptolemy.cg.kernel.generic.program.CodeStream#indent(String)},
 * for example:
 * <pre>
 *     StringBuffer code = new StringBuffer();
 *     code.append(super.generateWrapupCode());
 *     code.append("// Local wrapup code");
 *     return processCode(CodeStream.indent(code.toString()));
 * </pre>
 *
 * @author Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (rodiers)
 * @Pt.AcceptedRating Yellow (rodiers)
 */
public abstract class HTMLCodeGeneratorAdapter extends CodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given component.
     *  @param component The associated component.
     */
    public HTMLCodeGeneratorAdapter(NamedObj component) {
        _component = component;
    }

    /** Generate HTML code.
     *  @return The generated HTML.
     *  @exception IllegalActionException If there is a problem
     *  reading data from the model while generating HTML.
     */
    abstract public String generateHTML() throws IllegalActionException;

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    @Override
    public GenericCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    @Override
    public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The code generator that contains this adapter class.*/
    protected GenericCodeGenerator _codeGenerator;

    /** The associated component. */
    private NamedObj _component;
}
