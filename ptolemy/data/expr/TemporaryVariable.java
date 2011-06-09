/* A variable is an attribute that contains a token and can be referenced
 in expressions.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TemporaryVariable

/**
 This is identical to a Variable except that creating it does not
 increment the workspace version. It should be used to create
 a variable to store information and make that information visible
 to the user, but where the presence of this variable has no bearing
 on type resolution, scheduling, model structure, or any other
 property of the model that might invalidate cached information.
 This variable should be used with caution, but use of this
 variable can significantly improve performance since it makes
 caching of information that is expensive to compute much more
 effective.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class TemporaryVariable extends Variable {

    /** Construct a variable with the given name as an attribute of the
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts,
     *  but creation of this variable will not increment the version number
     *  of that workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name of the variable.
     *  @exception IllegalActionException If the container does not accept
     *   a variable as its attribute.
     *  @exception NameDuplicationException If the name coincides with a
     *   variable already in the container.
     */
    public TemporaryVariable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, null, false);
    }
}
