/* This composite implements a Ptides platform.

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

package ptolemy.domains.ptides.kernel;

import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.hoc.MirrorComposite;
import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.actor.parameters.MirrorPortParameter;
import ptolemy.actor.parameters.ParameterMirrorPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.domains.ptides.lib.PtidesPort;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** This composite implements a Ptides platform,
 *  which is used for the design of distributed real-time systems.
 *  The main reason for implementing this composite is to allow for
 *  special treatment of tokens on input and output ports. Ptides
 *  platforms can only contain the following ports: SensorPort,
 *  ActuatorPort, NetworkReceiverPort, NetworkTransmitterPort.
 *  A Ptides platform must contain a PtidesDirector.
 *  <p>
 *  Network ports receive and transmit RecordTokens that encapsulate
 *  Ptides events (timestamp and value).
 *  A NetworkReceiverPort extracts the timestamp of the
 *  Ptides event in the RecordToken and creates a new event on the
 *  event queue on the PtidesDirector with this timestamp.
 *
 *  FIXME: The base class seems to have some support for
 *  ParameterPorts but nothing that really works?
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 10.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class PtidesPlatform extends MirrorComposite {

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtidesPlatform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an PtidesPlatform in the specified workspace with
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
    public PtidesPlatform(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Clone the object into the specified workspace. This overrides
     *  the base class to set the association with iterationCount.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PtidesPlatform result = (PtidesPlatform) super.clone(workspace);
        try {
            // Remove the old inner PtidesPlatformDirector that is in the wrong workspace.
            String ptidesPlatformDirectorName = null;
            Iterator ptidesPlatformDirectors = result.attributeList(
                    PtidesPlatformDirector.class).iterator();
            while (ptidesPlatformDirectors.hasNext()) {
                PtidesPlatformDirector oldPtidesPlatformDirector = (PtidesPlatformDirector) ptidesPlatformDirectors
                        .next();
                if (ptidesPlatformDirectorName == null) {
                    ptidesPlatformDirectorName = oldPtidesPlatformDirector
                            .getName();
                }
                oldPtidesPlatformDirector.setContainer(null);
            }

            // Create a new PtidesPlatformDirector that is in the right workspace.
            PtidesPlatformDirector platformDirector = result.new PtidesPlatformDirector(
                    workspace);
            platformDirector.setContainer(result);
            platformDirector.setName(ptidesPlatformDirectorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        return result;
    }

    /** FIXME: Return SensorPort or ActuatorPort. How do i know if
     *  the port is an input or an output?
     *
     *  Override the base class to return a specialized port.
     *  @param name The name of the port to create.
     *  @return A new instance of PtidesMirrorPort, an inner class.
     *  @exception NameDuplicationException If the container already has a port
     *  with this name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            PtidesMirrorPort result = new PtidesMirrorPort(this, name);

            return result;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _addPort(Port port) throws IllegalActionException,
            NameDuplicationException {
        // TODO Auto-generated method stub
        super._addPort(port);
    }

    /** Read inputs from ParameterPorts and update the paramter of the
     *  associatedPort.
     *  @exception IllegalActionException If reading from parameter associated
     *  with port fails.
     */
    @Override
    protected void _transferPortParameterInputs() throws IllegalActionException {
        // Need to read from port parameters
        // first because in some domains (e.g. SDF)
        // the behavior of the schedule might depend on rate variables
        // set from ParameterPorts.
        for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                .hasNext() && !_stopRequested;) {
            IOPort p = (IOPort) inputPorts.next();

            if (p instanceof ParameterMirrorPort) {
                ParameterMirrorPort port = (ParameterMirrorPort) p;
                ParameterMirrorPort associatedPort = port.getAssociatedPort();
                PortParameter associatedParameter = associatedPort
                        .getParameter();

                // Coverity says that port cannot be null here.
                if (port.isOutsideConnected() && port.hasToken(0)) {
                    Token token = port.get(0);
                    associatedParameter.setCurrentValue(token);
                    // Have to validate so that containers of dependent
                    // variables get attributeChanged() called.
                    associatedParameter.validate();

                    if (_debugging) {
                        _debug("Updated parameter value to: " + token);
                    }
                }
            }
        }
    }

    /** Check types from a source port to a group of destination ports,
     *  assuming the source port is connected to all the ports in the
     *  group of destination ports.  Return a list of instances of
     *  Inequality that have type conflicts.  This overrides the base
     *  class so that if one of the ports belongs to this PtidesPlatform
     *  actor, then its element type is compared against the inside port.
     *  @param sourcePort The source port.
     *  @param destinationPortList A list of destination ports.
     *  @return A list of instances of Inequality indicating the
     *   type constraints that are not satisfied.
     */
    @Override
    protected List _checkTypesFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();

        boolean isUndeclared = sourcePort.getTypeTerm().isSettable();

        if (!isUndeclared) {
            // sourcePort has a declared type.
            Type srcDeclared = sourcePort.getType();
            Iterator destinationPorts = destinationPortList.iterator();

            while (destinationPorts.hasNext()) {
                TypedIOPort destinationPort = (TypedIOPort) destinationPorts
                        .next();
                isUndeclared = destinationPort.getTypeTerm().isSettable();

                if (!isUndeclared) {
                    // both source/destination ports are declared,
                    // check type
                    Type destinationDeclared = destinationPort.getType();

                    int compare;

                    // If the source port belongs to me, then we want to
                    // compare its payload type to the type of the
                    // destination.
                    if (sourcePort.getContainer() == this
                            && _isAssociatedWithNetworkReceiver(sourcePort)
                            && destinationPort.getContainer() != this) {
                        // The source port belongs to me, but not the
                        // destination.

                        Type srcPayloadType = ((RecordType) srcDeclared)
                                .get("payload");
                        compare = TypeLattice.compare(srcPayloadType,
                                destinationDeclared);
                    } else if (sourcePort.getContainer() != this
                            && destinationPort.getContainer() == this
                            && _isAssociatedWithNetworkTransmitter(destinationPort)) {
                        // The destination port belongs to me, but not
                        // the source.

                        Type destinationPayloadType = ((RecordType) destinationDeclared)
                                .get("payload");

                        compare = TypeLattice.compare(srcDeclared,
                                destinationPayloadType);
                    } else {
                        compare = TypeLattice.compare(srcDeclared,
                                destinationDeclared);
                    }

                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        Inequality inequality = new Inequality(
                                sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(inequality);
                    }
                }
            }
        }

        return result;
    }

    /** Return the type constraints on all connections starting from the
     *  specified source port to all the ports in a group of destination
     *  ports. This overrides the base class to ensure that if the source
     *  port or the destination port is a port of this composite, then
     *  the port is forced to be an array type and the proper constraint
     *  on the element type of the array is made. If the source port
     *  has no possible sources of data, then no type constraints are
     *  added for it.
     *  @param sourcePort The source port.
     *  @return A list of instances of Inequality.
     */
    @Override
    protected List _destinationTypeConstraints(TypedIOPort sourcePort) {
        Iterator<IOPort> destinationPorts;
        List<Inequality> result = new LinkedList<Inequality>();
        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();

        if (sourcePort.isInput()) {
            destinationPorts = sourcePort.insideSinkPortList().iterator();
        } else {
            destinationPorts = sourcePort.sinkPortList().iterator();
        }

        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort) destinationPorts.next();
            boolean destUndeclared = destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if (sourcePort.getContainer() == this
                        && destinationPort.getContainer() == this) {
                    // Both ports belong to this, so their type must be equal.
                    // Represent this with two inequalities.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);

                    Inequality ineq2 = new Inequality(
                            destinationPort.getTypeTerm(),
                            sourcePort.getTypeTerm());
                    result.add(ineq2);
                } else if (sourcePort.getContainer().equals(this)) {
                    if (sourcePort.sourcePortList().size() == 0) {
                        // Skip this port. It is not connected on the outside.
                        continue;
                    }

                    if (destinationPort instanceof PtidesPort
                            && ((PtidesPort) destinationPort).isSensorPort()) {
                        Inequality ineq = new Inequality(
                                sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(ineq);
                    } else if (destinationPort instanceof PtidesPort
                            && ((PtidesPort) destinationPort)
                                    .isNetworkReceiverPort()) {

                        if (_useRealTime) {
                            sourcePort.setTypeEquals(new PtidesNetworkRealTimeType());
                        } else {
                            sourcePort.setTypeEquals(new PtidesNetworkModelTimeType());
                        }
                        RecordType sourcePortType = (RecordType) sourcePort
                                .getType();
                        Inequality ineq = new Inequality(
                                sourcePortType.getTypeTerm("payload"),
                                destinationPort.getTypeTerm());
                        result.add(ineq);
                    }
                } else if (destinationPort.getContainer().equals(this)) {
                    // Require that the destination port type be an array
                    // with elements compatible with the source port.

                    Inequality ineq = null;
                    if (sourcePort instanceof PtidesPort
                            && ((PtidesPort) sourcePort).isActuatorPort()) {
                        ineq = new Inequality(sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                    } else if (sourcePort instanceof PtidesPort
                            && ((PtidesPort) sourcePort)
                                    .isNetworkTransmitterPort()) {

                        if (_useRealTime) {
                            destinationPort.setTypeEquals(new PtidesNetworkRealTimeType());
                        } else {
                            destinationPort.setTypeEquals(new PtidesNetworkModelTimeType());
                        }
                        RecordType outputType = (RecordType) destinationPort
                                .getType();

                        ineq = new Inequality(sourcePort.getTypeTerm(),
                                outputType.getTypeTerm("payload"));
                    }
                    result.add(ineq);
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        setClassName("ptolemy.domains.ptides.kernel.PtidesPlatform");
        // Create the PtidesPlatformDirector in the proper workspace.
        PtidesPlatformDirector platformDirector = new PtidesPlatformDirector(
                workspace());
        platformDirector.setContainer(this);
        platformDirector.setName(uniqueName("PtidesPlatformDirector"));
    }

    /** Check if given MirrorPort is associated with a NetworkReceiverPort.
     *  @param port The MirrorPort.
     *  @return True if is associated with a NetworkReceiverPort.
     */
    private boolean _isAssociatedWithNetworkReceiver(Port port) {
        return ((PtidesPort) ((MirrorPort) port).getAssociatedPort())
                .isNetworkReceiverPort();
    }

    /** Check if given MirrorPort is associated with a NetworkTransmitterPort.
     *  @param port The MirrorPort.
     *  @return True if is associated with a NetworkTransmitterPort.
     */
    private boolean _isAssociatedWithNetworkTransmitter(Port port) {
        return ((PtidesPort) ((MirrorPort) port).getAssociatedPort())
                .isNetworkTransmitterPort();
    }
    
    private boolean _useRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The Ptides type that is sent over the network. This is a RecordType
     *  containing timestamp, microstep and payload.
     */
    public static class PtidesNetworkModelTimeType extends RecordType {

        /** Create a new default PtidesNetworkType.
         */
        public PtidesNetworkModelTimeType() {
            super(LABELS, TYPES);
        }

        /** The types of the RecordType fields. */
        public static Type[] TYPES = { BaseType.DOUBLE, BaseType.INT,
                BaseType.UNKNOWN };

        /** The labels of the RecordType fields. */
        public static String[] LABELS = new String[] { "timestamp",
                "microstep", "payload" };

        /** Label of the timestamp that is transmitted within the RecordToken.
         */
        private static final String timestamp = "timestamp";

        /** Label of the microstep that is transmitted within the RecordToken.
         */
        private static final String microstep = "microstep";

        /** Label of the payload that is transmitted within the RecordToken.
         */
        private static final String payload = "payload";
    }
    
    /** The Ptides type that is sent over the network. This is a RecordType
     *  containing timestamp, microstep and payload.
     */
    public static class PtidesNetworkRealTimeType extends RecordType {

        /** Create a new default PtidesNetworkType.
         */
        public PtidesNetworkRealTimeType() {
            super(LABELS, TYPES);
        }

        /** The types of the RecordType fields. */
        public static Type[] TYPES = { BaseType.DATE, BaseType.INT,
                BaseType.UNKNOWN };

        /** The labels of the RecordType fields. */
        public static String[] LABELS = new String[] { "timestamp",
                "microstep", "payload" };

        /** Label of the timestamp that is transmitted within the RecordToken.
         */
        private static final String timestamp = "timestamp";

        /** Label of the microstep that is transmitted within the RecordToken.
         */
        private static final String microstep = "microstep";

        /** Label of the payload that is transmitted within the RecordToken.
         */
        private static final String payload = "payload";
    }

    ///////////////////////////////////////////////////////////////////
    //// PtidesComposite

    /** This is a specialized composite actor for use in PtidesPlatform.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class PtidesPlatformContents extends
            MirrorComposite.MirrorCompositeContents {
        // NOTE: This has to be a static class so that MoML can
        // instantiate it.

        /** Construct an actor with a name and a container.
         *  @param container The container.
         *  @param name The name of this actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public PtidesPlatformContents(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            useRealTime = new Parameter(this, "useRealTime", new BooleanToken(false));
            useRealTime.setTypeEquals(BaseType.BOOLEAN);
        }
        
        /** This boolean parameter defaults to false and indicates whether
         *  the timestamp in the recordtokens sent and received by network
         *  ports are model time or real time.
         */
        public Parameter useRealTime;

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////
        
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (attribute == useRealTime) {
                ((PtidesPlatform)getContainer())._useRealTime = ((BooleanToken)useRealTime.getToken()).booleanValue();
            } else {
                super.attributeChanged(attribute);
            }
        }

        /** Override the base class to return a specialized port.
         *  @param name The name of the port to create.
         *  @return A new instance of PtidesMirrorPort, an inner class.
         *  @exception NameDuplicationException If the container already has
         *  a port with this name.
         */
        @Override
        public Port newPort(String name) throws NameDuplicationException {
            try {
                return new PtidesPort(this, name);
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }

        /** Return a specialized ParameterPort.
         * @param name The name of the port to create.
         * @return The new ParameterPort.
         * @exception NameDuplicationException Thrown if the container already has
         *  a port with this name.
         */
        public Port newParameterPort(String name)
                throws NameDuplicationException {
            try {
                PortParameter parameter = new MirrorPortParameter(this, name);
                return parameter.getPort();
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PtidesPlatformDirector

    /** This is a specialized director that forwards most functionality
     *  to the embedded PtidesDirector. Transferring inputs and outputs
     *  is modified in order to deal with building and extracting
     *  RecordTokens in NetworkPorts.
     */
    private class PtidesPlatformDirector extends Director {

        /** Construct an PtidesPlatformDirector in the specified workspace with
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
        public PtidesPlatformDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** Invoke fire of the embedded PtidesDirector.
         *  @exception IllegalActionException Thrown by embedded
         *  PtidesDirector.
         */
        @Override
        public void fire() throws IllegalActionException {
            _getEmbeddedPtidesDirector().fire();
        }

        /** Forward this call to the director that governs this
         *  PtidesPlatform actor.
         *  @exception IllegalActionException Thrown if fireContainerAt
         *  of the enclosing director cannot be invoked.
         */
        @Override
        public Time fireContainerAt(Time time) throws IllegalActionException {
            if (getContainer() instanceof Actor) {
                Actor container = (Actor) getContainer();
                if (container != null && container.getContainer() != null) {
                    container = (Actor) container.getContainer();
                    Director director = container.getDirector();
                    if (director != null) {
                        return director.fireContainerAt(time);
                    }
                }
            }
            throw new IllegalActionException(this, "FireContainerAt of the "
                    + "enclosing director cannot be invoked.");
        }

        /** Get current environment time from the director that contains
         *  this PtidesPlatform actor.
         *  @return Environment time.
         */
        @Override
        public Time getEnvironmentTime() {
            NamedObj namedObjContainer = getContainer();
            if (namedObjContainer instanceof Actor) {
                Actor container = (Actor) namedObjContainer;
                if (container != null && container.getContainer() != null) {
                    // This could be an EntityLibrary, see tests in
                    // $PTII/doc.
                    NamedObj parentContainer = container.getContainer();
                    if (parentContainer instanceof Actor) {
                        container = (Actor) parentContainer;
                        Director director = container.getDirector();
                        if (director != null) {
                            return director.getModelTime();
                        }
                    }
                }
            }
            return localClock.getLocalTime();
        }

        /** Return the environment time.
         *  @return The current environment time.
         *  @see #getEnvironmentTime()
         */
        @Override
        public Time getModelTime() {
            // We don't call super.getModelTime() because
            // Director.getModelTime() returns the local time of the
            // localClock, which is not what we want.
            return getEnvironmentTime();
        }

        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        @Override
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Call prefire of super class to update local time and
         *  invoke prefire of the embedded PtidesDirector.
         *  @exception IllegalActionException Thrown by embedded
         *  PtidesDirector.
         */
        @Override
        public boolean prefire() throws IllegalActionException {
            super.prefire();
            return _getEmbeddedPtidesDirector().prefire();
        }

        /** Invoke postfire of the embedded PtidesDirector.
         *  @exception IllegalActionException Thrown by embedded
         *  PtidesDirector.
         */
        @Override
        public boolean postfire() throws IllegalActionException {
            return _getEmbeddedPtidesDirector().postfire();
        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method extracts tokens from a record token if the
         *  associated port is a network port.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;
            PtidesDirector director = (PtidesDirector) _getEmbeddedPtidesDirector();

            for (int channelIndex = 0; channelIndex < port.getWidth(); channelIndex++) {
                // NOTE: This is not compatible with certain cases
                // in PN, where we don't want to block on a port
                // if nothing is connected to the port on the
                // inside.
                try {
                    if (port.isKnown(channelIndex)) {
                        if (port.hasToken(channelIndex)) {
                            Token t = port.get(channelIndex);

                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }

                            PtidesPort associatedPort = (PtidesPort) ((MirrorPort) port)
                                    .getAssociatedPort();
                            if (associatedPort.isNetworkReceiverPort()) {
                                if (!(t instanceof RecordToken)
                                        || ((RecordToken) t).labelSet().size() != 3) {
                                    throw new IllegalActionException(
                                            this,
                                            "The input token is not a RecordToken or "
                                                    + "does not have a size not equal to 3: "
                                                    + "Here we assume the Record is of types: timestamp"
                                                    + " + microstep + token + sourceTimestamp");
                                }

                                RecordToken record = (RecordToken) t;

                                Time recordTimestamp = null;
                                Token timestampToken = null;
                                if (_useRealTime) {
                                    timestampToken = record
                                            .get(PtidesNetworkRealTimeType.timestamp);
                                    DateToken token = ((DateToken)timestampToken); 
                                    recordTimestamp = new Time(
                                            director,
                                            (token.getCalendarInstance().getTimeInMillis() - director
                                            .getRealStartTimeMillis())
                                            * director.localClock.getTimeResolution());
                                } else {
                                    timestampToken = record
                                            .get(PtidesNetworkModelTimeType.timestamp);
                                    recordTimestamp = new Time(
                                            director,
                                            ((DoubleToken)timestampToken).doubleValue());
                                }

                                int recordMicrostep = ((IntToken) record
                                        .get(PtidesNetworkModelTimeType.microstep))
                                        .intValue();

                                Time sourceTimestamp = director.getModelTime();
                                //                                    new Time(
                                //                                        director,
                                //                                        ((DoubleToken) (record
                                //                                                .get(PtidesNetworkType.sourceTimestamp)))
                                //                                                .doubleValue());

                                Receiver[][] farReceivers = associatedPort
                                        .deepGetReceivers();
                                if (farReceivers.length > 0) {
                                    for (int i = 0; i < farReceivers[channelIndex].length; i++) {
                                        director.addInputEvent(
                                                associatedPort,
                                                new PtidesEvent(
                                                        associatedPort,
                                                        channelIndex,
                                                        recordTimestamp,
                                                        recordMicrostep,
                                                        -1,
                                                        record.get(PtidesNetworkModelTimeType.payload),
                                                        farReceivers[channelIndex][i],
                                                        sourceTimestamp),
                                                PtidesDirector
                                                        ._getDoubleParameterValue(
                                                                associatedPort,
                                                                "deviceDelay"));
                                    }
                                }
                            } else if (associatedPort.isSensorPort()) {
                                Receiver[][] farReceivers = associatedPort
                                        .deepGetReceivers();
                                if (farReceivers.length > 0) {
                                    for (int i = 0; i < farReceivers[channelIndex].length; i++) {
                                        director.addInputEvent(
                                                associatedPort,
                                                new PtidesEvent(
                                                        associatedPort,
                                                        channelIndex,
                                                        director.getModelTime(),
                                                        1,
                                                        -1,
                                                        t,
                                                        farReceivers[channelIndex][i],
                                                        director.getModelTime()),
                                                PtidesDirector
                                                        ._getDoubleParameterValue(
                                                                associatedPort,
                                                                "deviceDelay"));
                                    }
                                }
                            } else {
                                ((MirrorPort) port).getAssociatedPort()
                                        .sendInside(channelIndex, t);
                            }

                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }

            return result;
        }

        /** Transfer data from the inside receivers of an output port of the
         *  container to the ports it is connected to on the outside.
         *  Create a RecordToken if the associated port is a NetworkTransmitterPort.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @see IOPort#transferOutputs
         */
        @Override
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;
            PtidesDirector director = (PtidesDirector) _getEmbeddedPtidesDirector();
            
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {

                    while (port.isKnownInside(i) && port.hasTokenInside(i)) {
                        Token t = port.getInside(i);

                        if (_isAssociatedWithNetworkTransmitter(port)) {

                            ((CompositeActor) ((MirrorPort) port)
                                    .getAssociatedPort().getContainer())
                                    .getDirector();

                            Object[] timestamps = ((PtidesPort) ((MirrorPort) port)
                                    .getAssociatedPort())
                                    .getTimestampForToken(t);

                            int microstep = ((PtidesPort) ((MirrorPort) port)
                                    .getAssociatedPort())
                                    .getMicrostepForToken(t);

                            Token timeToken;
                            Time timestamp = (Time) timestamps[0];
                            
                            if (_useRealTime) {
                                long time = (long)(timestamp.getDoubleValue() 
                                        / director.localClock.getTimeResolution()) 
                                        + director.getRealStartTimeMillis();
                                timeToken = new DateToken(time);
                            } else {
                                timeToken = new DoubleToken(timestamp.getDoubleValue());
                            }

                            Token[] values = new Token[] {
                                    timeToken,
                                    new IntToken(microstep), t };
                            
                            RecordToken record = new RecordToken(
                                    PtidesNetworkModelTimeType.LABELS, values);
                            try {
                                ((MirrorPort) port).send(i, record);
                            } catch (IllegalActionException ex) {
                                throw new IllegalActionException(this,
                                        ex.getMessage());
                            }
                        } else {
                            ((MirrorPort) port).send(i, t);
                        }
                    }
                    result = true;
                } catch (NoTokenException ex) {
                    throw new InternalErrorException(this, ex, null);
                }
            }

            return result;
        }

        //////////////////////////////////////////////////////////////
        ////                   private methods                    ////

        /** Get the PtidesDirector that is contained by the PtidesComposite.
         *  @return The PtidesDirector.
         *  @exception IllegalActionException If no PtidesDirector is contained.
         */
        private Director _getEmbeddedPtidesDirector()
                throws IllegalActionException {
            CompositeActor container = (CompositeActor) getContainer();

            Iterator actors = container.entityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();

                if (!((ComponentEntity) actor).isOpaque()) {
                    throw new IllegalActionException(container,
                            "Inside actor is not opaque "
                                    + "(perhaps it needs a director).");
                }
                return ((CompositeActor) actor).getDirector();
            }
            return null;
        }

    }

    ///////////////////////////////////////////////////////////////////
    //// PtidesMirrorPort

    /** This is a specialized port for PtidesPlatform.
     *  If the container is an instance of PtidesPlatform,
     *  then it handles type conversions between
     *  the record types and the payload field of the record used
     *  in NetworkPorts.
     */
    public static class PtidesMirrorPort extends MirrorPort {
        /** Construct a port in the specified workspace with an empty
         *  string as a name. You can then change the name with setName().
         *  If the workspace argument
         *  is null, then use the default workspace.
         *  The object is added to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the port.
         * @exception IllegalActionException If port parameters cannot be initialized.
         */
        public PtidesMirrorPort(Workspace workspace)
                throws IllegalActionException {
            // This constructor is needed for Shallow codgen.
            super(workspace);
        }

        // NOTE: This class has to be static because otherwise the
        // constructor has an extra argument (the first argument,
        // actually) that is an instance of the enclosing class.
        // The MoML parser cannot know what the instance of the
        // enclosing class is, so it would not be able to instantiate
        // these ports.

        /** Create a new instance of a port for PtidesPlatform.
         *  @param container The container for the port.
         *  @param name The name of the port.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @exception NameDuplicationException Not thrown in this base class.
         */
        public PtidesMirrorPort(TypedCompositeActor container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            // NOTE: Ideally, Port are created when an entity is added.
            // However, there appears to be no clean way to do this.
            // Instead, ports are added when an entity is added via a
            // change request registered with this PtidesPlatform actor.
            // Consequently, these ports have to be persistent, and this
            // constructor and class have to be public.
            // setPersistent(false);
        }

        /** Override the base class to not convert the token if it is a PtidesPlatform.
         *  FIXME: correct?
         *  @param token The token to convert.
         *  @return The converted token.
         *  @exception IllegalActionException If the conversion is
         *   invalid.
         */
        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (!(getContainer() instanceof PtidesPlatform)
                    || !isOutput()
                    || !((PtidesPort) this.insidePortList().get(0))
                            .isNetworkTransmitterPort()) {
                return super.convert(token);
            }
            if (getType().equals(BaseType.GENERAL)) {
                return token;
            }
            Type type = ((RecordType) getType()).get("payload");

            if (type.equals(token.getType())) {
                return token;
            } else {
                Token newToken = type.convert(token);
                return newToken;
            }
        }

    }

}
