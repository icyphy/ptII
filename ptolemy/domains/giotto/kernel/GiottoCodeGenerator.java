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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.util.StringUtilities;

import java.awt.Frame;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// GiottoCodeGenerator
/**
This attribute is a visible attribute that when configured (by double
clicking on it or by invoking Configure in the context menu) it generates
Giotto code and displays it a text editor.  It is up to the user to save
the Giotto code in an appropriate file, if necessary.

@author Edward A. Lee, Steve Neuendorffer, Haiyang Zheng
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
            model.wrapup();

        } catch (KernelException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalActionException(ex.getMessage());
        }

        return generatedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    /** Throw an exception if the given string is a valid giotto
     *  reserved word, which prevents it from being used as an identifier.
     */
    private static void _checkGiottoID(String string)
            throws IllegalActionException {
        if (string.equals("output")) {
            throw new RuntimeException("The identifier " + string +
                    " cannot be used in a Giotto program.  " +
                    "Please change your model and attempt to " +
                    "generate code again.");
        }
    }

    /** Return the correct Giotto type string for the given port.
     */
    private static String _getTypeString(IOPort port) {
        return "Token_port";//ort.getType().toString();
    }

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

        return true;
    }

    /** Generate code for the sensors.
     *  @return The sensors code.
     */
    private static String _sensorCode(TypedCompositeActor model)
            throws IllegalActionException {

        String codeString = "";

        codeString += "sensor" + _endLine;

        Iterator inPorts = model.inputPortList().iterator();
        while (inPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort)inPorts.next();
            // FIXME: Assuming ports are either
            // input or output and not both.
            // FIXME: May want the driver name
            // specified by a port parameter.
            // FIXME: Use a real type.
            String portID = port.getName();
            String portTypeID = _getTypeString(port);
            String actuatorDriverName = port.getName() + "_device_driver";
            _checkGiottoID(portID);
            codeString +=  "  "
                + portTypeID
                + " "
                + portID
                + " uses "
                + actuatorDriverName
                + ";"
                + _endLine;
        }

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
            // FIXME: Use a real type.
            String portID = port.getName();
            String portTypeID = _getTypeString(port);
            String actuatorDriverName = port.getName() + "_device_driver";
            _checkGiottoID(portID);
            codeString +=  "  "
                + portTypeID
                + " "
                + portID
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
                String portID = StringUtilities.sanitizeName(
                        port.getName(model));
                String portTypeID = _getTypeString(port);
                String portInitialValue = "CGinit_" + portID;
                _checkGiottoID(portID);
                codeString +=  "  "
                    + portTypeID
                    + " "
                    + portID
                    + " := " + portInitialValue
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
        boolean first;
        String taskName = StringUtilities.sanitizeName(
                ((NamedObj)actor).getName());

        codeString += "task "
            + taskName
            + " (";

        String stateParas = ""; //taskName.toUpperCase() +
        //  "_PARAM param := init_" + taskName + "_param";

        // Write the input port specification of the task
        first = true;
        String inputPorts = "";
        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            if (first) {
                first = false;
            } else {
                inputPorts += ",";
                codeString += ",";
            }
            TypedIOPort port = (TypedIOPort)inPorts.next();
            String portID = StringUtilities.sanitizeName(
                    port.getName(model));
            String portTypeID = _getTypeString(port);

            codeString += portTypeID + " " + portID;
            inputPorts += portID;
        }

        codeString += ")" + _endLine;
        codeString +=  "        output (";

        // write the output port specification of the task.
        first = true;
        String outputPorts = "";
        for (Iterator outPorts = actor.outputPortList().iterator();
             outPorts.hasNext();) {
            if (first) {
                first = false;
            } else {
                codeString += ",";
                outputPorts += ",";
            }
            TypedIOPort port = (TypedIOPort)outPorts.next();
            String portID = StringUtilities.sanitizeName(
                    port.getName(model));
            codeString += portID;
            outputPorts += portID;
        }
        codeString += ")" + _endLine;
        codeString +=  "        state ("
            + stateParas
            + ")"
            + _endLine;
        codeString +=  "{"
            + _endLine;
        String portSeparator = ",";
        if(inputPorts.equals("") || outputPorts.equals("")) {
            portSeparator = "";
        }
        codeString +=  "        schedule CG"
            + taskName
            + "_Task(" + inputPorts + portSeparator + outputPorts + ")"
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
        if(!_needsInputDriver(actor)) {
            return "";
        }

        String codeString = "";

        String driverParas;
        String actorName;

        driverParas = "";
        actorName = "";

        actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());

        codeString += "driver "
            + actorName
            + "_driver (";

        Map driverIOMap = new LinkedHashMap();
        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            IOPort inPort = (IOPort) inPorts.next();
            String sanitizedPortName =
                StringUtilities.sanitizeName(
                        inPort.getName(model));
            List sourcePortList = inPort.sourcePortList();
            if (sourcePortList.size() > 1) {
                throw new IllegalActionException(inPort, "Input port " +
                        "cannot receive data from multiple sources in Giotto.");
            }
            Iterator sourcePorts = inPort.sourcePortList().iterator();
            while (sourcePorts.hasNext()) {
                IOPort port = (IOPort)sourcePorts.next();
                String sanitizedPortName2 = StringUtilities.sanitizeName(
                        port.getName(model));
                if (driverParas.length() == 0) {
                    driverParas +=  sanitizedPortName2;
                } else {
                    driverParas += ", " + sanitizedPortName2;
                }
                driverIOMap.put(sanitizedPortName2, sanitizedPortName);
            }
        }

        codeString += driverParas
            + ")"
            + _endLine;
        codeString +=  "        output (";
        // Write the input port specification of the task
        boolean first = true;
        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            if (first) {
                first = false;
            } else {
                codeString += ",";
            }
            TypedIOPort port = (TypedIOPort)inPorts.next();
            String portID = StringUtilities.sanitizeName(
                    port.getName(model));
            String portTypeID = _getTypeString(port);
            codeString += portTypeID + " " + portID;
        }
        codeString += ")" + _endLine;
        codeString +=  "{"
            + _endLine;

        for (Iterator sourceNames = driverIOMap.keySet().iterator();
             sourceNames.hasNext();) {
            String sourceName = (String) sourceNames.next();
            String destName = (String) driverIOMap.get(sourceName);
            codeString +=
                "          if constant_true() then copy_Token_port( "
                + sourceName + ", " + destName
                + ")"
                + _endLine;
        }
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
        for (Iterator outPorts = model.outputPortList().iterator();
             outPorts.hasNext();) {
            String driverParas = "";
            TypedIOPort port = (TypedIOPort)outPorts.next();
            String portTypeID = _getTypeString(port);
            String portID = StringUtilities.sanitizeName(port.
                    getName());
            codeString += "driver "
                + portID
                + "_driver (";

            Iterator portConnected = port.insidePortList().iterator();
            while (portConnected.hasNext()) {
                IOPort outPort = (IOPort)portConnected.next();
                String sanitizedPortName = StringUtilities.sanitizeName(
                        outPort.getName(model));
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
                + portTypeID
                + " "
                + portID
                + "_output)"
                + _endLine;
            codeString +=  "{"
                + _endLine;
            codeString +=  "  if c_true() then "
                + portID
                + "_input_driver( "
                + driverParas
                + ", "
                + portID
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
            String driverName = "";
            if(_needsInputDriver(actor)) {
                driverName = actorName + "_driver";
            }
            codeString += "    taskfreq "
                + actorFreq
                + " do "
                + actorName
                + "("
                + driverName
                + ");"
                + _endLine;

        }
        codeString += "  }"
            + _endLine;

        return codeString;

    }

    /** Return true if the given actor has at least one input port, which 
     *  requires it to have an input driver.
     */
    private static boolean _needsInputDriver(Actor actor) {
        if( actor.inputPortList().size() <= 0) {
            return false;
        } else {
            return true;
        }
    }


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

                // NamedObj container = (NamedObj)object.getContainer();

                TypedCompositeActor model = (TypedCompositeActor)
                    GiottoCodeGenerator.this.getContainer();

                // Preinitialize and resolve types.
                CompositeActor toplevel = (CompositeActor)model.toplevel();
                Manager manager = toplevel.getManager();
                if (manager == null) {
                    manager = new Manager(
                            toplevel.workspace(), "manager");
                    toplevel.setManager(manager);
                }

                manager.preinitializeAndResolveTypes();

                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(),
                        generateCode(model));
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
