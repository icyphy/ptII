/*
@Copyright (c) 2008-2014 The Regents of the University of California.
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

                        PT_COPYRIGHT_VERSION_2
                        COPYRIGHTENDKEY


 */
package ptolemy.domains.tdl.kernel;

import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * The TDL refinement port represents an actuator and has some TDL specific
 * parameters: - frequency: update frequency - initialValue: at time 0, this
 * initial value is used - fast: if the actuator is connected to a fast task,
 * this parameter is true - slots: string that supports TDL slot selection.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 *
 */
public class TDLRefinementPort extends RefinementPort {
    /**
     * Construct a port in the given workspace.
     *
     * @param workspace
     *            The workspace.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                TypedActor interface.
     * @exception NameDuplicationException
     */
    public TDLRefinementPort(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Construct a port with a containing actor and a name that is neither an
     * input nor an output. The specified container must implement the
     * TypedActor interface, or an exception will be thrown.
     *
     * @param container
     *            The container actor.
     * @param name
     *            The name of the port.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                TypedActor interface.
     * @exception NameDuplicationException
     *                If the name coincides with a port already in the
     *                container.
     */
    public TDLRefinementPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * frequency for the port update.
     */
    public Parameter frequency;

    /**
     * Initial value of the port.
     */
    public Parameter initialValue;

    /**
     * Describes a fast actuator.
     */
    public Parameter fast;

    /**
     * Slot selection string.
     */
    public Parameter slots;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Make port to an output port, for TDL this means that it is an actuator.
     *
     * @param isOutput True if port is an output port.
     * @exception IllegalActionException Thrown by parent class.
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        super.setOutput(isOutput);
        setMirrorDisable(false);
        frequency.setVisibility(Settable.FULL);
        initialValue.setVisibility(Settable.FULL);
        fast.setVisibility(Settable.FULL);
        slots.setVisibility(Settable.FULL);
    }

    /**
     * Make port to an input port, for TDL this means that it is a sensor.
     *
     * @param isInput True if port is an input port.
     * @exception IllegalActionException Thrown by parent class.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        super.setInput(isInput);
        setMirrorDisable(false);
        if (!isOutput()) {
            frequency.setVisibility(Settable.NONE);
            initialValue.setVisibility(Settable.NONE);
            fast.setVisibility(Settable.NONE);
            slots.setVisibility(Settable.NONE);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Initialize the parameters of the port.
     * @exception IllegalActionException Thrown if parameters cannot be set.
     * @exception NameDuplicationException Thrown if parameters cannot be set.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        setMirrorDisable(false);
        frequency = new Parameter(this, "frequency");
        initialValue = new Parameter(this, "initialValue");
        fast = new Parameter(this, "fast");
        slots = new Parameter(this, "slots");
        frequency.setExpression("1");
        initialValue.setExpression("0");
        slots.setExpression("'1*'");
        fast.setExpression("false");
        frequency.setVisibility(Settable.FULL);
        initialValue.setVisibility(Settable.FULL);
        fast.setVisibility(Settable.FULL);
        slots.setVisibility(Settable.FULL);
    }

}
