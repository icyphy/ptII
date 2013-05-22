/*

Copyright (c) 2013, AIT Austrian Institute of Technology GmbH. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in
    the documentation and/or other materials provided with the
    distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package ptolemy.actor.lib.fmi.fmipp;

import ptolemy.actor.lib.fmi.fmipp.swig.*;

//import com.sun.jna.*;
//import java.io.File;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.concurrent.ConcurrentHashMap;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Workspace;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DECQEventQueue;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.actor.util.CausalityInterfaceForComposites;

///////////////////////////////////////////////////////////////////
//// FMUModelExchange

/**
 This is a FMU actor. Does only act like a FMU if you use one.

 @author Wolfgang Müller
 @version $Id: FMUImport.java 66188 2013-05-01 19:10:29Z eal $
 @since Ptolemy II 9.0 devel
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMUModelExchange extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUModelExchange(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

	System.loadLibrary("IncrementalFMU_wrap");

        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
	input.setMultiport(false);
	output.setTypeEquals(BaseType.DOUBLE_MATRIX);
	output.setMultiport(true);

        fmuFile = new FileParameter(this, "fmuFile");
        new Parameter(fmuFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(fmuFile, "allowDirectories", BooleanToken.FALSE);

	/*
        workingDirectory = new FileParameter(this, "workingDirectory");
        new Parameter(workingDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(workingDirectory, "allowDirectories", BooleanToken.TRUE);
        workingDirectory.setExpression(".");
	*/

	inputNames = new Parameter(this, "inputNames");
	inputNames.setTypeEquals(BaseType.STRING);
	inputNames.setExpression("");
	inputNames.setLazy(true);

	outputNames = new Parameter(this, "outputNames");
	outputNames.setTypeEquals(BaseType.STRING);
	outputNames.setExpression("");
	outputNames.setLazy(true);

	startValues = new Parameter(this, "startValues");
	startValues.setTypeEquals(BaseType.STRING);
	startValues.setExpression("");
	startValues.setLazy(true);

	lookAheadHorizon = new Parameter(this, "lookAheadHorizon");
	lookAheadHorizon.setTypeEquals(BaseType.DOUBLE);
	lookAheadHorizon.setExpression("");
	lookAheadHorizon.setLazy(true);

	lookAheadStepSize = new Parameter(this, "lookAheadStepSize");
	lookAheadStepSize.setTypeEquals(BaseType.DOUBLE);
	lookAheadStepSize.setExpression("");
	lookAheadStepSize.setLazy(true);

	integratorStepSize = new Parameter(this, "integratorStepSize");
	integratorStepSize.setTypeEquals(BaseType.DOUBLE);
	integratorStepSize.setExpression("");
	integratorStepSize.setLazy(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
	SWIGTYPE_p_std__string foo;
	if (attribute == fmuFile) {
	    try { // make the error handling better, because if the file is not valid, its not possible to cancel the error message, one has to use ok instead ;)
		if(fmuFile.getExpression() != "") {
		    int BUFFER = 2048;
		    ZipFile file = new ZipFile(fmuFile.getExpression());
		    // change this, because it's error-prone
		    if(fmuFile.getExpression().lastIndexOf("/") != -1) {
			fmuName = fmuFile.getExpression().substring(fmuFile.getExpression().lastIndexOf("/"), fmuFile.getExpression().length() - 4);
		    } else {
			fmuName = fmuFile.getExpression().substring(0, fmuFile.getExpression().length() - 4);
		    }

		    tmpPath = System.getProperty("java.io.tmpdir") + "/fmus.tmp/" + fmuName;
		    
		    File f = new File(tmpPath);
		    if(!f.exists()) {
			f.mkdirs();
			Enumeration entries = file.entries();

			while(entries.hasMoreElements()) {
			    ZipEntry entry = (ZipEntry)entries.nextElement();
			    String currentEntry = entry.getName();
			    File destFile = new File(tmpPath, currentEntry);
			    File destinationParent = destFile.getParentFile();

			    // create the parent directory structure if needed
			    destinationParent.mkdirs();
			    
			    if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(file.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];
				
				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
				
				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
				    dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			    }
			}
		    }
		}
	    } catch (IOException ioe) {
		fmuFile.setExpression("");
		throw new IllegalActionException("Error: " + ioe.getMessage());
	    }
	} else if (attribute == lookAheadHorizon) {
	    if(!lookAheadHorizon.getExpression().isEmpty()) {
		lookAheadHorizonValue = Double.valueOf(lookAheadHorizon.getExpression());
	    } else {
		lookAheadHorizonValue = 0.1;
	    }
	} else if (attribute == lookAheadStepSize) {
	    if(!lookAheadStepSize.getExpression().isEmpty()) {
		lookAheadStepSizeValue = Double.valueOf(lookAheadStepSize.getExpression());
	    } else {
		lookAheadStepSizeValue = 0.01;
	    }
	} else if (attribute == integratorStepSize) {
	    if(!integratorStepSize.getExpression().isEmpty()) {
		integratorStepSizeValue = Double.valueOf(integratorStepSize.getExpression());
	    } else {
		integratorStepSizeValue = 0.001;
	    }
	    /*
	} else if (attribute == inputNames) {
	    if(inputNames.getExpression() != "") {
		inputVariables = inputNames.getExpression().split(",");
		inputVector = new double[inputVariables.length];
	    
		foo = helper.new_string_array(inputVariables.length);
		for(int i = 0; i < inputVariables.length; i++) {
		    helper.string_array_setitem(foo, i, inputVariables[i]);
		}
	    } else {
		inputVariables = new String[0];
		inputVector = new double[0];
		foo = helper.new_string_array(0);
	    }
		
	    //	    fmu.setInputs(foo, inputVariables.length);
	} else if (attribute == outputNames) {
	    if(outputNames.getExpression() != "") {
		outputVariables = outputNames.getExpression().split(",");
		outputVector = new double[outputVariables.length];	    

		foo = helper.new_string_array(outputVariables.length);
		for(int i = 0; i < outputVariables.length; i++) {
		    helper.string_array_setitem(foo, i, outputVariables[i]);
		}
	    } else {
		outputVariables = new String[0];
		outputVector = new double[0];
		foo = helper.new_string_array(0);
	    }

	    //	    fmu.setOutputs(foo, outputVariables.length);
	    */
	} else {
	    super.attributeChanged(attribute);
	}
    }


    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FMUModelExchange newObject = (FMUModelExchange) super.clone(workspace);

        newObject.fmuFile = (FileParameter) newObject.getAttribute("fmuFile");
	newObject.inputNames = (Parameter) newObject.getAttribute("inputNames");
	newObject.outputNames = (Parameter) newObject.getAttribute("outputNames");
	newObject.startValues = (Parameter) newObject.getAttribute("startValues");
	newObject.lookAheadHorizon = (Parameter) newObject.getAttribute("lookAheadHorizon");
	newObject.lookAheadStepSize = (Parameter) newObject.getAttribute("lookAheadStepSize");
	newObject.integratorStepSize = (Parameter) newObject.getAttribute("integratorStepSize");

	newObject.inputVariables = inputVariables;
	newObject.inputVector = inputVector;
	newObject.outputVariables = outputVariables;
	newObject.outputVector = outputVector;
	newObject.fmuName = fmuName;

	//	newObject.fmu = new IncrementalFMU(fmu);

	/*
        newObject.workingDirectory = (FileParameter) newObject
                .getAttribute("workingDirectory");
	*/

        return newObject;
    }

    public void fire() throws IllegalActionException {
	_debug("Called fire()");
	DEDirector director = (DEDirector) getDirector();
	double currentTime = getDirector().getModelTime().getDoubleValue();
	//	DECQEventQueue queue = (DECQEventQueue) director.getEventQueue();
	SWIGTYPE_p_double foo;

	// remove the next event from the queue, so we don't fire in case this is an external event
	//	if(nextevent.timeStamp().compareTo(getDirector().getModelTime()) != 0)
	/*
	if(nextevent != null) {
	    queue.remove(nextevent);
	    nextevent = null;
	}
	*/
	//	System.out.format("-FMUModelExchange::fire: check for inputs: ");
	// if thereis the possibility of inputs -> check if a token is ready
	if((input.getWidth() != 0) && (input.hasToken(0))) {
	    //System.out.format("got some%n");
	    some++;
	    DoubleMatrixToken inputToken = (DoubleMatrixToken)input.get(0);

	    foo = helper.new_double_array(inputVariables.length);
	
	    for(int i = 0; i < inputVariables.length; i++) {
		inputVector[i] = inputToken.getElementAt(i, 0);
		_debug("inputVector", Double.toString(inputVector[i]));
		helper.double_array_setitem(foo, i, inputToken.getElementAt(i, 0));
	    }
	    eventTime = fmu.sync(lastCallTime, currentTime, foo); // if there was an input, set it
	    //	    System.out.format("-FMUModelExchange::fire: call fmu.sync(%f, %f, (%f[, ...])) -> %f%n", lastCallTime, currentTime, inputToken.getElementAt(0,0), eventTime);
	} else {
	    //System.out.format("got none%n");
	    none++;
	    eventTime = fmu.sync(lastCallTime, currentTime); // or sync without setting an input
	    //	    System.out.format("-FMUModelExchange::fire: call fmu.sync(%f, %f) -> %f%n", lastCallTime, currentTime, eventTime);
	}
	
	lastCallTime = currentTime; // remember the last calltime for the next sync

	_debug("currentTime=", Double.toString(currentTime), " eventTime=", Double.toString(eventTime));

	Time zeroTime = new Time(getDirector());
	Time checkTime = zeroTime.add(eventTime);
		
	if(eventTime - checkTime.getDoubleValue() < 0) { // check if we could be outside the lookahead horizone
	    //	    _fireAt(eventTime - getDirector().getTimeResolution()); // if yes, fire a little bit earlier
	    //nextevent = director.fireFMU((Actor) this, new Time(getDirector(), eventTime - getDirector().getTimeResolution()), 1);
	    nextEvent = new Time(getDirector(), eventTime - getDirector().getTimeResolution());
	    _fireAt(nextEvent);
	} else {
	    //	    _fireAt(eventTime); // fire at next event or now + lookahead
	    //nextevent = director.fireFMU((Actor) this, new Time(getDirector(), eventTime), 1);
	    nextEvent = new Time(getDirector(), eventTime);
	    _fireAt(nextEvent);
	}

	/*	
	CausalityInterfaceForComposites cifc = (CausalityInterfaceForComposites)getDirector().getCausalityInterface();
	nextevent = new DEEvent((Actor) this, new Time(getDirector(), eventTime), 1, cifc.getDepthOfActor(this));
	*/
	
	foo = fmu.getCurrentOutputs();

	for(int i = 0; i < outputVariables.length; i++) {
	    //	    outputVector[i] = inputVector[i];
	    _debug("outputVector", Double.toString(outputVector[i]));
	    outputVector[i] = helper.double_array_getitem(foo, i);
	}

	output.send(0, new DoubleMatrixToken(outputVector, outputVariables.length, 1));
    }

    public void initialize() throws IllegalActionException {
	super.initialize();

	fmu = new IncrementalFMU(System.getProperty("java.io.tmpdir") + "/fmus.tmp/" , fmuName);

	SWIGTYPE_p_std__string foo;

	//	System.out.format("-FMUModelExchange::initialize: check for inputs: ");

	if(inputNames.getExpression() != "") {
	    //	System.out.format("got some%n");
	    inputVariables = inputNames.getExpression().split(",");
	    inputVector = new double[inputVariables.length];
	    
	    foo = helper.new_string_array(inputVariables.length);
	    for(int i = 0; i < inputVariables.length; i++) {
		helper.string_array_setitem(foo, i, inputVariables[i]);
	    }
	} else {
	    //	System.out.format("got none%n");
	    inputVariables = new String[0];
	    inputVector = new double[0];
	    foo = helper.new_string_array(0);
	}
		
	fmu.defineInputs(foo, inputVariables.length);
	//fmu.setInputs(foo, inputVariables.length);

	if(outputNames.getExpression() != "") {
	    outputVariables = outputNames.getExpression().split(",");
	    outputVector = new double[outputVariables.length];	    

	    foo = helper.new_string_array(outputVariables.length);
	    for(int i = 0; i < outputVariables.length; i++) {
		helper.string_array_setitem(foo, i, outputVariables[i]);
		//		System.out.format("-FMUModelExchange::initialize() - outputvariables[%d] == %s%n", i, outputVariables[i]);
	    }
	} else {
	    outputVariables = new String[0];
	    outputVector = new double[0];
	    foo = helper.new_string_array(0);
	}

	fmu.defineOutputs(foo, outputVariables.length);
	//fmu.setOutputs(foo, outputVariables.length);

	//	_count = 0;
	//	_sum = null;

	if(fmuFile.getExpression() == "") {
	    throw new IllegalActionException("Error: No FMU given!");
	}

	// if(inputNames.getExpression() == "") {
	//     throw new IllegalActionException("Error: No Inputs given!");
	// }

	// if(outputNames.getExpression() == "") {
	//     throw new IllegalActionException("Error: No Outputs given!");
	// }

	//	SWIGTYPE_p_std__string foo;
	SWIGTYPE_p_double bar;
	if(!startValues.getExpression().isEmpty()) {
	    String[] pairs = startValues.getExpression().split(",");

	    foo = helper.new_string_array(pairs.length);
	    bar = helper.new_double_array(pairs.length);

	    for(int i = 0; i < pairs.length; i++) {
		String pair = pairs[i];
		String[] keyValue = pair.split("=");
		helper.string_array_setitem(foo, i, keyValue[0]);
		helper.double_array_setitem(bar, i, Double.valueOf(keyValue[1]));
	    }
	    nStartValues = pairs.length;
	} else {
	    foo = helper.new_string_array(0);
	    bar = helper.new_double_array(0);
	    nStartValues = 0;
	}	    

	double startTime = getDirector().getModelStartTime().getDoubleValue();
	DEDirector director = (DEDirector) getDirector();

	_debug("init - ", getName());

	fmu.init(getName(), foo, bar, nStartValues, startTime, lookAheadHorizonValue, lookAheadStepSizeValue, integratorStepSizeValue);
	//	eventTime = fmu.sync(startTime-1, startTime);

	lastCallTime = startTime-1;
	//	_fireAt(startTime);
	//nextevent = director.fireFMU((Actor) this, getDirector().getModelStartTime(), 1);
	nextEvent = getDirector().getModelStartTime();
	_fireAt(nextEvent);

	_fireAt(getDirector().getModelStopTime());

	_debug("ModelStopTime=", Double.toString(getDirector().getModelStopTime().getDoubleValue()));
    }

    public boolean prefire() throws IllegalActionException {
	_debug("Called prefire()");

	_debug("ModelTime=", getDirector().getModelTime().toString(), ", nextEvent=", nextEvent.toString());

	// in case it was an old event, that was put on the queue, drop it
	if(nextEvent != getDirector().getModelTime() && (input.getWidth() != 0) && !input.hasToken(0)) {
	    return false;
	}

	return true;
    }

    public void preinitialize() throws IllegalActionException {
        fmu = null;
    }

    public void wrapup() throws IllegalActionException {
        if (fmu != null) {
            fmu.delete();
            fmu = null;
        } else {
            System.out.println("FMUModelExchange: not calling fmu.delete because fmu is null");

        }
        System.out.format("-FMUModelExchange::wrapup: %d times fired with and %d times without input%n", some, none);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Working directory of the simulation. */
    //    public FileParameter workingDirectory;

    /** Name of program that starts the simulation. */
    public FileParameter fmuFile;

    /** Names of the input variables. */
    public Parameter inputNames;

    /** Names of the output variables. */
    public Parameter outputNames;

    /** Start values for the FMU */
    public Parameter startValues;

    /** Value for the lookahead */
    public Parameter lookAheadHorizon;

    /** Value for the stepsize of the lookahead */
    public Parameter lookAheadStepSize;

    /** Value for the stepsize of the integrator */
    public Parameter integratorStepSize;

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    private IncrementalFMU fmu;

    private int nStartValues = 0;
    //    private Token _sum;
    //    private int _count = 0;
    private String[] inputVariables;
    private double[] inputVector;
    private String[] outputVariables;
    private double[] outputVector;
    private String tmpPath;
    private String fmuName;

    private double lastCallTime;
    private double eventTime;
    private double lookAheadHorizonValue = 0.1;
    private double lookAheadStepSizeValue = 0.01;
    private double integratorStepSizeValue = 0.001;

    private int none = 0;
    private int some = 0;

    //    private DEEvent nextevent;
    private Time nextEvent;
}
