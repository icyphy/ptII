/* Thrown when a property regression test fails.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties;

//////////////////////////////////////////////////////////////////////////
//// PropertyFailedRegressionTestException

/**
 Thrown when a property regression test fails.

 @author Man-kit (Jackie) Leung, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PropertyFailedRegressionTestException extends
        PropertyResolutionException {

    /** Construct an exception that includes the PropertySolver that 
     *  that was involved.
     *  
     *  @param solver The PropertySolver.
     *  @param detail The message.
     */
    public PropertyFailedRegressionTestException(PropertySolver solver,
            String detail) {
        super(solver, detail);
    }

    /** Construct an exception that includes the PropertySolver that 
     *  that was involved and the cause.
     *  @param solver The PropertySolver.
     *  @param cause The cause of this exception, or null if the cause
     *  is not known or nonexistent
     *  @param detail The message.
     */
    public PropertyFailedRegressionTestException(PropertySolver solver,
            Throwable cause, String detail) {
        super(solver, cause, detail);
    }
}
