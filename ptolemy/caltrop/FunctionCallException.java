/*
 @Copyright (c) 2004-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.caltrop;

//////////////////////////////////////////////////////////////////////////
//// FunctionCallException

/**
 A convenience exception used to indicate an error in a built-in function.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class FunctionCallException extends
caltrop.interpreter.InterpreterException {
    /**
     * Create a FunctionCallException for an exception
     * in a function with the given cause.
     * @param name The name bound to the function.
     * @param cause The cause.
     */
    public FunctionCallException(String name, Throwable cause) {
        super("Failed to apply function '" + name + "'", cause);
    }

    /**
     * Create a FunctionCallException for an exception
     * in a function with the given cause.
     * @param name The name bound to the function.
     * @param arg The argument.
     * @param cause The cause.
     */
    public FunctionCallException(String name, Object arg, Throwable cause) {
        super("Failed to apply function '" + name + "' to " + arg, cause);
    }

    /**
     * Create a FunctionCallException for an exception
     * in a function with the given cause.
     * @param name The name bound to the function.
     * @param arg1 The argument.
     * @param arg2 The argument.
     * @param cause The cause.
     */
    public FunctionCallException(String name, Object arg1, Object arg2,
            Throwable cause) {
        super("Failed to apply function '" + name + "' to " + arg1 + " and "
                + arg2, cause);
    }
}
