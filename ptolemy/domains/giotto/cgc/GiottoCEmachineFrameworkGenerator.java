/* An attribute that generates the framework code needed by the emachine
   This involves creation of the input and output driver files to take care
   of copying data between task ports.

   Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.giotto.cgc;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.giotto.kernel.GiottoCodeGenerator;
import ptolemy.domains.giotto.kernel.GiottoCodeGeneratorUtilities;
import ptolemy.domains.giotto.kernel.GiottoDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

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

import javax.swing.JFileChooser;


//////////////////////////////////////////////////////////////////////////
//// GiottoCEmachineFrameworkGenerator

/**
   This attribute is a visible attribute that when configured (by double
   clicking on it), displays a dialog box asking the user to select a
   directory. Once the user selects and has clicked on the "Generate Files"
   button, the attribute does two things<DL>
   <DT>1. Generates the Giotto code for the model and stores it in
   "selected_directory/model_name/model_name.giotto".</DT>
   <DT>2. Generates the Emachine framework code files and stores them in
   "selected_directory/model_name/c_functionality/fcode". This particular
   direcotry structure is chosen to reflect the Emachine directory
   structure.</DT>
   <DT>The framework code consists of</DT>
   <DT><DD>f_code.c : This contains the driver code for the model, including the
   output driver initialization code, and memory allocation
   for array data types.</DD>
   <DD>f_code.h : Header file for the previous file. Simply contains the
   driver function declarations, and the task frequency definitions
   <DD>task_code.h : Contains the function declarations for the task codes.</DD>
   this is simply provided as a convenience to the user as he
   does not need to worry about the exact syntax of the
   functions, and can simply copy them from here.</DD></DT></DL>

   @author Edward A. Lee, Vinay Krishnan
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (vkris)
*/
public class GiottoCEmachineFrameworkGenerator extends GiottoCodeGenerator {
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Giotto code for the given model and write into the file
     *  model name.giotto in the directory specified by parameter "directory"
     *  @param model The model for which the Giotto code is to be generated
     *  @param directory The directory into which the generated file
     *                   (model_name.giotto) is to be written
     *  @exception IllegalActionException If the file
     *              "directory"/"model_name"/model_name.giotto cannot be opened
     *  @exception NameDuplicationException If any actor name coincides with
     *   the name of another actor already in the model.
     */
    public void writeGiottoCode(TypedCompositeActor model, File directory)
            throws IllegalActionException, NameDuplicationException {
        String modelName = StringUtilities.sanitizeName(model.getName());
        String giottoDirectoryName = directory.getAbsolutePath() + "/"
            + modelName + "/";

        File outDirFile = new File(giottoDirectoryName);

        if (!outDirFile.isDirectory()) {
            outDirFile.mkdirs();
        }

        File writeGiottoFile = new File(giottoDirectoryName,
                modelName + ".giotto");

        try {
            FileWriter giottoWriter = null;
            try {
            giottoWriter = new FileWriter(writeGiottoFile);
            giottoWriter.write(generateGiottoCode(model));
            } finally {
            	if (giottoWriter != null) {
            		giottoWriter.close();
                }
            }
        } catch (IOException e) {
            throw new IllegalActionException(model, e,
                    "Failed to open file " + modelName + ".giotto"
                    + " for writing.");
        }
    }

    /** Generate the Framework code for the given model. It creates the
     *  directory "directory"/"model_name"/c_functionality/fcode with the
     * files f_code.c, f_code.h and task_code.h in it.
     *  @param model The model for which the Framework code is to be generated
     *  @param directory The directory into which the generated files
     *                   are to be written
     *  @exception IllegalActionException If any of the files cannot be opened
     *  @exception NameDuplicationException If any actor name coincides with
     *   the name of another actor already in the model.
     */
    public void writeFrameworkCode(TypedCompositeActor model, File directory)
            throws IllegalActionException, NameDuplicationException {
        dataTypes = new HashSet(); // Creating a set of all the unique types used

        FHfuncVarDeclString = ""; // Contains the declaration of functions defined in the C file
        FCoutDriversImplString = ""; // Contains the code for the initialization of output drivers
        FCinDriversImplString = ""; // Contains functions to implement the input drivers
        THfuncDeclString = ""; // Contains the declaration of the task functions
        TCfuncImplString = ""; // Contains the skeleton code for the task functions
        FCVarInitString = ""; // Contains the initialization function f_code_init

        _generateCodeStrings(model);

        String fcodeDirectoryName = directory.getAbsolutePath() + "/"
            + StringUtilities.sanitizeName(model.getName())
            + "/c_functionality/fcode/";

        File outDirFile = new File(fcodeDirectoryName);

        if (!outDirFile.isDirectory()) {
            outDirFile.mkdirs();
        }

        File writeFCFile = new File(fcodeDirectoryName, "f_code.c");
        File writeFHFile = new File(fcodeDirectoryName, "f_code.h");

        //File writeTCFile = new File(directoryName, "task_code.c"); // This file is unneeded once we have the function declarations in the header file
        File writeTHFile = new File(fcodeDirectoryName, "task_code.h");

        try {
        	FileWriter FCwriter = null;
            try {
            FCwriter = new FileWriter(writeFCFile);
            FCwriter.write(_generateFrameworkImplementationCode(model));
            } finally {
            	if (FCwriter != null) {
            		FCwriter.close();
                }
            }
          

            FileWriter FHwriter = null;
            try {
            	FHwriter = new FileWriter(writeFHFile);
            	FHwriter.write(_generateFrameworkHeaderCode(model));
            } finally {
            	if (FHwriter != null) {
            		FHwriter.close();
            	}
            }

            //FileWriter TCwriter = new FileWriter(writeTCFile);
            //TCwriter.write(_generateTaskImplementationCode(model));
            //TCwriter.close();
            FileWriter THwriter = null;
            try {
            	THwriter = new FileWriter(writeTHFile);
            	THwriter.write(_generateTaskHeaderCode(model));
            } finally {
            	if (THwriter != null) {
            		THwriter.close();
            	}
            }
        } catch (IOException e) {
            throw new IllegalActionException(model, e,
                    "Failed to open file for writing.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method to instantiate the Editor Factory class called from the
     *  constructor. The reason for having this is that it can be
     *  overridden by subclasses
     */
    protected void _instantiateEditorFactoryClass()
            throws IllegalActionException, NameDuplicationException {
        new CEmachineFrameworkEditorFactory(this, "_editorFactory");
    }

    /** The Giotto Code Generator has been changed from the earlier generator
     * implemented by Haiyang and Steve in the following respects :-
     *
     * _getTypeString has been modified from the earlier function
     *    implemented by Haiyang and Steve to actually return the type
     *    instead of Token_Port. Also, the type returned has been
     *    modified to fit the giotto compiler restrictions, namely the
     *    exclusion of the data_type * format. Instead, a new data type
     *    is created and used in the giotto code, and defined in f_code.h
     *
     */
    protected String _getTypeString(TypedIOPort port) {
        String type = port.getType().toString();
        StringBuffer retval;
        retval = new StringBuffer();

        switch (type.charAt(0)) {
        case '{':
            retval.append(type.substring(1, type.length() - 1) + "array");
            break;

        default:
            retval.append(type);
            break;
        }

        return retval.toString(); //"Token_port";
    }

    /** Return the correct Giotto initial value string for the given port.
     */
    protected String _getInitialValueString(TypedIOPort port)
            throws IllegalActionException {
        String retVal = "";
        Parameter initVal = (Parameter) port.getAttribute("initialOutputValue");

        if (initVal != null) {
            retVal = initVal.getToken().toString();
        }

        return retVal;
    }

    /** Return the value of the parameter "arrayLength" for the given port.
     */
    protected String _getArrayLength(TypedIOPort port)
            throws IllegalActionException {
        String retVal = "";
        Parameter initVal = (Parameter) port.getAttribute("arrayLength");

        if (initVal != null) {
            retVal = initVal.getToken().toString();
        } else {
            retVal = "1"; // If no length parameter is given, assume an array length of 1
        }

        return retVal;
    }

    /** Generate Framework Implementation C code for the given model.
     *  @return The Framework Implementation C code.
     */
    /** Generate code for the C file f_code.c.
     *  This function generates function implementation for a couple of
     * legacy emachine implementations, and the output driver initialization
     * code as well as the input driver code.
     *  @return The output code.
     */
    protected String _generateFrameworkImplementationCode(
            TypedCompositeActor model) throws IllegalActionException {
        String codeString = "";

        codeString += copyrightString;
        codeString += ("/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */"
                + _endLine);
        codeString += ("\n#include \"f_code.h\"" + _endLine);
        codeString += ("#include <stdlib.h>" + _endLine);
        codeString += ("#include <string.h>" + _endLine);
        codeString += ("\n// Legacy Emachine code" + _endLine);
        codeString += ("void giotto_timer_enable_code(e_machine_type e_machine_func, int relative_time) {"
                + _endLine);
        codeString += ("}" + _endLine);
        codeString += ("\nint giotto_timer_save_code(void) {" + _endLine);
        codeString += (_tabChar + "return get_logical_time();" + _endLine);
        codeString += ("}" + _endLine);
        codeString += ("\nunsigned giotto_timer_trigger_code(int initial_time, int relative_time) {"
                + _endLine);
        codeString += (_tabChar
                + "return (get_logical_time() == (initial_time + relative_time) % get_logical_time_overflow());"
                + _endLine);
        codeString += ("}" + _endLine);
        codeString += ("\ninline unsigned constant_true( void ) {" + _endLine);
        codeString += (_tabChar + "return ( (unsigned)1 );" + _endLine);
        codeString += ("}" + _endLine + _endLine);

        codeString += FCVarInitString;
        codeString += FCoutDriversImplString;
        codeString += FCinDriversImplString;

        return codeString;
    }

    /** Generate code for the H file f_code.h.
     *  This function generates the function and variable declarations for
     *  the implementation in f_code.c
     *  @return The output code.
     */
    protected String _generateFrameworkHeaderCode(TypedCompositeActor model)
            throws IllegalActionException {
        String codeString = "";

        codeString += copyrightString;
        codeString += ("/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */"
                + _endLine);
        codeString += _endLine;

        // Writing code to prevent multiple inclusion of the file
        codeString += ("#ifndef _F_CODE_" + _endLine);
        codeString += ("#define _F_CODE_" + _endLine);
        codeString += _endLine;
        codeString += ("// Header file Inclusions" + _endLine);
        codeString += ("#include \"f_table.h\"" + _endLine);
        codeString += ("#include \"f_spec.h\"" + _endLine);
        codeString += ("#include \"f_interface.h\"" + _endLine);
        codeString += _endLine;
        codeString += ("// Task Frequency Definitions" + _endLine);
        codeString += ("#define SUPER_PERIOD" + _tabChar + _tabChar + "("
                + ((GiottoDirector) model.getDirector()).getIntPeriod() + ")"
                + _endLine);

        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String actorName = StringUtilities.sanitizeName(((NamedObj) actor)
                    .getName());
            int actorFreq = 0;

            Parameter actorFreqPara = (Parameter) ((NamedObj) actor)
                .getAttribute("frequency");

            if (actorFreqPara == null) {
                actorFreq = 1;
            } else {
                actorFreq = ((IntToken) actorFreqPara.getToken()).intValue();
            }

            codeString += ("#define " + actorName + "_FREQ" + _tabChar
                    + _tabChar + "(" + actorFreq + ")" + _endLine);
        }

        codeString += _endLine;

        codeString += ("// Datatype Declarations" + _endLine);

        Iterator dataType = dataTypes.iterator();

        while (dataType.hasNext()) {
            String type = (String) dataType.next();

            if (type.endsWith("array")) {
                codeString += ("typedef "
                        + type.substring(0, type.length() - 5 /* Length of "array" */    )
                        + " *" + type + ";" + _endLine + _endLine);
            }
        }

        codeString += ("typedef unsigned char boolean;" + _endLine + _endLine);

        codeString += ("// Legacy Emachine function declarations" + _endLine);
        codeString += ("void giotto_timer_enable_code(e_machine_type, int);"
                + _endLine);
        codeString += _endLine;
        codeString += ("int giotto_timer_save_code(void);" + _endLine);
        codeString += _endLine;
        codeString += ("unsigned giotto_timer_trigger_code(int, int);"
                + _endLine);
        codeString += _endLine;
        codeString += ("inline unsigned constant_true( void );" + _endLine);
        codeString += _endLine;
        codeString += ("void f_code_init( void );" + _endLine);
        codeString += _endLine;
        codeString += FHfuncVarDeclString;
        codeString += _endLine;
        codeString += ("#endif" + _endLine);

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
    protected String _generateTaskImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
        String codeString = "";

        codeString += copyrightString;
        codeString += ("/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */"
                + _endLine);
        codeString += ("\n#include \"task_code.h\"" + _endLine + _endLine);
        codeString += ("// Function called upon Termination" + _endLine);
        codeString += ("void func_shutdown() {" + _endLine);
        codeString += ("}" + _endLine + _endLine);
        codeString += ("// Task Code" + _endLine);
        codeString += TCfuncImplString;

        return codeString;
    }

    /** Generate code for the H file task_code.h.
     *  This function generates the function and variable declarations for
     *  the implementation in f_code.c
     *  @return The output code.
     */
    protected String _generateTaskHeaderCode(TypedCompositeActor model)
            throws IllegalActionException {
        String codeString = "";

        codeString += copyrightString;
        codeString += ("/* This file was automatically generated by the Ptolemy-II C-Emachine Framework Generator */"
                + _endLine);
        codeString += _endLine;

        // Writing code to prevent multiple inclusion of the file
        codeString += ("#ifndef _TASK_CODE_" + _endLine);
        codeString += ("#define _TASK_CODE_" + _endLine);
        codeString += _endLine;
        codeString += ("// Header file Inclusions" + _endLine);
        codeString += ("#include \"f_code.h\"" + _endLine);
        codeString += _endLine;
        codeString += THfuncDeclString;
        codeString += ("#endif" + _endLine);

        return codeString;
    }

    /** Generate the various code strings for the framework C & Header code,
     *  as well as the strings for the task C and Header code.
     */
    protected void _generateCodeStrings(TypedCompositeActor model)
            throws IllegalActionException {
        _outputInitializationCode(model);
        _arrayVariablesAllocationCode(model);
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
    protected void _outputInitializationCode(TypedCompositeActor model)
            throws IllegalActionException {
        FCoutDriversImplString += ("// Output Drivers Initialization Code"
                + _endLine + _endLine);

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
                    dataTypes.add(portTypeID);

                    String portInitialValueFunction = "CGinit_" + portID;
                    String portInitialValue = _getInitialValueString(port);
                    GiottoCodeGeneratorUtilities.checkGiottoID(portID);
                    FCoutDriversImplString += ("inline void" + " "
                            + portInitialValueFunction + "(" + portTypeID + " *p"
                            + portID + ") {" + _endLine);

                    // TODO: Take care of array datatype initialization. Ignoring it for the moment
                    if (!portTypeID.endsWith("array")) {
                        FCoutDriversImplString += (_tabChar + "*p" + portID
                                + " = " + portInitialValue + ";" + _endLine);
                    }

                    FCoutDriversImplString += ("}" + _endLine + _endLine);

                    FHfuncVarDeclString += ("inline void" + " "
                            + portInitialValueFunction + "(" + portTypeID + " *p"
                            + portID + ");" + _endLine + _endLine);
                }
            }
        }
    }

    /** Generate the memory allocation code for
     *  the output ports that are of type array.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     */
    protected void _arrayVariablesAllocationCode(TypedCompositeActor model)
            throws IllegalActionException {
        FHfuncVarDeclString += ("// Allocating Memory for Array data types"
                + _endLine);
        FCVarInitString += ("// Initialization function containing the global and local array variables"
                + _endLine + "void f_code_init ( void ) {" + _endLine);

        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String actorName = StringUtilities.sanitizeName(((NamedObj) actor)
                    .getName());

            FHfuncVarDeclString += ("// For Task " + actorName + _endLine);
            FCVarInitString += (_tabChar + "// For Task " + actorName
                    + _endLine);

            for (Iterator ports = actor.outputPortList().iterator();
                 ports.hasNext();) {
                TypedIOPort port = (TypedIOPort) ports.next();
                String portType = _getTypeString(port);

                if (portType.endsWith("array")) {
                    String sanitizedPortName = StringUtilities.sanitizeName(port
                            .getName(model));
                    String arrayLength = _getArrayLength(port);

                    FHfuncVarDeclString += (portType.substring(0,
                                                    portType.length() - 5 /* Length of "array" */    )
                            + " array" + sanitizedPortName + "_1" + "[" + arrayLength
                            + "]" + ";" + _endLine);
                    FHfuncVarDeclString += (portType.substring(0,
                                                    portType.length() - 5 /* Length of "array" */    )
                            + " array" + sanitizedPortName + "_2" + "[" + arrayLength
                            + "]" + ";" + _endLine);
                    FHfuncVarDeclString += _endLine;

                    FHfuncVarDeclString += ("extern " + portType + " local_"
                            + sanitizedPortName + ";" + _endLine);
                    FHfuncVarDeclString += ("extern " + portType + " global_"
                            + sanitizedPortName + ";" + _endLine);
                    FHfuncVarDeclString += _endLine;

                    // Writing into f_code_init
                    FCVarInitString += (_tabChar + "local_" + sanitizedPortName
                            + " = array" + sanitizedPortName + "_1;" + _endLine);
                    FCVarInitString += (_tabChar + "global_"
                            + sanitizedPortName + " = array" + sanitizedPortName
                            + "_2;" + _endLine);
                }
            }

            for (Iterator ports = actor.inputPortList().iterator();
                 ports.hasNext();) {
                TypedIOPort port = (TypedIOPort) ports.next();
                String portType = _getTypeString(port);

                if (portType.endsWith("array")) {
                    String sanitizedPortName = StringUtilities.sanitizeName(port
                            .getName(model));

                    // Since the array size need not be declared at the input port
                    // in the model, we resort to the following
                    List sourcePortList = port.sourcePortList();

                    if (sourcePortList.size() > 1) {
                        throw new IllegalActionException(port,
                                "Input port "
                                + "cannot receive data from multiple sources in Giotto.");
                    }

                    TypedIOPort sport = (TypedIOPort) port.sourcePortList().get(0);
                    String arrayLength = _getArrayLength(sport);

                    FHfuncVarDeclString += (portType.substring(0,
                                                    portType.length() - 5 /* Length of "array" */    )
                            + " array" + sanitizedPortName + "[" + arrayLength + "]"
                            + ";" + _endLine);
                    FHfuncVarDeclString += _endLine;

                    FHfuncVarDeclString += ("extern " + portType + " "
                            + actorName + "_" + sanitizedPortName + ";" + _endLine);
                    FHfuncVarDeclString += _endLine;

                    // Writing into f_code_init
                    FCVarInitString += (_tabChar + actorName + "_"
                            + sanitizedPortName + " = array" + sanitizedPortName + ";"
                            + _endLine);
                }
            }
        }

        FCVarInitString += ("}" + _endLine);
    }

    /** Generate implementation code for the drivers.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     *
     */
    protected void _driversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
        _outputDriversImplementationCode(model);
        _inputDriversImplementationCode(model);
    }

    /** Generate code which will copy the output local
     *  data to the global data. This has to generate
     *  one function for each data type present.
     * @param model
     * @return The generated copy functions
     * @exception IllegalActionException
     */
    protected void _outputDriversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
        FCoutDriversImplString += ("// Output drivers to copy values from the local to the global stage"
                + _endLine);

        Iterator dataType = dataTypes.iterator();

        while (dataType.hasNext()) {
            String type = (String) dataType.next();
            FCoutDriversImplString += ("inline void" + " copy_" + type + " ("
                    + type + " *src, " + type + " *dst) {" + _endLine + _tabChar + type
                    + " temp;" + _endLine + _endLine + _tabChar + "temp = *dst;"
                    + _endLine + _tabChar + "*dst = *src;" + _endLine + _tabChar
                    + "*src = temp;" + _endLine + "}" + _endLine + _endLine);

            FHfuncVarDeclString += ("inline void" + " copy_" + type + " ("
                    + type + " *src, " + type + " *dst);" + _endLine + _endLine);
        }
    }

    protected void _inputDriversImplementationCode(TypedCompositeActor model)
            throws IllegalActionException {
        String assgtStmtString = "";
        String initStmtString = "";
        Actor actor;
        String actorName;

        FCinDriversImplString += ("// Input Drivers for all the Tasks requiring one"
                + _endLine + _endLine);
        initStmtString += (_tabChar + "static int counter = 0;" + _endLine);
        initStmtString += _endLine;

        // generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            actor = (Actor) actors.next();

            if (!GiottoCodeGeneratorUtilities.needsInputDriver(actor)) {
                continue;
            }

            actorName = StringUtilities.sanitizeName(((NamedObj) actor).getName());

            FCinDriversImplString += ("inline void " + actorName
                    + "_inputdriver( ");
            FHfuncVarDeclString += ("inline void " + actorName
                    + "_inputdriver( ");

            assgtStmtString = "";

            Map driverIOMap = new LinkedHashMap();
            boolean firstParameter = true;
            boolean firstArray = true;

            for (Iterator inPorts = actor.inputPortList().iterator();
                 inPorts.hasNext();) {
                TypedIOPort inPort = (TypedIOPort) inPorts.next();
                String sanitizedInPortName = StringUtilities.sanitizeName(inPort
                        .getName(model));
                String inPortType = _getTypeString(inPort);

                List sourcePortList = inPort.sourcePortList();

                if (sourcePortList.size() > 1) {
                    throw new IllegalActionException(inPort,
                            "Input port "
                            + "cannot receive data from multiple sources in Giotto.");
                }

                TypedIOPort outPort = (TypedIOPort) inPort.sourcePortList().get(0);
                String sanitizedOutPortName = StringUtilities.sanitizeName(outPort
                        .getName(model));
                String arrayLength = _getArrayLength(outPort);
                String sourceActorName = StringUtilities.sanitizeName((outPort
                                                                              .getContainer()).getName());

                if (firstParameter) {
                    firstParameter = false;
                } else {
                    FCinDriversImplString += ", ";
                    FHfuncVarDeclString += ", ";
                }

                FCinDriversImplString += (inPortType + " *"
                        + sanitizedOutPortName + ", " + _getTypeString(inPort) + " *"
                        + sanitizedInPortName);
                FHfuncVarDeclString += (inPortType + " *"
                        + sanitizedOutPortName + ", " + _getTypeString(inPort) + " *"
                        + sanitizedInPortName);

                assgtStmtString += (_tabChar + "if ( (" + actorName
                        + "_FREQ <= " + sourceActorName + "_FREQ)"
                        + " || !(counter % (" + actorName + "_FREQ/" + sourceActorName
                        + "_FREQ)) ) {" + _endLine);

                if (inPortType.endsWith("array")) {
                    assgtStmtString += (_tabChar + _tabChar + "memcpy( *"
                            + sanitizedInPortName + ", *" + sanitizedOutPortName + ", "
                            + arrayLength + ");" + _endLine);
                } else {
                    assgtStmtString += (_tabChar + _tabChar + "*"
                            + sanitizedInPortName + " = *" + sanitizedOutPortName + ";"
                            + _endLine);
                }

                assgtStmtString += (_tabChar + "}" + _endLine + _endLine);
            }

            assgtStmtString += (_tabChar + "counter = (counter % " + actorName
                    + "_FREQ) + 1;" + _endLine);
            FCinDriversImplString += (") {" + _endLine);
            FHfuncVarDeclString += (");" + _endLine + _endLine);
            FCinDriversImplString += initStmtString;
            FCinDriversImplString += assgtStmtString; // Copy values from the global section to the input of the task
            FCinDriversImplString += ("}" + _endLine);
        }

        // TODO : Generate driver code for the actuators.
    }

    /** Generate code for the task.
     *  @return The task code.
     */
    protected void _taskCodeSkeleton(TypedCompositeActor model)
            throws IllegalActionException {
        boolean first;

        Iterator actors = model.entityList().iterator();

        while (actors.hasNext()) {
            TypedActor actor = (TypedActor) actors.next();
            String taskName = StringUtilities.sanitizeName(((NamedObj) actor)
                    .getName());

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
                        inputPorts += ",";
                    }

                    String portID = StringUtilities.sanitizeName(port.getName());
                    String portTypeID = _getTypeString(port);
                    List sourcePortList = port.sourcePortList();

                    if (sourcePortList.size() > 1) {
                        throw new IllegalActionException(port,
                                "Input port "
                                + "cannot receive data from multiple sources in Giotto.");
                    }

                    TypedIOPort sport = (TypedIOPort) port.sourcePortList().get(0);
                    String sanitizedSourceActorName = StringUtilities
                        .sanitizeName(sport.getContainer().getName());

                    inputPorts += (portTypeID + " *" + sanitizedSourceActorName
                            + "_" + portID);
                }
            }

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
                        outputPorts += ",";
                    }

                    String portID = StringUtilities.sanitizeName(port.getName());
                    String portTypeID = _getTypeString(port);

                    outputPorts += (portTypeID + " *" + portID);
                }
            }

            String portSeparator = ",";

            if (inputPorts.equals("") || outputPorts.equals("")) {
                portSeparator = "";
            }

            TCfuncImplString += ("void CG" + taskName + "_Task(" + inputPorts
                    + portSeparator + outputPorts + ") {" + _endLine);
            TCfuncImplString += ("}" + _endLine + _endLine);

            THfuncDeclString += ("void CG" + taskName + "_Task(" + inputPorts
                    + portSeparator + outputPorts + ");" + _endLine + _endLine);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    protected Set dataTypes;
    protected String _endLine = "\n";
    protected String _tabChar = "\t";
    protected String FHfuncVarDeclString; // Contains the declaration of the driver functions & array variables
    protected String FCoutDriversImplString; // Contains the code for the initialization of output drivers
    protected String FCVarInitString; // Contains the code for the initialization of the array variables
    protected String FCinDriversImplString; // Contains functions to implement the input drivers
    protected String THfuncDeclString; // Contains the declaration of the task functions
    protected String TCfuncImplString; // Contains the skeleton code for the task functions
    protected String copyrightString = "/*" + _endLine + _endLine
    + " Copyright (c) 2001-2005 The Regents of the University of California."
    + _endLine + " All rights reserved." + _endLine
    + " Permission is hereby granted, without written agreement and without"
    + _endLine
    + " license or royalty fees, to use, copy, modify, and distribute this"
    + _endLine
    + " software and its documentation for any purpose, provided that the above"
    + _endLine
    + " copyright notice and the following two paragraphs appear in all copies"
    + _endLine + " of this software." + _endLine + "" + _endLine
    + " IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY"
    + _endLine
    + " FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES"
    + _endLine
    + " ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF"
    + _endLine
    + " THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF"
    + _endLine + " SUCH DAMAGE." + _endLine + "" + _endLine
    + " THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,"
    + _endLine
    + " INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF"
    + _endLine
    + " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE"
    + _endLine
    + " PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF"
    + _endLine
    + " CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,"
    + _endLine + " ENHANCEMENTS, OR MODIFICATIONS." + _endLine + ""
    + _endLine + "*/" + _endLine;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    protected class CEmachineFrameworkEditorFactory extends EditorFactory {
        public CEmachineFrameworkEditorFactory(NamedObj container, String name)
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
                // Open a dialog box for the user to choose the directory to save the files
                JFileChooser dirDialog = new JFileChooser();
                dirDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dirDialog.setDialogTitle("Choose Directory to store files...");

                String cwd = StringUtilities.getProperty("user.dir");

                if (cwd != null) {
                    dirDialog.setCurrentDirectory(new File(cwd));
                }

                int returnVal = dirDialog.showDialog(parent, "Generate Files");

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File directory = dirDialog.getSelectedFile();

                    Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                    // NamedObj container = (NamedObj)object.getContainer();
                    TypedCompositeActor model = (TypedCompositeActor) GiottoCEmachineFrameworkGenerator.this
                        .getContainer();

                    // Preinitialize and resolve types.
                    CompositeActor toplevel = (CompositeActor) model.toplevel();
                    Manager manager = toplevel.getManager();

                    if (manager == null) {
                        manager = new Manager(toplevel.workspace(), "manager");
                        toplevel.setManager(manager);
                    }

                    manager.preinitializeAndResolveTypes();

                    // Generate the Giotto Code and write it into the
                    // corresponding files.
                    writeGiottoCode(model, directory);

                    // Generate the Framework code and write it into the
                    // corresponding files.
                    writeFrameworkCode(model, directory);

                    // end the model execution.
                    manager.stop();
                    manager.wrapup();
                }
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
