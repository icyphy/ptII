/* Code generator for HTML.

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

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////HTMLCodeGenerator

/** Generate a HTML description of a model.
 *  <p>To generate an HTML version of a model, use:
 *  <pre>
 java -classpath $PTII ptolemy.cg.kernel.generic.html.HTMLCodeGenerator -generatorPackage ptolemy.cg.kernel.generic.html -generatorPackageList generic.html adapter/generic/html/demo/HierarchicalModel/HierarchicalModel.xml
 * </pre>
 *  @author Man-Kit Leung, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (rodiers)
 *  @Pt.AcceptedRating red (rodiers)
 */
public class HTMLCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the HTMLCodeGenerator.
     *  The value of the <i>generatorPackageList</i> parameter of the
     *  base class is set to <code>generic.html</code>
     *  @param container The container.
     *  @param name The name of the HTMLCodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public HTMLCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, "html");
        generatorPackageList.setExpression("generic.html");
    }

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a HTML-style comment, which
     *  begins with "<!--" and ends with "-->" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    @Override
    public String comment(String comment) {
        return "<!-- " + comment + " -->" + _eol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate HTML and append it to the given string buffer.
     *  Write the code to the directory specified by the <i>codeDirectory</i>
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.cg.kernel.generic.html</code>, then the file that is
     *  written will be <code>$HOME/Foo.html</code>
     *  This method is the main entry point to generate html.
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {
        // FIXME: We should put in some default html version info.
        // e.g. <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
        // <html xmlns="http://www.w3.org/1999/xhtml"xml:lang="en" lang="en" dir="ltr">
        code.append("<html>" + _eol);

        code.append("<head>" + _eol);
        code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                + _eol);
        code.append("<title>" + toplevel().getName() + "</title>" + _eol);
        code.append("</head>" + _eol);

        code.append("<body>" + _eol);
        code.append(((HTMLCodeGeneratorAdapter) getAdapter(toplevel()))
                .generateHTML());
        code.append("</body>" + _eol);

        code.append("</html>" + _eol);

        return super._generateCode(code);
    }

    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.
     */
    @Override
    protected Class<?> _getAdapterClassFilter() {
        return HTMLCodeGeneratorAdapter.class;
    }
}
