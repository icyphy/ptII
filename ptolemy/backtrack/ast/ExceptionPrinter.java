/* A default exception printer that prints to the standard output.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

//////////////////////////////////////////////////////////////////////////
//// ExceptionPrinter
/**
   A default exception printer that prints to the standard output. This
   exception printer is used in {@link
   net.sourceforge.jrefactory.factory.ParserFactory#getAbstractSyntaxTree(boolean, ExceptionPrinter)}
   to print out error messages while parsing Java source code.
   <p>
   An error in the Java source code detected by the parser is a syntax
   error, e.g.,
   <pre>    a := 1;</pre>
   while the correct syntax should be
   <pre>    a = 1;</pre>
   in Java.
   <p>
   However, such an error, as can be detected by the Java compiler, is not
   a syntax error but a semantic error:
   <pre>    int i = "Hello World!";</pre>

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
*/
public class ExceptionPrinter implements
        net.sourceforge.jrefactory.factory.ExceptionPrinter {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Output the exception produced while paring the Java source
     *  code. The user may extend this method to customize the
     *  handling of syntax errors.
     *  
     *  @param exc The exception to be output.
     *  @param interactive Whether the parser is run in interactive
     *   mode.
     */
    public void printException(Throwable exc, boolean interactive) {
        exc.printStackTrace();
    }
}
