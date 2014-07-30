/* This port mirrors properties of associated parameter ports.

@Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.parameters;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;

/** This port mirrors properties of associated parameter ports.
 *
 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (derler)
 @Pt.AcceptedRating Red (derler)
 */
public class ParameterMirrorPort extends ParameterPort {

    /** Construct a new input port in the specified container with the
     *  specified name. The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *  @param container The container.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ParameterMirrorPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This overrides
     *  the base class to unset the associated port. Users of this
     *  class are responsible for resetting it in their clone methods.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(java.io.Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ParameterMirrorPort result = (ParameterMirrorPort) super
                .clone(workspace);
        result._associatedPort = null;
        return result;
    }

    /** Return the associated port, or null if there is none.
     *  @return The associated port, or null if there is none.
     *  @see #setAssociatedPort(ParameterMirrorPort)
     */
    public ParameterMirrorPort getAssociatedPort() {
        return _associatedPort;
    }

    /** Specify an associated port.  Once this is specified,
     *  then any changes made to this port (its name, whether it
     *  is an input or output, and whether it is a multiport) are
     *  mirrored in the associated port, and any changes made in
     *  the associated port are mirrored here.
     *  @param port The associated port.
     *  @see #getAssociatedPort()
     */
    public void setAssociatedPort(ParameterMirrorPort port) {
        _associatedPort = port;
        port._associatedPort = this;

        // NOTE: The association is not propagated to derived
        // objects because we explicitly propagate all the changes.
    }

    /** Override the base class so that if the container is being
     *  set to null, then the associated port is also deleted
     *  (via a change request).  Note that if the container
     *  of this port is changed to something other than null,
     *  there is no reasonable basis for changing the container
     *  of the associated port, so it is left unchanged.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);

        if (container == null && _associatedPort != null
                && _associatedPort.getContainer() != null) {
            // Use a MoML change request to ensure propagation.
            // Note that when that change request is executed,
            // this port will be the associated port, but no
            // change request will be issued because its container
            // is already null.
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    _associatedPort.getContainer(), "<deletePort name=\""
                            + _associatedPort.getName() + "\"/>");
            _associatedPort.getContainer().requestChange(request);
        }
    }

    /** Override the base class to also set the associated port,
     *  if there is one.
     *  @param isInput True to make this an input port.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        super.setInput(isInput);

        if (_associatedPort != null && _associatedPort.isInput() != isInput) {
            // Use a MoML change request to ensure propagation.
            // Note that when that change request is executed,
            // this port will be the associated port, but no
            // change request will be issued because it already
            // has matching status.
            String value = isInput ? "true" : "false";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    _associatedPort.getContainer(),
                    "<property name=\"input\" value=\"" + value + "\"/>");
            _associatedPort.getContainer().requestChange(request);
        }
    }

    /** Override the base class to also set the associated port,
     *  if there is one.
     *  @param name The name of the port.
     *  @exception IllegalActionException If name cannot be set.
     *  @exception NameDuplicationException If port with the name already exists.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        super.setName(name);

        if (_associatedPort != null && !_associatedPort.getName().equals(name)) {
            // Use a MoML change request to ensure propagation.
            // Note that when that change request is executed,
            // this port will be the associated port, but no
            // change request will be issued because it already
            // has matching status.
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    _associatedPort, "<rename name=\"" + name + "\"/>");
            _associatedPort.requestChange(request);
        }
    }

    /** Set PortParameter of this ParameterPort.
     * @param parameter The PortParameter.
     */
    public void setParameter(PortParameter parameter) {
        _parameter = parameter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated port, if there is one. */
    private ParameterMirrorPort _associatedPort = null;
}
