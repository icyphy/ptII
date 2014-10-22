/*  This parameter, when inserted into a model, causes types to be displayed for all ports.

 @Copyright (c) 2007-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.vergil.actor.lib;

import java.util.List;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ShowTypes

/**
 This attribute, when inserted into a model causes types to be
 displayed on all ports at the same level of the hierarchy as
 this attribute. Note that this conflicts with other
 attributes that display info on ports, such as
 {@link MonitorReceiverAttribute}. The two should not be used together.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ShowTypes extends AbstractInitializableAttribute implements
        ExecutionListener {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ShowTypes(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"90\" height=\"20\" "
                + "style=\"fill:#00FFFF\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:blue\">\n"
                + "ShowTypes\n" + "</text>\n" + "</svg>\n");

        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update displayed types.
     *  @param manager The manager controlling the execution.
     *  @param throwable The throwable to report.
     */
    @Override
    public void executionError(Manager manager, Throwable throwable) {
        _showTypes();
    }

    /** Do nothing.
     *  @param manager The manager controlling the execution.
     */
    @Override
    public void executionFinished(Manager manager) {
    }

    /** Update displayed types.
     *  @param manager The manager controlling the execution.
     *  @see Manager#getState()
     */
    @Override
    public void managerStateChanged(Manager manager) {
        // NOTE: This could be done only when changing state from
        // RESOLVING_TYPES to ITERATING, but for now, we just always do it.
        _showTypes();
    }

    /** Override the base class to also register as an execution
     *  listener with the Manager.
     *  @exception IllegalActionException If thrown by a subclass.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        NamedObj container = getContainer();
        if (container instanceof Actor) {
            _manager = ((Actor) container).getManager();
            // Note that this will have no effect if this is already a listener.
            _manager.addExecutionListener(this);
        }
    }

    /** Specify the container. If the container is not the same as the
     *  previous container, then stop monitoring queue contents in the
     *  previous container, and start monitoring them in the new one.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(final NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        NamedObj previousContainer = getContainer();
        if (previousContainer == container) {
            return;
        }
        if (previousContainer != null
                && previousContainer instanceof CompositeActor) {
            // Remove all _showInfo attributes in the ports of the previous container.
            List<Actor> entities = ((CompositeActor) previousContainer)
                    .entityList();
            for (Actor entity : entities) {
                List<IOPort> ports = entity.inputPortList();
                for (IOPort port : ports) {
                    Attribute attribute = port.getAttribute("_showInfo");
                    if (attribute != null) {
                        attribute.setContainer(null);
                    }
                }
            }
        }
        super.setContainer(container);
        _showTypes();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Show the types on ports. This is done in a change request.
     */
    private void _showTypes() {
        final NamedObj container = getContainer();
        if (container != null && container instanceof CompositeEntity) {
            ChangeRequest request = new ChangeRequest(this,
                    "Update types on ports", true) {
                @Override
                protected void _execute() throws IllegalActionException {
                    List<Entity> entities = ((CompositeEntity) container)
                            .entityList();
                    for (Entity entity : entities) {
                        List<IOPort> ports = entity.portList();
                        for (IOPort port : ports) {
                            // If there is already a _showInfo attribute, do not add one.
                            Attribute attribute = port
                                    .getAttribute("_showInfo");
                            if (attribute == null) {
                                try {
                                    attribute = new StringParameter(port,
                                            "_showInfo");
                                    attribute.setPersistent(false);
                                } catch (NameDuplicationException e) {
                                    throw new InternalErrorException(e);
                                }
                            }
                            if (attribute instanceof StringParameter) {
                                if (port instanceof TypedIOPort) {
                                    ((StringParameter) attribute)
                                            .setExpression(((TypedIOPort) port)
                                                    .getType().toString());
                                } else {
                                    ((StringParameter) attribute)
                                            .setExpression("untyped");
                                }
                            }
                        }
                    }
                }
            };
            // To prevent prompting for saving the model, mark this
            // change as non-persistent.
            request.setPersistent(false);
            requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Manager, if there is one. */
    private Manager _manager;
}
