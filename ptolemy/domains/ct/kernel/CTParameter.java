/* The parameter class for CT domain

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTParameter
/** 
In the CT domain, the actor is the ParameteListener for parameter.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTParameter extends Parameter{
    /** Construct a CTParameter in the default workspace with an empty string
     *  as its name.
     *  The parameter is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public CTParameter() {
        super();
    }

    /** Construct a CTParameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public CTParameter(Workspace workspace) {
        super(workspace);
    }

    /** Construct a CTParameter with the given name contained by the specified
     *  CTActor. The container argument must not be null, or a
     *  NullPointerException will be thrown.  The container must be an 
     *  instance of ParameterListener, otherwise an IllegalActionException
     *  is thrown. This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public CTParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try {
            addParameterListener((ParameterListener)container);
        } catch (ClassCastException ex) {
            // remove the parameter from the container.
            this.setContainer(null);
            throw new IllegalActionException(this, container,
                "CTParameter can only be attached to CT actors.");
        }
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  The container must be a 
     *  ParameterListener, otherwise an IllegalActionException will be thrown.
     *  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public CTParameter(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
        try {
            addParameterListener((ParameterListener)container);
            evaluate();
        } catch (ClassCastException ex) {
            // remove the parameter from the container.
            this.setContainer(null);
            throw new IllegalActionException(this, container,
                "CTParameter can only be attached to CT actors.");
        }
    }

}
