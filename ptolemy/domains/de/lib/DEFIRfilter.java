/* An actor that outputs monotonically increasing values.

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
import ptolemy.graph.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DEFIRfilter
/**


@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEFIRfilter extends DEActor {

    /** Construct a FIR filter with the default taps.
     *  @param container The container.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEFIRfilter(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, _DEFAULT_TAPS);
    }    


    /** Construct an FIR filter with the specified filter taps.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param taps The filter taps expressed as an array of double values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEFIRfilter(TypedCompositeActor container, String name,
            double[] taps)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        double[][] taps2d = new double[1][];
        taps2d[0]=taps;
         
        _taps = new Parameter(this, "taps", new DoubleMatrixToken(taps2d));
        _paramDelay = new Parameter( this, "Delay", new DoubleToken(_delay));
        // create an output port
        output = new DEIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        // create an input port
        input = new DEIOPort(this, "input", true, false);
        input.setDeclaredType(DoubleToken.class);
    }

    /** Construct a DEFIRfilter with the specified container, name, initial
     *  value and step size. The initial value and step size are
     *  represented by String expressions which will be evaluated
     *  by the corresponding Parameters.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param taps The filter taps expressed as a string of double values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    //FIXME: This is a hack
    public DEFIRfilter(TypedCompositeActor container, String name,
            String taps)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        StringTokenizer stokens = new StringTokenizer(taps);
        int index = 0;
        double[] dtaps = new double[stokens.countTokens()];
        while(stokens.hasMoreTokens()) {
            String valueToken = stokens.nextToken();
            dtaps[index++] = (new Double(valueToken)).doubleValue();
        }
        // set the parameters.
        double[][] taps2d = new double[1][];
        taps2d[0]=dtaps;
         
        _taps = new Parameter(this, "taps", new DoubleMatrixToken(taps2d));
        _paramDelay = new Parameter( this, "Delay", new DoubleToken(_delay));

        // create an output port
        output = new DEIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        // create an input port
        input = new DEIOPort(this, "input", true, false);
        input.setDeclaredType(DoubleToken.class);
        
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Describe me
     */
    public void initialize() throws IllegalActionException {

        // Empty the taps.
        _tapContents = new double[0];
    }



    /** Produce the next ramp output with the same time stamp as the current
     *  input.
     *  FIXME: better exception tags needed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        
        if (input.hasToken(0)) {
            double newdata = ((DoubleToken)input.get(0)).doubleValue();
            // figure out the number of taps.
            DoubleMatrixToken tapsToken = (DoubleMatrixToken)_taps.getToken();
            int numTaps = tapsToken.getColumnCount();

            // check if the number of taps match to the contentTaps length.
            // If not, then create a new contentTaps with the appropriate
            // length and values.
            int contentLength = _tapContents.length;
            if (contentLength != numTaps) {
                double[] oldContents = _tapContents;
                _tapContents = new double[numTaps];
                for (int i = 0; i < numTaps; i++) {
                    if (i < contentLength) {
                        _tapContents[i] = oldContents[i];
                    } else {
                        _tapContents[i] = 0;
                    }
                }
            }

            // shift the content by 1.
            for (int i = numTaps-1; i >= 1; i--) {
                _tapContents[i] = _tapContents[i-1];
            }
            _tapContents[0] = newdata;
            
            // calculate the output.
            double sum = 0;
            for (int i = 0; i < numTaps; i++) {
                sum = sum + _tapContents[i]*tapsToken.getElementAt(0, i);
            }
            _delay = ((DoubleToken)_paramDelay.getToken()).doubleValue();
            // broadcast the output.
            output.broadcast((new DoubleToken(sum)), _delay);


        } else {
            throw new InternalErrorException("Schedule error! " + 
                    "DEFIRfilter is" + 
                    " fired w/o any tokens.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the ports.
    public DEIOPort output;
    public DEIOPort input;    


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // The value of the filter taps.
    private Parameter _taps;
    
    // The content of the filter taps.
    private double[] _tapContents = new double[0];

    // Parameter for delay
    private Parameter _paramDelay;
    // delay of the actor 
    private double _delay = 0.0;

    // the default filter taps.
    private static final double[] _DEFAULT_TAPS = {1, 1}; 
    
}

