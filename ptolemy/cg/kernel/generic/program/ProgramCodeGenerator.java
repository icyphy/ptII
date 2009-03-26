/* Code generator for the Procedural languages.

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

package ptolemy.cg.kernel.generic.program;

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;


//////////////////////////////////////////////////////////////////////////
////ProceduralCodeGenerator

/** Base class for Procedural code generator.
*
*  @author Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 7.2
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (rodiers)
*/
public class ProgramCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the ProceduralCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProceduralCodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ProgramCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generateComment = new Parameter(this, "generateComment");
        generateComment.setTypeEquals(BaseType.BOOLEAN);
        generateComment.setExpression("true");

        run = new Parameter(this, "run");
        run.setTypeEquals(BaseType.BOOLEAN);
        run.setExpression("true");
        
        generatorPackageList.setExpression("generic.program");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, generate comments in the output code; otherwise,
     *  no comments is generated. The default value is a parameter
     *  with the value true.
     */
    public Parameter generateComment;
    
    /** If true, then run the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter run;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a formatted comment containing the
     *  specified string with a specified indent level.
     *  @param comment The string to put in the comment.
     *  @param indentLevel The indentation level.
     *  @return A formatted comment.
     */
    public String comment(int indentLevel, String comment) {
        try {
            if (generateComment.getToken() == BooleanToken.TRUE) {
                return StringUtilities.getIndentPrefix(indentLevel)
                + _formatComment(comment);
            }
        } catch (IllegalActionException e) {
            // do nothing.
        }
        return "";
    }

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/".
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        try {
            if (generateComment.getToken() == BooleanToken.TRUE) {
                return _formatComment(comment);
            } 
        } catch (IllegalActionException e) {
            // do nothing.
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a C-style comment, which
     *  begins with "\/*" and ends with "*\/" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    private String _formatComment(String comment) {
        return "/* " + comment + " */" + _eol;
    }

    
}
