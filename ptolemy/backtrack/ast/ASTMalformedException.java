/* The AST from a Java source file is malformed.

 Copyright (c) 2005 The Regents of the University of California.
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
//// ASTMalformedException

/**
 Thrown by the {@link ASTBuilder} when the AST created from a Java
 source file is malformed, either because the source file do not conform
 to the Java grammar, or because some unknown error occurs in the
 Eclipse parser.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ASTMalformedException extends ASTException {
    /** Create an exception without identifying the name of the source file
     *  that causes it.
     */
    public ASTMalformedException() {
        this(null);
    }

    /** Create an exception with the name of the source file that causes it.
     *
     *  @param sourceFileName The name of the problematic source file, or
     *   <tt>null</tt> if the file name is missing.
     */
    public ASTMalformedException(String sourceFileName) {
        super("The AST"
                + ((sourceFileName == null) ? "" : (" of Java file \""
                        + sourceFileName + "\"")) + " is malformed.");
    }
}
