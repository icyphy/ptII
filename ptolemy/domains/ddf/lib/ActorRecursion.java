/* An actor that clones a composite actor containing itself into itself.

Copyright (c) 2003-2004 The Regents of the University of California.
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
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.Type;
import ptolemy.domains.ddf.kernel.DDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
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
   is referred to by the StringParameter recursionActor. It then places
   the clone inside itself and connects the corresponding ports of both
   actors. It uses local DDFDirector to preinitialize the clone and then
   transfers all tokens contained by input ports of this actor to the
   connected opaque ports inside. It again uses local DDFDirector to
   initialize all actors contained by this actor and classifies each of them
   such as their enabling and deferrable status. It then transfers all
   tokens contained by output ports of this actor to the connected opaque
   ports outside. It finally merges local DDFDirector with its executive
   DDFDirector and then removes local DDFDirector. Thus during execution
   this actor is fired at most once, after which the executive director
   directly controls all actors inside.

   @author Gang Zhou
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
     *  its reposibility of prinitializing and initializing the cloned
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
        recursionActor = new StringParameter(this, "resursionActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A StringParameter representing the name of the composite actor
     *  to clone from.
     */
    public StringParameter recursionActor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the composite actor referred to by the StringParameter
     *  recursionActor into itself. Use local DDFDirector to
     *  preinitialize all (just cloned) actors contained by this actor.
     *  Transfer all tokens contained by input ports of this actor to
     *  the connected opaque ports inside. Read rate parameters of
     *  input ports of all actors receiving tokens from this actor and
     *  propagate these parameters back to the connected output ports
     *  of this actor. Use local DDFDirector to initialize all actors
     *  contained by this actor and classify each of them such as their
     *  enabling and deferrable status. Transfer all tokens contained
     *  by output ports of this actor to the connected opaque ports
     *  outside. Merge local DDFDirector with the outside DDFDirector
     *  and finally remove local DDFDirector.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    public void fire() throws IllegalActionException {
        try {
            // Disable redoing type resolution because type compatibility
            // has been guaranteed during initialization.
            ((DDFDirector)getExecutiveDirector()).flagTypeResolution(true);
            ((DDFDirector)getDirector()).flagTypeResolution(true);
            
            _cloneRecursionActor();
            getDirector().preinitialize();
            _transferInputs();
            _setOutputPortRate();
            getDirector().initialize();
            _transferOutputs();
            ((DDFDirector)getExecutiveDirector()).
                    merge((DDFDirector)getDirector());
            try {
                getDirector().setContainer(null);
            } catch (NameDuplicationException ex) {
                //should not happen.
                throw new InternalErrorException(this, ex, null);
            }
        } finally {
            ((DDFDirector)getExecutiveDirector()).flagTypeResolution(false);
        }
    }

    /** Find the composite actor to be cloned, which is the first
     *  containing actor up in hierarchy with name referred to by
     *  StringParameter recursionActor.
     *  Check the compatibility of the found composite actor with
     *  this actor. It is only done once due to the recursive
     *  nature of this actor.
     *  @exception IllegalActionException If no actor is found with
     *   the given name or the found actor is not compatible.
     */
    public void initialize() throws IllegalActionException {
        _searchRecursionActor();
        if (!_isCompatibilityChecked)
            _checkCompatibility();
    }

    /** Override the base class to return false. Upon seeing the return
     *  value, its executive director will remove this actor from the
     *  active actors list so that it won't be invoked again. This is
     *  important since its local director has been removed and its
     *  executive director now controls all actors contained by this actor.
     *  @return false.
     *  @exception IllegalActionException Not thrown here.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /** Override the base class to describe contained ports and
     *  attributes, but not inside entities, links and relations
     *  created during execution.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {

        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            attribute.exportMoML(output, depth);
        }

        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            port.exportMoML(output, depth);
        }
    }

    /** Override the base class to do nothing. This will prevent it
     *  from calling requestInitialization(Actor) to the cloned
     *  composite actor. The preinitialization and initialization
     *  have already been done in the fire method.
     */
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
        if (!(getExecutiveDirector() instanceof DDFDirector))
            throw new IllegalActionException(this, "The executive Director " +
                    "must be DDFDirector.");
        if (_recursionActor.inputPortList().size() != inputPortList().size() ||
                _recursionActor.outputPortList().size() != outputPortList().size())
            throw new IllegalActionException(this, "The recursionActor " +
                    recursionActor.stringValue() + " must have the same " +
                    "number of input ports and same number of output " +
                    "ports as the actor to be replaced.");
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort)ports.next();
            Object matching = _recursionActor.getPort(port.getName());
            if (matching == null)
                throw new IllegalActionException(this, "Each port of the " +
                        "recursionActor " + recursionActor.stringValue() +
                        " must have the same name as the matching " +
                        "port of the actor to be replaced.");
            TypedIOPort matchingPort = (TypedIOPort)matching;
            if (port.getWidth() != matchingPort.getWidth())
                throw new IllegalActionException(this, "The matching " +
                        "ports must have same width.");
            if (port.isInput() && !matchingPort.isInput() ||
                    port.isOutput() && !matchingPort.isOutput())
                throw new IllegalActionException(this, "The matching " +
                        "ports must be both input ports or output ports.");
            Type portType = port.getType();
            Type matchingPortType  = matchingPort.getType();
            if (port.isInput() && !matchingPortType.isCompatible(portType))
                throw new IllegalActionException(this, "The type of the port " +
                        port.getName() + " of the actor " + getName() + " must " +
                        "be the same or less than the matching port.");
            if (port.isOutput() && !portType.isCompatible(matchingPortType))
                throw new IllegalActionException(this, "The type of the port " +
                        port.getName() + " of the actor " + getName() + " must " +
                        "be the same or less than the matching port.");
        }
        _isCompatibilityChecked = true;
    }

    /** Clone the composite actor into the same workspace as this actor.
     *  Set its container to this actor. Store all tokens contained by
     *  input ports of this actor. Connect the corresponding ports of
     *  this actor and the cloned composite actor.
     *  @exception IllegalActionException If any called method throws
     *  IllegalActionException, or CloneNotSupportedException is caught
     *  in this method.
     */
    private void _cloneRecursionActor() throws IllegalActionException {
        try {
            // Clone the composite actor.
            CompositeActor clone = (CompositeActor)
                _recursionActor.clone(workspace());
            // Place the clone inside this actor.
            clone.setContainer(this);
            int i = 0;
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort)ports.next();

                // Store all tokens contained by input ports of this actor
                // because connecting ports will result in creating receivers
                // again and all tokens in the original receivers will be lost.
                if (port.isInput()) {
                    int width = port.getWidth();
                    Receiver[][] receivers = port.getReceivers();
                    Token[][] tokens = new Token[width][0];
                    for (int channel = 0; channel < width; channel++) {
                        int size = ((SDFReceiver)receivers[channel][0]).size();
                        tokens[channel] = new Token[size];
                        for (int count = 0; count < size; count++) {
                            tokens[channel][count] = port.get(channel);
                        }
                    }
                    _inputTokensHolder.put(port, tokens);
                }

                // Connect the corresponding ports of both actors.
                // FIXME: Note that this will invalidate resolved types,
                // which is really not necessary because the compatibility
                // is already checked during initialization. In current
                // implementation, the model will redo type resolution
                // every time a composite actor is cloned and connected.
                // It will be more efficient to get around this.
                IOPort matchingPort = (IOPort)clone.getPort(port.getName());
                IORelation relation = (IORelation)newRelation("r_" + i++);
                port.link(relation);
                matchingPort.link(relation);
                if (port.isMultiport()) {
                    relation.setWidth(port.getWidth());
                }
            }
        } catch (CloneNotSupportedException ex) {
            throw new IllegalActionException(this,
                    "couldn't clone: " + ex.toString());
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, "name duplication.");
        }
    }

    /** Get the to-be-cloned composite actor's name from StringParameter
     *  recusionActor. Go up in hierarchy and find the first container
     *  with matching name.
     *  @exception IllegalActionException If no actor is found with
     *   the given name.
     */
    private void _searchRecursionActor() throws IllegalActionException {
        String recursionActorValue = recursionActor.stringValue();
        CompositeActor container = (CompositeActor)getContainer();
        while (container != null) {
            if (recursionActorValue.equals(container.getName())) {
                _recursionActor = container;
                return;
            } else {
                container = (CompositeActor)container.getContainer();
            }
        }
        throw new IllegalActionException(this, "Can not find a " +
                "container with name " + recursionActorValue);
    }

    /** Read rate parameters of input ports of all actors receiving tokens
     *  from this actor and propagate these parameters back to the connected
     *  output ports of this actor. This is needed because during the
     *  initialization of the local director, the contained actors only
     *  see opaque output ports of this actor instead of the connected
     *  opaque ports on the outside after the local director is removed.
     *  To determine the deferrability of the contained actors, which
     *  happens during the initialization of the local director, we need to
     *  propagate these rate parameters back.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    private void _setOutputPortRate() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();
        while (outputPorts.hasNext()) {

            IOPort outputPort = (IOPort)outputPorts.next();
            int[] productionRate = new int[outputPort.getWidthInside()];
            // If there are more inside channels than outside channels,
            // it sets default rates of these non-connecting inside
            // channels to be Integer.MAX_VALUE. This will effectively
            // nullify these non-connecting inside channels while deciding
            // deferrability of the actors connected to these channels.
            // After the local director is removed, these channels won't
            // transfer any data.
            Arrays.fill(productionRate, Integer.MAX_VALUE);
            Receiver[][] farReceivers = outputPort.getRemoteReceivers();
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (farReceivers[i] != null) {
                    for (int j = 0; j < farReceivers[i].length; j++) {
                        SDFReceiver farReceiver = (SDFReceiver)farReceivers[i][j];
                        IOPort port = farReceiver.getContainer();

                        // Having a self-loop doesn't make it deferrable.
                        if (port == outputPort)
                            continue;

                        // The default rate for the port containing
                        // farReceiver is 1.
                        int portRate = 1;
                        Parameter rate = null;
                        if (port.isInput()) {
                            rate = (Parameter)port.
                                getAttribute("tokenConsumptionRate");
                            // Ports of opaque SDF composite actors contain
                            // parameters named "_tokenConsumptionRate" given
                            // by inside scheduler.
                            if (rate == null) {
                                rate = (Parameter)port.
                                    getAttribute("_tokenConsumptionRate");
                            }
                        }
                        // If DDF domain is inside another domain and the
                        // farReceiver is contained by an output port.
                        if (port.isOutput()) {
                            rate = (Parameter)port.
                                getAttribute("tokenProductionRate");
                            if (rate == null) {
                                rate = (Parameter)port.
                                    getAttribute("_tokenProductionRate");
                            }
                        }
                        if (rate != null) {
                            Token token = rate.getToken();
                            if (token instanceof ArrayToken) {
                                Token[] tokens = ((ArrayToken)token).arrayValue();
                                // Scan the contained receivers of the remote
                                // port to find the channel index.
                                Receiver[][] portReceivers =
                                    port.getReceivers();
                                int channelIndex = 0;
                                foundChannelIndex:
                                for (int m = 0; m < portReceivers.length; m++)
                                    for (int n = 0; n < portReceivers[m].length; n++)
                                        if (farReceiver == portReceivers[m][n]) {
                                            channelIndex = m;
                                            break foundChannelIndex;
                                        }
                                portRate =
                                    ((IntToken)tokens[channelIndex]).intValue();
                            } else {
                                portRate = ((IntToken)token).intValue();
                            }
                        }
                        // According to the definition of deferrability,
                        // we need to find the minimum rate associated with
                        // this channel.
                        if (productionRate[i] > portRate) {
                            productionRate[i] = portRate;
                        }
                    }
                }
            }
            IntToken[] productionRateToken =
                new IntToken[outputPort.getWidthInside()];
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                productionRateToken[i] = new IntToken(productionRate[i]);
            }
            // Since this is output port, we look for token production rate
            // instead of token consumption rate.
            Parameter tokenProductionRate = (Parameter)
                outputPort.getAttribute("tokenProductionRate");
            if (tokenProductionRate == null) {
                tokenProductionRate = (Parameter)
                    outputPort.getAttribute("_tokenProductionRate");
            }
            if (tokenProductionRate == null) {
                try {
                    tokenProductionRate =
                        new Parameter(outputPort, "tokenProductionRate");
                } catch (NameDuplicationException ex) {
                    //should not happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
            tokenProductionRate.setToken(new ArrayToken(productionRateToken));
        }
    }

    /** Transfer all tokens contained by input ports of this actor and
     *  stored by an internal variable to the connected opaque ports inside.
     *  We cannot use transferInputs(IOPort) of the local director for two
     *  reason. One is that tokens are now stored by an internal variable.
     *  The other is that we have to transfer <i>all</i> tokens instead of
     *  those specified by rate parameters.
     *  @throws IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    private void _transferInputs() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            Token[][] tokens = (Token[][])_inputTokensHolder.get(inputPort);
            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                for (int j = 0; j < tokens[channel].length; j++) {
                    inputPort.sendInside(channel, tokens[channel][j]);
                }
            }
        }
    }

    /** Transfer all tokens contained by output ports of this actor to the
     *  connected opaque ports outside.We cannot use transferOutputs(IOPort)
     *  of the executive director because we have to transfer <i>all</i>
     *  tokens instead of those specified by rate parameters.
     *  @throws IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    private void _transferOutputs() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort)outputPorts.next();
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                while (outputPort.hasTokenInside(i)) {
                    outputPort.send(i, outputPort.getInside(i));
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

    // The composite actor to be cloned.
    private CompositeActor _recursionActor = null;

    // A flag indicating if the compatibility of the to-be-cloned composite
    // actor has been checkd. It is set to true after checking so that
    // checking is performed only once during the execution of the model.
    private boolean _isCompatibilityChecked = false;

    // Store tokens of the input ports.
    private HashMap _inputTokensHolder = new HashMap();
}


