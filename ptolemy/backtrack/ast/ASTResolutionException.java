/* A name in a source program cannot be resolved.

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
//// ASTResolutionException

/**
 Thrown when a name in a source program cannot be resolved. When {@link
 TypeAnalyzer} analyzes a Java program, it tries to assign a type to each
 (sub)expression, each method call, each field, and each local variable.
 If an appropriate type cannot be found, this exception is raised.
 <p>
 Because AST functions are not declared to raise exceptions, this exception
 is designed to be a descendant of {@link RuntimeException}.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ASTResolutionException extends ASTRuntimeException {
    /** Construct an exception representing a name resolution failure.
     *
     *  @param className The name of the class that contains the erroneous
     *   identifier.
     *  @param fieldOrMethodName The identifier in the class that cannot
     *   be resolved as a type.
     */
    public ASTResolutionException(String className, String fieldOrMethodName) {
        super("Cannot resolve field or method \"" + fieldOrMethodName
                + "\" in class \"" + className + "\".");
    }
}
