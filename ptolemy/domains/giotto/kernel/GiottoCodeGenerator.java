/* An attribute that manages generation of Giotto code.

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// GiottoCodeGenerator
/**
   This attribute is a visible attribute that when configured (by double
   clicking on it or by invoking Configure in the context menu) it generates
   Giotto code and displays it a text editor.  It is up to the user to save
   the Giotto code in an appropriate file, if necessary.

   <p>The Giotto Code Generator has been changed from the earlier generator
   implemented by Haiyang and Steve in the following respect :-

   <p>Any and all unconnected ports are ignored. This includes :
   <ol>
   <li> Removal of its mention in the output drivers
   <li> Removal of its mention in task (...) output (...)
   <li> Removal of driver code for tasks without inputs
   </ol>

   @author Edward A. Lee, Steve Neuendorffer, Haiyang Zheng, Christopher Brooks
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/
public class GiottoCodeGenerator extends Attribute {

    /** Construct a factory with the default workspace and "" as name.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoCodeGenerator()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

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
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Giotto code for the given Giotto model.
     *  @param model The given Giotto model.
     *  @return The Giotto code.
     *  @throws IllegalActionException If code can not be generated. 
     */
    public String generateGiottoCode(TypedCompositeActor model)
            throws IllegalActionException {
        String generatedCode = "";

        try {
            if (_initialize(model)) {
                String containerName = model.getName();
                
                generatedCode += _headerCode(model);
                generatedCode += _sensorCode(model);
                generatedCode += _actuatorCode(model);
                generatedCode += _outputCode(model);
                generatedCode += _tasksCode(model);
                generatedCode += _driversCode(model);
                
                generatedCode += 
                    "\n//////////////////////////////////////////////////////\n"
                    + "////                    modes                     ////\n\n";

                generatedCode += "start "
                    + containerName
                    + " {\n";
                
                generatedCode += _modeCode(model);
                
                generatedCode +=  "}\n";
            }
            model.wrapup();
        } catch (KernelException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalActionException(ex.getMessage());
        }

        return generatedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method to instantiate the Editor Factory class called from the
     *  constructor. The reason for having this is that it can be
     *  overridden by subclasses
     *  @throws IllegalActionException If the editor factory can not be 
     *  created. 
     *  @throws NameDuplicationException If there is already anothter editor 
     *  factory with the same name. 
     */
    protected void _instantiateEditorFactoryClass()
            throws IllegalActionException, NameDuplicationException {
        new GiottoEditorFactory(this, "_editorFactory");
    }
    
    /** Throw an exception if the given string is a valid giotto
     *  reserved word, which prevents it from being used as an identifier.
     *  @param string A string to be used in Giotto program.
     *  @throws IllegalActionException If the string can not be used. 
     */
    protected void _checkGiottoID(String string)
            throws IllegalActionException {
        if (string.equals("output")) {
            throw new IllegalActionException("The identifier " + string +
                    " cannot be used in a Giotto program.  " +
                    "Please change your model and attempt to " +
                    "generate code again.");
        }
    }

    /** Return the correct Giotto type string for the given port.
     *  @param port An IO port.
     *  @return A string containing the type of the port.
     */
    protected String _getTypeString(TypedIOPort port) {
        return "Token_port";//ort.getType().toString();
    }

    /** Initialize the code geenration process by checking whether the
     *  given model is a Giotto model. Return false if it is not. 
     *  @param model A model to generate Giotto code from.
     *  @return True if in the given model is a giotto model.
     */
    protected boolean _initialize(TypedCompositeActor model) {
        Director director = model.getDirector();
        return (director instanceof GiottoDirector);
    }

    /** Generate code for the sensors.
     *  @return The sensors code.
     */
    protected String _sensorCode(TypedCompositeActor model)
            throws IllegalActionException {

        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    sensors                   ////\n\n"
                + "sensor\n");

        Iterator inPorts = model.inputPortList().iterator();
        while (inPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort)inPorts.next();
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
                _checkGiottoID(portID);
            	code.append("  " + portTypeID + " " + portID
                	+ " uses " + actuatorDriverName	+ ";\n");
            }
        }

        return code.toString();

    }

    /** Generate code for the actuator.
     *  Usually, there is only one actuator.
     *  @return The actuator code.
     */
    protected String _actuatorCode(TypedCompositeActor model)
            throws IllegalActionException {

        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    actuators                 ////\n\n"
                + "actuator\n");

        Iterator outPorts = model.outputPortList().iterator();
        while (outPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort)outPorts.next();
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
            	_checkGiottoID(portID);
                code.append("  " + portTypeID + " " + portID
                        + " uses " + actuatorDriverName + ";\n");
            }
        }

        return code.toString();
    }

    /** Generate header code for the file.
     *  Usually, there is only one header.
     *  @return The header code.
     */
    protected String _headerCode(TypedCompositeActor model)
            throws IllegalActionException {
        return "/* Giotto code for " +  model.getName() + "\n"
            + "   Generated by Ptolemy II Giotto Code Generator.\n"
            + " */\n\n"
            + "//////////////////////////////////////////////////////\n"
            + "//// " + model.getName() + "\n"
            + "/**\n"
            + model.getName() + "\n"
            + "@author\n"
            + "@version $Id$\n"
            + "*/\n";
    }

    /** Generate code for the output ports.
     *  In Giotto, the situation that one port has several inputs
     *  is illegal. From the output ports, it is easy to trace
     *  to get receivers for output delivery.
     *  @return The output code.
     */
    protected String _outputCode(TypedCompositeActor model)
            throws IllegalActionException {

        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    output ports              ////\n\n"
                + "output\n");

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
            	    code.append("  " + portTypeID + " " + portID
                            + " := " + portInitialValue + ";\n");
                }
            }
        }

        return code.toString();
    }

    /** Generate code for the task.
     *  @return The task code.
     */
    protected String _taskCode(TypedCompositeActor model, Actor actor)
            throws IllegalActionException {
        boolean first;
        String taskName = StringUtilities.sanitizeName(
                ((NamedObj)actor).getName());

        StringBuffer code = new StringBuffer(
                "\n/** " + taskName + "\n"
                + " */\n"
                + "task " + taskName + " (");

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
                    inputPorts += ", ";
                    code.append(", ");
            	}
                String portID = StringUtilities.sanitizeName(
    	                port.getName(model));
                String portTypeID = _getTypeString(port);
	
                code.append(portTypeID + " " + portID);
                inputPorts += portID;
            }
        }

        code.append(")\n"
                + "        output (");

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
                    code.append(", ");
                    outputPorts += ", ";
            	}
    	        String portID = StringUtilities.sanitizeName(
                        port.getName(model));
            	code.append(portID);
                outputPorts += portID;
    	    }
        }
        code.append(")\n"
                + "        state (" + stateParas + ")\n"
                + "{\n");
        String portSeparator = ", ";
        if (inputPorts.equals("") || outputPorts.equals("")) {
            portSeparator = "";
        }
        code.append("        schedule CG" + taskName
                + "_Task(" + inputPorts + portSeparator + outputPorts + ")\n"
                + "}\n");

        return code.toString();
    }

    /** Generate code for the tasks.
     *  @return The task code.
     */
    protected String _tasksCode(TypedCompositeActor model)
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

    /** Generate code for the driver.
     *  @return The driver code.
     */
    protected String _driverCode(TypedCompositeActor model, Actor actor)
            throws IllegalActionException {
        if (!_needsInputDriver(actor)) {
            return "";
        }

        String driverParas = "";
        String actorName = 
            StringUtilities.sanitizeName(((NamedObj) actor).getName());

        StringBuffer code =
            new StringBuffer("\ndriver " + actorName + "_driver (");

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

        code.append(driverParas + ")\n"
                + "        output (");
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
            	    code.append(", ");
                }
                String portID = StringUtilities.sanitizeName(
            	        port.getName(model));
                String portTypeID = _getTypeString(port);
                code.append(portTypeID + " " + portID);
            }
        }
        code.append(")\n"
                + "{\n"
                + "          if constant_true() then " + actorName
                + "_inputdriver( ");
   
        first = true;
        for (Iterator sourceNames = driverIOMap.keySet().iterator();
             sourceNames.hasNext();) {
            if(first) {
                first = false;
            } else {
                code.append(", ");
            }
            String sourceName = (String) sourceNames.next();
            String destName = (String) driverIOMap.get(sourceName);
            code.append(sourceName + ", " + destName);
        }
        code.append(")\n"
                +  "}\n");
        return code.toString();
    }

    /** Generate code for the drivers.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     */
    protected String _driversCode(TypedCompositeActor model)
            throws IllegalActionException {

        StringBuffer code = new StringBuffer(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    drivers for common actors ////\n"
                );

        Actor actor;

        // generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            actor = (Actor) actors.next();
            code.append(_driverCode(model, actor));
        }

        code.append(
                "\n//////////////////////////////////////////////////////\n"
                + "////                    output drivers            ////\n\n"
                );

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
            	code.append("\ndriver " + portID + "_driver (");

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
                code.append(driverParas	+ ")\n"
                        + "        output ("
        	        + portTypeID + " " + portID + "_output)\n"
                        + "{\n"
                        + "  if c_true() then " + portID + "_input_driver( "
                        + driverParas + ", " + portID
                        + "_output)\n"
                        + "}\n");
            }
        }

        return code.toString();
    }

    /** Generate code for the modes.
     *  @return The modes code.
     */
    protected String _modeCode(TypedCompositeActor model)
            throws IllegalActionException {

        int actorFreq = 0;

        String outputName, actorName, modeName;

        modeName = StringUtilities.sanitizeName(model.getName());

        int periodValue =
            ((GiottoDirector) model.getDirector()).getIntPeriod();

        StringBuffer code = new StringBuffer(
                "\n    //////////////////////////////////////////////////////\n"
                + "    ////                   mode " + modeName + "\n"
                +  "    mode " + modeName + " () period "
                + periodValue + " {\n");

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
                        code.append( "    actfreq " + actorFreq
                                + " do " + outputName + " ("
                                + outputName + "_driver);\n");
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
            code.append("        taskfreq " + actorFreq + " do "
                    + actorName + "(" + driverName + ");\n");

        }
        code.append("    }\n");

        return code.toString();

    }

    /** Return true if the given actor has at least one connected
     *  input port, which requires it to have an input driver.
     */
    protected boolean _needsInputDriver(Actor actor) {
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
    ////                       private methods                     ////

    private void _init()
            throws IllegalActionException, NameDuplicationException {
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _instantiateEditorFactoryClass();

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An attribute that can create an Giotto code editor for a Giotto model.
     */
    protected class GiottoEditorFactory extends EditorFactory {

        /** Constructs a Giotto EditorFactory object for a Giotto model.
         *
         *  @param container The container, which is a Giotto model.
         *  @param name The name for this attribute.
         *  @throws IllegalActionException If the factory is not of an 
         *  acceptable attribute for the container.
         *  @throws NameDuplicationException If the name coincides with 
         *  an attribute already in the container.
         */
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
                        generateGiottoCode(model));
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
                
                // end the model execution.
                manager.stop();
                manager.wrapup();
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
