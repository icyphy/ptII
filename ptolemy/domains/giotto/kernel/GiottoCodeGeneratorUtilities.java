/* Giotto Code Generator Utilties

Copyright (c) 2004-2005 The Regents of the University of California.
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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;


//////////////////////////////////////////////////////////////////////////
//// GiottoCodeGeneratorUtilties

/**
   Utilities for generating Giotto code.

   <p>The Giotto Code Generator has been changed from the earlier generator
   implemented by Haiyang and Steve in the following respect :-

   <p>Any and all unconnected ports are ignored. This includes :
   <ol>
   <li> Removal of its mention in the output drivers
   <li> Removal of its mention in task (...) output (...)
   <li> Removal of driver code for tasks without inputs
   </ol>

   <p>This class is separate from GiottoCodeGenerator so that we can
   easily generate Giotto code without using a UI.

   @author Edward A. Lee, Steve Neuendorffer, Haiyang Zheng, Christopher Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/
public class GiottoCodeGeneratorUtilities {
    /** Instances of this class cannot be created.
     */
    private GiottoCodeGeneratorUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception if the given string is a valid giotto
     *  reserved word, which prevents it from being used as an identifier.
     *  @param string A string to be used in Giotto program.
     *  @exception IllegalActionException If the string can not be used.
     */
    public static void checkGiottoID(String string)
            throws IllegalActionException {
        if (string.equals("output")) {
            throw new IllegalActionException("The identifier " + string
                    + " cannot be used in a Giotto program.  "
                    + "Please change your model and attempt to "
                    + "generate code again.");
        }
    }

    /** Generate Giotto code for the given Giotto model.
     *  @param model The given Giotto model.
     *  @return The Giotto code.
     *  @exception IllegalActionException If code can not be generated.
     */
    public static String generateGiottoCode(TypedCompositeActor model)
            throws IllegalActionException {
        String generatedCode = "";

        try {
            if (_initialize(model)) {
                String containerName = model.getName();

                generatedCode = _headerCode(model) + _sensorCode(model)
                    + _actuatorCode(model) + _outputCode(model)
                    + _tasksCode(model) + _driversCode(model)
                    + "\n///////////////////////////"
                    + "///////////////////////////\n"
                    + "////                    modes"
                    + "                     ////\n\n" + "start "
                    + containerName + " {\n" + _modeCode(model) + "}\n";
            }

            model.wrapup();
        } catch (Throwable throwable) {
            System.out.println(throwable.getMessage());
            throw new IllegalActionException(model, throwable,
                    "Failed to generate Giotto code.");
        }

        return generatedCode;
    }

    /** Create an instance of a model and generate Giotto code for it
     *  The Giotto code is printed on standard out.
     *  @param args The command-line arguments naming the .xml or
     *  .moml file to run
     *  @exception Throwable If there is a problem reading the model
     *  or generating code.
     */
    public static void main(String[] args) throws Throwable {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException(
                        "Usage: java -classpath $PTII "
                        + "ptolemy.domains.giotto.kernel"
                        + ".GiottoCodeGeneratorUtilities ptolemyModel.xml\n"
                        + "The model is read in and Giotto code is "
                        + "generated on stdout.");
            }

            MoMLParser parser = new MoMLParser();

            // We set the list of MoMLFilters to handle Backward Compatibility.
            parser.setMoMLFilters(BackwardCompatibility.allFilters());

            // Filter out any graphical classes.
            parser.addMoMLFilter(new RemoveGraphicalClasses());

            // If there is a MoML error, then throw the exception as opposed
            // to skipping the error.  If we call StreamErrorHandler instead,
            // then the nightly build may fail to report MoML parse errors
            // as failed tests
            //parser.setErrorHandler(new StreamErrorHandler());
            // We use parse(URL, URL) here instead of parseFile(String)
            // because parseFile() works best on relative pathnames and
            // has problems finding resources like files specified in
            // parameters if the xml file was specified as an absolute path.
            TypedCompositeActor toplevel = (TypedCompositeActor) parser.parse(null,
                    new File(args[0]).toURL());

            System.out.println(generateGiottoCode(toplevel));
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    /** Return true if the given actor has at least one connected
     *  input port, which requires it to have an input driver.
     *  @param actor The actor to test.
     *  @return True if the given actor has at least on connected input port.
     */
    public static boolean needsInputDriver(Actor actor) {
        boolean retVal = false;
        Iterator inPorts = actor.inputPortList().iterator();

        while (inPorts.hasNext() && !retVal) {
            TypedIOPort port = (TypedIOPort) inPorts.next();

            if (port.getWidth() > 0) {
                retVal = true;
            }
        }

        return retVal;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for the actuator.
     *  Usually, there is only one actuator.
     *  @param model The model.
     *  @return The actuator code.
     *  @exception IllegalActionException If there is a problem accessing
     *  the ports.
     */
    protected static String _actuatorCode(TypedCompositeActor model)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    actuators                 ////\n\n"
                + "actuator\n");

        Iterator outPorts = model.outputPortList().iterator();

        while (outPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort) outPorts.next();

            // Ignore unconnected ports
            if (port.getWidthInside() > 0) {
                // FIXME: Assuming ports are either
                // input or output and not both.
                // FIXME: May want the driver name
                // specified by a port parameter.
                // FIXME: Use a real type.
                String portID = port.getName();
                String portTypeID = _getTypeString(port);
                String actuatorDriverName = port.getName() + "_device_driver";
                checkGiottoID(portID);
                code.append("  " + portTypeID + " " + portID + " uses "
                        + actuatorDriverName + ";\n");
            }
        }

        return code.toString();
    }

    /** Generate code for the driver.
     *  @param model The given model.
     *  @param actor The given actor.
     *  @return The driver code.
     *  @exception IllegalActionException If there is a problem accessing
     *  the ports.
     */
    protected static String _driverCode(TypedCompositeActor model, Actor actor)
            throws IllegalActionException {
        if (!needsInputDriver(actor)) {
            return "";
        }

        String driverParas = "";
        String actorName = StringUtilities.sanitizeName(((NamedObj) actor)
                .getName());

        StringBuffer code = new StringBuffer("\ndriver " + actorName
                + "_driver (");

        Map driverIOMap = new LinkedHashMap();

        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            IOPort inPort = (IOPort) inPorts.next();
            String sanitizedPortName = StringUtilities.sanitizeName(inPort
                    .getName(model));
            List sourcePortList = inPort.sourcePortList();

            if (sourcePortList.size() > 1) {
                throw new IllegalActionException(inPort,
                        "Input port cannot "
                        + "receive data from multiple sources in Giotto.");
            }

            Iterator sourcePorts = inPort.sourcePortList().iterator();

            while (sourcePorts.hasNext()) {
                IOPort port = (IOPort) sourcePorts.next();
                String sanitizedPortName2 = StringUtilities.sanitizeName(port
                        .getName(model));

                if (driverParas.length() == 0) {
                    driverParas += sanitizedPortName2;
                } else {
                    driverParas += (", " + sanitizedPortName2);
                }

                driverIOMap.put(sanitizedPortName2, sanitizedPortName);
            }
        }

        code.append(driverParas + ")\n" + "        output (");

        // Write the input port specification of the task
        boolean first = true;

        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            TypedIOPort port = (TypedIOPort) inPorts.next();

            // Ignore unconnected ports
            if (port.getWidth() > 0) {
                if (first) {
                    first = false;
                } else {
                    code.append(", ");
                }

                String portID = StringUtilities.sanitizeName(port.getName(model));
                String portTypeID = _getTypeString(port);
                code.append(portTypeID + " " + portID);
            }
        }

        code.append(")\n" + "{\n" + "          if constant_true() then "
                + actorName + "_inputdriver( ");

        first = true;

        for (Iterator sourceNames = driverIOMap.keySet().iterator();
             sourceNames.hasNext();) {
            if (first) {
                first = false;
            } else {
                code.append(", ");
            }

            String sourceName = (String) sourceNames.next();
            String destName = (String) driverIOMap.get(sourceName);
            code.append(sourceName + ", " + destName);
        }

        code.append(")\n" + "}\n");
        return code.toString();
    }

    /** Generate code for the drivers.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @param model The model.
     *  @return The drivers code.
     *  @exception IllegalActionException If there is a problem accessing
     *  the ports.
     */
    protected static String _driversCode(TypedCompositeActor model)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    drivers for common actors ////\n");

        Actor actor;

        // Generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            actor = (Actor) actors.next();
            code.append(_driverCode(model, actor));
        }

        code.append(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    output drivers            ////\n\n");

        // Generate driver functions for toplevel output ports.
        // FIXME: the giotto director should do some checking to
        // avoid several outputs of actors connect to the same output port?
        for (Iterator outPorts = model.outputPortList().iterator();
             outPorts.hasNext();) {
            String driverParas = "";
            TypedIOPort port = (TypedIOPort) outPorts.next();

            // Ignore unconnected ports
            if (port.getWidth() > 0) {
                String portTypeID = _getTypeString(port);
                String portID = StringUtilities.sanitizeName(port.getName());
                code.append("\ndriver " + portID + "_driver (");

                Iterator portConnected = port.insidePortList().iterator();

                while (portConnected.hasNext()) {
                    IOPort outPort = (IOPort) portConnected.next();
                    String sanitizedPortName = StringUtilities.sanitizeName(outPort
                            .getName(model));

                    if (outPort.isOutput()) {
                        if (driverParas.length() == 0) {
                            driverParas += sanitizedPortName;
                        } else {
                            driverParas += (", " + sanitizedPortName);
                        }
                    }
                }

                code.append(driverParas + ")\n" + "        output ("
                        + portTypeID + " " + portID + "_output)\n" + "{\n"
                        + "  if c_true() then " + portID + "_input_driver( "
                        + driverParas + ", " + portID + "_output)\n" + "}\n");
            }
        }

        return code.toString();
    }

    /** Return the correct Giotto type string for the given port.
     *  @param port An IO port.
     *  @return A string containing the type of the port.
     */
    protected static String _getTypeString(TypedIOPort port) {
        return "Token_port"; //ort.getType().toString();
    }

    /** Generate header code for the file.
     *  Usually, there is only one header.
     *  @param model The model.
     *  @return The header code.
     *  @exception IllegalActionException If there is a problem
     *  getting the model name.
     */
    protected static String _headerCode(TypedCompositeActor model)
            throws IllegalActionException {
        return "/* Giotto code for " + model.getName() + "\n"
            + "   Generated by Ptolemy II Giotto Code Generator.\n" + " */\n\n"
            + "//////////////////////////////////////////////////////\n" + "//// "
            + model.getName() + "\n" + "/**\n" + model.getName() + "\n"
            + "@author\n"
            + "@version $Id$\n"
            + "*/\n";
    }

    /** Initialize the code generation process by checking whether the
     *  given model is a Giotto model. Return false if it is not.
     *  @param model A model to generate Giotto code from.
     *  @return True if in the given model is a giotto model.
     */
    protected static boolean _initialize(TypedCompositeActor model) {
        Director director = model.getDirector();
        return (director instanceof GiottoDirector);
    }

    /** Generate code for the modes.
     *  @param model The model.
     *  @return The modes code.
     *  @exception IllegalActionException If there is a problem
     *  getting the director or accessing the ports.
     */
    protected static String _modeCode(TypedCompositeActor model)
            throws IllegalActionException {
        int actorFreq = 0;

        String outputName;
        String actorName;
        String modeName;

        modeName = StringUtilities.sanitizeName(model.getName());

        int periodValue = ((GiottoDirector) model.getDirector()).getIntPeriod();

        StringBuffer code = new StringBuffer(
                "\n    /////////////////////////////////////////////"
                + "/////////\n" + "    ////                   mode " + modeName
                + "\n" + "    mode " + modeName + " () period " + periodValue
                + " {\n");

        //FIXME: How to deal with several outputs of Giotto director?
        Iterator outPorts = model.outputPortList().iterator();

        while (outPorts.hasNext()) {
            outputName = "";

            TypedIOPort port = (TypedIOPort) outPorts.next();

            // Ignore unconnected ports
            if (port.getWidth() > 0) {
                outputName = StringUtilities.sanitizeName(port.getName(model));

                if (port.insidePortList().size() != 0) {
                    Iterator portConnected = port.insidePortList().iterator();

                    while (portConnected.hasNext()) {
                        TypedIOPort outPort = (TypedIOPort) portConnected.next();

                        if (!outPort.isOutput()) {
                            continue;
                        }

                        Nameable actor = outPort.getContainer();

                        if (actor instanceof Actor) {
                            Parameter actorFreqPara = (Parameter) ((NamedObj) actor)
                                .getAttribute("frequency");

                            if (actorFreqPara == null) {
                                actorFreq = 1;
                            } else {
                                actorFreq = ((IntToken) actorFreqPara.getToken())
                                    .intValue();
                            }
                        }

                        code.append("    actfreq " + actorFreq + " do "
                                + outputName + " (" + outputName + "_driver);\n");
                    }
                }
            }
        }

        // Generate mode code for each actor driver.
        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            TypedActor actor = (TypedActor) actors.next();
            actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName(
                                                             model));

            Parameter actorFreqPara = (Parameter) ((NamedObj) actor)
                .getAttribute("frequency");

            if (actorFreqPara == null) {
                actorFreq = 1;
            } else {
                actorFreq = ((IntToken) actorFreqPara.getToken()).intValue();
            }

            String driverName = "";

            if (needsInputDriver(actor)) {
                driverName = actorName + "_driver";
            }

            code.append("        taskfreq " + actorFreq + " do " + actorName
                    + "(" + driverName + ");\n");
        }

        code.append("    }\n");

        return code.toString();
    }

    /** Generate code for the output ports.
     *  In Giotto, the situation that one port has several inputs
     *  is illegal. From the output ports, it is easy to trace
     *  to get receivers for output delivery.
     *  @param model The model.
     *  @return The output code.
     *  @exception IllegalActionException If there is a problem
     *  accessing the ports.
     */
    protected static String _outputCode(TypedCompositeActor model)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    output ports              ////\n\n"
                + "output\n");

        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            TypedActor actor = (TypedActor) actors.next();
            Iterator outPorts = actor.outputPortList().iterator();

            while (outPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) outPorts.next();

                // Ignore unconnected ports
                if (port.getWidth() > 0) {
                    String portID = StringUtilities.sanitizeName(port.getName(
                                                                         model));
                    String portTypeID = _getTypeString(port);
                    String portInitialValue = "CGinit_" + portID;
                    checkGiottoID(portID);
                    code.append("  " + portTypeID + " " + portID + " := "
                            + portInitialValue + ";\n");
                }
            }
        }

        return code.toString();
    }

    /** Generate code for the sensors.
     *  @param model The model from which we generate code.
     *  @return The sensors code.
     *  @exception IllegalActionException If there is a problem iterating
     *  over the actors.
     */
    protected static String _sensorCode(TypedCompositeActor model)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    sensors                   ////\n\n"
                + "sensor\n");

        Iterator inPorts = model.inputPortList().iterator();

        while (inPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort) inPorts.next();

            // Ignore unconnected ports
            if (port.getWidthInside() > 0) {
                // FIXME: Assuming ports are either
                // input or output and not both.
                // FIXME: May want the driver name
                // specified by a port parameter.
                // FIXME: Use a real type.
                String portID = port.getName();
                String portTypeID = _getTypeString(port);
                String actuatorDriverName = port.getName() + "_device_driver";
                checkGiottoID(portID);
                code.append("  " + portTypeID + " " + portID + " uses "
                        + actuatorDriverName + ";\n");
            }
        }

        return code.toString();
    }

    /** Generate code for the task.
     *  @param model The model from which we generate code.
     *  @param actor The actor we are generating code for.
     *  @return The task code.
     *  @exception IllegalActionException If there is a problem iterating
     *  over the ports of the actor
     */
    protected static String _taskCode(TypedCompositeActor model, Actor actor)
            throws IllegalActionException {
        boolean first;
        String taskName = StringUtilities.sanitizeName(((NamedObj) actor)
                .getName());

        StringBuffer code = new StringBuffer("\n/** " + taskName + "\n"
                + " */\n" + "task " + taskName + " (");

        String stateParas = ""; //taskName.toUpperCase() +

        //  "_PARAM param := init_" + taskName + "_param";
        // Write the input port specification of the task
        first = true;

        String inputPorts = "";

        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            TypedIOPort port = (TypedIOPort) inPorts.next();

            // Ignore unconnected ports
            if (port.getWidth() > 0) {
                if (first) {
                    first = false;
                } else {
                    inputPorts += ", ";
                    code.append(", ");
                }

                String portID = StringUtilities.sanitizeName(port.getName(model));
                String portTypeID = _getTypeString(port);

                code.append(portTypeID + " " + portID);
                inputPorts += portID;
            }
        }

        code.append(")\n" + "        output (");

        // write the output port specification of the task.
        first = true;

        String outputPorts = "";

        for (Iterator outPorts = actor.outputPortList().iterator();
             outPorts.hasNext();) {
            TypedIOPort port = (TypedIOPort) outPorts.next();

            // Ignore unconnected ports
            if (port.getWidth() > 0) {
                if (first) {
                    first = false;
                } else {
                    code.append(", ");
                    outputPorts += ", ";
                }

                String portID = StringUtilities.sanitizeName(port.getName(model));
                code.append(portID);
                outputPorts += portID;
            }
        }

        code.append(")\n" + "        state (" + stateParas + ")\n" + "{\n");

        String portSeparator = ", ";

        if (inputPorts.equals("") || outputPorts.equals("")) {
            portSeparator = "";
        }

        code.append("        schedule CG" + taskName + "_Task(" + inputPorts
                + portSeparator + outputPorts + ")\n" + "}\n");

        return code.toString();
    }

    /** Generate code for the tasks.
     *  @param model The model from which we generate code.
     *  @return The task code.
     *  @exception IllegalActionException If there is a problem iterating
     *  over the actors.
     */
    protected static String _tasksCode(TypedCompositeActor model)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    tasks                     ////\n");
        Actor actor;

        // Generate task code for common actors.
        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            actor = (Actor) actors.next();
            code.append(_taskCode(model, actor));
        }

        return code.toString();
    }
}
