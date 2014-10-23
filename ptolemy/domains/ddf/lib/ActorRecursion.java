/* An actor that clones a composite actor containing itself into itself.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.ddf.lib;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.ddf.kernel.DDFDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 This actor performs actor recursion dynamically during execution.
 Upon firing, it clones the composite actor which contains itself and
 is referred to by the StringParameter <i>recursionActor</i>. It then
 places the clone inside itself and connects the corresponding ports of
 both actors. It uses a local DDFDirector to preinitialize the clone and
 then transfers all tokens contained by input ports of this actor to the
 connected opaque ports inside. It again uses the local DDFDirector to
 initialize all actors contained by this actor and classifies each of them
 such as their enabling and deferrable status. It then transfers all
 tokens contained by output ports of this actor to the connected opaque
 ports outside. It finally merges the local DDFDirector with its executive
 DDFDirector and then removes the local DDFDirector. Thus during execution
 this actor is fired at most once, after which the executive director
 directly controls all actors inside. Since there is no type constraint
 between input ports and output ports of this actor, users have to
 manually configure types for all outputs of this actor.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class ActorRecursion extends TypedCompositeActor {
    /** Create an ActorRecursion with a name and a container.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown. This actor will use the workspace of the container
     *  for synchronization and version counts. If the name argument is
     *  null, then the name is set to the empty string. Increment the
     *  version of the workspace.
     *  The actor creates a DDFDirector initially, which will be removed
     *  toward the end of firing this actor, when the director completes
     *  its responsibility of preinitializing and initializing the cloned
     *  composite actor and merging with the outside DDFDirector.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ActorRecursion(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new DDFDirector(this, uniqueName("DDFDirector"));
        recursionActor = new StringParameter(this, "recursionActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A StringParameter representing the name of the composite actor
     *  to clone from. The composite actor contains this actor in some
     *  hierarchy.
     */
    public StringParameter recursionActor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the composite actor referred to by the StringParameter
     *  recursionActor into itself. Use a local DDFDirector to
     *  preinitialize all (just cloned) actors contained by this actor.
     *  Transfer all tokens contained by input ports of this actor to
     *  the connected opaque ports inside. Read rate parameters of
     *  input ports of all actors receiving tokens from this actor and
     *  propagate these parameters back to the connected output ports
     *  of this actor. Use the local DDFDirector to initialize all actors
     *  contained by this actor and classify each of them according to
     *  their enabling and deferrable status. Transfer all tokens contained
     *  by output ports of this actor to the connected opaque ports
     *  outside. Merge the local DDFDirector with the outside DDFDirector
     *  and finally remove local DDFDirector.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call super.fire() here. It does not follow what a regular
        // composite actor would do.
        try {
            // Disable redoing type resolution because type compatibility
            // has been guaranteed during initialization.
            ((DDFDirector) getExecutiveDirector()).disableTypeResolution(true);
            ((DDFDirector) getDirector()).disableTypeResolution(true);

            try {
                _cloneRecursionActor();
            } catch (CloneNotSupportedException ex) {
                throw new IllegalActionException(this, ex, "The actor "
                        + recursionActor.stringValue() + " cannot be cloned.");
            }

            getDirector().preinitialize();
            _transferInputs();
            _setOutputPortRate();
            getDirector().initialize();
            _transferOutputs();
            ((DDFDirector) getExecutiveDirector())
            .merge((DDFDirector) getDirector());

            try {
                // get rid of the local director.
                getDirector().setContainer(null);
            } catch (NameDuplicationException ex) {
                // should not happen.
                throw new InternalErrorException(this, ex, null);
            }
        } finally {
            ((DDFDirector) getExecutiveDirector()).disableTypeResolution(false);
        }
    }

    /** Initialize this actor. First find the composite actor to be
     *  cloned, which is the first containing actor up in the hierarchy
     *  with the name referred to by the StringParameter recursionActor.
     *  Then check the compatibility of the found composite actor with
     *  this actor. It is only done once due to the recursive
     *  nature of this actor.
     *  @exception IllegalActionException If no actor is found with
     *   the given name or the found actor is not compatible.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _searchRecursionActor();

        if (!_isCompatibilityChecked) {
            _checkCompatibility();
        }
    }

    /** Override the base class to return false. Upon seeing the return
     *  value, its executive director disables this actor and only fires
     *  all inside actors next time.
     *  @return false.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /** Write a MoML description of the contents of this object.
     *  Override the base class to describe contained ports and
     *  attributes, but not inside entities, links and relations
     *  created during execution.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        Iterator attributes = attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            attribute.exportMoML(output, depth);
        }

        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            Port port = (Port) ports.next();
            port.exportMoML(output, depth);
        }
    }

    /** Notify this actor that the given entity has been added inside it.
     *  Override the base class to do nothing. This will prevent it from
     *  calling requestInitialization(Actor) to the cloned composite actor.
     *  The preinitialization and initialization have already been done in
     *  the fire() method.
     */
    @Override
    protected void _finishedAddEntity(ComponentEntity entity) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the compatibility of the to-be-cloned composite actor with
     *  this actor. The compatibility criteria are self-evident in the code.
     *  Basically two actors should look the same from outside and it should
     *  not violate type constraints by placing the cloned composite actor
     *  inside this actor and connecting the corresponding ports.
     *  @exception IllegalActionException If the composite actor to be
     *   cloned is not compatible with this actor.
     */
    private void _checkCompatibility() throws IllegalActionException {
        if (!(getExecutiveDirector() instanceof DDFDirector)) {
            throw new IllegalActionException(this,
                    "The executive Director must be a DDFDirector.");
        }

        if (_recursionActor.inputPortList().size() != inputPortList().size()
                || _recursionActor.outputPortList().size() != outputPortList()
                .size()) {
            throw new IllegalActionException(this, "The recursionActor "
                    + recursionActor.stringValue()
                    + " must have the same number of input ports and "
                    + "same number of output ports as this actor.");
        }

        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            Object matching = _recursionActor.getPort(port.getName());

            if (matching == null) {
                throw new IllegalActionException(this,
                        "Each port of this actor must have the same name as "
                                + "the matching port of the recursionActor "
                                + recursionActor.stringValue()
                                + ". However, the port " + port.getFullName()
                                + " does not have a matching "
                                + "port with the same name.");
            }

            TypedIOPort matchingPort = (TypedIOPort) matching;

            if (port.getWidth() != matchingPort.getWidth()) {
                throw new IllegalActionException(this, "The matching ports: "
                        + port.getFullName() + " and "
                        + matchingPort.getFullName()
                        + " must have the same width. Port "
                        + port.getFullName() + "'s width " + port.getWidth()
                        + " is not equal to " + matchingPort.getFullName()
                        + "'s width " + matchingPort.getWidth() + ".");
            }

            if (port.isInput() && !matchingPort.isInput() || port.isOutput()
                    && !matchingPort.isOutput()) {
                throw new IllegalActionException(this, "The matching ports: "
                        + port.getFullName() + " and "
                        + matchingPort.getFullName()
                        + " must be both input ports or output ports.");
            }

            Type portType = port.getType();
            Type matchingPortType = matchingPort.getType();

            if (port.isInput() && !matchingPortType.isCompatible(portType)) {
                throw new IllegalActionException(this, "The type of the port "
                        + port.getName() + " of the actor " + getName()
                        + " must be equal to or less than "
                        + "that of the matching port.");
            }

            if (port.isOutput() && !portType.isCompatible(matchingPortType)) {
                throw new IllegalActionException(this, "The type of the port "
                        + port.getName() + " of the actor " + getName()
                        + " must be equal to or greater than "
                        + "that of the matching port.");
            }
        }

        _isCompatibilityChecked = true;
    }

    /** Clone the composite actor into the same workspace as this actor.
     *  Set its container to this actor. Store all tokens contained by
     *  input ports of this actor. Connect the corresponding ports of
     *  this actor and the cloned composite actor.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException, or NameDuplicationException is caught
     *   in this method.
     *  @exception CloneNotSupportedException If the CompositeActor cannot
     *   be cloned.
     */
    private void _cloneRecursionActor() throws IllegalActionException,
    CloneNotSupportedException {
        try {
            // Clone the composite actor.
            CompositeActor clone = (CompositeActor) _recursionActor
                    .clone(workspace());

            // Place the clone inside this actor.
            clone.setContainer(this);

            // i is used to generate different names for new relations.
            int i = 0;
            Iterator ports = portList().iterator();

            _inputTokensHolder.clear();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();

                // Store all tokens contained by input ports of this actor
                // because connecting ports will result in creating receivers
                // again and all tokens in the original receivers will be lost.
                if (port.isInput()) {
                    int width = port.getWidth();
                    Receiver[][] receivers = port.getReceivers();
                    Token[][] tokens = new Token[width][0];

                    for (int channel = 0; channel < width; channel++) {
                        int size = ((QueueReceiver) receivers[channel][0])
                                .size();
                        tokens[channel] = new Token[size];

                        for (int count = 0; count < size; count++) {
                            tokens[channel][count] = port.get(channel);
                        }
                    }

                    _inputTokensHolder.put(port, tokens);
                }

                // Connect the corresponding ports of both actors.
                IOPort matchingPort = (IOPort) clone.getPort(port.getName());
                IORelation relation = (IORelation) newRelation("r_" + i++);
                port.link(relation);
                matchingPort.link(relation);

                if (port.isMultiport()) {
                    relation.setWidth(port.getWidth());
                }
            }
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, "name duplication.");
        }
    }

    /** Get token consumption rate for the given receiver. If the port
     *  containing the receiver is an input port, return the consumption
     *  rate for that receiver. If the port containing the receiver is
     *  an opaque output port, return the production rate for that receiver.
     *  @param receiver The receiver to get token consumption rate.
     *  @return The token consumption rate of the given receiver.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    private int _getTokenConsumptionRate(Receiver receiver)
            throws IllegalActionException {
        int tokenConsumptionRate;

        IOPort port = receiver.getContainer();
        Variable rateVariable = null;
        Token token = null;
        Receiver[][] portReceivers = null;

        // If DDF domain is inside another domain and the
        // receiver is contained by an opaque output port...
        // The default production rate is -1 which means all
        // tokens in the receiver are transferred to the outside.
        if (port.isOutput()) {
            rateVariable = DFUtilities.getRateVariable(port,
                    "tokenProductionRate");
            portReceivers = port.getInsideReceivers();

            if (rateVariable == null) {
                return -1;
            } else {
                token = rateVariable.getToken();

                if (token == null) {
                    return -1;
                }
            }
        }

        if (port.isInput()) {
            rateVariable = DFUtilities.getRateVariable(port,
                    "tokenConsumptionRate");
            portReceivers = port.getReceivers();

            if (rateVariable == null) {
                return 1;
            } else {
                token = rateVariable.getToken();

                if (token == null) {
                    return 1;
                }
            }
        }

        if (token instanceof ArrayToken) {
            Token[] tokens = ((ArrayToken) token).arrayValue();

            // Scan the contained receivers of the port to find
            // out channel index.
            int channelIndex = 0;
            foundChannelIndex: for (int m = 0; m < portReceivers.length; m++) {
                for (int n = 0; n < portReceivers[m].length; n++) {
                    if (receiver == portReceivers[m][n]) {
                        channelIndex = m;
                        break foundChannelIndex;
                    }
                }
            }

            tokenConsumptionRate = ((IntToken) tokens[channelIndex]).intValue();
        } else {
            tokenConsumptionRate = ((IntToken) token).intValue();
        }

        return tokenConsumptionRate;
    }

    /** Get the to-be-cloned composite actor's name from StringParameter
     *  recursionActor. Go up in hierarchy and find the first container
     *  with matching name.
     *  @exception IllegalActionException If no actor is found with
     *   the given name.
     */
    private void _searchRecursionActor() throws IllegalActionException {
        String recursionActorValue = recursionActor.stringValue();
        CompositeActor container = (CompositeActor) getContainer();

        while (container != null) {
            if (recursionActorValue.equals(container.getName())) {
                _recursionActor = container;
                return;
            } else {
                container = (CompositeActor) container.getContainer();
            }
        }

        throw new IllegalActionException(this,
                "Can not find a container with name " + recursionActorValue);
    }

    /** Read the rate parameters of the input ports of all actors receiving
     *  tokens from this actor and propagate these parameters back to the
     *  connected output ports of this actor. This is needed because during
     *  the initialization of the local director, the contained actors only
     *  see the opaque output ports of this actor instead of the connected
     *  opaque ports on the outside after the local director is removed.
     *  To determine the deferrability (see DDFDirector for its definition)
     *  of the contained actors, which happens during the initialization of
     *  the local director, we need to propagate these rate parameters back
     *  to the connected output ports of this actor.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    private void _setOutputPortRate() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int[] productionRate = new int[outputPort.getWidthInside()];

            // If there are more inside channels than outside channels,
            // it sets default rates of these extra inside channels to
            // be -1 which then won't cause an upstream actor to be
            // deferrable because any tokens on these extra channels
            // are discarded.
            Arrays.fill(productionRate, -1);

            Receiver[][] farReceivers = outputPort.getRemoteReceivers();

            for (int i = 0; i < farReceivers.length; i++) {
                if (i < outputPort.getWidthInside()) {
                    for (int j = 0; j < farReceivers[i].length; j++) {
                        QueueReceiver farReceiver = (QueueReceiver) farReceivers[i][j];
                        int rate = _getTokenConsumptionRate(farReceiver);

                        // According to the definition of deferrability,
                        // we need to find the minimum rate associated with
                        // this channel. -1 is actually the largest rate in
                        // some sense.
                        if (productionRate[i] < 0) {
                            productionRate[i] = rate;
                        } else if (rate >= 0 && rate < productionRate[i]) {
                            productionRate[i] = rate;
                        }
                    }
                }
            }

            IntToken[] productionRateToken = new IntToken[outputPort
                                                          .getWidthInside()];

            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                productionRateToken[i] = new IntToken(productionRate[i]);
            }

            // Since this is output port, we look for token production rate
            // instead of token consumption rate.
            Variable rateVariable = DFUtilities.getRateVariable(outputPort,
                    "tokenProductionRate");

            if (rateVariable == null) {
                try {
                    rateVariable = new Parameter(outputPort,
                            "tokenProductionRate");
                } catch (NameDuplicationException ex) {
                    //should not happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }

            rateVariable.setToken(new ArrayToken(BaseType.INT,
                    productionRateToken));
        }
    }

    /** Transfer all tokens contained by input ports of this actor and
     *  stored by an internal variable to the connected opaque ports inside.
     *  We cannot use transferInputs(IOPort) of the local director for two
     *  reason. One is that tokens are now stored by an internal variable.
     *  The other is that we have to transfer <i>all</i> tokens instead of
     *  those specified by rate parameters because all input ports will
     *  become transparent after this firing.
     *  @exception IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    private void _transferInputs() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            Token[][] tokens = (Token[][]) _inputTokensHolder.get(inputPort);

            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                for (int j = 0; j < tokens[channel].length; j++) {
                    inputPort.sendInside(channel, tokens[channel][j]);
                }
            }
        }
    }

    /** Transfer all tokens contained by output ports of this actor to the
     *  connected opaque ports outside.We cannot use transferOutputs(IOPort)
     *  of the local director because we have to transfer <i>all</i> tokens
     *  instead of those specified by rate parameters because all output
     *  ports will become transparent after this firing.
     *  @exception IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    private void _transferOutputs() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();

            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                while (outputPort.hasTokenInside(i)) {
                    outputPort.send(i, outputPort.getInside(i));
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The composite actor to be cloned.
     */
    private CompositeActor _recursionActor = null;

    /** A flag indicating if the compatibility of the to-be-cloned composite
     *  actor has been checked. It is set to true after checking so that
     *  checking is performed only once during the execution of the model.
     */
    private boolean _isCompatibilityChecked = false;

    /** A HashMap to store tokens of the input ports.
     */
    private HashMap _inputTokensHolder = new HashMap();
}
