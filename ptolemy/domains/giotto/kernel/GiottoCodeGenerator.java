/* An attribute that manages generation of Giotto code.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
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
import ptolemy.util.StringUtilities;
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

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/

public class GiottoCodeGenerator extends Attribute {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

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
    ////                         public methods                    ////

    /** Generate Giotto code for the given model.
     *  @return The Giotto code.
     */
    public static String generateCode(TypedCompositeActor model) 
            throws IllegalActionException {
	    String generatedCode = "";
            try {

	    _initialize(model);
	    
	    String containerName = model.getName();

	    generatedCode += _sensorCode(model);
	    generatedCode += _actuatorCode(model);
	    generatedCode += _outputCode(model);
	    generatedCode += _tasksCode(model);
	    generatedCode += _driversCode(model);

	    generatedCode += "start "
                + containerName
                + " {"
                + _endLine;

	    generatedCode += _modeCode(model);

	    generatedCode +=  "}"
                + _endLine;

	} catch (IllegalActionException ex) {
	    System.out.println(ex.getMessage());
	    throw new IllegalActionException(ex.getMessage());
	}

	return generatedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    /** Topology analysis and initialization.
     *
     * @ return Ture if in giotto domain, False if in other domains.
     */
    private static boolean _initialize(TypedCompositeActor model) 
            throws IllegalActionException {
	Director director = model.getDirector();

	if (!(director instanceof GiottoDirector)) {
            throw new IllegalActionException(model, director, 
                    "Director of model is not a GiottoDirector.");
        }

	Iterator actors = model.deepEntityList().iterator();

	return true;
    }

    /** Generate code for the sensors.
     *  @return The sensors code.
     */
    private static String _sensorCode(TypedCompositeActor model)
            throws IllegalActionException {

	String codeString = "";

	codeString += "sensor" + _endLine;

	// we combine several inputs into one composite sensor

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
    private static String _actuatorCode(TypedCompositeActor model)
            throws IllegalActionException {

	String codeString = "";

	codeString += "actuator" + _endLine;

	Iterator outPorts = model.outputPortList().iterator();
	while (outPorts.hasNext()) {
	    TypedIOPort port = (TypedIOPort)outPorts.next();
	    // FIXME: Assuming ports are either
	    // input or output and not both.
	    // FIXME: May want the driver name
	    // specified by a port parameter.
	    String actuatorDriverName = port.getName() + "_device_driver";
	    codeString +=  "  "
                + port.getName().toUpperCase()
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
     *  to get receivers for output delivery.
     *  @return The output code.
     */
    private static String _outputCode(TypedCompositeActor model) 
            throws IllegalActionException {

	String codeString =  "";

	codeString += "output" + _endLine;

	Iterator actors = model.entityList().iterator();
	while (actors.hasNext()) {
	    TypedActor actor = (TypedActor)actors.next();
            Iterator outPorts = actor.outputPortList().iterator();
	    while (outPorts.hasNext()) {
		TypedIOPort port = (TypedIOPort)outPorts.next();
		String outPortName = StringUtilities.sanitizeName(
                        port.getName(model));
		codeString +=  "  "
                    + outPortName.toUpperCase()
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
    private static String _taskCode(TypedCompositeActor model, Actor actor) 
            throws IllegalActionException {
	String codeString = "";

	String taskName = StringUtilities.sanitizeName(
                ((NamedObj)actor).getName());

	codeString += "task "
            + taskName
            + "_task (";

	String inPortsNames = taskName.toUpperCase() + "_INPUT";
	String outPortsNames = taskName + "_output";
	String stateParas = taskName.toUpperCase() + 
            "_PARAM param := init_" + taskName + "_param";

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
     *  @return The task code.
     */
    private static String _tasksCode(TypedCompositeActor model)
            throws IllegalActionException {

	String codeString = "";
	Actor actor;

	// Generate task code for common actors.
	Iterator actors = model.entityList().iterator();

	while (actors.hasNext()) {
	    actor = (Actor) actors.next();
	    codeString += _taskCode(model, actor);
	}

	return codeString;
    }

    /** Generate code for the driver.
     *  @return The driver code.
     */
    private static String _driverCode(TypedCompositeActor model, Actor actor) 
            throws IllegalActionException {

	String codeString = "";

	String driverParas;
	String actorName;

	driverParas = "";
	actorName = "";

	actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());

	codeString += "driver "
            + actorName
            + "_driver (";

	//get the "deep source" ports(the driver's inputs) for
	//each input port of this actor.
        int currentDepth = model.depthInHierarchy();
	    
	Iterator inPorts = actor.inputPortList().iterator();
	while (inPorts.hasNext()) {
	    IOPort inPort = (IOPort) inPorts.next();
	    String sanitizedPortName =
                StringUtilities.sanitizeName(
                        inPort.getName(model));
	    Iterator sourcePorts = inPort.connectedPortList().iterator();
	    while (sourcePorts.hasNext()) {
		IOPort port = (IOPort)sourcePorts.next();
		sanitizedPortName = StringUtilities.sanitizeName(
                        port.getName(model));
		NamedObj portContainer = (NamedObj) port.getContainer();
		if (port.isOutput() && 
                        (port.depthInHierarchy() == (currentDepth + 1))) {
		    if (driverParas.length() == 0) {
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
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     */
    private static String _driversCode(TypedCompositeActor model) 
            throws IllegalActionException {

	String codeString = "";
	Actor actor;

	// generate "Driver functions" for common actors.
	Iterator actors = model.entityList().iterator();
	while (actors.hasNext()) {
	    actor = (Actor) actors.next();
	    codeString += _driverCode(model, actor);
	}

	// Generate driver functions for toplevel output ports.
	// FIXME: the giotto director should do some checking to
	// avoid several outputs of actors connect to the same output port?

	Iterator outPorts = model.outputPortList().iterator();
	String outputName ="";
	String sanitizedPortName= "";
	String driverParas;

	while (outPorts.hasNext()) {
	    driverParas = "";
	    outputName = "";
	    TypedIOPort port = (TypedIOPort)outPorts.next();
	    outputName = StringUtilities.sanitizeName(port.
                    getName());
	    codeString += "driver "
                + outputName
                + "_driver (";

	    Iterator portConnected = port.insidePortList().iterator();
	    while (portConnected.hasNext()) {
		IOPort outPort = (IOPort)portConnected.next();
		sanitizedPortName = StringUtilities.sanitizeName(
                        outPort.getName(model));
		NamedObj portContainer = (NamedObj) outPort.getContainer();
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
    private static String _modeCode(TypedCompositeActor model)
            throws IllegalActionException {

	String codeString = "";

	int actorFreq = 0;
	int actFreq = 0;
	int exitFreq = 0;

	String outputName, actorName, modeName;

	modeName = StringUtilities.sanitizeName(model.getName());

	int periodValue = ((GiottoDirector) model.getDirector()).getIntPeriod();

	codeString +=  "  mode "
            + modeName
            + " () period "
            + periodValue
            + " {"
            + _endLine;

	//FIXME how to deal with several outputs of Giotto director

	Iterator outPorts = model.outputPortList().iterator();
	while (outPorts.hasNext()) {
	    outputName = "";
	    TypedIOPort port = (TypedIOPort)outPorts.next();
	    outputName = StringUtilities.sanitizeName(port.
                    getName(model));
	    if (port.insidePortList().size() != 0) {
                Iterator portConnected = port.
                    insidePortList().iterator();
		while (portConnected.hasNext()) {
		    TypedIOPort outPort =
			(TypedIOPort) portConnected.next();
                    if (!outPort.isOutput()) {
                        continue;
                    }
   		    Nameable actor = outPort.getContainer();
		    if (actor instanceof Actor) {
			Parameter actorFreqPara = (Parameter)
			    ((NamedObj)actor).
                            getAttribute("frequency");
                        if (actorFreqPara == null) {
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
	Iterator actors = model.entityList().iterator();
	while (actors.hasNext()) {
	    TypedActor actor = (TypedActor) actors.next();
	    actorName = StringUtilities.sanitizeName(
                    ((NamedObj) actor).getName(model));
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



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _commActors;
    private static String _endLine = "\n";
   
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class GiottoEditorFactory extends EditorFactory {

        public GiottoEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
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

                NamedObj container = (NamedObj)object.getContainer();
                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(), 
                        generateCode((TypedCompositeActor)GiottoCodeGenerator.this.getContainer()));
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
