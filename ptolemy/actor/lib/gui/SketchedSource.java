/* A plotter that is also a source of sketched signals.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.gui;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.EditListener;
import ptolemy.plot.EditablePlot;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;

///////////////////////////////////////////////////////////////////
//// SketchedSource

/**
 This actor is a plotter that also produces as its output a
 signal that has been sketched by the user on the screen.
 The <i>length</i> parameter specifies the
 number of samples in the sketched signal.  The <i>periodic</i>
 parameter, if true, specifies that the signal should be repeated.
 If this parameter is false, then the sketched signal is produced
 exactly once, at the beginning of the execution of the model.  If
 <i>periodic</i> is true and the sketch is modified during
 execution of the model, then the modification appears in the next
 cycle after the modification has been completed.  In
 other words, the change does not appear mid-cycle.
 <p>
 This actor is also a plotter, and will plot the input signals
 on the same plot as the sketched signal.  It can be used in a
 feedback loop where the output affects the input. The first batch
 of outputs is produced in the initialize() method, so it can
 be put in a feedback loop in a dataflow model.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (vogel)
 */
public class SketchedSource extends SequencePlotter implements EditListener {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SketchedSource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Create the parameters.
        length = new Parameter(this, "length", new IntToken(100));
        length.setTypeEquals(BaseType.INT);

        // The initial trace is used to make the sketched value
        // persistent, and also to provide an initial trace when
        // an instance of the actor is first dragged onto a model.
        initialTrace = new Parameter(this, "initialTrace");
        initialTrace.setExpression("repeat(length, 0.0)");
        initialTrace.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        initialTrace.setVisibility(Settable.EXPERT);

        periodic = new Parameter(this, "periodic", BooleanToken.TRUE);
        periodic.setTypeEquals(BaseType.BOOLEAN);
        yBottom = new Parameter(this, "yBottom", new DoubleToken(-1.0));
        yBottom.setTypeEquals(BaseType.DOUBLE);
        yTop = new Parameter(this, "yTop", new DoubleToken(1.0));
        yTop.setTypeEquals(BaseType.DOUBLE);

        runOnModification = new Parameter(this, "runOnModification",
                BooleanToken.FALSE);
        runOnModification.setTypeEquals(BaseType.BOOLEAN);

        // Fill on wrapup no longer makes sense.
        // NOTE: This gets overridden with zero if the MoML file
        // gives the value of this variable.  Hence, we need to
        // reset later as well.
        fillOnWrapup.setToken(BooleanToken.FALSE);
        fillOnWrapup.setVisibility(Settable.NONE);

        // Starting data set for producing plots is now always 1.
        // NOTE: This gets overridden with zero if the MoML file
        // gives the value of this variable.  Hence, we need to
        // reset later as well.
        startingDataset.setToken(_one);
        startingDataset.setVisibility(Settable.NONE);

        // Set the initial token production parameter of the
        // output port so that this can be used in SDF in feedback
        // loops.
        Parameter tokenInitProduction = new Parameter(output,
                "tokenInitProduction");

        // Use an expression here so change propagate.
        tokenInitProduction.setExpression("length");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The default signal to generate, prior to any user sketch.
     *  By default, this contains an array of zeros with the length
     *  given by the <i>length</i> parameter.
     */
    public Parameter initialTrace;

    /** The length of the output signal that will be generated.
     *  This parameter must contain an IntToken.  By default, it has
     *  value 100.
     */
    public Parameter length;

    /** The output port.  The type of this port is double.
     */
    public TypedIOPort output = null;

    /** An indicator of whether the signal should be periodically
     *  repeated.  This parameter must contain a boolean token.
     *  By default, it has value true.
     */
    public Parameter periodic;

    /** If <i>true</i>, then when the user edits the plot, if the
     *  manager is currently idle, then run the model.
     *  This is a boolean that defaults to <i>false</i>.
     */
    public Parameter runOnModification;

    /** The bottom of the Y range. This is a double, with default value -1.0.
     */
    public Parameter yBottom;

    /** The top of the Y range. This is a double, with default value 1.0.
     */
    public Parameter yTop;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>length</i>,
     *  then set the trace to its initial value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>length</i> and its value is not positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == length) {
            int lengthValue = ((IntToken) length.getToken()).intValue();

            if (lengthValue < 0) {
                throw new IllegalActionException(this,
                        "length: value is required to be positive.");
            }

            if (lengthValue != _previousLengthValue) {
                _previousLengthValue = lengthValue;
                _initialTraceIsSet = false;
                _showInitialTrace();
            }
        } else if (attribute == yBottom || attribute == yTop) {
            _setRanges();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SketchedSource newObject = (SketchedSource) super.clone(workspace);
        _data = null;
        _dataModified = false;
        _count = 0;
        _initialTraceIsSet = false;
        _previousLengthValue = -1;
        _settingInitialTrace = false;
        return newObject;
    }

    /** React to the fact that data in the specified plot has been modified
     *  by a user edit action by recording the data.  Note that this is
     *  typically called in the UI thread, and it is synchronized.
     *  @param source The plot containing the modified data.
     *  @param dataset The data set that has been modified.
     */
    @Override
    public synchronized void editDataModified(EditablePlot source, int dataset) {
        if (dataset == 0 && !_settingInitialTrace) {
            _dataModified = true;
            _data = ((EditablePlot) plot).getData(0);

            // Optionally execute the model here if it is idle.
            try {
                boolean runValue = ((BooleanToken) runOnModification.getToken())
                        .booleanValue();

                if (runValue) {
                    Manager manager = getManager();

                    if (manager != null && manager.getState() == Manager.IDLE) {
                        // Instead of calling manager.startRun(),
                        // call manager.execute().
                        // Otherwise applets have problems.
                        manager.execute();
                    }
                }
            } catch (ptolemy.kernel.util.KernelException ex) {
                // Should be thrown only if the manager is not idle, or
                // if the parameter is not boolean valued.
                throw new InternalErrorException(ex);
            }
        }
    }

    /** Produce one data sample from the sketched signal on the output
     *  port.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Read the trigger input, if there is one.
        super.fire();

        boolean periodicValue = ((BooleanToken) periodic.getToken())
                .booleanValue();

        // If this isn't periodic, then send zero only, since we already
        // sent out the entire waveform in the initialize method.
        if (!periodicValue) {
            output.send(0, _zero);
            return;
        }

        ArrayToken arrayToken = (ArrayToken) initialTrace.getToken();
        output.send(0, arrayToken.getElement(_count));
        _count++;

        if (_count == arrayToken.length()) {
            _count = 0;
            _updateInitialTrace();
        }
    }

    /** Override the base class to read data from the plot and to
     *  produce all the data on the output.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // NOTE: These gets overridden with zero after construction
        // if the MoML file gives the value.
        // Hence, we need to reset here as well.
        startingDataset.setToken(_one);
        fillOnWrapup.setToken(BooleanToken.FALSE);

        super.initialize();

        if (!_initialTraceIsSet) {
            _showInitialTrace();
        }

        _updateInitialTrace();

        // Produce the data on the output so that this can be used in
        // feedback look in dataflow models.
        ArrayToken arrayToken = (ArrayToken) initialTrace.getToken();
        output.send(0, arrayToken.arrayValue(), arrayToken.length());

        _count = 0;
    }

    /** Override the base class to create an initial trace.
     *  @param container The container into which to place the plot.
     */
    @Override
    public void place(PortableContainer container) {
        super.place(container);

        if (container != null) {
            // Set the default signal value in the plot.
            try {
                _showInitialTrace();
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex.getMessage());
            }
        }
    }

    /** Override the base class to not clear the plot.  The PlotterBase
     *  class clears the entire plot, which will erase sketched data.
     *  @exception IllegalActionException If triggered by creating receivers.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // This code is copied from AtomicActor, since we can't call super.
        _stopRequested = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new plot. In this class, it is an instance of EditablePlot.
     *  @return A new editable plot object.
     */
    @Override
    protected PlotBox _newPlot() {
        EditablePlot result = new EditablePlot();
        result.addEditListener(this);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Set the X and Y ranges of the plot.
    private void _setRanges() throws IllegalActionException {
        if (plot == null) {
            return;
        }

        double xInitValue = ((DoubleToken) xInit.getToken()).doubleValue();
        double xUnitValue = ((DoubleToken) xUnit.getToken()).doubleValue();
        int lengthValue = ((IntToken) length.getToken()).intValue();
        plot.setXRange(xInitValue, xUnitValue * lengthValue);

        double yBottomValue = ((DoubleToken) yBottom.getToken()).doubleValue();
        double yTopValue = ((DoubleToken) yTop.getToken()).doubleValue();
        plot.setYRange(yBottomValue, yTopValue);
    }

    // Show the initial value on the plot.
    // If the plot is null, return without doing anything.
    private void _showInitialTrace() throws IllegalActionException {
        if (plot == null) {
            return;
        }

        try {
            // Prevent update of initialTrace parameter.
            _settingInitialTrace = true;
            _initialTraceIsSet = true;

            int lengthValue = ((IntToken) length.getToken()).intValue();
            ((Plot) plot).clear(0);

            boolean connected = false;
            ArrayToken defaultValues = (ArrayToken) initialTrace.getToken();

            for (int i = 0; i < lengthValue; i++) {
                double value = 0.0;

                if (defaultValues != null && i < defaultValues.length()) {
                    value = ((DoubleToken) defaultValues.getElement(i))
                            .doubleValue();
                }

                ((Plot) plot).addPoint(0, i, value, connected);
                connected = true;
            }

            _setRanges();
            plot.repaint();
        } finally {
            _settingInitialTrace = false;
        }
    }

    // Update the initial trace parameter if the sketch on screen has
    // been modified by the user.
    private synchronized void _updateInitialTrace()
            throws IllegalActionException {
        if (_dataModified) {
            try {
                // Data has been modified on screen by the user.
                Token[] record = new Token[_data[1].length];

                for (int i = 0; i < _data[1].length; i++) {
                    record[i] = new DoubleToken(_data[1][i]);
                }

                ArrayToken newValue = new ArrayToken(BaseType.DOUBLE, record);
                initialTrace.setToken(newValue);
            } finally {
                _dataModified = false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Current position in the signal. */
    private int _count;

    /** Sketched data. */
    private double[][] _data;

    /** Indicator that the user has modified the data. */
    private boolean _dataModified = false;

    /** Indicator that initial trace has been supplied. */
    private boolean _initialTraceIsSet = false;

    // Constant one.
    private static IntToken _one = new IntToken(1);

    // Previous value of length parameter.
    private int _previousLengthValue = -1;

    // Indicator that we are setting the initial trace.
    private boolean _settingInitialTrace = false;

    /** Zero token. */
    private static DoubleToken _zero = new DoubleToken(0.0);
}
