/* Calculate the statistics of the inputs.

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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEStatistics
/*
Calculate the average and variance of the input values that have
arrived since the last reset.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEStatistics extends DEActor {
    /** Construct a DEStatistics star.
     *
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEStatistics(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Initialize fields.
        _sum = 0;
        _sum2 = 0;
        _num = 0;

        // create output ports.
        average = new DEIOPort(this, "average", false, true);
        average.setDeclaredType(DoubleToken.class);
        variance = new DEIOPort(this, "variance", false, true);
        variance.setDeclaredType(DoubleToken.class);
        // create input ports
        input = new DEIOPort(this, "data input", true, false);
        input.setDeclaredType(DoubleToken.class);
        demand = new DEIOPort(this, "demand", true, false);
        demand.setDeclaredType(Token.class);
        reset = new DEIOPort(this, "reset", true, false);
        reset.setDeclaredType(Token.class);

	// Assert priorities
        input.before(demand);
        demand.before(reset);
        demand.triggers(average);
        demand.triggers(variance);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the "input" port, pass it depending on
     *  the "gate" input; if there's no event in the input port, but only
     *  in the "gate" input, pass event when the gate reopens.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{
        // Gather all available datas.
        while (input.hasToken(0)) {
            double newData = ((DoubleToken)input.get(0)).doubleValue();
            _sum = _sum + newData;
            _sum2 = _sum2 + newData*newData;
            _num++;
        }

        // Check for the demand input.
        if (demand.getWidth() != 0) {
            while (demand.hasToken(0)) {
                demand.get(0);
                double mean;
                double mean2;
                double var;
                if (_num == 0) {
                    // Haven't seen any datas yet.
                    // FIXME: I choose to not output anything...
                } else {
                    mean = _sum / _num;
                    mean2 = _sum2 / _num;
                    var = mean2 - mean*mean;
                    average.broadcast(new DoubleToken(mean));
                    variance.broadcast(new DoubleToken(var));
                }
            }
        }

        if (reset.getWidth() != 0) {
            // Check the reset input.
            if (reset.hasToken(0)) {
                reset.get(0);
                _sum = 0;
                _sum2 = 0;
                _num = 0;
            }
        }
    }

    /**
     */
    public double getAverage() {
        return _sum / _num;
    }

    /**
     */
    public double getVariance() {
        return _sum2 / _num - (_sum / _num)*(_sum / _num);
    }

    /** Intialize the object fields.
     *
     *  @exception IllegalActionException Thrown if could not create the
     *   receivers.
     */
    public void initialize() throws IllegalActionException {
        _sum = 0;
        _sum2 = 0;
        _num = 0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the ports.
    public DEIOPort average;
    public DEIOPort variance;
    public DEIOPort input;
    public DEIOPort demand;
    public DEIOPort reset;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // Number of datas seen.
    private long _num;
    // The sum of the datas
    private double _sum = 0;
    // The sum of the square of the datas.
    private double _sum2 = 0;

}






