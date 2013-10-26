/* A class to parse the Procedural template macro constructs in a code generation scope.

Copyright (c) 2009-2013 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaParseTreeCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////ProceduralTemplateParser

/**
A class that allows to parse macros of templates in a code generator
perspective.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class ProceduralTemplateParser extends TemplateParser {

    /** Construct the ProceduralTemplateParser.
     */
    public ProceduralTemplateParser() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        Set<String> files = super.getHeaderFiles();
        files.addAll(_includeFiles);
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                    ////

    /** The set of header files that needed to be included. */
    protected Set<String> _includeFiles = new HashSet<String>();

    /** Generate code that corresponds with the fire() method.
     *  @return The generated code.
     */
    public String generateFireCode() {
        return "//stuff here";
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        // FIXME: We need to create new ParseTreeCodeGenerator each time
        // here or else we get lots of test failures.  It would be better
        // if we could use the same CParseTreeCodeGenerator over and over.
        _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
        return _parseTreeCodeGenerator;
    }

}
