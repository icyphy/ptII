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
@AcceptedRating Red (eal@eecs.berkeley.edu)

// NOTE: Downgraded to red due to extensive changes.  EAL
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.*;
import ptolemy.actor.NoTokenException;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.util.Workspace;

// FIXME: replace this with per-class imports.
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirector
/**
This class implements a director for the Giotto model of computation
without Giotto modes. Schedules are generated according to the Giotto
semantics. The GiottoScheduler class contains methods to compute the
schedules. The GiottoReceiver class implements the data flow between
actors using double-buffering.
<p>
If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
then the director will not process events until the real time elapsed
since the model started matches the time stamp of the event.
This ensures that the director does not get ahead of real time,
but, of course, it does not ensure that the director keeps up with
real time.

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

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    /** Code generation file name. */
    public Parameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  If the specified attribute is <i>period</i> or
     *  <i>synchronizeToRealTime</i>, then cache the new values.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
	throws IllegalActionException {
        if (attribute == filename) {
            generateGiottoCode();
        } else if (attribute == period) {
	    _periodValue = ((DoubleToken)period.getToken()).doubleValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime =
                ((BooleanToken)synchronizeToRealTime.getToken())
                        .booleanValue();
        }
    }

    /** Generate Giotto code..
     *  NOTE: This is highly preliminary.
     */
    public void generateGiottoCode()
	throws IllegalActionException {
        try {
            int thisDepth = depthInHierarchy();
            //if (thisDepth <= 1) {
	    String file = ((StringToken)filename.getToken()).stringValue();
	    FileOutputStream fout = new FileOutputStream(file);
	    PrintStream pout = new PrintStream(fout);
	    LinkedList commActors = new LinkedList();
	    LinkedList modesList = new LinkedList();
	    TypedCompositeActor container1 = null;
	    FSMActor controller = null;

	    //get containers, commActors and modes List, and controller first.
	    //commActors List contains actors which are the common
	    // part among modes. They might be at the top level rather than in
	    //the mode CompositeActor.
	    TypedCompositeActor container = (TypedCompositeActor)getContainer();

	    //In our former design, we get all the atomicActors or compositeActors
	    // with directors other than FSM or Giotto
	    // but in the demo (tunc1.xml) we only consider the compositeActors
	    // in the same level, otherwise, there are too many tasks and drivers...
	    // One problem with modal model, the refinement always makes trouble..
	    // now, I have no time to figure it out...

	    //Iterator actors = container.deepEntityList().iterator();
	    Iterator actors = container.entityList().iterator();

	    while(actors.hasNext()) {
		TypedActor actor = (TypedActor)actors.next();
		/*
		if (actor instanceof AtomicActor) {
		    commActors.addLast(actor);
		} else {
		    Director director1 = actor.getDirector();
                    if (director1 instanceof FSMDirector) {
			container1 = (TypedCompositeActor)
			    director1.getContainer();
			Iterator actors2 = container1.entityList().
			    iterator();
			while(actors2.hasNext()) {
			    TypedActor actor2 =
				(TypedActor) actors2.next();
			    Director director2 = actor2.getDirector();
			    int depth = director2.depthInHierarchy();
			    if (director2 instanceof GiottoDirector
				& depth >1) {
				modesList.addLast(actor2);
			    } else{ //???any other situation in the hierarchy?
				controller = (FSMActor) actor2;
			    }
			}
		    } else {
                        // Must be opaque and not modal.
                        commActors.addLast(actor);
                    }
		}
		*/
		commActors.addLast(actor);
	    }

	    // Generate sensor list.
	    pout.println("sensor");
	    Iterator inPorts = container.inputPortList().iterator();
	    while (inPorts.hasNext()) {
		TypedIOPort port = (TypedIOPort)inPorts.next();
		// FIXME: Assuming ports are either
		// input or output and not both.
		// FIXME: May want the driver name
		// specified by a port parameter.

		// FORDEMO
		// we combine several inputs into one composite sensor
		// reason?? no
		/*
		String driverName = port.getName() + "_device_driver";
		pout.println("  "
			     + "giotto_c_type"
			     //+ port.getType()
			     + " "
			     + port.getName()
			     + " uses "
			     + driverName
			     + ";");
	        */
	    }
	    String compositeSensor = "composite_sensor";
	    String sensorDriverName = compositeSensor + "_device_driver";
	    pout.println("  "
			 + compositeSensor.toUpperCase()
			 + " "
			 + compositeSensor
			 + " uses "
			 + sensorDriverName
			 + ";");


	    // Generate actuator list.
	    pout.println("actuator");
	    Iterator outPorts = container.outputPortList().iterator();
	    while (outPorts.hasNext()) {
		TypedIOPort port = (TypedIOPort)outPorts.next();
		// FIXME: Assuming ports are either
		// input or output and not both.
		// FIXME: May want the driver name
		// specified by a port parameter.
		String actuatorDriverName = port.getName() + "_device_driver";
		pout.println("  "
			     + port.getName().toUpperCase()
			     //+ port.getType()
			     + " "
			     + port.getName()
			     + " uses "
			     + actuatorDriverName
			     + ";");
	    }

	    // Generate output list.
	    pout.println("output");
	    actors = commActors.iterator();
	    while(actors.hasNext()) {
		TypedActor actor = (TypedActor)actors.next();
		outPorts = actor.outputPortList().iterator();
		while (outPorts.hasNext()) {
		    TypedIOPort port = (TypedIOPort)outPorts.next();
		    //String outPortName = StringUtilities.sanitizeName(port.getName(container));
		    String outPortName = StringUtilities.sanitizeName(port.getName());
		    pout.println("  "
				 + outPortName.toUpperCase()
				 //+ port.getType()
				 + " "
				 + outPortName
				 + " := init_"
				 + outPortName
				 + ";");
		}
	    }

	    // in demo, this was not used....
	    Iterator modes = modesList.iterator();
	    while(modes.hasNext()) {
		TypedCompositeActor mode = (TypedCompositeActor) modes.next();
		Iterator modeActors = mode.deepEntityList().iterator();
		while(modeActors.hasNext()) {
		    TypedActor modeActor = (TypedActor)modeActors.next();
		    outPorts = modeActor.outputPortList().iterator();
		    while (outPorts.hasNext()) {
			TypedIOPort port = (TypedIOPort)outPorts.next();
			String outPortName = StringUtilities.sanitizeName(
									  ((NamedObj)port).getName(container1));
			pout.println("  "
				     + "giotto_c_type"
				     //+ port.getType()
				     + " "
				     + outPortName
				     + " := init_"
				     + outPortName
				     + ";");
		    }
		}
	    }

	    // Generate task functions for common actors.
	    actors = commActors.iterator();
	    while(actors.hasNext()) {
		TypedActor actor = (TypedActor)actors.next();
		String taskName = StringUtilities.sanitizeName(
                        ((NamedObj)actor).getName(container));
		pout.print("task " + taskName + "_task (");
		inPorts = actor.inputPortList().iterator();

		String inPortsNames = taskName.toUpperCase() + "_INPUT";
		String outPortsNames = taskName + "_output";
		String stateParas = taskName.toUpperCase() + "_PARAM param := init_" + taskName + "_param";

		pout.println(inPortsNames + " input)");
		pout.println("        output (" + outPortsNames + ")");
		pout.println("        state (" + stateParas + ")");
		pout.println("{");
		pout.println("        schedule "
			     + taskName
			     + "_task(input, "
			     + taskName
			     + "_output, "
			     + "param)");
		pout.println("}");

		/*
		String inPortsNames = "";
		String outPortsNames = "";
		while (inPorts.hasNext()) {
		    TypedIOPort port = (TypedIOPort)inPorts.next();
		    inPortsNames += port.getName();
		    pout.print("giotto_c_type" + " " + port.getName());
		    //pout.print(port.getType() + " " + port.getName());
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
		pout.println(") state () {");
		pout.println("  schedule "
			     + taskName
			     + "_fire( "
			     + inPortsNames
			     + ", "
			     + outPortsNames
			     + ")");
		pout.println("}");
		*/
	    }

	    //Generate task code for mode actors.
	    // not used for demo
	    modes = modesList.iterator();
	    while(modes.hasNext()) {
		TypedCompositeActor mode = (TypedCompositeActor) modes.next();
		Iterator modeActors = mode.deepEntityList().iterator();
		while(modeActors.hasNext()) {
		    TypedActor modeActor = (TypedActor)modeActors.next();
		    String taskName = StringUtilities.sanitizeName(
								   ((NamedObj)modeActor).getName(container1));
		    pout.print("task " + taskName + " (");
		    inPorts = modeActor.inputPortList().iterator();
		    String inPortsNames = "";
		    String outPortsNames = "";
		    while (inPorts.hasNext()) {
			TypedIOPort port = (TypedIOPort)inPorts.next();
			inPortsNames += port.getName();
			pout.print("giotto_c_type" + " " + port.getName());
			//pout.print(port.getType() + " " + port.getName());
			if (inPorts.hasNext()) {
			    inPortsNames += ", ";
			    pout.print(", ");
			}
		    }
		    pout.print(") output (");
		    outPorts = modeActor.outputPortList().iterator();
		    while (outPorts.hasNext()) {
			TypedIOPort port = (TypedIOPort)outPorts.next();
			String sanitizedPortName = StringUtilities.sanitizeName(
										port.getName(container1));
			outPortsNames += sanitizedPortName;
			pout.print(sanitizedPortName);
			if (outPorts.hasNext()) {
			    outPortsNames += ", ";
			    pout.print(", ");
			}
		    }
		    pout.println(") state () {");
		    pout.println("  schedule "
				 + taskName
				 + "_fire( "
				 + inPortsNames
				 + ", "
				 + outPortsNames
				 + ")");
		    pout.println("}");
		}
	    }

	    //generate "Driver functions" for common actors.
	    // warning, the order of ports in model has effect on the
	    // order driver parameters
	    // here I use the solution that compare the depth in hierarchy
	    // to include the same level actors input/output ports ...

	    actors = commActors.iterator();
	    String driverParas, outParas, typedOutParas;
	    String actorName;
	    while (actors.hasNext()) {
		driverParas = "";
		outParas = "";
		typedOutParas = "";
		actorName = "";
		TypedActor actor = (TypedActor)actors.next();
		//actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName(container));
		actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());
		pout.print("driver "
			   + actorName
			   + "_driver (");
		//get the "deep source" ports(the driver's inputs) for
		//each input port of this actor.

		// NOTICE: for demo, we only consider those at same level in hierarchy
		// so, we did not use deep... method

		inPorts = actor.inputPortList().iterator();
		while (inPorts.hasNext()) {
		    IOPort inPort = (IOPort) inPorts.next();
		    String sanitizedPortName =
                        StringUtilities.sanitizeName(
                                inPort.getName(container));
		    //we can not use sourcePortList() in the existing demo
		    //since the composite actors have no other directors but giotto director
		    //Iterator sourcePorts = inPort.sourcePortList().iterator();
		    Iterator sourcePorts = inPort.connectedPortList().iterator();
		    while(sourcePorts.hasNext()) {
			IOPort port = (IOPort)sourcePorts.next();
			//sanitizedPortName = StringUtilities.sanitizeName(
                        //        port.getName(container));
			sanitizedPortName = StringUtilities.sanitizeName(
                                port.getName());
			NamedObj portContainer = (NamedObj) port.getContainer();
			//System.out.println(portContainer.getName() + " " + port.depthInHierarchy() + " " + thisDepth);
			// I hate to make (thisDepth + 1) here but have to
			// anyway, I think we should make the inside should has some different director
			// again, not modal model.
			if ((port.isOutput()) && (port.depthInHierarchy() == (thisDepth + 1))) {
			    if (driverParas.length()==0) {
				driverParas +=  sanitizedPortName;
			    } else {
				driverParas += ", " + sanitizedPortName;
			    }
			}

			/*
			if (driverParas.length()==0) {
			    driverParas += sanitizedPortName;
			} else {
			    driverParas += ", " + sanitizedPortName;
			}
			*/
		    }
		}

		if (driverParas.length()==0) {
		    driverParas += "composite_sensor";
		} else {
		    driverParas += ", composite_sensor";
		}

		//reset inPorts and get the driver's outputs
		/*
		inPorts = actor.inputPortList().iterator();
		while (inPorts.hasNext()) {
		    TypedIOPort port = (TypedIOPort) inPorts.next();
		    if (outParas == "") {
			typedOutParas += "giotto_c_type"
			//typedOutParas += port.getType()
                            + " "
                            + port.getName();
			outParas += port.getName();
		    } else {
			typedOutParas += ", "
                            + "giotto_c_type"
			    //+ port.getType()
                            + " "
                            + port.getName();
			outParas += ", " + port.getName();
		    }
		}
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
		*/
		pout.println(driverParas
			     + ")");
		pout.println("        output ("
			     + actorName.toUpperCase()
			     + "_INPUT input)");
	        pout.println("{");
		pout.println("  if c_true() then "
			     + actorName
			     + "_input_driver( "
			     + driverParas
			     + ", input"
			     + ")");
		pout.println("}");

      	    }

	    //Generate driver functions for mode actors.
	    // not used for demo
	    modes = modesList.iterator();
	    while(modes.hasNext()) {
		TypedCompositeActor mode = (TypedCompositeActor) modes.next();
		Iterator modeActors = mode.deepEntityList().iterator();
		while(modeActors.hasNext()) {
		    driverParas = "";
		    outParas = "";
		    typedOutParas = "";
		    actorName = "";
		    TypedActor modeActor = (TypedActor)modeActors.next();
		    actorName = StringUtilities.sanitizeName(((NamedObj)
							      modeActor).getName(container1));
		    pout.print("driver "
			       + actorName
			       + "_driver (");
		    //get the "deep source" ports(the driver's inputs) for
		    //each input port of this actor.
		    inPorts = modeActor.inputPortList().iterator();
		    while (inPorts.hasNext()) {
			IOPort inPort = (IOPort) inPorts.next();
			String sanitizedPortName = StringUtilities.sanitizeName(
										inPort.getName(container1));
			Iterator sourcePorts = inPort.
                            sourcePortList().iterator();
			while(sourcePorts.hasNext()) {
			    IOPort port = (IOPort)sourcePorts.next();
			    Actor actor = (Actor) port.getContainer();
			    Director director3 = actor.getDirector();
			    TypedCompositeActor portContainer = container1;
			    if ((director3 instanceof GiottoDirector) &&
                                director3.depthInHierarchy()<=1) {
				portContainer = container;
			    }
			    sanitizedPortName = StringUtilities.sanitizeName(
									     port.getName(portContainer));
			    if (driverParas.length()==0) {
				driverParas += sanitizedPortName;
			    } else {
				driverParas += ", " + sanitizedPortName;
			    }
			}
		    }
		    //reset inPorts and get the driver's outputs
		    inPorts = modeActor.inputPortList().iterator();
		    while (inPorts.hasNext()) {
			TypedIOPort port = (TypedIOPort) inPorts.next();
			if (outParas == "") {
			    typedOutParas += "giotto_c_type"
			    //typedOutParas += port.getType()
                                + " "
                                + port.getName();
			    outParas += port.getName();
			} else {
			    typedOutParas += ", "
                                + "giotto_c_type"
				//+ port.getType()
                                + " "
                                + port.getName();
			    outParas += ", " + port.getName();
			}
		    }
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
	    }

	    //Generate driver functions for the controller's inputs
	    //Only when there are several modes, the following drivers for
	    // modes switch are necessary.
	    if(controller != null) {
		inPorts = controller.inputPortList().iterator();
		while (inPorts.hasNext()) {
		    driverParas = "";
		    IOPort inPort = (IOPort) inPorts.next();
		    String sanitizedPortName = StringUtilities.sanitizeName(
									    inPort.getName(container1));
		    pout.print("driver "
			       + sanitizedPortName
			       + "_driver (");
		    Iterator sourcePorts = inPort.sourcePortList().iterator();
		    while(sourcePorts.hasNext()) {
			IOPort port = (IOPort)sourcePorts.next();
			Actor actor = (Actor) port.getContainer();
			Director director3 = actor.getDirector();
			TypedCompositeActor portContainer = container1;
			if ((director3 instanceof GiottoDirector) &&
			    director3.depthInHierarchy()<=1) {
			    portContainer = container;
			}
			sanitizedPortName = StringUtilities.sanitizeName(
									 port.getName(portContainer));
			if (driverParas.length()==0) {
			    driverParas += sanitizedPortName;
			} else {
			    driverParas += ", " + sanitizedPortName;
			}
		    }
		    pout.println(driverParas
				 + ") output ("
				 + ") {");
		    pout.println("  if "
				 + sanitizedPortName
				 + "_guard ("
				 + driverParas
				 + ") then");
		    pout.println("    "
				 + sanitizedPortName
				 + "doNothing()");
		    pout.println("}");
		}
	    }

	    // Generate driver functions for toplevel output ports.
	    // FIXME: the giotto director should do some checking to
	    // avoid several outputs of actors connect to the same output port?
	    outPorts = container.outputPortList().iterator();
	    String outputName ="";
	    String sanitizedPortName= "";
	    while (outPorts.hasNext()) {
		driverParas = "";
		outParas = "";
		typedOutParas = "";
		outputName = "";
		TypedIOPort port = (TypedIOPort)outPorts.next();
		outputName = StringUtilities.sanitizeName(port.
							  getName());
		pout.print("driver "
			   + outputName
			   + "_driver (");
		outParas += port.getName();

	        Iterator portConnected = port.insidePortList().iterator();
		while(portConnected.hasNext()) {
		    IOPort outPort = (IOPort)portConnected.next();
		    sanitizedPortName = StringUtilities.sanitizeName(
                            outPort.getName());
		    NamedObj portContainer = (NamedObj) outPort.getContainer();
		    //System.out.println(portContainer.getName() + " " + outPort.depthInHierarchy() + " " + thisDepth);
		    // I hate to make (thisDepth + 1) here but have to
		    // anyway, I think we should make the inside should has some different director
		    // again, not modal model.
		    if (outPort.isOutput()) {
			if (driverParas.length()==0) {
			    driverParas +=  sanitizedPortName;
			} else {
			    driverParas += ", " + sanitizedPortName;
			}
		    }
		}
		pout.println(driverParas
			     + ")");
		pout.println("        output ("
			     + outputName.toUpperCase()
			     + " "
			     + outputName
			     + "_output)");
	        pout.println("{");
		pout.println("  if c_true() then "
			     + outputName
			     + "_input_driver( "
			     + driverParas
			     + ", "
			     + outputName
			     + "_output)");
		pout.println("}");
		/*
		typedOutParas += port.getType() + " " + port.getName();
		Iterator portConnected = port.insidePortList().iterator();
		if (port.insidePortList().size() != 0) {
		    while (portConnected.hasNext()) {
			TypedIOPort outPort = (TypedIOPort)
                            portConnected.next();
			Actor actor = (Actor) port.getContainer();
			Director director3 = actor.getDirector();
			TypedCompositeActor portContainer = container1;
			if ((director3 instanceof GiottoDirector) &&
                            director3.depthInHierarchy()<=1) {
			    portContainer = container;
			}
			sanitizedPortName = StringUtilities.sanitizeName(
									 outPort.getName(portContainer));
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
		*/
	    }

	    //generate code inside "start {}"
	    String containerName = container.getName();

	    //In ptolemy model, for simulation, we need double
	    double periodValue =
                ((DoubleToken)period.getToken()).doubleValue() * 1000;
	    // however, for giotto code, we need integer and its unit is ms ...
	    // System.out.println("period is " + (new Double(periodValue)).intValue());

	    int actorFreq = 0;
	    int actFreq = 0;
	    int exitFreq = 0;
	    pout.println("start "
			 + containerName
			 + " {");
	    if (controller != null) {
                // check to make sure it is multi-modes
		// find the modes from the state's refinement of the
                // controller
		State initState = controller.getInitialState();
		Iterator states = controller.entityList().iterator(); //???
		while(states.hasNext()) {
		    State state = (State) states.next();
		    if (state != initState) {
			StringAttribute statePara = (StringAttribute)
                            state.getAttribute("refinementName");
			String modeName = statePara.getExpression();
			pout.println("  mode "
				     + modeName
				     + " () period "
				     //+ periodValue
				     + (new Double(periodValue)).intValue()
				     + " {");
			// generate mode code for toplevel outputs drivers
			// FIXME: if there are several OUTPUTs..., we have
			// multiple ACTFREQ?
			// find the lowest frequency
			// trace the output port updating frequency
			outPorts = container.outputPortList().iterator();
			while (outPorts.hasNext()) {
			    outputName = "";
			    TypedIOPort outPort = (TypedIOPort)outPorts.next();
			    Actor actor = (Actor) outPort.getContainer();
			    Director director3 = actor.getDirector();
			    TypedCompositeActor portContainer = container1;
			    if ((director3 instanceof GiottoDirector) &&
                                director3.depthInHierarchy()<=1) {
				portContainer = container;
			    }
			    outputName =
                                StringUtilities.sanitizeName(
                                        outPort.getName(portContainer));
			    Iterator portConnected = outPort.
                                insidePortList().iterator();
			    if (outPort.insidePortList().size() != 0) {
				while (portConnected.hasNext()) {
				    TypedIOPort port = (TypedIOPort)
                                        portConnected.next();
				    Nameable portActor = port.getContainer();
				    if (portActor instanceof AtomicActor) {
					Parameter actorFreqPara = (Parameter)
                                            ((NamedObj)portActor).
                                            getAttribute("frequency");
					actorFreq = ((IntToken) actorFreqPara.
						     getToken()).intValue();
				    }
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

			//generate mode code for the controller
			Iterator trs = state.nonpreemptiveTransitionList().
			    iterator();
			while (trs.hasNext()) {
			    Transition tr = (Transition)trs.next();
			    State trState = (State) tr.destinationState();
			    StringAttribute trStatePara = (StringAttribute) trState.
				getAttribute("refinementName");
			    String trModeName = trStatePara.getExpression();
			    inPorts = controller.inputPortList().iterator();
			    while (inPorts.hasNext()) {
				IOPort inPort = (IOPort) inPorts.next();
				String inPortName = StringUtilities.sanitizeName(
										 inPort.getName(container1));
				Iterator sourcePorts = inPort.sourcePortList().
				    iterator();
				if (sourcePorts.hasNext()) {
				    IOPort port = (IOPort)sourcePorts.next();
				    Nameable portActor = port.getContainer();
				    if (portActor instanceof AtomicActor) {
					Parameter actorPara = (Parameter)
					    ((NamedObj)portActor).
					    getAttribute("frequency");
					exitFreq = ((IntToken) actorPara.
						    getToken()).intValue();
				    }
				    pout.println("    exitfreq "
						 + exitFreq
						 + " do "
						 + trModeName
						 + " ("
						 + inPortName
						 + "_driver);");
				}
			    }


			}

			//generate mode code for each common actor driver
			actors = commActors.iterator();
			while (actors.hasNext()) {
			    TypedActor actor = (TypedActor) actors.next();
			    actorName = StringUtilities.sanitizeName(((NamedObj)
								      actor).getName(container));
			    Parameter actorFreqPara = (Parameter) ((NamedObj)
								   actor).getAttribute("frequency");
			    actorFreq = ((IntToken) actorFreqPara.
					 getToken()).intValue();
			    pout.println("    taskfreq "
					 + actorFreq
					 + " do "
					 + actorName
					 + "("
					 + actorName
					 + "_driver);"
					 );
			}

			//generate mode code for each mode actor driver
			modes = modesList.iterator();
			while(modes.hasNext()) {
			    TypedCompositeActor mode =
				(TypedCompositeActor) modes.next();
			    if (mode.getName().trim().equals(modeName)){;
			    Iterator modeActors = mode.
                                deepEntityList().iterator();
			    while(modeActors.hasNext()) {
				TypedActor modeActor =
                                    (TypedActor)modeActors.next();
				actorName = StringUtilities.sanitizeName((
									  (NamedObj)modeActor).
									 getName(container1));
				Parameter actorFreqPara = (Parameter)
                                    ((NamedObj)modeActor).
                                    getAttribute("frequency");
				actorFreq = ((IntToken) actorFreqPara.
					     getToken()).intValue();
				pout.println("    taskfreq "
					     + actorFreq
					     + " do "
					     + actorName
					     + "("
					     + actorName
					     + "_driver);");
				pout.println("  }");
			    }
			    }
			}
		    }
		}
	    } else { //the model only has single mode
		pout.println("  mode "
			     + containerName
			     + " () period "
			     //+ periodValue
			     + (new Double(periodValue)).intValue()
			     + " {");
		outPorts = container.outputPortList().iterator();
		while (outPorts.hasNext()) {
		    outputName = "";
		    TypedIOPort port = (TypedIOPort)outPorts.next();
		    outputName = StringUtilities.sanitizeName(port.
							      getName(container));
		    if (port.insidePortList().size() != 0) {
                        Iterator portConnected = port.
                            insidePortList().iterator();
			while (portConnected.hasNext()) {
			    TypedIOPort outPort =
				(TypedIOPort) portConnected.next();
                            if(!outPort.isOutput()) {
                                continue;
                            }
			    Nameable actor = outPort.getContainer();
			    if (actor instanceof Actor) {
				Parameter actorFreqPara = (Parameter)
				    ((NamedObj)actor).
                                    getAttribute("frequency");
                                if(actorFreqPara == null) {
                                    actorFreq = 1;
                                } else {
                                    actorFreq = ((IntToken) actorFreqPara.
                                            getToken()).intValue();
                                }
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
		actors = commActors.iterator();
		while (actors.hasNext()) {
		    TypedActor actor = (TypedActor) actors.next();
		    actorName = StringUtilities.sanitizeName(
                            ((NamedObj) actor).getName(container));
		    Parameter actorFreqPara = (Parameter)
                        ((NamedObj) actor).getAttribute("frequency");
                    if (actorFreqPara == null) {
                        actorFreq = 1;
                    } else {
                        actorFreq = ((IntToken) actorFreqPara.
                                getToken()).intValue();
                    }
                    pout.println("    taskfreq "
                            + actorFreq
                            + " do "
                            + actorName
  			    + "_task"
                            + "("
                            + actorName
                            + "_driver);"
                                 );

		}
		pout.println("  }");
	    }
	    // End of else

	    pout.println("}");

	    // the below } is the complementary for if (thisDepth <= 1) {
	    //	    }
	} catch (IOException ex) {
	    throw new IllegalActionException(this, ex.getMessage());
	}
    }

    /** Return the next time that this director expects activity.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
	return getCurrentTime() + _minTimeStep;
    }

    /** Iterate the actors in the next minor cycle of the schedule.
     *  After iterating the actors, increment time by the minor cycle time.
     *  Also, update the receivers that are destinations of all actors that
     *  will be invoked in the next minor cycle of the schedule.
     *  This works because all actors in Giotto are invoked periodically,
     *  and the ones that will be invoked in the next cycle are the ones
     *  that are completing invocation at the end of this cycle.
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
	TypedCompositeActor container = (TypedCompositeActor) getContainer();
	if (container == null) {
	    throw new IllegalActionException(this, "Has no container!");
        }
        // A schedule consists of a set of subschedules, one for each
        // "unit" of Giotto.  If there are no more subschedules to
        // execute, then start over with the original schedule.

        if (_debugging) {
            _debug("Giotto director firing!");
        }

        // Grab the next schedule to execute.
        Schedule unitSchedule = (Schedule)_schedule.get(_unitIndex++);
        if (_unitIndex >= _schedule.size()) {
            _unitIndex = 0;
            // Iteration is complete when the unit index wraps around.
            _iterationCount++;
        }

        // Find the actors that will be invoked in this minor cycle (unit)
        // and update the receivers that are their destinations.
        // The reason that the destinations are updated is that any
        // actor that is to be run in this unit is also presumably
        // just completing a run from the previous iteration (because
        // of the periodic semantics of Giotto).
        // Of course, in the first iteration, the actors are not
        // (obviously) completing any previous cycle.  However, according
        // to Giotto semantics, the first unit always involves a firing
        // of every actor.  Hence, the first unit schedule will include
        // every actor.  Thus, in the first iteration, we will be
        // committing the outputs of every actor.  This has the effect
        // of committing any data values that the actor may have produced
        // in its initialize() method.
        Iterator scheduleIterator = unitSchedule.iterator();
        while (scheduleIterator.hasNext()) {
            Actor actor = ((Firing) scheduleIterator.next()).getActor();
            if (_debugging) {
                _debug("Updating destination receivers of "
                         + ((NamedObj)actor).getFullName());
            }
            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
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

        scheduleIterator = unitSchedule.iterator();
        while (scheduleIterator.hasNext()) {
            Actor actor = ((Firing) scheduleIterator.next()).getActor();
            int actorFrequency = GiottoScheduler.getFrequency(actor);
            if (_debugging) {
                _debug("Iterating " + ((NamedObj)actor).getFullName());
            }
            if (actor.iterate(1) == STOP_ITERATING) {
                // FIXME: put the actor on a no-fire hashtable.
            }
	}

	// Update time.
        // FIXME: This isn't the right thing to do if we are inside another
        // actor.
        double currentTime = getCurrentTime();
	setCurrentTime(currentTime + _minTimeStep);

	if (_synchronizeToRealTime) {
	    long elapsedTime = System.currentTimeMillis() - _realStartTime;
	    double elapsedTimeInSeconds = ((double) elapsedTime) / 1000.0;

	    if (currentTime > elapsedTimeInSeconds) {
		long timeToWait = (long)
                        ((currentTime - elapsedTimeInSeconds) * 1000.0);
		if (timeToWait > 0) {
		    if (_debugging) {
			_debug("Waiting for real time to pass: " + timeToWait);
		    }
                    // Synchronize on the scheduler.
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

    /** Initialize the actors associated with this director.
     *  The order in which the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
	_iterationCount = 0;
        _unitIndex = 0;
	Iterator receivers = _receivers.iterator();
	while(receivers.hasNext()) {
	    GiottoReceiver receiver = (GiottoReceiver) receivers.next();
	    receiver.reset();
	}

	super.initialize();

        // Iterate through all output ports to see if any have initialValue
        // parameters.
        CompositeActor compositeActor = (CompositeActor) (getContainer());
        List actorList = compositeActor.deepEntityList();
        ListIterator actors = actorList.listIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                Parameter initialValueParameter = (Parameter)
                        ((NamedObj) port).getAttribute("initialValue");
                if (initialValueParameter != null) {
                    port.broadcast(initialValueParameter.getToken());
                }
            }
        }

        // Next, construct the schedule.
        // FIXME: Note that mutations will not be supported since the
        // schedule is constructed only once.
        GiottoScheduler scheduler = (GiottoScheduler) getScheduler();
        _schedule = scheduler.getSchedule();
        _minTimeStep = scheduler.getMinTimeStep(_periodValue);

        _realStartTime = System.currentTimeMillis();
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    public Receiver newReceiver() {
	Receiver receiver = new GiottoReceiver();
	_receivers.add(receiver);
	return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or by having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
	int numberOfIterations =
	        ((IntToken) (iterations.getToken())).intValue();
        if (_debugging) {
            _debug("===== Director completing unit of iteration: "
                    + _iterationCount);
        }
	if((numberOfIterations > 0)
                && (_iterationCount >= numberOfIterations)) {
	    return false;
	}
	return true;
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
                   "transferInputs: port argument is not an opaque"
                   + "input port.");
	}
	boolean transfer = false;
	Receiver[][] insideReceivers = port.deepGetReceivers();
	for (int i = 0; i < port.getWidth(); i++) {
	    if (port.hasToken(i)) {
                Token t = port.get(i);
                if (insideReceivers != null && insideReceivers[i] != null) {
                    if(_debugging) _debug(getName(),
                            "transferring input from " + port.getName());
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        insideReceivers[i][j].put(t);
                        ((GiottoReceiver)insideReceivers[i][j]).update();
                    }
                    transfer = true;
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

            synchronizeToRealTime = new Parameter(this,
                    "synchronizeToRealTime",
                    new BooleanToken(false));
	} catch (KernelException ex) {
	    throw new InternalErrorException(
                    "Cannot initialize director: " + ex.getMessage());
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The count of iterations executed.
    private int _iterationCount = 0;

    // Minimum time step size (a Giotto "unit").
    private double _minTimeStep = 0.0;

    // Period of the director.
    private double _periodValue = 0.0;

    // The real time at which the initialize() method was invoked.
    private long _realStartTime = 0;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();

    // Schedule to be executed.
    private Schedule _schedule;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime = false;

    // Counter for minimum time steps.
    private int _unitIndex = 0;
}
