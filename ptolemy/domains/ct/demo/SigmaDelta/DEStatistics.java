/* Calculate the statistics of the inputs.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.domains.ct.demo.SigmaDelta;

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
<P>
Note: This is a temporary version of the actor. It is not written in
the correct Ptolemy II style. It may be removed, or replaced by other
actors. Please do not use this actor as a template.

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
        average.setTypeEquals(DoubleToken.class);
        variance = new DEIOPort(this, "variance", false, true);
        variance.setTypeEquals(DoubleToken.class);
        // create input ports
        input = new DEIOPort(this, "input", true, false);
        input.setTypeEquals(DoubleToken.class);
        demand = new DEIOPort(this, "demand", true, false);
        demand.setTypeEquals(Token.class);
        reset = new DEIOPort(this, "reset", true, false);
        reset.setTypeEquals(Token.class);

	// Assert priorities
        //input.before(demand);
        //demand.before(reset);
        //demand.triggers(average);
        //demand.triggers(variance);
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
        // Gather all available data.
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
                    // Haven't seen any data yet.
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

    /** Initialize the object fields.
     *
     *  @exception IllegalActionException Thrown if could not create the
     *   receivers.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
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

    // Number of data seen.
    private long _num;
    // The sum of the data
    private double _sum = 0;
    // The sum of the square of the data.
    private double _sum2 = 0;

}






