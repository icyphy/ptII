/* A director for multidimensional dataflow.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.domains.pthales.lib.PthalesDynamicCompositeActor;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A director for multidimensional dataflow.
 * <p>This is based on Array-OL, as described by:</p>
 * <ol>
 * <li>
 * Boulet, P. (2007). "Array-OL Revisited, Multidimensional Intensive
 * Signal Processing Specification," Technical Report 6113,
 * INRIA, Orsay, France.
 * </ol>
 *
 * <p>A more detailed documentation of the Pthales domain can be found in
 * a technical report currently under preparation and accessible at:</p>
 * <ol>
 * <li>Remi Barrere, Eric Lenormand, Dai Bui, Edward A. Lee, Christopher Shaver and Stavros Tripakis,
 * "An Introduction to the Pthales Domain of Ptolemy II,
 * EECS Department,
 * University of California, Berkeley,
 * Technical Report No. UCB/EECS-2011-32,
 * April 26, 2011.
 * (<a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2011/EECS-2011-32.html">http://www.eecs.berkeley.edu/Pubs/TechRpts/2011/EECS-2011-32.html</a>)
 * </li>
 * </ol>
 *
 * <p>The notation used here is intended to follow the spirit of
 * SpearDE [FIXME: Reference?], from Thales, a software system
 * based on Array-OL. In this notation, unlike Boulet's,
 * dimensions are named, and patterns for reading and writing
 * arrays are given using those names.</p>
 * <p>
 * [FIXME: the description that follows needs update, the contents and syntax of parameter specs have changed]
 * [FIXME: it seems mostly OK to me (Stavros): I made a mild pass. Please check]
 * The execution is governed by the following parameters
 * in the model. In all cases except for the "repetitions"
 * parameter, the parameters are OrderedRecords
 * of the form "[x = n, y = m, ...]", where x and y are arbitrary
 * dimension names and n and m are non-negative integers.
 * For parameters that supports strides (such as the "pattern" parameter),
 * n and m can be replaced by {n,s} or {m,s}, where s is the stride
 * (a positive integer).
 * The stride defaults to 1 if not specified. Unless otherwise
 * stated, the parameters do not support strides.
 * Ports contain the following parameters:</p>
 * <ol>
 *
 * <li> <i>size</i>: This is a parameter of each output port
 * that specifies the size of the array written in that output
 * port. All dimensions must be specified. This parameter
 * is optional, as the size of an output array can be deduced
 * from the other parameters. In addition, every input
 * port of a composite actor that contains a PthalesDirector
 * must also have such a parameter. [FIXME: true?]</li>
 *
 * <li> <i>base</i>: This mandatory parameter gives the base location
 * (origin) of the output or input array at which an actor begins
 * writing or reading at each iteration of this director.
 * All dimensions must be specified. The order in which they are
 * specified does not matter.</li>
 *
 * <li> <i>pattern</i>: This is a parameter of each port that
 * specifies the shape of the portion of the array produced or consumed
 * on that port at each firing of the actor within an iteration.
 * The number of firings of an actor within an iteration is specified
 * by the "repetitions" parameter of the actor (see below).
 * Moreover, if an actor reads from or writes to the port
 * sequentially (using get() and send() methods), then the pattern
 * specifies the order in which the array is filled.
 * For example, if you send tokens with values 1, 2, 3, 4, 5, 6
 * using a pattern [x=3, y=2], then the array is filled as
 * follows (assuming base=[x=0, y=0]):
 * <table>
 * <tr> <td> x </td> <td> y </td> <td> value </td></tr>
 * <tr> <td> 0 </td> <td> 0 </td> <td> 1 </td></tr>
 * <tr> <td> 1 </td> <td> 0 </td> <td> 2 </td></tr>
 * <tr> <td> 2 </td> <td> 0 </td> <td> 3 </td></tr>
 * <tr> <td> 0 </td> <td> 1 </td> <td> 4 </td></tr>
 * <tr> <td> 1 </td> <td> 1 </td> <td> 5 </td></tr>
 * <tr> <td> 2 </td> <td> 1 </td> <td> 6 </td></tr>
 * </table>
 * If on the other hand you specify a pattern
 * [y=2, x=3], then an array of the same shape is used,
 * but it is now filled as follows:
 * <table>
 * <tr> <td> x </td> <td> y </td> <td> value </td></tr>
 * <tr> <td> 0 </td> <td> 0 </td> <td> 1 </td></tr>
 * <tr> <td> 0 </td> <td> 1 </td> <td> 2 </td></tr>
 * <tr> <td> 1 </td> <td> 0 </td> <td> 3 </td></tr>
 * <tr> <td> 1 </td> <td> 1 </td> <td> 4 </td></tr>
 * <tr> <td> 2 </td> <td> 0 </td> <td> 5 </td></tr>
 * <tr> <td> 2 </td> <td> 1 </td> <td> 6 </td></tr>
 * </table>
 * If a stride is given, then the pattern may have gaps
 * in it. For example, "x = {2,2}" specifies that two values
 * are produced in the x dimension, and that they are separated
 * by one value that is not produced. Values that are not
 * produced default to zero (the value of zero depends on the
 * data type; for example, zero for strings is the empty string,
 * whereas zero for doubles is 0.0).</li>
 *
 * <li> <i>tiling</i>: This parameter gives the increment of the
 * base location in each dimension for each successive firing
 * of the actor within an iteration. This is a property of an
 * output or an input port of an actor.</li>
 *
 * </ol>
 *
 * In addition, actors must contain the following parameter:
 * <ol>
 * <li> <i>repetitions</i>: This is a required parameter
 * for every actor in the Pthales domain. It is an array of
 * positive integers of the form "{ k, l, ... }". It specifies
 * the number of times an actor fires within an iteration. This
 * number is equal to the product of all elements in the repetitions
 * vector. So, "{2, 4}" specifies that the actor should fire a total
 * of 2*4=8 times at each iteration. Moreover, this parameter defines
 * a set of nested loops (of depth equal to the length of the array) and
 * the number of iterations of each loop (from the inner to the outer loop).
 * So "{2, 4}" specifies an inner loop with 2 iterations and an outer loop
 * with 4 iterations. There is a one-to-one mapping between the elements
 * of the repetitions array and the fields of the tiling parameters of all
 * ports of the corresponding actor. The body of the inner-most loop
 * specifies the processing that the actor performs on a given pattern,
 * during one firing of the actor within an iteration. A complete
 * execution of the entire nested loop specifies the entire processing
 * of the array by the actor. Thus, each firing of the actor produces
 * or consumes only portions of the array and the complete processing
 * is done by assembling these portions together according to the
 * above parameters.</li>
 * </ol>
 * <p>
 * In all cases, when indexes are incremented, they are incremented
 * in a toroidal fashion, wrapping around when they reach the size
 * of the array. Thus, it is always possible (though rarely useful)
 * for an array size to be 1 in every dimension. [FIXME?]</p>
 * <p>
 * NOTE: It should be possible to define a PtalesPort and
 * PtalesCompositeActor that contain the above parameters, as
 * a convenience. These could be put in a library.</p>
 * <p>
 * NOTE: It should be possible to create a single interface
 * for this director so that when double clicked, it
 * brings up an interactive dialog that has the form of a Spear
 * table. It would have one row per port, plus a header row
 * to specify the iterations.</p>
 * <p>
 * FIXME: Need to export production and consumption data for
 * SDF, allowing these Pthales models to be nested within SDF
 * or within Pthales, which will also allow it to be nested
 * within modal models.</p>
 *
 * @author Edward A. Lee, Eric Lenormand, Stavros Tripakis
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesDirector extends SDFDirector {

    // FIXME: Change the parameters to be ordered record types
    // so that the full expression language is supported.
    // They should not be strings, and they need not be parsed.
    // Moreover, record types provide nice support for merging in defaults, etc.

    // FIXME: The value of the size parameter could optionally be inferred
    // if there is no torroidal wrap around desired.  Perhaps there should
    // be parameter indicating to do such inference.

    /**
     * Constructs a PthalesDirector object, using PthalesScheduler.
     *
     * @param container Container of the director.
     * @param name Name of this director.
     * @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in a derived class.
     * @exception NameDuplicationException If the container is not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public PthalesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setScheduler(new PthalesScheduler(this, "PthalesScheduler"));

        if (getAttribute("library") == null) {
            _library = new StringParameter(this, "library");
            _library.setExpression("");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Attribute update.
     * @see ptolemy.domains.sdf.kernel.SDFDirector#attributeChanged(ptolemy.kernel.util.Attribute)
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == _library) {
            _libraryName = _library.getExpression();
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PthalesDirector newObject = (PthalesDirector) super.clone(workspace);
        newObject._library = (StringParameter) newObject
                .getAttribute("library");
        newObject._receivers = new ArrayList<PthalesReceiver>();
        return newObject;
    }

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule.
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  NOTE: This method does not conform with the strict actor semantics
     *  because it calls postfire() of actors. Thus, it should not be used
     *  in domains that require a strict actor semantics, such as SR or
     *  Continuous.
     *  @exception IllegalActionException If any actor executed by this
     *   actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *   container.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();" here because if you do then
        // everything happens twice.

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        Iterator firings = schedule.firingIterator();

        while (firings.hasNext() && !_stopRequested) {
            Firing firing = (Firing) firings.next();
            Actor actor = firing.getActor();
            int iterationCount = firing.getIterationCount();

            //check if we need to compute the iteration for this actor
            //the actor will compute the iteration itself and return the value to the director.
            if (iterationCount == 0
                    && actor instanceof PthalesDynamicCompositeActor) {
                iterationCount = ((PthalesDynamicCompositeActor) actor)
                        .computeIterations();
            }

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE,
                        iterationCount));
            }

            int returnValue = actor.iterate(iterationCount);

            if (returnValue == STOP_ITERATING) {
                _postfireReturns = false;
            } else if (returnValue == NOT_READY) {
                // See de/test/auto/knownFailedTests/DESDFClockTest.xml
                throw new IllegalActionException(this, actor, "Actor "
                        + "is not ready to fire.  Perhaps " + actor.getName()
                        + ".prefire() returned false? "
                        + "Try debugging the actor by selecting "
                        + "\"Listen to Actor\".  Also, for SDF check moml for "
                        + "tokenConsumptionRate on input.");
            }

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE,
                        iterationCount));
            }
        }
    }

    /** Get the name of the library to use.
     * @return the name of the library to use.
     * @exception IllegalActionException
     */
    public String getLibName() throws IllegalActionException {
        return _libraryName;
    }

    /** Add a new receiver.
     */
    @Override
    public Receiver newReceiver() {
        PthalesReceiver receiver = new PthalesReceiver();
        _receivers.add(receiver);

        return receiver;
    }

    /** Return true if the director is ready to fire.
     *  @return true If all of the input ports of the container of this
     *  director have enough tokens.
     *  @exception IllegalActionException If the port methods throw it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // Set current time based on the enclosing model.
        for (PthalesReceiver recv : _receivers) {
            recv.reset();
        }
        return super.prefire();
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly, since the act of computing the
     *  schedule sets the rate parameters of the external ports.  In
     *  addition, performing scheduling during preinitialization
     *  enables it to be present during code generation.  The order in
     *  which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // Garbage collector
        System.gc();

        // Load library needed to project
        if (!(_libraryName.length() == 0)) {
            System.loadLibrary(_libraryName);
        }

        // Empties list of receivers before filling it
        // FindBugs: DMI:Don't use removeAll to clear a collection
        // (DMI_USING_REMOVEALL_TO_CLEAR_COLLECTION) "If you want to
        // remove all elements from a collection c, use c.clear, not
        // c.removeAll(c). Calling c.removeAll(c) to clear a
        // collection is less clear, susceptible to errors from typos,
        // less efficient and for some collections, might throw a
        // ConcurrentModificationException."
        //_receivers.removeAll(_receivers);
        _receivers.clear();

        super.preinitialize();
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.  If there are not enough tokens,
     *  then throw an exception.  If the port is not connected on the
     *  inside, or has a narrower width on the inside than on the outside,
     *  then consume exactly one token from the corresponding outside
     *  channels and discard it.  Thus, a port connected on the outside
     *  but not on the inside can be used as a trigger for an SDF
     *  composite actor.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if there are not enough input tokens available.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }

        // The number of tokens depends on the schedule, so make sure
        // the schedule is valid.
        getScheduler().getSchedule();

        boolean wasTransferred = false;

        if (((Actor) port.getContainer()).getExecutiveDirector() instanceof PNDirector) {
            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (i < port.getWidthInside()) {
                        while (port.hasNewToken(i)) { //when to stop?
                            Token t = port.get(i);
                            port.sendInside(i, t);

                            wasTransferred = true;
                        }
                    }

                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        } else {
            int rate = DFUtilities.getTokenConsumptionRate(port);

            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (i < port.getWidthInside()) {
                        if (port.hasToken(i, rate)) {
                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }

                            if (port.getRemoteReceivers().length > 0) {
                                port.send(i, port.get(i, rate), rate);
                            } else {
                                CompositeActor compositeActor = (CompositeActor) port
                                        .getContainer();
                                List<Actor> actors = compositeActor
                                        .deepEntityList();

                                // External ports
                                List<TypedIOPort> externalPorts = compositeActor
                                        .inputPortList();
                                for (TypedIOPort externalPort : externalPorts) {
                                    Receiver recv = externalPort.getReceivers()[0][0];
                                    Token[] buffer = null;
                                    if (recv instanceof SDFReceiver) {
                                        // Buffer acquisition
                                        buffer = ((SDFReceiver) recv)
                                                .getArray(DFUtilities
                                                        .getRate(externalPort));
                                    }

                                    // Dispatch to all input ports using output port
                                    for (Actor actor : actors) {
                                        List<IOPort> ports = actor
                                                .inputPortList();
                                        for (IOPort inputPort : ports) {
                                            if (inputPort.connectedPortList()
                                                    .contains(externalPort)) {
                                                Receiver[][] receivers = inputPort
                                                        .getReceivers();
                                                if (receivers != null
                                                        && receivers.length > 0) {
                                                    for (Receiver[] receiverss : receivers) {
                                                        if (receiverss != null
                                                                && receiverss.length > 0) {
                                                            for (Receiver receiver : receiverss) {
                                                                if (receiver instanceof PthalesReceiver) {
                                                                    ((PthalesReceiver) receiver)
                                                                    .setExternalBuffer(
                                                                            compositeActor,
                                                                            externalPort,
                                                                            buffer);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(
                                    this,
                                    port,
                                    "Port should consume "
                                            + rate
                                            + " tokens, but not enough tokens available.");
                        }
                    } else if (port.isKnown(i)) {
                        // No inside connection to transfer tokens to.
                        // Tolerate an unknown input, but if it is known, then
                        // transfer the input token if there is one.
                        // In this case, consume one input token if there is one.
                        if (_debugging) {
                            _debug(getName(), "Dropping single input from "
                                    + port.getName());
                        }

                        if (port.hasToken(i)) {
                            port.get(i);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }

        return wasTransferred;
    }

    /** Override the base class method to transfer enough tokens to
     *  fulfill the output production rate.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port. If any channel of the output port has no data, then
     *  that channel is ignored.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque output port.");
        }

        boolean wasTransferred = false;

        if (!(((Actor) port.getContainer()).getExecutiveDirector() instanceof PNDirector)) {
            wasTransferred = super.transferOutputs(port);
        } else {

            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    while (port.hasNewTokenInside(i)) {
                        Token t = port.getInside(i);

                        if (_debugging) {
                            _debug(getName(), "transferring output from "
                                    + port.getName());
                        }

                        port.send(i, t);
                        wasTransferred = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }

        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Buffer memory. */
    protected StringParameter _library;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The dimensions relevant to this receiver. */
    private ArrayList<PthalesReceiver> _receivers = new ArrayList<PthalesReceiver>();

    private String _libraryName = "";
}
