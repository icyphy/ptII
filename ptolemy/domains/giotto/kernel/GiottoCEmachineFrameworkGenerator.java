/* An attribute that generates the framework code needed by the emachine
This involves creation of the input and output driver files to take care
of copying data between task ports.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.giotto.kernel;

// Ptolemy imports.
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

//////////////////////////////////////////////////////////////////////////
//// GiottoCEmachineFrameworkGenerator

/* The Giotto Code Generator has been changed from the earlier generator
 * implemented by Haiyang and Steve in the following respects :-
 * 
 * 1. _getTypeString has been modified to actually return the tye
 *    instead of Token_Port. Also, the type returned has been 
 *    modified to fit the giotto compiler restrictions, namely the 
 *    exclusion of the data_type * format. Instead, a new data type
 *    is created and used in the giotto code, and defined in f_code.h
 * 
 *  2. Any and all unconnected ports are ignored. This includes :
 *   a. Removal of its mention in the output drivers
 *   b. Removal of its mention in task (...) output (...)
 *   c. Removal of driver code for tasks without inputs
 */
/**
   This attribute is a visible attribute that when configured (by double
   clicking on it or by invoking Configure in the context menu) it generates
   Giotto code and displays it a text editor.  It is up to the user to save
   the Giotto code in an appropriate file, if necessary.

   @author Edward A. Lee, Vinay Krishnan
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (vkris)
*/

public class GiottoCEmachineFrameworkGenerator extends Attribute {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoCEmachineFrameworkGenerator(NamedObj container, String name)
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
    ////             Giotto Code Generation Functions              ////
    ///////////////////////////////////////////////////////////////////

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
    private static String _getTypeString(TypedIOPort port) {
        String type = port.getType().toString();
        StringBuffer retval;
        retval = new StringBuffer();
        switch(type.charAt(0)) {
            case '{':
             retval.append(type.substring(1,type.length()-1) + "array");
             break;
             default:
             retval.append(type);
             break;
        }
        return retval.toString();//"Token_port";
    }

    /** Return the correct Giotto initial value string for the given port.
     */
    private static String _getInitialValueString(TypedIOPort port)
        throws IllegalActionException {
        String retVal = "";
        Parameter initVal = (Parameter)port.getAttribute("_init");
        if(initVal != null) {
            retVal = initVal.getToken().toString();
        }
        return retVal;
    }

    /** Return the value of the parameter "_length" for the given port.
     */
    private static String _getArrayLength(TypedIOPort port)
        throws IllegalActionException {
        String retVal = "";
        Parameter initVal = (Parameter)port.getAttribute("_length");
        if(initVal != null) {
            retVal = initVal.getToken().toString();
        }
        else {
            retVal = "1"; // If no length parameter is given, assume an array length of 1
        }
        return retVal;
    }

    /** Topology analysis and initialization.
     *
     * @ return True if in giotto domain, False if in other domains.
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
            if (port.getWidthInside() > 0) {
                // FIXME: Assuming ports are either
                // input or output and not both.
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
            if (port.getWidthInside() > 0) {
                // FIXME: Assuming ports are either
                // input or output and not both.
                // FIXME: May want the driver name
                // specified by a port parameter.
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
                // Ignore unconnected ports
                if (port.getWidth()>0) {
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
            TypedIOPort port = (TypedIOPort)inPorts.next();
            // Ignore unconnected ports
            if (port.getWidth()>0) {
                if (first) {
                    first = false;
                } else {
                    inputPorts += ",";
                    codeString += ",";
                }
                String portID = StringUtilities.sanitizeName(
                        port.getName(model));
                String portTypeID = _getTypeString(port);
    
                codeString += portTypeID + " " + portID;
                inputPorts += portID;
            }
        }

        codeString += ")" + _endLine;
        codeString +=  "        output (";

        // write the output port specification of the task.
        first = true;
        String outputPorts = "";
        for (Iterator outPorts = actor.outputPortList().iterator();
             outPorts.hasNext();) {
             TypedIOPort port = (TypedIOPort)outPorts.next();
             // Ignore unconnected ports
             if (port.getWidth()>0) {
                if (first) {
                    first = false;
                } else {
                    codeString += ",";
                    outputPorts += ",";
                }
                String portID = StringUtilities.sanitizeName(
                        port.getName(model));
                codeString += portID;
                outputPorts += portID;
             }
        }
        codeString += ")" + _endLine;
        codeString +=  "        state ("
            + stateParas
            + ")"
            + _endLine;
        codeString +=  "{"
            + _endLine;
        String portSeparator = ",";
        if (inputPorts.equals("") || outputPorts.equals("")) {
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
        if (!_needsInputDriver(actor)) {
            return "";
        }

        String codeString = "";

        String driverParas;
        String actorName;

        driverParas = "";
        actorName = "";

        actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());

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

        codeString += "driver "
            + actorName
            + "_driver (";

        codeString += driverParas
            + ")"
            + _endLine;
        codeString +=  "        output (";
        // Write the input port specification of the task
        boolean first = true;
        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
           TypedIOPort port = (TypedIOPort)inPorts.next();
           // Ignore unconnected ports
           if (port.getWidth()>0) {
               if (first) {
                    first = false;
                } else {
                    codeString += ",";
                }
                String portID = StringUtilities.sanitizeName(
                        port.getName(model));
                String portTypeID = _getTypeString(port);
                codeString += portTypeID + " " + portID;
           }
        }
        codeString += ")" + _endLine;
        codeString +=  "{"
            + _endLine;
        codeString +=
            "          if constant_true() then " + actorName + "_inputdriver( ";
   
            first = true;
            for (Iterator sourceNames = driverIOMap.keySet().iterator();
                 sourceNames.hasNext();) {
                if(first) {
                    first = false;
                }
                else {
                    codeString += ", ";
            }
            String sourceName = (String) sourceNames.next();
            String destName = (String) driverIOMap.get(sourceName);
            codeString += sourceName + ", " + destName;
        }
        codeString += ")"
        + _endLine
        +  "}"
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
            // Ignore unconnected ports
            if (port.getWidth()>0) {
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
            // Ignore unconnected ports
            if (port.getWidth()>0) {
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
            if (_needsInputDriver(actor)) {
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

    /** Return true if the given actor has at least one conneted input port, which
     *  requires it to have an input driver.
     */
    private static boolean _needsInputDriver(Actor actor) {
        boolean retVal = false;
        Iterator inPorts = actor.inputPortList().iterator();
        while (inPorts.hasNext() && !retVal) {
            TypedIOPort port = (TypedIOPort)inPorts.next();
            if (port.getWidth()>0) {
                retVal = true;
            }
        }
        return retVal;
    }





    ///////////////////////////////////////////////////////////////////
    ////            Framework Code Generation Functions            ////
    ///////////////////////////////////////////////////////////////////





    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the Framework code. Presently this functionality
     * is hard coded to generate the files in $PTII/domains/giotto/kernel
     * It creates the directory c_functionality/fcode with the files
     * f_code.c and f_code.h in it.
     */
    public static void writeFrameworkCode(TypedCompositeActor model)
                    throws IllegalActionException, NameDuplicationException {

        dataTypes = new HashSet(); // Creating a set of all the unique types used
        
        FHfuncDeclString = "";  // Contains the declaration of functions defined in the C file
        FCoutDriversImplString = ""; // Contains the code for the initialization of output drivers
        FCinDriversImplString = ""; // Contains functions to implement the input drivers
        THfuncDeclString = ""; // Contains the declaration of the task functions
        TCfuncImplString = ""; // Contains the skeleton code for the task functions

        _generateCodeStrings(model);

        // TODO: Get the ptolemy installation directory
        String directoryName = "ptolemy/domains/giotto/kernel/"+StringUtilities.sanitizeName(model.getName())+"/c_functionality/fcode/";
        File directory;
            
        File outDirFile = new File(directoryName);
        if (!outDirFile.isDirectory()) {
            outDirFile.mkdirs();
        }

        File writeFCFile = new File(directoryName, "f_code.c");
        File writeFHFile = new File(directoryName, "f_code.h");
        File writeTCFile = new File(directoryName, "task_code.c");
        File writeTHFile = new File(directoryName, "task_code.h");
        try {
            FileWriter FCwriter = new FileWriter(writeFCFile);
            FCwriter.write(_generateFrameworkImplementationCode(model));
            FCwriter.close();

            FileWriter FHwriter = new FileWriter(writeFHFile);
            FHwriter.write(_generateFrameworkHeaderCode(model));
            FHwriter.close();
            
            FileWriter TCwriter = new FileWriter(writeTCFile);
            TCwriter.write(_generateTaskImplementationCode(model));
            TCwriter.close();

            FileWriter THwriter = new FileWriter(writeTHFile);
            THwriter.write(_generateTaskHeaderCode(model));
            THwriter.close();
        } catch (IOException e) {
            throw new IllegalActionException(model, e,
                    "Failed to open file for writing.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** Generate Framework Implementation C code for the given model.
     *  @return The Framework Implementation C code.
     */
    /** Generate code for the C file f_code.c.
     *  This function generates function implementation for a couple of
     * legacy emachine implementations, and the output driver initialization
     * code as well as the input driver code.
     *  @return The output code.
     */
    private static String _generateFrameworkImplementationCode(TypedCompositeActor model)
        throws IllegalActionException {
        String codeString = "";
    
        codeString += copyrightString;
        codeString += "/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */" + _endLine;
        codeString += "\n#include \"f_code.h\"" + _endLine;
        codeString += "#include <stdlib.h>" + _endLine;
        codeString += "\n// Legacy Emachine code" + _endLine;
        codeString += "void giotto_timer_enable_code(e_machine_type e_machine_func, int relative_time) {" + _endLine;
        codeString += "}" + _endLine;
        codeString += "\nint giotto_timer_save_code(void) {" + _endLine;
        codeString += _tabChar + "return get_logical_time();" + _endLine;
        codeString += "}" + _endLine;
        codeString += "\nunsigned giotto_timer_trigger_code(int initial_time, int relative_time) {" + _endLine;
        codeString += _tabChar + "return (get_logical_time() == (initial_time + relative_time) % get_logical_time_overflow());" + _endLine;
        codeString += "}" + _endLine;
        codeString += "\ninline unsigned constant_true( void ) {" + _endLine;
        codeString += _tabChar + "return ( (unsigned)1 );" + _endLine;
        codeString += "}" + _endLine + _endLine;
        
        codeString += FCoutDriversImplString;
        codeString += FCinDriversImplString;
        
    return codeString;
    }
    
    /** Generate code for the H file f_code.h.
     *  This function generates the function and variable declarations for
     *  the implementation in f_code.c
     *  @return The output code.
     */
    private static String _generateFrameworkHeaderCode(TypedCompositeActor model)
        throws IllegalActionException {
        String codeString = "";
        
        codeString += copyrightString;
        codeString += "/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */" + _endLine;
        codeString +=  _endLine;
        
        // Writing code to prevent multiple inclusion of the file
        codeString += "#ifndef _F_CODE_" + _endLine;
        codeString += "#define _F_CODE_" + _endLine;
        codeString +=  _endLine;
        codeString += "// Header file Inclusions" + _endLine;
        codeString += "#include \"f_table.h\"" + _endLine;
        codeString += "#include \"f_spec.h\"" + _endLine;
        codeString += "#include \"f_interface.h\"" + _endLine;
        codeString +=  _endLine;
        codeString += "// Task Frequency Definitions" + _endLine;
        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
        String actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());
        int actorFreq = 0;

            Parameter actorFreqPara = (Parameter)
                ((NamedObj) actor).getAttribute("frequency");
            if (actorFreqPara == null) {
                actorFreq = 1;
            } else {
                actorFreq = ((IntToken) actorFreqPara.
                        getToken()).intValue();
            }
    
            codeString += "#define " + actorName + "_FREQ" + _tabChar + _tabChar + "(" + actorFreq + ")" + _endLine;
        }
        codeString += _endLine;
            
        codeString += "// Datatype Declarations" + _endLine;
        Iterator dataType = dataTypes.iterator();
        while(dataType.hasNext()) {
            String type = (String)dataType.next();
            if (type.endsWith("array")) {
            codeString += "typedef " + type.substring(0, type.length()-5 /* Length of "array" */) + " *"
                        + type + ";" + _endLine + _endLine;
            }
        }
        codeString += "typedef unsigned char boolean;" + _endLine + _endLine;
        
        codeString += "// Legacy Emachine function declarations" + _endLine;
        codeString += "void giotto_timer_enable_code(e_machine_type, int);" +  _endLine;
        codeString += _endLine;
        codeString += "int giotto_timer_save_code(void);" +  _endLine;
        codeString += _endLine;
        codeString += "unsigned giotto_timer_trigger_code(int, int);" +  _endLine;
        codeString += _endLine;
        codeString += "inline unsigned constant_true( void );" +  _endLine;
        codeString += _endLine;
        codeString += FHfuncDeclString;
        codeString += _endLine;
        codeString += "#endif" +  _endLine;
        
        return codeString;
    }
    
    /** Generate the Task Implementation C code for the given model.
     *  @return The Task Implementation C code.
     */
    /** Generate code for the C file task_code.c.
     *  This function generates function implementation for a couple of
     * legacy emachine implementations, and the output driver initialization
     * code as well as the input driver code.
     *  @return The output code.
     */
    private static String _generateTaskImplementationCode(TypedCompositeActor model)
        throws IllegalActionException {
        String codeString = "";
        
        codeString += copyrightString;
        codeString += "/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */" + _endLine;
        codeString += "\n#include \"task_code.h\"" + _endLine + _endLine;
        codeString += "// Function called upon Termination" + _endLine;
        codeString += "void func_shutdown() {" + _endLine;
        codeString += "}" + _endLine + _endLine;
        codeString += "// Task Code" + _endLine;
        codeString += TCfuncImplString;

        return codeString;
    }
    
    /** Generate code for the H file task_code.h.
     *  This function generates the function and variable declarations for
     *  the implementation in f_code.c
     *  @return The output code.
     */
    private static String _generateTaskHeaderCode(TypedCompositeActor model)
        throws IllegalActionException {
        String codeString = "";
            
        codeString += copyrightString;
        codeString += "/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */" + _endLine;
        codeString +=  _endLine;
            
        // Writing code to prevent multiple inclusion of the file
        codeString += "#ifndef _TASK_CODE_" + _endLine;
        codeString += "#define _TASK_CODE_" + _endLine;
        codeString +=  _endLine;
        codeString += "// Header file Inclusions" + _endLine;
        codeString += "#include \"f_code.h\"" + _endLine;
        codeString +=  _endLine;
        codeString += THfuncDeclString;
        codeString += "#endif" +  _endLine;
        
        return codeString;
    }
    
    /** Generate the various code strings for the framework C & Header code,
     *  as well as the strings for the task C and Header code.
     */
    private static void _generateCodeStrings(TypedCompositeActor model)
        throws IllegalActionException {
            
            _outputInitializationCode(model);
            _driversImplementationCode(model);
            _taskCodeSkeleton(model);
    }
        
    /** Generate Initialization code for the output drivers.
     *  @return The initialization code.
     */
    /** Generate code for the output ports.
     *  In Giotto, the situation that one port has several inputs
     *  is illegal. From the output ports, it is easy to trace
     *  to get receivers for output delivery.
     *  @return The output code.
     */
    private static void _outputInitializationCode(TypedCompositeActor model)
            throws IllegalActionException {

                FCoutDriversImplString += "// Output Drivers Initialization Code" + _endLine + _endLine;


        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            TypedActor actor = (TypedActor)actors.next();
            Iterator outPorts = actor.outputPortList().iterator();
            while (outPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort)outPorts.next();
                // Ignore unconnected ports
                if (port.getWidth()>0) {
                    String portID = StringUtilities.sanitizeName(
                            port.getName(model));
                    String portTypeID = _getTypeString(port);
                    dataTypes.add(portTypeID);

                    String portInitialValueFunction = "CGinit_" + portID;
                    String portInitialValue = _getInitialValueString(port);
                    _checkGiottoID(portID);
                    FCoutDriversImplString += "inline void"
                        + " "
                        + portInitialValueFunction
                        + "(" + portTypeID + " *p" + portID + ") {" + _endLine
                        + _tabChar + "*p" + portID + " = " + portInitialValue + ";" + _endLine
                        + "}" + _endLine + _endLine;

                    FHfuncDeclString += "inline void"
                    + " "
                    + portInitialValueFunction
                    + "(" + portTypeID + " *p" + portID + ");"
                    + _endLine + _endLine;
                }
            }
        }
    }

    /** Generate implementation code for the drivers.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     */
    private static void _driversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {

        _outputDriversImplementationCode(model);
        _inputDriversImplementationCode(model);
    }
    
    /** Generate code which will copy the output local
     *  data to the global data. This has to generate
     *  one function for each data type present.
     * @param model
     * @return The generated copy functions
     * @throws IllegalActionException
     */
    private static void _outputDriversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
                
        FCoutDriversImplString += "// Output drivers to copy values from the local to the global stage"
                    + _endLine;
        Iterator dataType = dataTypes.iterator();
        while(dataType.hasNext()) {
            String type = (String)dataType.next();
            FCoutDriversImplString += "inline void"
                        + " copy_"
                        + type
                        + " ("
                        + type + " *src, "
                        + type + " *dst) {"
                        + _endLine
                        + _tabChar + "*dst = *src;"
                        + _endLine + "}"
                        + _endLine + _endLine;

            FHfuncDeclString += "inline void"
                        + " copy_"
                        + type
                        + " ("
                        + type + " *src, "
                        + type + " *dst);"
                        + _endLine + _endLine;

        }
    }

    private static void _inputDriversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
             
        String varDeclString = "";
        String arrayInitString = ""; // Contains the initial assignment of the input driver variables to the 
                                     // statically allocated array variables
        String assgtStmtString = "";
        Actor actor;
        String actorName;

        FCinDriversImplString += "// Input Drivers for all the Tasks requiring one" + _endLine
                   + "// These functions make an assumption in the case of arrays" + _endLine
                   + "// - that the output array has been allocated already and" + _endLine
                   + "//   that there are valid values present in the allocated memory" + _endLine
                   + "// - The input array is allocated by the driver. Therefore the" + _endLine
                   + "//   corresponding task should assume the memory present." + _endLine + _endLine;
                   
        // generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            actor = (Actor) actors.next();
            if (!_needsInputDriver(actor)) {
                continue;
            }

            actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());
            
            FCinDriversImplString += "inline void " + actorName + "_inputdriver( ";
            FHfuncDeclString      += "inline void " + actorName + "_inputdriver( ";

	    varDeclString = _tabChar + "// Counter to optimize the copying to execute only when required" + _endLine;
	    varDeclString += _tabChar + "static int copy_counter = 0;" + _endLine;
            arrayInitString = "";
	    assgtStmtString = _tabChar + "if (copy_counter > 0) {" + _endLine;

            Map driverIOMap = new LinkedHashMap();
            boolean firstParameter = true;
	    boolean firstArray = true;

            for (Iterator inPorts = actor.inputPortList().iterator();
                 inPorts.hasNext();) {
                TypedIOPort inPort = (TypedIOPort) inPorts.next();
                String sanitizedPortName =
                    StringUtilities.sanitizeName(
                            inPort.getName(model));
                String inPortType = _getTypeString(inPort);

                List sourcePortList = inPort.sourcePortList();
                if (sourcePortList.size() > 1) {
                    throw new IllegalActionException(inPort, "Input port " +
                            "cannot receive data from multiple sources in Giotto.");
                }
                TypedIOPort port = (TypedIOPort)inPort.sourcePortList().get(0);
                String sanitizedPortName2 = StringUtilities.sanitizeName(
                        port.getName(model));
                String portType = _getTypeString(port);
                if (firstParameter) {
                    firstParameter = false;
                }
                else {
                    FCinDriversImplString += ", ";
                    FHfuncDeclString += ", ";
                }
                FCinDriversImplString += portType + " *" + sanitizedPortName2
                           + ", " + _getTypeString(inPort) + " *" + sanitizedPortName;
                FHfuncDeclString += portType + " *" + sanitizedPortName2
                           + ", " + _getTypeString(inPort) + " *" + sanitizedPortName;
                
		// Allocate memory for the arrays
		if (portType.endsWith("array")) {
		    if (firstArray) { // First time an array has been found
			firstArray = false;
			varDeclString += _tabChar + "int i;" + _endLine;
		    }
		    String arrayLength = _getArrayLength(port);
		    varDeclString += _tabChar + "static "
			                 + portType.substring(0, portType.length()-5/* Length of "array" */)
			                 + " array" + sanitizedPortName2
			                 + "[" + arrayLength + "]" + ";" + _endLine;
                    
		    arrayInitString += _tabChar + "*" + sanitizedPortName + " = "
			                 + "array" + sanitizedPortName2 + ";" + _endLine;
		    
		    String sourceActorName = 
			StringUtilities.sanitizeName(
				   port.getContainer().getName());
		    assgtStmtString += _tabChar + _tabChar + "if (!(copy_counter % "
			             + "(" + actorName + "_FREQ/" + sourceActorName + "_FREQ) )) {" + _endLine
			             + _tabChar + _tabChar + _tabChar + "for( i=0 ; i<" + arrayLength + " ; i++ ) {" + _endLine
			             + _tabChar + _tabChar + _tabChar + _tabChar + "(*" + sanitizedPortName + ")[i] = (*" + sanitizedPortName2 + ")[i];" + _endLine
			             + _tabChar + _tabChar + _tabChar + "}" + _endLine
			             + _tabChar + _tabChar + "}" + _endLine;
		}
		else {
		    assgtStmtString += _tabChar + _tabChar + "*" + sanitizedPortName
			                 + " = *" + sanitizedPortName2 + ";" + _endLine;
		}
            }
	    assgtStmtString += _tabChar + "}" + _endLine
		                  + _tabChar + "copy_counter = (copy_counter % " + actorName + "_FREQ) + 1;" + _endLine;

        FCinDriversImplString += ") {" + _endLine;
            FHfuncDeclString += ");" + _endLine + _endLine;
            FCinDriversImplString += varDeclString + _endLine // Statically allocate space for all the arrays used
                        + arrayInitString + _endLine // Include static variable allocations
                        + assgtStmtString; // Copy values from the global section to the input of the task
            FCinDriversImplString += "}" + _endLine;
        }
        // TODO : Generate driver code for the actuators.
    }
    
    /** Generate code for the task.
     *  @return The task code.
     */
    private static void _taskCodeSkeleton(TypedCompositeActor model)
            throws IllegalActionException {
        boolean first;

        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            TypedActor actor = (TypedActor)actors.next();
            String taskName = StringUtilities.sanitizeName(
                    ((NamedObj)actor).getName());
    
            // Write the input port specification of the task
            first = true;
            String inputPorts = "";
            for (Iterator inPorts = actor.inputPortList().iterator();
                 inPorts.hasNext();) {
                TypedIOPort port = (TypedIOPort)inPorts.next();
                // Ignore unconnected ports
                if (port.getWidth()>0) {
                    if (first) {
                        first = false;
                    } else {
                        inputPorts += ",";
                    }
                    String portID = StringUtilities.sanitizeName(
                            port.getName(model));
                    String portTypeID = _getTypeString(port);
        
                    inputPorts += portID;
                }
            }
    
            // write the output port specification of the task.
            first = true;
            String outputPorts = "";
            for (Iterator outPorts = actor.outputPortList().iterator();
                 outPorts.hasNext();) {
                 TypedIOPort port = (TypedIOPort)outPorts.next();
                 // Ignore unconnected ports
                 if (port.getWidth()>0) {
                    if (first) {
                        first = false;
                    } else {
                        outputPorts += ",";
                    }
                    String portID = StringUtilities.sanitizeName(
                            port.getName(model));
                    outputPorts += portID;
                 }
            }
            String portSeparator = ",";
            if (inputPorts.equals("") || outputPorts.equals("")) {
                portSeparator = "";
            }
            TCfuncImplString +=  "void CG"
                + taskName
                + "_Task(" + inputPorts + portSeparator + outputPorts + ") {"
                + _endLine;
            TCfuncImplString +=  "}"
                + _endLine + _endLine;

            THfuncDeclString +=  "void CG"
                + taskName
                + "_Task(" + inputPorts + portSeparator + outputPorts + ");"
                + _endLine + _endLine;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private static Set dataTypes;
    private static String _endLine = "\n";
    private static String _tabChar = "\t";
    private static String FHfuncDeclString;  // Contains the declaration of the driver functions
    private static String FCoutDriversImplString; // Contains the code for the initialization of output drivers
    private static String FCinDriversImplString; // Contains functions to implement the input drivers
    private static String THfuncDeclString; // Contains the declaration of the task functions
    private static String TCfuncImplString; // Contains the skeleton code for the task functions

    private static String copyrightString = "/*" + _endLine + _endLine+
" Copyright (c) 2001 The Regents of the University of California." + _endLine +
" All rights reserved." + _endLine +
" Permission is hereby granted, without written agreement and without" + _endLine +
" license or royalty fees, to use, copy, modify, and distribute this" + _endLine +
" software and its documentation for any purpose, provided that the above" + _endLine +
" copyright notice and the following two paragraphs appear in all copies" + _endLine +
" of this software." + _endLine +
"" + _endLine +
" IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY" + _endLine +
" FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES" + _endLine +
" ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF" + _endLine +
" THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF" + _endLine +
" SUCH DAMAGE." + _endLine +
"" + _endLine +
" THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES," + _endLine +
" INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF" + _endLine +
" MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE" + _endLine +
" PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF" + _endLine +
" CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES," + _endLine +
" ENHANCEMENTS, OR MODIFICATIONS." + _endLine +
"" + _endLine +
"*/" + _endLine;
 
    
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
                    GiottoCEmachineFrameworkGenerator.this.getContainer();

                // Preinitialize and resolve types.
                CompositeActor toplevel = (CompositeActor)model.toplevel();
                Manager manager = toplevel.getManager();
                if (manager == null) {
                    manager = new Manager(
                            toplevel.workspace(), "manager");
                    toplevel.setManager(manager);
                }

                manager.preinitializeAndResolveTypes();
                
                // Generate the Framework code and write it into the
                // corresponding files.
                writeFrameworkCode(model);

                // Generate the Giotto Code
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
