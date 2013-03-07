/* This actor implements a quantity manager that is a composite actor.

@Copyright (c) 2011-2013 The Regents of the University of California.
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

package ptolemy.actor.lib.qm;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.Const;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This class implements functionality of a composite quantity manager.
*
*  <p>
*  When an intermediate receiver sends a token to an input port of this
*  quantity manager, the original receiver and the token are encoded in a
*  RecordToken. When such a token arrives at an output port, the original token
*  is extracted and sent to the original receiver.
*  <p>
*  A color parameter is used to perform highlighting on the ports that use this
*  quantity manager.
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class CompositeQM extends TypedCompositeActor implements QuantityManager {

    /** Construct a CompositeQM in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeQM(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initialize();
    }

    @Override
    public void setTempPort(IOPort port) {
        // TODO Auto-generated method stub

    }

    /** Construct a CompositeQM with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeQM(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    /** The color associated with this actor used to highlight other
     *  actors or connections that use this quantity manager. The default value
     *  is the color red described by the expression {1.0,0.0,0.0,1.0}.
     */
    public ColorAttribute color;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>color</i>, then update the highlighting colors
     *  in the model.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == color) {
            // FIXME not implemented yet.
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown here.
     *  @return A new CompositeQM.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CompositeQM newObject = (CompositeQM) super.clone(workspace);
        return newObject;
    }

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException Not thrown in this class but may be thrown in derived classes.
     */
    public Receiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Nothing to do here. FIXME: should be deleted.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return null;
    }

    /** Override the fire and change the transferring tokens
     * from and to input/output placeholders.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire() at " + getDirector().getModelTime());
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            // No input ports.

            if (_stopRequested) {
                return;
            }

            for (Const mappedConst : _tokens.keySet()) {
                mappedConst.value.setToken(_tokens.get(mappedConst));
                mappedConst.fire();
            }
            _tokens.clear();

            getDirector().fire();

            if (_stopRequested) {
                return;
            }

            // No output ports.

            List attributes = this.attributeList();
            for (int k = 0; k < attributes.size(); k++) {
                Attribute attribute = (Attribute) attributes.get(k);
                if (attribute instanceof Parameter) {
                    String parameterName = ((Parameter) attribute).getName();
                    if (parameterName.startsWith("sendTo_")) {
                        if (((Parameter) attribute).getToken() != null) {
                            String actorName = parameterName.substring(
                                    parameterName.indexOf("_") + 1,
                                    parameterName.indexOf("_",
                                            parameterName.indexOf("_") + 1));
                            String portName = parameterName
                                    .substring(parameterName.indexOf("_",
                                            parameterName.indexOf("_") + 1) + 1);
                            Actor actor = (Actor) ((CompositeActor) getContainer())
                                    .getEntity(actorName);
                            for (Object object : actor.inputPortList()) {
                                IOPort port = (IOPort) object;
                                if (port.getName().equals(portName)) {
                                    ((IntermediateReceiver) port.getReceivers()[0][0])._receiver
                                            .put(((Parameter) attribute)
                                                    .getToken());
                                    ((CompositeActor) actor.getContainer())
                                            .getDirector().fireAtCurrentTime(
                                                    actor);
                                }
                            }
                            ((Parameter) attribute).reset();
                        }
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Reset.
     */
    public void reset() {
        // FIXME what to do here?
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        if (_mappedConsts == null) {
            _mappedConsts = new HashMap<Receiver, Const>();
        }
        Const mappedConst = _mappedConsts.get(receiver);
        if (mappedConst == null) {
            List entities = this.entityList();
            for (int j = 0; j < entities.size(); j++) {
                Object object = entities.get(j);
                if (object instanceof Const
                        && ((Const) object).getName().equals(
                                receiver.getContainer().getContainer()
                                        .getName()
                                        + "_"
                                        + receiver.getContainer().getName())) {
                    mappedConst = (Const) object;
                    _mappedConsts.put(receiver, mappedConst);
                    break;
                }
            }
        }
        if (mappedConst == null) {
            throw new IllegalActionException(this, "No mapping constant in "
                    + this.getName() + " for "
                    + receiver.getContainer().getContainer().getName() + "_"
                    + receiver.getContainer().getName());
        }
        if (_tokens == null) {
            _tokens = new HashMap<Const, Token>();
        }
        _tokens.put(mappedConst, token);

        ((CompositeActor) getContainer()).getDirector().fireAtCurrentTime(this);

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Initialize color and private lists.
     * @exception IllegalActionException If color attribute cannot be initialized.
     * @exception NameDuplicationException If color attribute cannot be initialized.
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap<Const, Token> _tokens;

    private HashMap<Receiver, Const> _mappedConsts;
}
