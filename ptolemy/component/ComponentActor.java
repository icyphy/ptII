/* A ComponentActor is outside component and inside actor.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.component;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.TupleToken;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ComponentActor

/**
 A Component is outside compatible with components and inside compatable
 with actors.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating yellow (ellen_zh)
 @Pt.AcceptedRating red (cxh)
 */
public class ComponentActor extends TypedCompositeActor implements Component {
    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ComponentActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setContainer(container);
        input = new IOMethodPort(this, "input", true, false);

        output = new IOMethodPort(this, "output", false, true);
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public IOMethodPort input;

    public IOMethodPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a composite actor with clones of the ports of the
     *  original actor, the contained actors, and the contained relations.
     *  The ports of the returned actor are not connected to anything.
     *  The connections of the relations are duplicated in the new composite,
     *  unless they cross levels, in which case an exception is thrown.
     *  The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeActor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ComponentActor newObject = (ComponentActor) super.clone(workspace);
        //newObject._inputPortsVersion = -1;
        //newObject._outputPortsVersion = -1;
        return newObject;
    }

    /** Invalidate the schedule and type resolution and create
     *  new receivers if the specified port is an opaque
     *  output port.  Also, notify the containers of any ports
     *  deeply connected on the inside by calling their connectionsChanged()
     *  methods, since their width may have changed.
     *  @param port The port that has connection changes.
     */

    //FIXME: the reason that i need overwrite this method is because
    // it checked whether the container is compositeActor to avoid
    // infinite loop. But here it should be componentActor. We should change
    // the check to refer to a super interface of this two...
    @Override
    public void connectionsChanged(Port port) {
        if (_debugging) {
            _debug("Connections changed on port: " + port.getName());
        }

        super.connectionsChanged(port);

        if (port instanceof ComponentPort) {
            // NOTE: deepInsidePortList() is not the right thing here
            // since it will return the same port if it is opaque.
            Iterator<?> insidePorts = ((ComponentPort) port).insidePortList()
                    .iterator();

            try {
                _inConnectionsChanged = true;

                while (insidePorts.hasNext()) {
                    ComponentPort insidePort = (ComponentPort) insidePorts
                            .next();
                    Entity portContainer = (Entity) insidePort.getContainer();

                    // Avoid an infinite loop where notifications are traded.
                    if (!(portContainer instanceof ComponentActor)
                            || !((ComponentActor) portContainer)._inConnectionsChanged) {
                        portContainer.connectionsChanged(insidePort);
                    }
                }
            } finally {
                _inConnectionsChanged = false;
            }
        }

        if (port instanceof IOPort) {
            IOPort castPort = (IOPort) port;

            if (castPort.isOpaque()) {
                Manager manager = getManager();

                if (castPort.isOutput() && getDirector() != null
                        && manager != null
                        && manager.getState() != Manager.IDLE
                        && manager.getState() != Manager.INFERING_WIDTHS
                        && manager.getState() != Manager.PREINITIALIZING) {

                    // Note that even if castPort is opaque, we still have to
                    // check for director above.
                    try {
                        castPort.createReceivers();
                    } catch (IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(this, ex,
                                "Cannot create receivers");
                    }
                }

                if (castPort.isInput() && getExecutiveDirector() != null
                        && manager != null
                        && manager.getState() != Manager.IDLE
                        && manager.getState() != Manager.INFERING_WIDTHS
                        && manager.getState() != Manager.PREINITIALIZING) {
                    try {
                        castPort.createReceivers();
                    } catch (IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(this, ex,
                                "Cannot create receivers");
                    }
                }

                // Invalidate the local director schedule and types
                if (getDirector() != null) {
                    getDirector().invalidateSchedule();
                    getDirector().invalidateResolvedTypes();
                }
            }
        }
    }

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("Called fire()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            if (_stopRequested) {
                return;
            }

            Director director = getDirector();
            director.fire();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Load the generated class and search for its fire method.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    /** Execute the component, which in this base class means doing
     *  nothing and returning immediately.
     *  This is invoked after preinitialize()
     *  and initialize(), and may be invoked repeatedly.
     *  @exception IllegalActionException If the run cannot be completed
     *   (not thrown in this base class).
     */
    @Override
    public void run() throws IllegalActionException {
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Component#initialize()
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Component#wrapup()
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Invoke one iteration and transfer the token to a method call.
     */
    protected TupleToken _executeInside() {
        if (_debugging) {
            _debug("execute inside");
        }

        System.out.println("call execute inside");

        try {
            int iter = iterate(1);

            //System.out.println("the iterate return is: " + iter);
            if (iter == Executable.COMPLETED) {
                return output.call();
            } else {
                return TupleToken.VOID;
            }
        } catch (Exception ex) {
            // this shouldn't happen.
            throw new InternalErrorException(this, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */

    //FIXME: how should I modify this mehtod.
    //    public void createReceivers() throws IllegalActionException {
    //        Iterator ports = portList().iterator();
    //
    //        while (ports.hasNext()) {
    //            IOMethodPort onePort = (IOMethodPort) ports.next();
    //            onePort.createReceivers();
    //        }
    //    }
    @Override
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
    }

    // Indicator that we are in the connectionsChanged method.
    private boolean _inConnectionsChanged = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _addIcon() {
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:white\"/>\n"
                + "<polygon points=\"-20,-10 20,0 -20,10\" "
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // A class that encapsulates the declared and resolved types of a
    // field and implements the InequalityTerm interface.
    private class IOMethodPort extends TypedIOPort implements Method {
        /** Construct a port with the given name contained by the specified
         *  entity. The container argument must not be null, or a
         *  NullPointerException will be thrown.  This port will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty
         *  string.  Increment the version of the workspace.
         *  @param container The container entity.
         *  @param name The name of the port.
         *  @exception IllegalActionException If the port is not of an acceptable
         *   class for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   a port already in the container.
         */
        public IOMethodPort(ComponentEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Construct an IOPort with a container and a name that is
         *  either an input, an output, or both, depending on the third
         *  and fourth arguments. The specified container must implement
         *  the Actor interface or an exception will be thrown.
         *
         *  @param container The container actor.
         *  @param name The name of the port.
         *  @param isInput True if this is to be an input port.
         *  @param isOutput True if this is to be an output port.
         *  @exception IllegalActionException If the port is not of an acceptable
         *   class for the container, or if the container does not implement the
         *   Actor interface.
         *  @exception NameDuplicationException If the name coincides with
         *   a port already in the container.
         */
        public IOMethodPort(ComponentEntity container, String name,
                boolean isInput, boolean isOutput)
                        throws IllegalActionException, NameDuplicationException {
            this(container, name);
            setInput(isInput);
            setOutput(isOutput);
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Override the base class to attach an empty properties token.
         *  @see ptolemy.domains.de.kernel.DEReceiver#put(ptolemy.data.Token)
         */
        public synchronized TupleToken call() {
            //FIXME: we assume only one token is going to be outputed.
            //what is the correct sematics here?
            System.out.println("try to call outside");

            if (isOutput()) {
                try {
                    // FIXME: This loop will only go through
                    // the once and then return.
                    for (int i = 0; i < getWidthInside(); /*i++*/) {
                        if (hasTokenInside(i)) {
                            //System.out.println("has token to transfer to a method call outside");
                            Token t = getInside(i);
                            Token[] tokens = new Token[1];
                            tokens[0] = t;

                            Iterator<?> ports = this.deepConnectedPortList()
                                    .iterator();
                            MethodCallPort port = (MethodCallPort) ports.next();

                            //System.out.println("get the connected method call port");
                            return port.call(new TupleToken(tokens));
                        } else {
                            return TupleToken.VOID;
                        }
                    }
                } catch (Exception ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            } else {
                // The port provided should over write this method.
                return TupleToken.VOID;
            }

            return TupleToken.VOID;
        }

        /**
         *  @exception IllegalActionException If the transaction fails (e.g.
         *   the data type is incompatible).
         */
        @Override
        public synchronized TupleToken call(TupleToken token)
                throws IllegalActionException {
            if (isInput()) {
                int l = token.length();

                //Assume only one port is connected to this.
                Iterator<?> ports = this.deepInsidePortList().iterator();
                IOPort port = (IOPort) ports.next();
                Receiver[][] receivers = port.getReceivers();

                for (int i = 0; i < l; i++) {
                    Token t = token.getElement(i);

                    //assume not multiple port.
                    receivers[0][0].put(t);
                }

                return _executeInside();
            } else {
                return TupleToken.VOID;
            }
        }

        /** Create new receivers for this port, replacing any that may
         *  previously exist, and validate any instances of Settable that
         *  this port may contain. This method should only be called on
         *  opaque ports.
         *  <p>
         *  If the port is an input port, receivers are created as necessary
         *  for each relation connecting to the port from the outside.
         *  If the port is an output port, receivers are created as necessary
         *  for each relation connected to the port from the inside. Note that
         *  only composite entities will have relations connecting to ports
         *  from the inside.
         *  <p>
         *  Note that it is perfectly allowable for a zero width output port to
         *  have insideReceivers.  This can be used to allow a model to be
         *  embedded in a container that does not connect the port to anything.
         *  <p>
         *  This method is <i>not</i> write-synchronized on the workspace, so the
         *  caller should be.
         *  @exception IllegalActionException If this port is not
         *   an opaque input port or if there is no director.
         */
        @Override
        public void createReceivers() throws IllegalActionException {
            boolean output = isOutput();

            if (output) {
                Iterator<?> insideRelations = insideRelationList().iterator();

                if (insideRelations.hasNext()) {
                    _insideReceivers = new Receiver[1][1];
                    _insideReceivers[0][0] = _newInsideReceiver();
                }
            }
        }

        /** Override the base class to return the inside receiver.
         *  @return The local inside receivers, or an empty array if there are
         *   none.
         */
        @Override
        public Receiver[][] getInsideReceivers() {
            return _insideReceivers;
        }

        @Override
        public Receiver[][] getReceivers() {
            return _insideReceivers;
        }

        /** FIXME... see IORelation.deepReceivers().
         *
         *  @param relation Relations that are linked on the outside or inside.
         *  @param occurrence The occurrence number that we are interested in,
         *   starting at 0.
         *  @return The local receivers, or an empty array if there are none.
         *  @exception IllegalActionException If the relation is not linked
         *   from the outside.
         */
        @Override
        public Receiver[][] getReceivers(IORelation relation, int occurrence)
                throws IllegalActionException {
            return _insideReceivers;
        }

        /** Override parent method to ensure compatibility of the relation
         *  and validity of the width of the port.
         *  If this port is not a multiport, then the width of the
         *  relation is required to be specified to be one.  This method
         *  allows level-crossing links.
         *  This method is <i>not</i> synchronized on the
         *  workspace, so the caller should be.
         *
         *  @param relation The relation to link to on the inside.
         *  @exception IllegalActionException If this port has no container or
         *   the relation is not an IORelation, or the port already linked to a
         *   relation and is not a multiport, or the relation has width
         *   not exactly one and the port is not a multiport, or the
         *   relation is incompatible with this port, or the port is not
         *   in the same workspace as the relation.
         */
        @Override
        protected void _checkLiberalLink(Relation relation)
                throws IllegalActionException {
        }

        /** Override parent method to ensure compatibility of the relation
         *  and validity of the width of the port.
         *  If this port is not a multiport, then the width of the
         *  relation is required to be specified to be one.
         *  This method is <i>not</i> synchronized on the
         *  workspace, so the caller should be.
         *
         *  @param relation The relation to link to.
         *  @exception IllegalActionException If this port has no container or
         *   the relation is not an IORelation, or the port already linked to a
         *   relation and is not a multiport, or if the relation has width
         *   not exactly one and the port is not a multiport, or the port is
         *   not in the same workspace as the relation.
         */
        @Override
        protected void _checkLink(Relation relation)
                throws IllegalActionException {
        }

        // Lists of local receivers, indexed by relation.
        private Receiver[][] _insideReceivers;

        //private boolean _isProviedPort = false;
    }
}
