/* 

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEWaveForm
/**
Upon receiving an input event, output the next value specified by the
array parameter value (default "1 -1"). This array can periodically repeat
with any period, and you can halt a simulation when the end of the array is
reached. The following table summarizes the capabilities.
------------------------------------------------------------------------<br>
haltAtEnd | periodic | period   | operation                            |<br>
------------------------------------------------------------------------<br>
NO        | YES      | 0        | The period is the lengh of the array |<br>
NO        | YES      | N>0      | the period is N                      |<br>
NO        | NO       | anything | Output the array once then zeros     |<br>
YES       | anything | anything | Stop after outputting the array once |<br>
------------------------------------------------------------------------<br> 


@author Lukito Muliadi
@version $Id$
*/
public class DEWaveForm extends DEActor {

    /** Contruct a DEWaveForm actor.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEWaveForm(TypedCompositeActor container, String name,
            double[] value, boolean haltAtEnd,
            boolean periodic, int period)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        // ports
        output = new TypedIOPort(this, "output", false, true);
        input = new TypedIOPort(this, "input", true, false);
        
        // parameters
        // create a 2d double matrix from the 1d double array argument.
        double[][] value2d = new double[1][];
        if (value != null) {
            value2d[0] = value;
        } else {
            value2d[0] = new double[2];
            value2d[0][0] = -1;
            value2d[0][1] = 1;
        }

        _value = new Parameter(this, "value", new DoubleMatrixToken(value2d));
        _haltAtEnd = new Parameter(this, "haltAtEnd", 
                new BooleanToken(haltAtEnd));
        _periodic = new Parameter(this, "periodic", 
                new BooleanToken(periodic));
        _period = new Parameter(this, "period", new IntToken(period));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce an output event at the current time, and then schedule
     *  a firing in the future.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        
        double[] value = ((DoubleMatrixToken)_value.getToken()).doubleMatrix()[0];
        boolean haltAtEnd=((BooleanToken)_haltAtEnd.getToken()).booleanValue();
        boolean periodic=((BooleanToken)_periodic.getToken()).booleanValue();
        int period = ((IntToken)_period.getToken()).intValue();

        // get the triggering clock event.
        input.get(0);

        // haltAtEnd = no, periodic = yes, period = 0
        
        if (!haltAtEnd && periodic && period == 0) {
            if (_index >= value.length-1) {
                _index = 0;
            } else {
                _index++;
            }
            output.broadcast(new DoubleToken(value[_index]));
        }

        // haltAtEnd = no, periodic = yes, period = N > 0
        if (!haltAtEnd && periodic && period > 0) {
            if (_index >= period-1) {
                _index = 0;
            } else {
                _index++;
            }
            if (_index < value.length) {
                output.broadcast(new DoubleToken(value[_index]));
            } else {
                output.broadcast(new DoubleToken(0.0));
            }
        }
        
        // haltAtEnd = no, periodic = no, period = anything
        if (!haltAtEnd && !periodic) {
            if (_index < value.length-1) {
                _index++;
                output.broadcast(new DoubleToken(value[_index]));
            } else {
                output.broadcast(new DoubleToken(0.0));
            }
        }

        // haltAtEnd = yew, periodic = anything, period = anything
        if (haltAtEnd) {
            if (_index < value.length-1) {
                _index++;
                output.broadcast(new DoubleToken(value[_index]));
            } else {
                // stop outputting stuff.
            }
        }
    }


    /** Do nothing.  Derived classes override this method to define their
     *  initialization code, which gets executed exactly once prior to
     *  any other action methods. This method typically initializes
     *  internal members of an actor and produces initial output data.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _index = -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    // The output port.
    public TypedIOPort output;
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // last data index broadcasted.
    private int _index = -1;
    
    // parameters.
    private Parameter _value;
    private Parameter _haltAtEnd;
    private Parameter _periodic;
    private Parameter _period;
}







