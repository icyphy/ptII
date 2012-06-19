/* Director for a Synchronous Publisher Subscriber model of computation.

   Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.domains.sps.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SPSDirector

/**

   The Synchronous Publisher Subscriber (SPS) model of computation.
   <p>
   In this model of computation input and output ports have channel names
   that are similar to publisher and subscribers.  The output port name may
   have wildcards, which may be matched by input ports.  Output ports may
   have initial values.  If an output port does not have an initial value,
   then a default value of zero is used.  The actual default value depends
   on the token type.
   </p>

   <p>The way the execution occurs is that during the first iteration,
   the output ports are initialized with the initial values.  The corresponding
   input ports are then updated.  The actors are fired and the values
   of the output ports are updated and the iteration ends.</p>

   <p>This model of computation is similar to a Cellular Automaton model
   of computation.  This model of computation differs from the
   ptolemy/domains/ca/kernel/CADirector.java in that CADirector operates
   on a matrix and the value of a cell is dependent on the values
   of the adjacent values in the matrix.  In this model of computation,
   values may depend on many different values.</p>

   <p>This model of computation may be a subset of Linda.</p>

   @author Edward A. Lee, Christopher Brooks
   @version $Id: PNDirector.java 63582 2012-05-16 01:13:15Z cxh $
   @since Ptolemy II 8.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class SPSDirector extends PNDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SPSDirector() throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SPSDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  Thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SPSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SPSDirector newObject = (SPSDirector) super.clone(workspace);
        return newObject;
    }

    /** 
     *  Fire the actors contained by the container of this director.
     *  <p>The way the execution occurs is that during the first iteration,
     *  the output ports are initialized with the initial values.  The corresponding
     *  input ports are then updated.  The actors are fired and the values
     *  of the output ports are updated and the iteration ends.</p>
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void fire() throws IllegalActionException {
        // super.fire()
        // Don't call "Director.super.fire();" here, do the work instead.
        if (_debugging) {
            _debug("Called fire().");
        }
        
        CompositeEntity container = (CompositeEntity)getContainer();
        List<CompositeEntity> composites = container.deepCompositeEntityList();
        for (CompositeEntity composite : composites) {
            Iterator ports = composite.portList().iterator();
            // FIXME: we could define our own SPSPort.
            // FIXME: we should cache this list of output ports.
            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                if (port instanceof IOPort) {
                    IOPort ioPort = (IOPort) port;

                    if (ioPort.isInput()) { 
                        Parameter channelParameter = (Parameter)port.getAttribute("channel", Parameter.class);
                        if (channelParameter != null) {
                            Pattern pattern = Pattern.compile(channelParameter.getExpression());
                            if (_publishedPorts != null) {
                                for (Map.Entry<String, List<IOPort>> entry : _publishedPorts.entrySet()) {
                                    String name = entry.getKey();
                                    Matcher matcher = pattern.matcher(name);
                                    if (matcher.matches()) {
                                        List<IOPort> outputPort = entry.getValue();
                                        // FIXME: need to decide how many receivers the output port should have.
                                        Receiver [][] receivers = outputPort.get(0).getReceivers();
                                        // FIXME: get a token from the receivers
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** 
     *  If any of the composite actors contain output ports that
     *  have a property named "initialValue", then set the value parameter of
     *  the port to the value of the initial parameter.
     * 
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Get all the output ports, look for ports that have an initialValues
        // parameter and update the receivers if necessary
        CompositeEntity container = (CompositeEntity)getContainer();
        List<CompositeEntity> composites = container.deepCompositeEntityList();
        for (CompositeEntity composite : composites) {
            Iterator ports = composite.portList().iterator();
            // FIXME: we could define our own SPSPort.
            // FIXME: we should cache this list of output ports.
            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                if (port instanceof IOPort) {
                    IOPort ioPort = (IOPort) port;
                    if (ioPort.isOutput()) { 
                        // Store the channel name in hash table.
                        // CompositeActor.registerPublisherPort() has similar code.
                        if (_publishedPorts == null) {
                            _publishedPorts = new HashMap<String, List<IOPort>>();
                        }
                        Parameter channelParameter = (Parameter)port.getAttribute("channel", Parameter.class);
                        if (channelParameter != null) {
                            String channelExpression = channelParameter.getExpression();
                            List<IOPort> portList = _publishedPorts.get(channelExpression);
                            if (portList == null) {
                                portList = new LinkedList<IOPort>();
                                _publishedPorts.put(channelExpression, portList);
                            }
                        }

                        // Get the initialValue.
                        Parameter initialValueParameter = (Parameter)port.getAttribute("initialValue", Parameter.class);
                        if (initialValueParameter != null) {
                            // Set the receivers of the port to the
                            // value of the initialValue parameter.

                            Token token = initialValueParameter.getToken();

                            // See IOPort.broadcast()
                            // FIXEM: what about IOPortEventListeners

                            // First index is channel number, second
                            // is receiver number within the group of
                            // receivers that get copies from the same
                            // channel.

                            Receiver receivers[][] = ioPort.getReceivers();
                            for (int i = 0; i < receivers.length; i++) {
                                if (receivers[i] == null) {
                                    continue;
                                }
                                //ioPort.putToAll(token, receivers[i]);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Return true if the containing composite actor contains active
     *  processes and the composite actor has input ports and if stop()
     *  has not been called. Return false otherwise. This method is
     *  normally called only after detecting a real deadlock, or if
     *  stopFire() is called. True is returned to indicate that the
     *  composite actor can start its execution again if it
     *  receives data on any of its input ports.
     *  @return true to indicate that the composite actor can continue
     *  executing on receiving additional input on its input ports.
     *  @exception IllegalActionException Not thrown in this base class. May be
     *  thrown by derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        return super.postfire();
    }

    /** 
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _init() throws IllegalActionException,
            NameDuplicationException {

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** Keep track of all published ports accessible in this container.*/
    protected Map<String, List<IOPort>> _publishedPorts;
}