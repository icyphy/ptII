/* Continuous time actor.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;

// FIXME: This class is not needed...soon...

//////////////////////////////////////////////////////////////////////////
//// CTActor
/**
Note: This class will be removed soon.
CTActor is the base class for continuous time actors.
<P>
When a
parameter is changed, the <code>paramChanged</code> flag is set.
The parameter will be updated in the updateParams() method, if the
<code>paramChanged</code> flag is set.  In this base class,
 the updateParams() happens at the prefire()
stage of the iteration, so that the parameters are keep consistent
during the iteration.
@author Jie Liu
@version $Id$
@see ptolemy.actor.TypedAtomicActor
*/
public class CTActor extends TypedAtomicActor {
    /** Construct a CTActor in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     */
    public CTActor() {
	super();
    }

    /** Construct a CTActor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace that will list the entity.
     */
    public CTActor(Workspace workspace) {
	super(workspace);
    }

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param CTSubSystem The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CTActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Set a flag so that at the prefire stage of the next iteration, the
     *  changed attribute can be updated. Notice that the Parameter token
     *  is already changed. What we need to update here is the local copy
     *  of the token. The IllegalActionException may be thrown by the
     *  super class.
     *  @param att The attribute changed.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void attributeChanged(Attribute att) throws IllegalActionException{
        super.attributeChanged(att);
        _attChanged = true;
    }

    /** Update the parameters if they have been changed in the last
     *  iteration.
     *  @return True always.
     *  @exception IllegalActionException Not thrown in this base class.
     *       May be needed by the derived classes.
     */
    public boolean prefire() throws IllegalActionException  {
        if(_attChanged) {
            updateParameters();
            _attChanged = false;
        }
        return  super.prefire();
    }

    /** Update parameters. The local copy of the parameter should be
     *  updated to reflect the changes of the Parameter token.
     *  Do nothing in this base class. Derived class should override
     *  this method to update their parameters if there is any.
     *  @exception IllegalActionException Not thrown in this base class.
     *     May be needed by derived classes for indicating wrong parameter
     *     types or values.
     */
    public void updateParameters() throws IllegalActionException {}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // flag for parameter changes.
    private boolean _attChanged = false;

}
