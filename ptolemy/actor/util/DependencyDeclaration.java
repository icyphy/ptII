/* An instance of DependencyDeclaration is an attribute that declares
 dependencies between parameters.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DependencyDeclaration
/**
An instance of DependencyDeclaration is an attribute that declares
variable dependence information of a parameter.  This attribute is
usually created in a parameter, when necessary, during the
preinitialize method of an actor.  This class is used primarily by the
ConstVariableModelAnalysis class to determine a change context for
parameters whose value dependence is not declared through an
expression reference, but maintained by the actor's Java code instead.

<p> This attribute is not persistent by default, so it will not be exported
into a MoML representation of the model.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
@see ptolemy.actor.util.ConstVariableModelAnalysis
*/
public class DependencyDeclaration extends Attribute {

    /** Construct an DependencyDeclaration attribute in the given
     *  container with the given name. The container argument must not
     *  be null, or a NullPointerException will be thrown.  If the
     *  name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  Set this attribute to be not persistent.
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DependencyDeclaration(Variable container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of dependents of the parameter that contains
     * this attribute.  This attribute declares that the container
     * depends on at least the given set of parameters.
     * @return A list of variables.
     */
    public List getDependents() {
        return _dependents;
    }

    /** Set the set of dependents for this declaration.
     * @param dependents A list of variables.
     */
    public void setDependents(List dependents) {
        _dependents = dependents;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The declared dependents.
    private List _dependents;

}
