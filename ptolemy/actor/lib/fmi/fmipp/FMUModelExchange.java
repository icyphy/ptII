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

import java.io.File;
import java.io.IOException;

import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.lib.fmi.fmipp.swig.IncrementalFMU;
import ptolemy.actor.lib.fmi.fmipp.swig.SWIGTYPE_p_double;
import ptolemy.actor.lib.fmi.fmipp.swig.SWIGTYPE_p_std__string;
import ptolemy.actor.lib.fmi.fmipp.swig.helper;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// FMUModelExchange

/**
 This is a FMU actor. Does only act like a FMU if you use one.

 @author Wolfgang M&uuml;ller
 @version $Id$
 @since Ptolemy II 10.0
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
    ////                     ports and parameters                  ////

    /** Working directory of the simulation. */
    //    public FileParameter workingDirectory;

    /** Name of program that starts the simulation. */
    public FileParameter fmuFile;

    /** Names of the input variables. */
    public Parameter inputNames;

    /** Names of the output variables. */
    public Parameter outputNames;

    /** Start values for the FMU. */
    public Parameter startValues;

    /** Value for the lookahead. */
    public Parameter lookAheadHorizon;

    /** Value for the stepsize of the lookahead. */
    public Parameter lookAheadStepSize;

    /** Value for the stepsize of the integrato. */
    public Parameter integratorStepSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update an attribute.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>init<i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fmuFile) {
            // Call asFile() so that $CLASSPATH is expanded.
            String fmuFileName = fmuFile.asFile().toString();

            // Unzip the FMU file.
            try {
                _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "Failed to unzip \""
                        + fmuFileName + "\".");
            }

            // Set _tmpPath to the directory that contains modelDescription.xml
            _tmpPath = null;
            String sentinelFileName = "modelDescription.xml";
            for (File file : _fmiModelDescription.files) {
                String fileName = file.toString();
                if (fileName.endsWith(sentinelFileName)) {
                    _tmpPath = fileName.substring(0, fileName.length()
                            - sentinelFileName.length());
                    break;
                }
            }
            System.out.println("FMUModelExchange: _tmpPath: " + _tmpPath);
            if (_tmpPath == null) {
                throw new IllegalActionException(
                        this,
                        "Did not find sentinel file "
                                + sentinelFileName
                                + "."
                                + (_fmiModelDescription.files.size() <= 0 ? "No files were unzipped from the FMU file "
                                        + fmuFileName + "."
                                        : "The first file in the fmuFile "
                                        + fmuFileName
                                        + " was "
                                        + _fmiModelDescription.files
                                        .get(0) + "."));
            }

            //             try { // make the error handling better, because if the file is not valid, its not possible to cancel the error message, one has to use ok instead ;)
            //                 if (fmuFile.getExpression() != "") {
            //                     int BUFFER = 2048;
            //                     // Call asFile() so that $CLASSPATH is expanded.
            //                     ZipFile file = new ZipFile(fmuFile.asFile());
            //                     // change this, because it's error-prone
            //                     if (fmuFile.getExpression().lastIndexOf("/") != -1) {
            //                         _fmuName = fmuFile.getExpression().substring(
            //                                 fmuFile.getExpression().lastIndexOf("/"),
            //                                 fmuFile.getExpression().length() - 4);
            //                     } else {
            //                         _fmuName = fmuFile.getExpression().substring(0,
            //                                 fmuFile.getExpression().length() - 4);
            //                     }

            //                     _tmpPath = System.getProperty("java.io.tmpdir")
            //                             + "/fmus.tmp/" + _fmuName;

            //                     File tmpFile = new File(_tmpPath);
            //                     if (!tmpFile.delete()) {
            //                         throw new IOException("Could not delete temporary file "
            //                                 + tmpFile);
            //                     }
            //                     if (!tmpFile.exists()) {
            //                         if (!tmpFile.mkdir()) {
            //                             throw new IOException("Could not create directory "
            //                                     + tmpFile);
            //                         }
            //                         Enumeration entries = file.entries();

            //                         while (entries.hasMoreElements()) {
            //                             ZipEntry entry = (ZipEntry) entries.nextElement();
            //                             String currentEntry = entry.getName();
            //                             File destFile = new File(_tmpPath, currentEntry);
            //                             File destinationParent = destFile.getParentFile();

            //                             // Create the parent directory structure if needed.
            //                             destinationParent.mkdirs();

            //                             if (!entry.isDirectory()) {
            //                                 BufferedInputStream is = new BufferedInputStream(
            //                                         file.getInputStream(entry));
            //                                 int currentByte;
            //                                 // establish buffer for writing file
            //                                 byte data[] = new byte[BUFFER];

            //                                 // write the current file to disk
            //                                 FileOutputStream fos = new FileOutputStream(
            //                                         destFile);
            //                                 BufferedOutputStream dest = new BufferedOutputStream(
            //                                         fos, BUFFER);

            //                                 // read and write until last byte is encountered
            //                                 while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
            //                                     dest.write(data, 0, currentByte);
            //                                 }
            //                                 dest.flush();
            //                                 dest.close();
            //                                 is.close();
            //                             }
            //                         }
            //                     }
            //                 }
            //             } catch (IOException ioe) {
            //                 throw new IllegalActionException(this, ioe,
            //                         "Failed to open \"" + fmuFile + "\".");
            //             }
        } else if (attribute == lookAheadHorizon) {
            if (!lookAheadHorizon.getExpression().isEmpty()) {
                _lookAheadHorizonValue = Double.valueOf(lookAheadHorizon
                        .getExpression());
            } else {
                _lookAheadHorizonValue = 0.1;
            }
        } else if (attribute == lookAheadStepSize) {
            if (!lookAheadStepSize.getExpression().isEmpty()) {
                _lookAheadStepSizeValue = Double.valueOf(lookAheadStepSize
                        .getExpression());
            } else {
                _lookAheadStepSizeValue = 0.01;
            }
        } else if (attribute == integratorStepSize) {
            if (!integratorStepSize.getExpression().isEmpty()) {
                _integratorStepSizeValue = Double.valueOf(integratorStepSize
                        .getExpression());
            } else {
                _integratorStepSizeValue = 0.001;
            }
            /*
            } else if (attribute == inputNames) {
            if (inputNames.getExpression() != "") {
            _inputVariables = inputNames.getExpression().split(",");
            _inputVector = new double[_inputVariables.length];

            foo = helper.new_string_array(_inputVariables.length);
            for (int i = 0; i < _inputVariables.length; i++) {
                helper.string_array_setitem(foo, i, _inputVariables[i]);
            }
            } else {
            _inputVariables = new String[0];
            _inputVector = new double[0];
            foo = helper.new_string_array(0);
            }

            //            _fmu.setInputs(foo, _inputVariables.length);
            } else if (attribute == outputNames) {
            if (outputNames.getExpression() != "") {
            _outputVariables = outputNames.getExpression().split(",");
            _outputVector = new double[_outputVariables.length];

            foo = helper.new_string_array(_outputVariables.length);
            for (int i = 0; i < _outputVariables.length; i++) {
                helper.string_array_setitem(foo, i, _outputVariables[i]);
            }
            } else {
            _outputVariables = new String[0];
            _outputVector = new double[0];
            foo = helper.new_string_array(0);
            }

            //            _fmu.setOutputs(foo, _outputVariables.length);
             */
        } else {
            super.attributeChanged(attribute);
        }
    }

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FMUModelExchange newObject = (FMUModelExchange) super.clone(workspace);

        newObject.fmuFile = (FileParameter) newObject.getAttribute("fmuFile");
        newObject.inputNames = (Parameter) newObject.getAttribute("inputNames");
        newObject.outputNames = (Parameter) newObject
                .getAttribute("outputNames");
        newObject.startValues = (Parameter) newObject
                .getAttribute("startValues");
        newObject.lookAheadHorizon = (Parameter) newObject
                .getAttribute("lookAheadHorizon");
        newObject.lookAheadStepSize = (Parameter) newObject
                .getAttribute("lookAheadStepSize");
        newObject.integratorStepSize = (Parameter) newObject
                .getAttribute("integratorStepSize");

        newObject._inputVariables = _inputVariables;
        newObject._inputVector = _inputVector;
        newObject._outputVariables = _outputVariables;
        newObject._outputVector = _outputVector;
        newObject._fmiModelDescription = null;

        //        newObject._fmu = new IncrementalFMU(fmu);

        /*
            newObject.workingDirectory = (FileParameter) newObject
                    .getAttribute("workingDirectory");
         */

        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        getDirector();
        double currentTime = getDirector().getModelTime().getDoubleValue();
        //        DECQEventQueue queue = (DECQEventQueue) director.getEventQueue();
        SWIGTYPE_p_double foo;

        // remove the next event from the queue, so we don't fire in case this is an external event
        //        if (nextevent.timeStamp().compareTo(getDirector().getModelTime()) != 0)
        /*
        if (nextevent != null) {
            queue.remove(nextevent);
            nextevent = null;
        }
         */
        //        System.out.format("-FMUModelExchange::fire: check for inputs: ");
        // if thereis the possibility of inputs -> check if a token is ready
        if (input.getWidth() != 0 && input.hasToken(0)) {
            //System.out.format("got _some%n");
            _some++;
            DoubleMatrixToken inputToken = (DoubleMatrixToken) input.get(0);

            foo = helper.new_double_array(_inputVariables.length);

            for (int i = 0; i < _inputVariables.length; i++) {
                _inputVector[i] = inputToken.getElementAt(i, 0);
                _debug("_inputVector", Double.toString(_inputVector[i]));
                helper.double_array_setitem(foo, i,
                        inputToken.getElementAt(i, 0));
            }
            _eventTime = _fmu.sync(_lastCallTime, currentTime, foo); // if there was an input, set it
            //            System.out.format("-FMUModelExchange::fire: call _fmu.sync(%f, %f, (%f[, ...])) -> %f%n", _lastCallTime, currentTime, inputToken.getElementAt(0,0), _eventTime);
        } else {
            //System.out.format("got _none%n");
            _none++;
            _eventTime = _fmu.sync(_lastCallTime, currentTime); // or sync without setting an input
            //            System.out.format("-FMUModelExchange::fire: call _fmu.sync(%f, %f) -> %f%n", _lastCallTime, currentTime, _eventTime);
        }

        _lastCallTime = currentTime; // remember the last calltime for the next sync

        _debug("currentTime=", Double.toString(currentTime), " _eventTime=",
                Double.toString(_eventTime));

        Time zeroTime = new Time(getDirector());
        Time checkTime = zeroTime.add(_eventTime);

        if (_eventTime - checkTime.getDoubleValue() < 0) { // check if we could be outside the lookahead horizone
            //            _fireAt(_eventTime - getDirector().getTimeResolution()); // if yes, fire a little bit earlier
            //nextevent = director.fireFMU((Actor) this, new Time(getDirector(), _eventTime - getDirector().getTimeResolution()), 1);
            _nextEvent = new Time(getDirector(), _eventTime
                    - getDirector().getTimeResolution());
            _fireAt(_nextEvent);
        } else {
            //            _fireAt(_eventTime); // fire at next event or now + lookahead
            //nextevent = director.fireFMU((Actor) this, new Time(getDirector(), _eventTime), 1);
            _nextEvent = new Time(getDirector(), _eventTime);
            _fireAt(_nextEvent);
        }

        /*
        CausalityInterfaceForComposites cifc = (CausalityInterfaceForComposites)getDirector().getCausalityInterface();
        nextevent = new DEEvent((Actor) this, new Time(getDirector(), _eventTime), 1, cifc.getDepthOfActor(this));
         */

        foo = _fmu.getCurrentOutputs();

        for (int i = 0; i < _outputVariables.length; i++) {
            //            _outputVector[i] = _inputVector[i];
            _debug("_outputVector", Double.toString(_outputVector[i]));
            _outputVector[i] = helper.double_array_getitem(foo, i);
        }

        output.send(0, new DoubleMatrixToken(_outputVector,
                _outputVariables.length, 1));
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // We use this method to make sure that the shared library
        // exists before invoking C++.  A side effect is that this
        // will try to build the shared library.
        try {
            // FIXME: One strange bug is that if we call getNativeLibrary()
            // here, then the shared library is loaded and the jvm starts
            // segfaulting in the tests?  So, we just get the path.
            // _fmiModelDescription.getNativeLibrary();
            _fmiModelDescription.getNativeLibraryPath();
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to find or build the shared library for \""
                            + _fmiModelDescription + "\"");
        }
        _fmu = new IncrementalFMU(_tmpPath,
                _fmiModelDescription.modelIdentifier);

        SWIGTYPE_p_std__string foo;

        //        System.out.format("-FMUModelExchange::initialize: check for inputs: ");

        if (!inputNames.getExpression().equals("")) {
            //        System.out.format("got _some%n");
            _inputVariables = inputNames.getExpression().split(",");
            _inputVector = new double[_inputVariables.length];

            foo = helper.new_string_array(_inputVariables.length);
            for (int i = 0; i < _inputVariables.length; i++) {
                helper.string_array_setitem(foo, i, _inputVariables[i]);
            }
        } else {
            //        System.out.format("got _none%n");
            _inputVariables = new String[0];
            _inputVector = new double[0];
            foo = helper.new_string_array(0);
        }

        _fmu.defineInputs(foo, _inputVariables.length);
        //_fmu.setInputs(foo, _inputVariables.length);

        if (!outputNames.getExpression().equals("")) {
            _outputVariables = outputNames.getExpression().split(",");
            _outputVector = new double[_outputVariables.length];

            foo = helper.new_string_array(_outputVariables.length);
            for (int i = 0; i < _outputVariables.length; i++) {
                helper.string_array_setitem(foo, i, _outputVariables[i]);
                //                System.out.format("-FMUModelExchange::initialize() - outputvariables[%d] == %s%n", i, _outputVariables[i]);
            }
        } else {
            _outputVariables = new String[0];
            _outputVector = new double[0];
            foo = helper.new_string_array(0);
        }

        _fmu.defineOutputs(foo, _outputVariables.length);
        //_fmu.setOutputs(foo, _outputVariables.length);

        //        _count = 0;
        //        _sum = null;

        if (fmuFile.getExpression().equals("")) {
            throw new IllegalActionException(this, "Error: No FMU given!");
        }

        // if (inputNames.getExpression() == "") {
        //     throw new IllegalActionException("Error: No Inputs given!");
        // }

        // if (outputNames.getExpression() == "") {
        //     throw new IllegalActionException("Error: No Outputs given!");
        // }

        //        SWIGTYPE_p_std__string foo;
        SWIGTYPE_p_double bar;
        if (!startValues.getExpression().isEmpty()) {
            String[] pairs = startValues.getExpression().split(",");

            foo = helper.new_string_array(pairs.length);
            bar = helper.new_double_array(pairs.length);

            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split("=");
                helper.string_array_setitem(foo, i, keyValue[0]);
                helper.double_array_setitem(bar, i, Double.valueOf(keyValue[1]));
            }
            _nStartValues = pairs.length;
        } else {
            foo = helper.new_string_array(0);
            bar = helper.new_double_array(0);
            _nStartValues = 0;
        }

        double startTime = getDirector().getModelStartTime().getDoubleValue();
        getDirector();

        _debug("init - ", getName());

        _fmu.init(getName(), foo, bar, _nStartValues, startTime,
                _lookAheadHorizonValue, _lookAheadStepSizeValue,
                _integratorStepSizeValue);
        //        _eventTime = _fmu.sync(startTime-1, startTime);

        _lastCallTime = startTime - 1;
        //        _fireAt(startTime);
        //nextevent = director.fireFMU((Actor) this, getDirector().getModelStartTime(), 1);
        _nextEvent = getDirector().getModelStartTime();
        _fireAt(_nextEvent);

        _fireAt(getDirector().getModelStopTime());

        _debug("ModelStopTime=", Double.toString(getDirector()
                .getModelStopTime().getDoubleValue()));
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        boolean superReturnValue = super.prefire();

        _debug("ModelTime=", getDirector().getModelTime().toString(),
                ", _nextEvent=", _nextEvent.toString());

        // in case it was an old event, that was put on the queue, drop it
        if (_nextEvent != getDirector().getModelTime() && input.getWidth() != 0
                && !input.hasToken(0)) {
            return false;
        }

        return true && superReturnValue;
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _fmu = null;
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_fmu != null) {
            _fmu.delete();
            _fmu = null;
        } else {
            System.out
            .println("FMUModelExchange: not calling _fmu.delete because fmu is null");

        }
        System.out
        .format("-FMUModelExchange::wrapup: %d times fired with and %d times without input%n",
                _some, _none);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    private IncrementalFMU _fmu;

    private int _nStartValues = 0;
    //    private Token _sum;
    //    private int _count = 0;
    private String[] _inputVariables;
    private double[] _inputVector;
    private String[] _outputVariables;
    private double[] _outputVector;
    private String _tmpPath;

    private double _lastCallTime;
    private double _eventTime;

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    private FMIModelDescription _fmiModelDescription;

    private double _lookAheadHorizonValue = 0.1;
    private double _lookAheadStepSizeValue = 0.01;
    private double _integratorStepSizeValue = 0.001;

    private int _none = 0;
    private int _some = 0;

    //    private DEEvent nextevent;
    private Time _nextEvent;
}
