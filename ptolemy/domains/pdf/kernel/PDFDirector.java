/* Director for the synchronous dataflow model of computation.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 @ProposedRating Red (neuendor)
 @AcceptedRating Red (johnr)
 */
package ptolemy.domains.pdf.kernel;

import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// PDFDirector

/**
 <h1>PDF overview</h1>
 The Synchronous Dataflow(PDF) domain supports the efficient
 execution of Dataflow graphs that
 lack control structures.   Dataflow graphs that contain control structures
 should be executed using the Process Networks(PN) domain instead.
 PDF allows efficient execution, with very little overhead at runtime.  It
 requires that the rates on the ports of all actors be known before hand.
 PDF also requires that the rates on the ports not change during
 execution.  In addition, in some cases (namely systems with feedback) delays,
 which are represented by initial tokens on relations must be explicitly
 noted.  PDF uses this rate and delay information to determine
 the execution sequence of the actors before execution begins.
 <h2>Schedule Properties</h2>
 <ul>
 <li>The number of tokens accumulated on every relation is bounded, given
 an infinite number of executions of the schedule.
 <li>Deadlock will never occur, given and infinite number of executions of
 the schedule.
 </ul>
 <h1>Class comments</h1>
 An PDFDirector is the class that controls execution of actors under the
 PDF domain.  By default, actor scheduling is handled by the PDFScheduler
 class.  Furthermore, the newReceiver method creates Receivers of type
 PDFReceiver, which extends QueueReceiver to support optimized gets
 and puts of arrays of tokens.
 <p>
 The PDF director has a single parameter, "iterations", corresponding to a
 limit on the number of times the director will fire its hierarchy
 before it returns false in postfire.  If this number is not greater
 than zero, then no limit is set and postfire will always return true.
 The default value of the iterations parameter is an IntToken with value zero.
 @see ptolemy.domains.sdf.kernel.PDFScheduler
 @see ptolemy.domains.sdf.kernel.PDFReceiver

 @author Steve Neuendorffer
 @version $Id$
 */
public class PDFDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The PDFDirector will have a default scheduler of type PDFScheduler.
     */
    public PDFDirector() {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The PDFDirector will have a default scheduler of type PDFScheduler.
     *
     *  @param workspace The workspace for this object.
     */
    public PDFDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The PDFDirector will have a default scheduler of type
     *   PDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public PDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the initialization graph.  This parameter
     *  should contain a StringToken.  The model will be fired once
     *  every time this director is fired prior to the model.
     */
    public StringAttribute init;

    /** The name of the model.
     */
    public StringAttribute model;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calculate the current schedule, if necessary,
     *  and iterate the contained actors
     *  in the order given by the schedule.  No internal state of the
     *  director is updated during fire, so it may be used with domains that
     *  require this property, such as CT.
     *  <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's  prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = ((TypedCompositeActor) getContainer());

        if (container == null) {
            throw new InvalidStateException("PDFDirector " + getName()
                    + " fired, but it has no container!");
        } else {
            _postfirereturns = true;

            String name = init.getExpression();
            Actor actor = (Actor) container.getEntity(name);

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.ITERATE));
            }

            actor.fire();
            _postfirereturns = _postfirereturns && actor.postfire();

            name = model.getExpression();
            actor = (Actor) container.getEntity(name);

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.ITERATE));
            }

            actor.fire();
            _postfirereturns = _postfirereturns && actor.postfire();
        }
    }

    /** Initialize the actors associated with this director and
     *  then compute the schedule.  The schedule is computed
     *  during initialization so that hierarchical opaque composite actors
     *  can be scheduled properly (since the act of computing the
     *  schedule sets the rate parameters of the external ports).
     *  The order in which the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /** Return a new receiver consistent with the PDF domain.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  @exception IllegalActionException If port methods throw it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        _postfirereturns = true;

        TypedCompositeActor container = ((TypedCompositeActor) getContainer());
        Iterator inputPorts = container.inputPortList().iterator();
        int inputCount = 0;

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int threshold = SDFScheduler.getTokenConsumptionRate(inputPort);

            if (_debugging) {
                _debug("checking input " + inputPort.getFullName());
                _debug("Threshold = " + threshold);
            }

            Receiver[][] receivers = inputPort.getReceivers();

            int channel;

            for (channel = 0; channel < inputPort.getWidth(); channel++) {
                if (!receivers[channel][0].hasToken(threshold)) {
                    if (_debugging) {
                        _debug("Channel " + channel
                                + " does not have enough tokens."
                                + " Prefire returns false on "
                                + container.getFullName());
                    }

                    return false;
                }
            }
        }

        if (_debugging) {
            _debug("Prefire returns true on " + container.getFullName());
        }

        return true;
    }

    /** Preinitialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.
     *  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        return _postfirereturns;
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  input port. If any channel of the input port has no data, then
     *  that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                            + "input port.");
        }

        boolean trans = false;
        Receiver[][] insiderecs = port.deepGetReceivers();

        for (int i = 0; i < port.getWidth(); i++) {
            int rate = SDFScheduler.getTokenConsumptionRate(port);

            for (int k = 0; k < rate; k++) {
                try {
                    ptolemy.data.Token t = port.get(i);

                    if ((insiderecs != null) && (insiderecs[i] != null)) {
                        if (_debugging) {
                            _debug(getName(), "transferring input from "
                                    + port.getName());
                        }

                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }

                        trans = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "PDFDirector.transferInputs: Not enough tokens "
                                    + ex.getMessage());
                }
            }
        }

        return trans;
    }

    /** Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not "
                            + "an opaque output port.");
        }

        System.out
                .println("transferring Outputs for port" + port.getFullName());

        TypedCompositeActor container = ((TypedCompositeActor) getContainer());
        Entity initEntity = (Entity) container.getEntity(init.getExpression());
        Entity modelEntity = (Entity) container
                .getEntity(model.getExpression());
        Attribute attribute = modelEntity.getAttribute(port.getName());
        boolean trans = false;
        Receiver[][] insiderecs = port.getInsideReceivers();

        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                System.out.println("channel " + i);

                if (insiderecs[i] != null) {
                    System.out.println("has " + insiderecs[i].length
                            + " Receivers");

                    for (int j = 0; j < insiderecs[i].length; j++) {
                        System.out.println("checking hasToken number" + j);

                        while (insiderecs[i][j].hasToken()) {
                            try {
                                System.out.println("transferring");

                                ptolemy.data.Token t = insiderecs[i][j].get();

                                if (port.getContainer().equals(initEntity)
                                        && (port.deepConnectedPortList().size() > 0)) {
                                    System.out.println("Setting");

                                    Settable settable = (Settable) attribute;
                                    settable.setExpression(t.toString());
                                } else {
                                    System.out.println("Sending");
                                    port.send(i, t);
                                }

                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: "
                                                + "Internal error: "
                                                + ex.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return trans;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the PDFDirector a
     *  default scheduler of the class SDFScheduler.
     */
    private void _init() {
        try {
            init = new StringAttribute(this, "init");
            init.setExpression("");

            model = new StringAttribute(this, "model");
            model.setExpression("");
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalErrorException(
                    "Cannot create default parameter:\n" + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    protected boolean _postfirereturns = true;
}
