/* A PassiveVariable is a Variable that does not interact with listeners.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PassiveVariable
/**
A PassiveVariable is a variable that does ignores changes in parameters
and variables that its expression depends on.  That is, its expression
is not automatically re-evaluated when these variables change value.
You should use this class when you wish
to exercise explicit control over when its expression is evaluated.

@author Xiaojun Liu, Edward A. Lee
@version $Id$
*/

public class PassiveVariable extends Variable {
    
    // All the constructors are wrappers of the super class constructors.

    /** Construct a variable in the default workspace with an empty string
     *  as its name.
     *  The variable is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */    
    public PassiveVariable() {
        super();
    }

    /** Construct a variable in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The variable is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the variable.
     */
    public PassiveVariable(Workspace workspace) {
        super(workspace);
    }

    /** Construct a variable with the given name contained as an attribute
     *  by the specified entity. The container argument must not be null, 
     *  or a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the variable.
     *  @exception IllegalActionException If the variable is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   any attribute already in the container.
     */
    public PassiveVariable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a variable with the given container, name, and token.
     *  The container argument must not be null, or a NullPointerException 
     *  will be thrown. This variable will use the workspace of the 
     *  container for synchronization and version counts. If the name 
     *  argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name.
     *  @param token The token contained by this variable.
     *  @exception IllegalActionException If the variable is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   any attribute already in the container.
     */
    public PassiveVariable(NamedObj container, String name,
            ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing.  This class does not re-evaluate its expression when
     *  notified of changes in variables it depends on.
     *  @param event The ParameterEvent containing the information
     *   about why the referenced parameter/variable changed.
     */
    final public void parameterChanged(ParameterEvent event) {
        // Do nothing.
    }
}
