/* Director for the Giotto model of computation.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Yellow (cm@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.*;
import ptolemy.actor.NoTokenException;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.util.Workspace;

// FIXME: replace this with per-class imports.
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirector
/**
This class implements a director for the Giotto model of computation
without Giotto modes. Schedules are generated according to the Giotto
semantics. The GiottoScheduler class contains methods to compute the
schedules. The GiottoReceiver class implements the data flow between
actors using double-buffering.

@author  Christoph Meyer Kirsch and Edward A. Lee
@version $Id$
@see GiottoScheduler
@see GiottoReceiver
*/
public class GiottoDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GiottoDirector() {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public GiottoDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public GiottoDirector(CompositeEntity container, String name)
	throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** The period of an iteration. This is a double that defaults to
     *  <I>0.1</I>.
     */
    public Parameter period;

    /** Code generation file name. */
    public Parameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
	throws IllegalActionException {
        if (attribute == filename) {
            generateGiottoCode();
        }
    }

    /** Generate Giotto code for this model.
     *  NOTE: This is highly preliminary.
     */
    public void generateGiottoCode()
	throws IllegalActionException {
        try {
            String file = ((StringToken)filename.getToken()).stringValue();
            FileOutputStream fout = new FileOutputStream(file);
            PrintStream pout = new PrintStream(fout);

	    // Generate sensor list.
            pout.println("sensor");
            TypedCompositeActor container = (TypedCompositeActor)getContainer();
            Iterator inPorts = container.inputPortList().iterator();
            while (inPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort)inPorts.next();
                // FIXME: Assuming ports are either
                // input or output and not both.
                // FIXME: May want the driver name
                // specified by a port parameter.
                String driverName = port.getName()
		    + "_device_driver_fire";
                pout.println("  "
			     + port.getType()
			     + " "
			     + port.getName()
			     + " uses "
			     + driverName
			     + ";");
            }

	    // Generate actuator list.
            pout.println("actuator");
            Iterator outPorts = container.outputPortList().iterator();
            while (outPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort)outPorts.next();
                // FIXME: Assuming ports are either
                // input or output and not both.
                // FIXME: May want the driver name
                // specified by a port parameter.
                String driverName = port.getName()
		    + "_device_driver_fire";
                pout.println("  "
			     + port.getType()
			     + " "
			     + port.getName()
			     + " uses "
			     + driverName
			     + ";");
            }

	    // Generate output list.
            pout.println("output");
            Iterator actors = container.deepEntityList().iterator();
            while(actors.hasNext()) {
                TypedActor actor = (TypedActor)actors.next();
                outPorts = actor.outputPortList().iterator();
                while (outPorts.hasNext()) {
                    TypedIOPort port = (TypedIOPort)outPorts.next();
                    String sanitizedPortName = StringUtilities.sanitizeName(
                            port.getName(container));
                    pout.println("  "
				 + port.getType()
				 + " "
				 + sanitizedPortName
				 + " := init_function_name_"
				 + sanitizedPortName
				 + ";");
                }
            }

	    // Generate "task functions."
            actors = container.deepEntityList().iterator();
            while(actors.hasNext()) {
                TypedActor actor = (TypedActor)actors.next();
                String taskName = StringUtilities.sanitizeName(
                        ((NamedObj)actor).getName(container));
                pout.print("task " + taskName + " (");

                inPorts = actor.inputPortList().iterator();
                String inPortsNames = "";
                String outPortsNames = "";
                while (inPorts.hasNext()) {
                    TypedIOPort port = (TypedIOPort)inPorts.next();
                    inPortsNames += port.getName();
                    pout.print(port.getName());
                    if (inPorts.hasNext()) {
                        inPortsNames += ", ";
                        pout.print(", ");
                    }
                }
                pout.print(") output (");
                outPorts = actor.outputPortList().iterator();
                while (outPorts.hasNext()) {
                    TypedIOPort port = (TypedIOPort)outPorts.next();
                    String sanitizedPortName = StringUtilities.sanitizeName(
                            port.getName(container));
                    outPortsNames += sanitizedPortName;
                    pout.print(sanitizedPortName);
                    if (outPorts.hasNext()) {
                        outPortsNames += ", ";
                        pout.print(", ");
                    }
                }
                pout.println(") {");
                pout.println("  schedule "
			     + taskName
			     + "_fire( "
			     + inPortsNames
			     + ", "
			     + outPortsNames
			     + ")");
                pout.println("}");

            }

	    //generate "Driver functions" for actors.
            actors = container.deepEntityList().iterator();
            String driverParas, outParas, typedOutParas;
            String actorName;
            while (actors.hasNext()) {
                driverParas = "";
                outParas = "";
                typedOutParas = "";
                actorName = "";
                TypedActor actor = (TypedActor)actors.next();
                actorName = StringUtilities.sanitizeName(((NamedObj) actor).
                        getName(container));
                pout.print("driver "
			   + actorName
			   + "_driver (");

		//get the "source" ports(the driver's inputs) for
                //each input port of this actor.
                inPorts = actor.inputPortList().iterator();
                while (inPorts.hasNext()) {
                    IOPort thisPort = (IOPort) inPorts.next();
                    String sanitizedPortName = StringUtilities.sanitizeName(
                            thisPort.getName(container));
		    Iterator sourcePorts = thisPort.sourcePortList().iterator();
		    while(sourcePorts.hasNext()) {
			IOPort port = (IOPort)sourcePorts.next();
			sanitizedPortName = StringUtilities.sanitizeName(
                                port.getName(container));
                        if (driverParas.length()==0) {
                            driverParas += sanitizedPortName;
                        } else {
                            driverParas += ", " + sanitizedPortName;
                      	}
		    }
		}

                //reset inPorts and get the driver's outputs
                inPorts = actor.inputPortList().iterator();
                while (inPorts.hasNext()) {
                    TypedIOPort port = (TypedIOPort) inPorts.next();
                    if (outParas == "") {
                        typedOutParas += port.getType()
                                + " "
			        + port.getName();
                        outParas += port.getName();
                    } else {
                        typedOutParas += ", "
			        + port.getType()
			        + " "
			        + port.getName();
                        outParas += ", " + port.getName();
                    }
                }

                //generate the code.
                pout.println(driverParas
			     + ") output ("
			     + typedOutParas
			     + ") {");
                pout.println("  if "
                             + actorName
                             + "_guard ("
                             + driverParas
                             + ") then");
                pout.println("    "
                             + actorName
                             + "_transferInputs("
                             + driverParas
                             + ", "
                             + outParas
                             + ")");
                pout.println("}");

	    }

            // generate driver functions for toplevel output ports.
	    // which is 'motor' in the test model.
	    // FIXME: the giotto director should do some checking to
            //avoid several outputs of actors connect to the same output port?
	    Iterator topOutPorts = container.outputPortList().iterator();
	    String outputName ="";
	    String sanitizedPortName= "";
      	    while (topOutPorts.hasNext()) {
		driverParas = "";
		outParas = "";
		typedOutParas = "";
		outputName = "";
		TypedIOPort port = (TypedIOPort)topOutPorts.next();
		outputName = StringUtilities.sanitizeName(port.
                        getName(container));
		pout.print("driver "
			   + outputName
			   + "_driver (");
		outParas += port.getName();
		typedOutParas += port.getType() + " " + port.getName();
		Iterator portConnected = port.deepInsidePortList().iterator();
		// FIXME: there should be some situations that the
                //outputPort has no connected ports.
		if (port.deepInsidePortList().size() != 0) {
		    while (portConnected.hasNext()) {
			TypedIOPort outPort = (TypedIOPort)
                                portConnected.next();
			sanitizedPortName = StringUtilities.sanitizeName(
                                outPort.getName(container));
			if (outPort.isOutput()) {
			    if (driverParas.length()==0) {
				driverParas += sanitizedPortName;
			    } else {
				driverParas += ", " + sanitizedPortName;
			    }
			}
		    }
	      }

	      pout.println(driverParas
			   + ") output ("
			   + typedOutParas
			   + ") {");
	      pout.println("  if "
			   + outputName
			   + "_guard ("
			   + driverParas
			   + ") then");
	      pout.println("    "
			   + outputName
			   + "_transferInputs("
			   + driverParas
			   + ", "
			   + outParas
			   + ")");
	      pout.println("}");
	   }

            //generate mode code
            String containerName = container.getName();
	    double periodValue = ((DoubleToken)period.getToken()).doubleValue();
	    // FIXME: this should be achieved from the
            //GiottoScheduler.getFrequency(ACTOR)
	    int actorFreq = 0;
	    int actFreq = 0;
	    pout.println("start "
			 + containerName
			 + "_name {");
	    pout.println("  mode "
			 + containerName
			 + "_name () period "
			 + periodValue
			 + " {");

            //generate mode code for toplevel outputs drivers
	    //FIXME: if there are several OUTPUTs..., we have multiple ACTFREQ?
	    // find the lowest frequency
	    // trace the output port updating frequency
            topOutPorts = container.outputPortList().iterator();
      	    while (topOutPorts.hasNext()) {
                outputName = "";
		TypedIOPort port = (TypedIOPort)topOutPorts.next();
		outputName = StringUtilities.sanitizeName(port.
                        getName(container));
		Iterator portConnected = port.deepInsidePortList().iterator();
		// FIXME: there should be some situations that the
                // inputPort has no connected ports.
		if (port.deepInsidePortList().size() != 0) {
		    while (portConnected.hasNext()) {
			TypedIOPort outPort = (TypedIOPort) portConnected.next();
                        Nameable actor = outPort.getContainer();
                        if (actor instanceof Actor) {
                            Parameter actorFreqPara = (Parameter) ((NamedObj)
                                    actor).getAttribute("frequency");
	                    actorFreq = ((IntToken) actorFreqPara.
                                    getToken()).intValue();
                        }
                        pout.println("    actfreq "
			      + actorFreq
			      + " do "
			      + outputName
                              + " ("
			      + outputName
			      + "_driver);");
                    }
                }
            }

            //generate mode code for each actor driver
	    actors = container.deepEntityList().iterator();
	    while (actors.hasNext()) {
	      TypedActor actor = (TypedActor) actors.next();
              actorName = StringUtilities.sanitizeName(((NamedObj)
                      actor).getName(container));
	      Parameter actorFreqPara = (Parameter) ((NamedObj)
                      actor).getAttribute("frequency");
	      actorFreq = ((IntToken) actorFreqPara.getToken()).intValue();
	      pout.println("    taskfreq "
			   + actorFreq
			   + " do "
			   + actorName
			   + "("
			   + actorName
			   + "_driver);"
			   );
	    }

	    pout.println("  }");
	    pout.println("}");

	} catch (IOException ex) {
	    throw new IllegalActionException(this, ex.getMessage());
	}
    }

    /** Return the next time at which the calling actor will be fired.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
	return _nextIterationTime;
    }

    /** Return the system time at which the fire method of this director
     *  has been called.
     *  @return The real start time in terms of milliseconds counting
     *  from 1/1/1970.
     */
    public long getRealStartTime() {
	return _realStartTime;
    }

    /** Calculate the current schedule, if necessary, and iterate
     *  the contained actors in the order given by the schedule.
     *
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
	_postFireReturns = true;

	TypedCompositeActor container = (TypedCompositeActor) getContainer();

	if (container != null) {
	    /* change Enumeration into Schedule */
	    Schedule giottoSchedule = getScheduler().getSchedule();

	    if (_debugging)
		_debug("Giotto director firing!");

	    _realStartTime = System.currentTimeMillis();

	    /* have to see how to _fire(Schedule) */
	    _postFireReturns = _fire(giottoSchedule);
	} else
	    throw new IllegalActionException(this, "Has no container!");
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    public Receiver newReceiver() {
	Receiver receiver = new GiottoReceiver();
	_receivers.add(receiver);
	return receiver;
    }

    /** Initialize the actors associated with this director and
     *  initialize the iteration count to zero.  The order in which
     *  the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();

	_iteration = 0;

	Iterator receivers = _receivers.iterator();

	while(receivers.hasNext()) {
	    GiottoReceiver receiver = (GiottoReceiver) receivers.next();
	    receiver.reset();
	}
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {

	int numberOfIterations =
	    ((IntToken) (iterations.getToken())).intValue();

	_iteration++;

	if((numberOfIterations > 0) && (_iteration >= numberOfIterations)) {
	    _iteration = 0;

	    return false;
	}

	return _postFireReturns;
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside. The port argument must be an
     *  opaque input port. If any channel of the input port has no data,
     *  then that channel is ignored. This method will transfer exactly
     *  one token on each input channel that has at least one token
     *  available. Update all receivers to which a token is transferred.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
	if (!port.isInput() || !port.isOpaque()) {
	    throw new IllegalActionException(this, port,
					     "transferInputs: port argument is not an opaque" +
					     "input port.");
	}
	boolean transfer = false;
	Receiver[][] insideReceivers = port.deepGetReceivers();
	for (int i = 0; i < port.getWidth(); i++) {
	    if (port.hasToken(i)) {
		try {
		    Token t = port.get(i);
		    if (insideReceivers != null &&
			insideReceivers[i] != null) {
			if(_debugging) _debug(getName(),
					      "transferring input from " + port.getName());
			for (int j = 0; j < insideReceivers[i].length; j++) {
			    insideReceivers[i][j].put(t);
			    ((GiottoReceiver)insideReceivers[i][j]).update();
			}
			transfer = true;
		    }
		} catch (NoTokenException ex) {
		    // this shouldn't happen.
		    throw new InternalErrorException(
						     "Director.transferInputs: Internal error: " +
						     ex.getMessage());
		}
	    }
	}
	return transfer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The static default Giotto period is 100ms.
     */
    protected static double _DEFAULT_GIOTTO_PERIOD = 0.1;

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

    // Initialize the director by creating a scheduler and parameters.
    private void _init() {
	try {
	    GiottoScheduler scheduler = new GiottoScheduler(workspace());
	    setScheduler(scheduler);

	    period = new Parameter(this, "period");
	    period.setToken(new DoubleToken(_DEFAULT_GIOTTO_PERIOD));
	    iterations = new Parameter(this, "iterations", new IntToken(0));
	    setCurrentTime(0.0);

	    filename = new Parameter(this, "filename");
	    filename.setTypeEquals(BaseType.STRING);
	    filename.setExpression("\"ptolemy.giotto\"");
	} catch (KernelException ex) {
	    throw new InternalErrorException(
					     "Cannot initialize director: " + ex.getMessage());
	}
    }

    /*  Iterate actors according to the schedule.
     *  @param schedule of all actors represented as a tree.
     *  @see GiottoScheduler
     *  @return true iff all actors postfire method returned true.
     */
    private boolean _fire(Schedule schedule)
	throws IllegalActionException {

	boolean postfire = true;

	double periodValue = ((DoubleToken)period.getToken()).doubleValue();

	// schedule has to make iterator to call hasNext() or next()
	Iterator scheduleIterator = schedule.iterator();

	if (schedule != null)
	    while (scheduleIterator.hasNext()) {
		Schedule sameFrequencySchedule = (Schedule) scheduleIterator.next();

		Iterator sameFreqIterator = sameFrequencySchedule.iterator();

		while (sameFreqIterator.hasNext()) {

		    Actor actor = ((Firing) sameFreqIterator.next()).getActor();

		    double currentTime = getCurrentTime();

		    int actorFrequency =
			GiottoScheduler.getFrequency(actor);

		    _nextIterationTime =
			currentTime + (periodValue / actorFrequency);

		    if (_debugging)
			_debug("Prefiring " +
			       ((NamedObj)actor).getFullName());

		    if (actor.prefire()) {
			if (_debugging)
			    _debug("Firing " +
				   ((NamedObj)actor).getFullName());

			actor.fire();
		    }

		    if (_debugging)
			_debug("Postfiring " +
			       ((NamedObj)actor).getFullName());

		    if (!actor.postfire())
			postfire = false;
		}

		// Assumption: schedule has even number of elements.

		Schedule higherFrequencySchedule = (Schedule) scheduleIterator.next();

		if (higherFrequencySchedule.size() != 0) {
		    //   Enumeration higherFrequency = Collections.enumeration(higherFrequencyList);

		    // Recursive call.
		    postfire = _fire(higherFrequencySchedule) && postfire;
		} else {
		    // Update time for every invocation of the most frequent
		    // tasks which are stored at the bottom of the tree.
		    double currentTime;

		    currentTime = getCurrentTime();

		    // What is the highest frequency?
		    // We look it up in the first actor.
		    // Assumption: sameFrequencyList is non-empty.
		    Actor actor = ((Firing) sameFrequencySchedule.get(0)).getActor();

		    int maxFrequency =
			GiottoScheduler.getFrequency(actor);


		    setCurrentTime(currentTime + (periodValue / maxFrequency));

		    if (_synchronizeToRealTime) {
			long elapsedTime = System.currentTimeMillis()
			    - _realStartTime;

			double elapsedTimeInSeconds =
			    ((double) elapsedTime) / 1000.0;

			if (currentTime > elapsedTimeInSeconds) {
			    long timeToWait = (long) ((currentTime -
						       elapsedTimeInSeconds) * 1000.0);

			    if (timeToWait > 0) {
				if (_debugging) {
				    _debug("Waiting for real time to pass: " +
					   timeToWait);
				}

				// FIXME: Do I need to synchronize on anything?
				Scheduler scheduler = getScheduler();

				synchronized(scheduler) {
				    try {
					scheduler.wait(timeToWait);
				    } catch (InterruptedException ex) {
					// Continue executing.
				    }
				}
			    }
			}
		    }
		}


		sameFreqIterator = sameFrequencySchedule.iterator();

		while (sameFreqIterator.hasNext()) {
		    Actor actor = ((Firing) sameFreqIterator.next()).getActor();

		    if (_debugging)
			_debug("Updating " + ((NamedObj)actor).getFullName());

		    List outputPortList = actor.outputPortList();

		    Enumeration outputPorts =
			Collections.enumeration(outputPortList);

		    while (outputPorts.hasMoreElements()) {
			IOPort port = (IOPort) outputPorts.nextElement();

			Receiver[][] channelArray = port.getRemoteReceivers();

			for (int i = 0; i < channelArray.length; i++) {
			    Receiver[] receiverArray = channelArray[i];

			    for (int j = 0; j < receiverArray.length; j++) {
				GiottoReceiver receiver =
				    (GiottoReceiver) receiverArray[j];

				receiver.update();
			    }
			}
		    }
		}
	    }

	return postfire;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time of the next iteration in milliseconds.
    private double _nextIterationTime = 0.0;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime = false;

    // The real time at which the last unit has been invoked.
    private long _realStartTime = 0;

    // The count of iterations executed.
    private int _iteration = 0;

    // The anded result of the values returned by actors' postfire().
    private boolean _postFireReturns = true;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();

}
