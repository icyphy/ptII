/* Code generator for HTML.

Copyright (c) 2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
////HTMLCodeGenerator

/** Base class for HTML code generator.
*
*  @author Man-Kit Leung, Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 7.1
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (rodiers)
*/
public class HTMLCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the HTMLCodeGenerator.
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

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////
    
    /** Generate code and append it to the given string buffer.
     *  Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code>
     *  This method is the main entry point.
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    protected int _generateCode(StringBuffer code) throws KernelException {;
        // FIXME: We should put in some default html version info.
        // e.g. <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
        // <html xmlns="http://www.w3.org/1999/xhtml"xml:lang="en" lang="en" dir="ltr">
        code.append("<html>" + _eol);

        code.append("<head>" + _eol);        
        code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" + _eol);
        code.append("<title>" + toplevel().getName() + "</title>" + _eol);
        code.append("</head>" + _eol);        

        code.append("<body>" + _eol);        
        code.append(((HTMLCodeGeneratorAdapter)getAdapter(toplevel())).generateHTML());        
        code.append("</body>" + _eol);        

        code.append("</html>" + _eol);
        
        return super._generateCode(code);
    }
    
    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.  
     */
    protected Class<?> _getAdapterClassFilter() {
        return HTMLCodeGeneratorAdapter.class;
    }
        
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a HTML-style comment, which
     *  begins with "<!--" and ends with "-->" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    protected String _formatComment(String comment) {
        return "<!-- " + comment + " -->" + _eol;
    }

    
}
