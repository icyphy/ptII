/* Relation linking TypedIOPorts.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// IORelation
/**
This class overrides some of the methods in IORelation to ensure that
TypedIOPorts are only connected to TypedIOPorts. I.e., Instances of
TypedIORelation can only be linked to instances of TypedIOPort.
Derived classes may further constrain this to subclasses of TypedIOPort.
Such derived classes should override the protected method _checkPort()
to throw an exception.
<p>
To link a TypedIOPort to a TypedIORelation, use the link() or
liberalLink() method in the TypedIOPort class.  To remove a link,
use the unlink() method.
<p>
The container for instances of this class can only be instances of
TypedCompositeActor.  Derived classes may wish to further constrain the
container to subclasses of TypedComponentEntity.  To do this, they should
override the setContainer() method.

@author Yuhong Xiong
@version $Id$
*/
public class TypedIORelation extends IORelation {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public TypedIORelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public TypedIORelation(Workspace workspace) {
	super(workspace);
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public TypedIORelation(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the method in the super class to constrain the
     *  container to be an instance of TypedCompositeActor.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   TypedCompositeActor or null, or this relation and the container
     *   are not in the same workspace.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the relations list of the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedCompositeActor) &&
                (container != null)) {
            throw new IllegalActionException (this, container,
                    "TypedIORelation can only be contained by " +
		    "TypedCompositeActor.");
        }
        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class TypedIOPort).
     *  @param port The candidate port to link to.
     *  @exception IllegalActionException If the port is not an
     *   TypedIOPort.
     */
    protected void _checkPort (Port port) throws IllegalActionException {
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "TypedIORelation can only link to a TypedIOPort.");
        }
    }
}
