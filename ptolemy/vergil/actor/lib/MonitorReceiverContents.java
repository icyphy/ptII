/*  This parameter, when inserted into a model, causes the ports in the model to display their unconsumed inputs.

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

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;

///////////////////////////////////////////////////////////////////
//// MonitorReceiverContents

/**
 This parameter, when inserted into a model or an opaque composite actor,
 causes all input ports to acquire an attribute that makes them display
 their contents on the screen.  This works by piggybacking on the
 initialize() method of the container to insert the relevant parameters
 into the ports. It also piggybacks on postfire() and wrapup() to issue
 a ChangeRequest, which causes a repaint of the screen in Vergil.
 To stop monitoring the queue contents, simply delete
 this attribute from the model.
 To use this one option is to instantiate an attribute of type
 ptolemy.vergil.actor.lib.MonitorReceiverContents

 @author  Edward A. Lee, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class MonitorReceiverContents extends SingletonAttribute {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorReceiverContents(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-60\" y=\"-10\" " + "width=\"180\" height=\"20\" "
                + "style=\"fill:#00FFFF\"/>\n" + "<text x=\"-55\" y=\"5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:blue\">\n"
                + "MonitorReceiverContents\n" + "</text>\n" + "</svg>\n");

        // Hide the name.
        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
            // _piggybackContainer should be non-null, but we check anyway.
            if (_piggybackContainer != null) {
                _piggybackContainer.removePiggyback(_executable);
            }
            _executable = null;
            // Also, remove all _showInfo attributes in the ports of the previous container.
            List<Actor> entities = ((CompositeActor) previousContainer)
                    .deepEntityList();
            for (Actor entity : entities) {
                List<IOPort> ports = entity.inputPortList();
                for (IOPort port : ports) {
                    List<MonitorReceiverAttribute> attributes = port
                            .attributeList(MonitorReceiverAttribute.class);
                    for (MonitorReceiverAttribute attribute : attributes) {
                        attribute.setContainer(null);
                    }
                }
            }
        }
        super.setContainer(container);

        if (container != null && container instanceof CompositeActor) {
            if (_executable == null) {
                // The inner class will be piggybacked as an executable for the container to
                // execute change request at the appropriate times. These change request will
                // lead to repaints of the GUI.
                _executable = new Executable() {

                    @Override
                    public void initialize() throws IllegalActionException {
                        // Add _showInfo attributes to any input port that does not already have one.
                        try {
                            workspace().getWriteAccess();
                            List<Actor> entities = ((CompositeActor) container)
                                    .deepEntityList();
                            for (Actor entity : entities) {
                                List<IOPort> ports = entity.inputPortList();
                                for (IOPort port : ports) {
                                    if (port.getAttribute("_showInfo") == null) {
                                        MonitorReceiverAttribute attribute = new MonitorReceiverAttribute(
                                                port, "_showInfo");
                                        attribute.setPersistent(false);
                                    }
                                }
                            }
                        } catch (NameDuplicationException ex) {
                            throw new InternalErrorException(ex);
                        } finally {
                            workspace().doneTemporaryWriting();
                        }
                    }

                    // Request repaint on postfire() and wrapup().
                    @Override
                    public boolean postfire() {
                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true /*Although this not a structural change in my point of view
                                                                   , we however for some reason need to specify it is, otherwise the GUI won't update.*/
                                ) {
                            @Override
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                        return true;
                    }

                    @Override
                    public void wrapup() {
                        ChangeRequest request = new ChangeRequest(this,
                                "SetVariable change request", true) {
                            @Override
                            protected void _execute()
                                    throws IllegalActionException {
                            }
                        };
                        // To prevent prompting for saving the model, mark this
                        // change as non-persistent.
                        request.setPersistent(false);
                        requestChange(request);
                    }

                    // All other methods are empty.
                    @Override
                    public void fire() throws IllegalActionException {
                    }

                    @Override
                    public boolean isFireFunctional() {
                        return true;
                    }

                    @Override
                    public boolean isStrict() {
                        return true;
                    }

                    @Override
                    public int iterate(int count) {
                        return Executable.COMPLETED;
                    }

                    @Override
                    public boolean prefire() throws IllegalActionException {
                        return true;
                    }

                    @Override
                    public void stop() {
                    }

                    @Override
                    public void stopFire() {
                    }

                    @Override
                    public void terminate() {
                    }

                    @Override
                    public void addInitializable(Initializable initializable) {
                    }

                    @Override
                    public void preinitialize() throws IllegalActionException {
                    }

                    @Override
                    public void removeInitializable(Initializable initializable) {
                    }
                };
            }

            _piggybackContainer = (CompositeActor) container;
            _piggybackContainer.addPiggyback(_executable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The executable that creates the monitor attributes in initialize(). */
    private Executable _executable;

    /** The last container on which we piggybacked. */
    private CompositeActor _piggybackContainer;
}
