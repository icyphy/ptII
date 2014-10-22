/* An AtomicActor that handles some sort of control flow.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ControlActor

/**
An AtomicActor that handles some sort of control flow.
Examples of ControlActors include IfThen, IfThenElse, and Break.

Control Actors are responsible for setting the list of output ports
that are currently enabled (enabledOutports).

FIXME:  What to do about Ptolemy control actors?
FIXME:  Should this be a class, or an interface?  Has a new variable
        for holding control flow information.

  @author Elizabeth Latronico (Bosch)
  @version $Id$
  @since Ptolemy II 10.0
  @Pt.ProposedRating Red (beth)
  @Pt.AcceptedRating Red (beth)
 */
public class ControlActor extends TypedAtomicActor {
    // All the constructors are wrappers of the super class constructors.

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public ControlActor() {
        super();
        _enabledOutports = new ArrayList<TypedIOPort>();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ControlActor(Workspace workspace) {
        super(workspace);
        _enabledOutports = new ArrayList<TypedIOPort>();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ControlActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _enabledOutports = new ArrayList<TypedIOPort>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the (possibly empty) list of enabled output ports.
     *
     *  @return The (possible empty) list of enabled output ports
     *  @see #setEnabledOutports(List)
     **/
    public ArrayList<TypedIOPort> getEnabledOutports() {
        return _enabledOutports;
    }

    /** Return the (possibly empty) list of disabled output ports.
     *
     *  @return The (possible empty) list of disabled output ports.
     */
    public ArrayList<TypedIOPort> getDisabledOutports() {
        ArrayList<TypedIOPort> disabledOutports = new ArrayList<TypedIOPort>();
        for (Object outPort : outputPortList()) {
            if (!_enabledOutports.contains(outPort)) {
                disabledOutports.add((TypedIOPort) outPort);
            }
        }
        return disabledOutports;
    }

    /** Return true if there is at least one enabled output port,
     *  false otherwise.
     *
     *  @return True if at least one output port is enabled.
     */

    public boolean hasEnabledOutports() {
        // Should never be null, but check just in case
        if (_enabledOutports != null && !_enabledOutports.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the list of enabled output ports.  Used by subclasses.
     *  Returns true if successful, and false if there was a problem
     *  (for example, if a port is not an output port).
     *
     *  If unsuccessful, the list of output ports is cleared.
     *
     *  @param ports List of output ports
     *  @return True if operation was successful; false otherwise
     *  @see #getEnabledOutports()
     */
    protected boolean setEnabledOutports(List<TypedIOPort> ports) {
        // Clear the enabledOutports list
        _enabledOutports.clear();

        if (ports != null) {
            // Iterate through ports passed in
            // An empty list is OK
            Iterator portIterator = ports.iterator();

            while (portIterator.hasNext()) {
                TypedIOPort p = (TypedIOPort) portIterator.next();
                if (p.isOutput()) {
                    _enabledOutports.add(p);
                }

                else {
                    // Not allowed.  Clear the enabledOutports list
                    // and return false
                    _enabledOutports.clear();
                    return false;
                }
            }
            return true;
        }

        // Return false if a null list is passed in
        else {
            return false;
        }

    }

    /** Add a port to the list of enabled output ports.
     *  Used by subclasses.
     *  Returns true if successful, and false if there was a problem
     *  (for example, if a port is not an output port).
     *
     *  If unsuccessful, the list of output ports is unchanged.
     *
     *  @param port Output port
     *  @return True if operation was successful; false otherwise
     */
    protected boolean addEnabledOutport(TypedIOPort port) {
        if (port != null && port.isOutput()) {
            _enabledOutports.add(port);
            return true;
        } else {
            return false;
        }
    }

    /** Clear the list of enabled output ports.
     *  Used by subclasses.
     */

    protected void clearEnabledOutports() {
        _enabledOutports.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Returns a list of 0 or more output ports that are currently
     *  enabled.  An 'enabled' output port means that actors connected
     *  to this port should be executed.
     */

    protected ArrayList<TypedIOPort> _enabledOutports;

}
