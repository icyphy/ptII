/* An attribute that manages generation of Giotto code.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

// Ptolemy imports.
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
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
import ptolemy.domains.ct.kernel.CTDirector;
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
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.util.Workspace;

// Java imports.
import java.awt.Frame;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// GiottoCodeGenerator
/**
This attribute is a visible attribute that when configured (by double
clicking on it or by invoking Configure in the context menu) it generates
Giotto code and displays it a text editor.  It is up to the user to save
the Giotto code in an appropriate file, if necessary.

@author Edward A. Lee
@version $Id$
*/

public class GiottoCodeGenerator extends Attribute {

    /** Construct a factory with the specified _container and name.
     *  @param _container The _container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the _container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the _container.
     */
    public GiottoCodeGenerator(NamedObj _container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(_container, name);

	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");
        new SingletonAttribute(this, "_hideName");
        new GiottoEditorFactory(this, "_editorFactory");

    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The String of the generated code
     */
    public String generatedCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Topology analysis and initialization.
     *
     * @ return Ture if in giotto domain, False if in other domains.
     */
    private boolean _initialize() throws IllegalActionException {
	// Get containers, _commActors, modes List and _modeSwitchController  .
	// _commActors List contains actors which exist in all the modes.
	// Often, the depth of _commActors is smaller than modal director.

	_commActors = new LinkedList();
	_modeList = new LinkedList();

	_container = (TypedCompositeActor) getContainer();

	Director director = ((CompositeActor)_container).getDirector();

	if (!(director instanceof GiottoDirector))
	  return false;
	// FIXME _container1 should be a better name.
	//_container1 = null;

	_modeSwitchController   = null;

	Iterator actors = _container.deepEntityList().iterator();
	//Iterator actors = _container.entityList().iterator();

	while(actors.hasNext()) {
	    TypedActor actor = (TypedActor)actors.next();
	    _commActors.addLast(actor);
	}

	return true;
    }

    /** Generate code for the sensors.
     *  @return The sensors code.
     */
    private String _sensorCode() throws IllegalActionException {

	String codeString = "";

	codeString += "sensor" + _endLine;

	_inPorts = _container.inputPortList().iterator();

	/*
	while (_inPorts.hasNext()) {
	    TypedIOPort port = (TypedIOPort)_inPorts.next();
	    // FIXME: Assuming ports are either
	    // input or output but not both.
	    // FIXME: May want the driver name
	    // specified by a port parameter.


	    String driverName = port.getName() + "_device_driver";
	    codeString +=  "  "
			   + "giotto_c_type"
			   //+ port.getType()
			   + " "
			   + port.getName()
			   + " uses "
			   + driverName
			   + ";"
			   + _endLine;

	}
	*/

	// FORDEMO
	// we combine several inputs into one composite sensor
	// reason?? no

	String compositeSensor = "composite_sensor";
	String sensorDriverName = compositeSensor + "_device_driver";

	codeString +=  "  "
		       + compositeSensor.toUpperCase()
		       + " "
		       + compositeSensor
		       + " uses "
		       + sensorDriverName
		       + ";"
		       + _endLine;

	return codeString;

    }

    /** Generate code for the actuator.
     *  Usually, there is only one actuator.
     *  @return The actuator code.
     */
    private String _actuatorCode() throws IllegalActionException {

	String codeString = "";

	codeString += "actuator" + _endLine;

	_outPorts = _container.outputPortList().iterator();
	while (_outPorts.hasNext()) {
	    TypedIOPort port = (TypedIOPort)_outPorts.next();
	    // FIXME: Assuming ports are either
	    // input or output and not both.
	    // FIXME: May want the driver name
	    // specified by a port parameter.
	    String actuatorDriverName = port.getName() + "_device_driver";
	    codeString +=  "  "
			   + port.getName().toUpperCase()
			   //+ port.getType()
			   + " "
			   + port.getName()
			   + " uses "
			   + actuatorDriverName
			   + ";"
			   + _endLine;
	}

	return codeString;
    }

    /** Generate code for the output ports.
     *  In Giotto, the situation that one port has several inputs
     *  is illegal. From the output ports, it is easy to trace
     *  to getreceivers for output delievery.
     *  @return The output code.
     */
    private String _outputCode() throws IllegalActionException {

	String codeString =  "";

	codeString += "output" + _endLine;

	Iterator actors = _commActors.iterator();
	while(actors.hasNext()) {
	    TypedActor actor = (TypedActor)actors.next();
	    _outPorts = actor.outputPortList().iterator();
	    while (_outPorts.hasNext()) {
		TypedIOPort port = (TypedIOPort)_outPorts.next();
		String outPortName = StringUtilities.sanitizeName(port.getName(_container));
		codeString +=  "  "
			       + outPortName.toUpperCase()
			       //+ port.getType()
			       + " "
			       + outPortName
			       + " := init_"
			       + outPortName
			       + ";"
			       + _endLine;
	    }
	}

	return codeString;
    }

    /** Generate code for the task.
     *  @return The task code.
     */
    private String _taskCode(Actor actor) throws IllegalActionException {
	String codeString = "";

	String taskName = StringUtilities.sanitizeName(
                ((NamedObj)actor).getName(_container));

	codeString += "task "
		      + taskName
		      + "_task (";

	String inPortsNames = taskName.toUpperCase() + "_INPUT";
	String outPortsNames = taskName + "_output";
	String stateParas = taskName.toUpperCase() + "_PARAM param := init_" + taskName + "_param";

	codeString += inPortsNames + " input)" + _endLine;
	codeString +=  "        output ("
		       + outPortsNames
		       + ")"
		       + _endLine;
	codeString +=  "        state ("
		       + stateParas
		       + ")"
		       + _endLine;
	codeString +=  "{"
		       + _endLine;
	codeString +=  "        schedule "
		       + taskName
		       + "_task(input, "
		       + taskName
		       + "_output, "
		       + "param)"
		       + _endLine;
	codeString +=  "}"
		       + _endLine;

	return codeString;
    }

    /** Generate code for the tasks.
     *  There are two parts:
     *        _commActors AND modeActors
     *  @return The task code.
     */
    private String _tasksCode() throws IllegalActionException {

	String codeString = "";
	Actor actor;

	// Generate task functions for common actors.
	Iterator actors = _commActors.iterator();

	while(actors.hasNext()) {
	    actor = (Actor) actors.next();
	    codeString += _taskCode(actor);
	}

/*	//Generate task code for mode actors.

	Iterator modes = _modeList.iterator();
	while(modes.hasNext()) {
	    TypedCompositeActor mode = (TypedCompositeActor) modes.next();
	    Iterator modeActors = mode.deepEntityList().iterator();
	    while(modeActors.hasNext()) {
		actor = (TypedCompositeActor)modeActors.next();
		codeString += _taskCode(actor);
	    }
	}
*/
	return codeString;
    }

    /** Generate code for the driver.
     *  @return The driver code.
     */
    private String _driverCode(Actor actor) throws IllegalActionException {

	String codeString = "";

	String driverParas;
	String actorName;

	driverParas = "";
	actorName = "";

	//actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName(_container));
	actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());

	codeString += "driver "
		      + actorName
		      + "_driver (";

	//get the "deep source" ports(the driver's inputs) for
	//each input port of this actor.

	_inPorts = actor.inputPortList().iterator();
	while (_inPorts.hasNext()) {
	    IOPort inPort = (IOPort) _inPorts.next();
	    String sanitizedPortName =
                StringUtilities.sanitizeName(
                        inPort.getName(_container));
	    //Iterator sourcePorts = inPort.sourcePortList().iterator();
	    Iterator sourcePorts = inPort.connectedPortList().iterator();
	    while(sourcePorts.hasNext()) {
		IOPort port = (IOPort)sourcePorts.next();
		sanitizedPortName = StringUtilities.sanitizeName(
                        port.getName(_container));
		NamedObj portContainer = (NamedObj) port.getContainer();
		//System.out.println(portContainer.getName() + " " + port.depthInHierarchy() + " " + _currentDepth);
		// I hate to make (_currentDepth + 1) here but have to
		// anyway, I think we should make the inside should has some different director
		// again, not modal model.
		if ((port.isOutput()) && (port.depthInHierarchy() == (_currentDepth + 1))) {
		    if (driverParas.length()==0) {
			driverParas +=  sanitizedPortName;
		    } else {
			driverParas += ", " + sanitizedPortName;
		    }
		}
	    }
	}

	if (driverParas.length()==0) {
	    driverParas += "composite_sensor";
	} else {
	    driverParas += ", composite_sensor";
	}

	codeString += driverParas
		       + ")"
		       + _endLine;
	codeString +=  "        output ("
		       + actorName.toUpperCase()
		       + "_INPUT input)"
		       + _endLine;
	codeString +=  "{"
		       + _endLine;
	codeString +=  "  if c_true() then "
		       + actorName
		       + "_input_driver( "
		       + driverParas
		       + ", input"
		       + ")"
		       + _endLine;
	codeString +=  "}"
		       + _endLine;

	return codeString;
    }

    /** Generate code for the drivers.
     *  There are four parts:
     *        commActors + modeActors + controller + actuator
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     */
    private String _driversCode() throws IllegalActionException {

	String codeString = "";
	Actor actor;

	// generate "Driver functions" for common actors.
	Iterator actors = _commActors.iterator();
	while (actors.hasNext()) {
	    actor = (Actor) actors.next();
	    codeString += _driverCode(actor);
	}

	//Generate driver functions for mode actors.
	Iterator modes = _modeList.iterator();
	while(modes.hasNext()) {
	    Actor mode = (Actor) modes.next();
	    Iterator modeActors = ((TypedCompositeActor)mode).deepEntityList().iterator();
	    while(modeActors.hasNext()) {
		actor = (Actor) modeActors.next();
		codeString += _driverCode(actor);
	    }
	}

/*
	//Generate driver functions for the _modeSwitchController's inputs
	//Only when there are several modes, the following drivers for
	// modes switch are necessary.
	if(_modeSwitchController!= null) {
	    _inPorts = _modeSwitchController.inputPortList().iterator();
	    while (_inPorts.hasNext()) {
		driverParas = "";
		IOPort inPort = (IOPort) _inPorts.next();
		String sanitizedPortName = StringUtilities.sanitizeName(
									inPort.getName(_container1));
		pout.print("driver "
			   + sanitizedPortName
			   + "_driver (");
		Iterator sourcePorts = inPort.sourcePortList().iterator();
		while(sourcePorts.hasNext()) {
		    IOPort port = (IOPort)sourcePorts.next();
		    Actor actor = (Actor) port.getContainer();
		    Director director3 = actor.getDirector();
		    TypedCompositeActor portContainer = _container1;
		    if ((director3 instanceof GiottoDirector) &&
			director3.depthInHierarchy()<=1) {
			portContainer = _container;
		    }
		    sanitizedPortName = StringUtilities.sanitizeName(
								     port.getName(portContainer));
		    if (driverParas.length()==0) {
			driverParas += sanitizedPortName;
		    } else {
			driverParas += ", " + sanitizedPortName;
		    }
		}
		codeString += driverParas
			       + ") output ("
			       + ") {"
			       + _endLine;
		codeString +=  "  if "
			       + sanitizedPortName
			       + "_guard ("
			       + driverParas
			       + ") then"
			       + _endLine;
		codeString +=  "    "
			       + sanitizedPortName
			       + "doNothing()"
			       + _endLine;
		codeString +=  "}"
			       + _endLine;
	    }
	}
*/
	// Generate driver functions for toplevel output ports.
	// FIXME: the giotto director should do some checking to
	// avoid several outputs of actors connect to the same output port?

	_outPorts = _container.outputPortList().iterator();
	String outputName ="";
	String sanitizedPortName= "";
	String driverParas;

	while (_outPorts.hasNext()) {
	    driverParas = "";
	    outputName = "";
	    TypedIOPort port = (TypedIOPort)_outPorts.next();
	    outputName = StringUtilities.sanitizeName(port.
						      getName());
	    codeString += "driver "
			 + outputName
			 + "_driver (";

	    Iterator portConnected = port.insidePortList().iterator();
	    while(portConnected.hasNext()) {
		IOPort outPort = (IOPort)portConnected.next();
		sanitizedPortName = StringUtilities.sanitizeName(
                        outPort.getName(_container));
		NamedObj portContainer = (NamedObj) outPort.getContainer();
		//System.out.println(portContainer.getName() + " " + outPort.depthInHierarchy() + " " + _currentDepth);
		// I hate to make (_currentDepth + 1) here but have to
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
	    codeString += driverParas
			  + ")"
			  + _endLine;
	    codeString +=  "        output ("
			   + outputName.toUpperCase()
			   + " "
			   + outputName
			   + "_output)"
			   + _endLine;
	    codeString +=  "{"
			   + _endLine;
	    codeString +=  "  if c_true() then "
			   + outputName
			   + "_input_driver( "
			   + driverParas
			   + ", "
			   + outputName
			   + "_output)"
			   + _endLine;
	    codeString +=  "}"
			   + _endLine;
	}

	return codeString;
    }

    /** Generate code for the modes.
     *  @return The modes code.
     */
    private String _modeCode() throws IllegalActionException {

	String codeString = "";

	int actorFreq = 0;
	int actFreq = 0;
	int exitFreq = 0;

	String outputName, actorName, modeName;

	modeName = StringUtilities.sanitizeName(((NamedObj) _container).getName());

	int periodValue = ((GiottoDirector) ((CompositeActor) _container).getDirector()).getIntPeriod();

	codeString +=  "  mode "
		       + modeName
		       + " () period "
		       + periodValue
		       + " {"
		       + _endLine;

	//FIXME how to deal with several outputs of Giotto director

	_outPorts = _container.outputPortList().iterator();
	while (_outPorts.hasNext()) {
	    outputName = "";
	    TypedIOPort port = (TypedIOPort)_outPorts.next();
	    outputName = StringUtilities.sanitizeName(port.
						      getName(_container));
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
		    codeString +=  "    actfreq "
				   + actorFreq
				   + " do "
				   + outputName
				   + " ("
				   + outputName
				   + "_driver);"
				   + _endLine;
		}
	    }
	}

	//generate mode code for each actor driver
	Iterator actors = _commActors.iterator();
	while (actors.hasNext()) {
	    TypedActor actor = (TypedActor) actors.next();
	    actorName = StringUtilities.sanitizeName(
                    ((NamedObj) actor).getName(_container));
	    Parameter actorFreqPara = (Parameter)
                ((NamedObj) actor).getAttribute("frequency");
            if (actorFreqPara == null) {
                actorFreq = 1;
            } else {
                actorFreq = ((IntToken) actorFreqPara.
                        getToken()).intValue();
            }
            codeString += "    taskfreq "
			  + actorFreq
			  + " do "
			  + actorName
			  + "_task"
			  + "("
			  + actorName
			  + "_driver);"
			  + _endLine;

	}
	codeString += "  }"
		      + _endLine;

        return codeString;

    }

    /** Generate Giotto code for the _container model.
     *  @return The Giotto code.
     */
    public String generateCode() throws IllegalActionException {
	try {
	    // initialization
	    generatedCode = "";
	    if (!_initialize()) {
	        return "Not in Giotto domain!";
    	    }

	    _currentDepth = depthInHierarchy();
	    String containerName = _container.getName();

	    generatedCode += _sensorCode();
	    generatedCode += _actuatorCode();
	    generatedCode += _outputCode();
	    generatedCode += _tasksCode();
	    generatedCode += _driversCode();

	    generatedCode += "start "
			     + containerName
			     + " {"
			     + _endLine;

	    // for one mode only
	    generatedCode += _modeCode();

/*	    if (_modeSwitchController!= null) {
                // check to make sure it is multi-modes
		// find the modes from the state's refinement of the
		// _modeSwitchController
		State initState = _modeSwitchController.getInitialState();
		Iterator states = _modeSwitchController.entityList().iterator(); //???
		while(states.hasNext()) {
		    State state = (State) states.next();
		    if (state != initState) {
			StringAttribute statePara = (StringAttribute)
			    state.getAttribute("refinementName");
			String modeName = statePara.getExpression();
			codeString +=  "  mode "
				     + modeName
				     + " () period "
				     //+ periodValue
				     + (new Double(periodValue)).intValue()
				     + " {";
			// generate mode code for toplevel outputs drivers
			// FIXME: if there are several OUTPUTs..., we have
			// multiple ACTFREQ?
			// find the lowest frequency
			// trace the output port updating frequency
			_outPorts = _container.outputPortList().iterator();
			while (_outPorts.hasNext()) {
			    outputName = "";
			    TypedIOPort outPort = (TypedIOPort)_outPorts.next();
			    Actor actor = (Actor) outPort.getContainer();
			    Director director3 = actor.getDirector();
			    TypedCompositeActor portContainer = _container1;
			    if ((director3 instanceof GiottoDirector) &&
				director3.depthInHierarchy()<=1) {
				portContainer = _container;
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
				codeString +=  "    actfreq "
					     + actorFreq
					     + " do "
					     + outputName
					     + " ("
					     + outputName
					     + "_driver);";
			    }
			}

			//generate mode code for the _modeSwitchController
			Iterator trs = state.nonpreemptiveTransitionList().
			    iterator();
			while (trs.hasNext()) {
			    Transition tr = (Transition)trs.next();
			    State trState = (State) tr.destinationState();
			    StringAttribute trStatePara = (StringAttribute) trState.
				getAttribute("refinementName");
			    String trModeName = trStatePara.getExpression();
			    _inPorts = _modeSwitchController.inputPortList().iterator();
			    while (_inPorts.hasNext()) {
				IOPort inPort = (IOPort) _inPorts.next();
				String inPortName = StringUtilities.sanitizeName(
										 inPort.getName(_container1));
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
				    codeString +=  "    exitfreq "
						 + exitFreq
						 + " do "
						 + trModeName
						 + " ("
						 + inPortName
						 + "_driver);";
				}
			    }


			}

			//generate mode code for each common actor driver
			actors = _commActors.iterator();
			while (actors.hasNext()) {
			    TypedActor actor = (TypedActor) actors.next();
			    actorName = StringUtilities.sanitizeName(((NamedObj)
								      actor).getName(_container));
			    Parameter actorFreqPara = (Parameter) ((NamedObj)
								   actor).getAttribute("frequency");
			    actorFreq = ((IntToken) actorFreqPara.
					 getToken()).intValue();
			    codeString +=  "    taskfreq "
					 + actorFreq
					 + " do "
					 + actorName
					 + "("
					 + actorName
					 + "_driver;";
			}

			//generate mode code for each mode actor driver
			modes = _modeList.iterator();
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
									 getName(_container1));
				Parameter actorFreqPara = (Parameter)
				    ((NamedObj)modeActor).
				    getAttribute("frequency");
				actorFreq = ((IntToken) actorFreqPara.
					     getToken()).intValue();
				codeString +=  "    taskfreq "
					     + actorFreq
					     + " do "
					     + actorName
					     + "("
					     + actorName
					     + "_driver);";
				codeString +=  "  }";
			    }
			    }
			}
		    }
		}

    	    } else {
		generatedCode += _modeCode(_container);
	    }
	    */

	    generatedCode +=  "}"
			      + _endLine;

	} catch (IllegalActionException ex) {
	    System.out.println(ex.getMessage());
	    throw new IllegalActionException(ex.getMessage());
	}

	//return generatedCode;
	return generatedCode;
       }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _commActors;
    private LinkedList _modeList;
    // FIXME _container1 should be a better name.
    //private TypedCompositeActor _container1;
    private FSMActor _modeSwitchController;

    private TypedCompositeActor _container;

    private String _endLine = "\n";

    private int _currentDepth;
    private Iterator _inPorts, _outPorts;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class GiottoEditorFactory extends EditorFactory {

        public GiottoEditorFactory(NamedObj _container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(_container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration
                        = ((TableauFrame)parent).getConfiguration();

                NamedObj _container = (NamedObj)object.getContainer();
                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(), generateCode());
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
