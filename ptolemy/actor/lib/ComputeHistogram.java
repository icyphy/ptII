/* Compute a histogram of input data.
   
   @Copyright (c) 1998-2003 The Regents of the University of California.
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
   @ProposedRating Red (eal@eecs.berkeley.edu)
   @AcceptedRating Red (cxh@eecs.berkeley.edu)
 */

package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ComputeHistogram
/**
   
Compute a histogram
<p>
The output array consists of a set of vertical bars, each representing
a histogram bin.  The height of the bar is the count of the number
of inputs that have been observed that fall within that bin.
The <i>n</i>-th bin represents values in the range
(<i>x</i> - <i>w</i>/2 + <i>o</i>, <i>x</i> + <i>w</i>/2 + <i>o</i>),
where <i>w</i> is the value of the <i>binWidth</i> parameter,
and <i>o</i> is the value of the <i>binOffset</i> parameter.
So for example, if <i>o = w/2</i>,
then each bin represents values from <i>nw</i> to
(<i>n</i> + 1)<i>w</i> for some integer <i>n</i>.
The default offset is 0.5, half the default bin width, which is 1.0.
<p>
This actor has a <i>legend</i> parameter,
which gives a comma-separated list of labels to attach to
each dataset.  Normally, the number of elements in this list
should equal the number of input channels, although this
is not enforced.

@see ptolemy.plot.Histogram

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
 */
public class ComputeHistogram extends TypedAtomicActor {
        
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ComputeHistogram(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
  
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.INT));
      
        minimumValue = new Parameter(this, "minimumValue");
        minimumValue.setExpression("0.0");
        minimumValue.setTypeEquals(BaseType.DOUBLE);
        
        maximumValue = new Parameter(this, "maximumValue");
        maximumValue.setExpression("1.0");
        maximumValue.setTypeEquals(BaseType.DOUBLE);

        numberOfBins = new Parameter(this, "numberOfBins");
        numberOfBins.setExpression("10");
        numberOfBins.setTypeEquals(BaseType.INT);
        
        inputCount = new PortParameter(this, "inputCount");
        inputCount.setExpression("10");
        inputCount.setTypeEquals(BaseType.INT);

        input_tokenConsumptionRate =
                new Parameter(input, "tokenConsumptionRate");
        input_tokenConsumptionRate.setExpression("inputCount");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setPersistent(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The lowest value that will be recorded in the histogram.
     *  This parameter has type double, with default value 0.0.
     */
    public Parameter minimumValue;

    /** The highest value that will be recorded in the histogram.
     *  This parameter has type double, with default value 1.0.
     */
    public Parameter maximumValue;

    /** The number of bins.
     *  This parameter has type int, with default value 10.
     */
    public Parameter numberOfBins;

    /** The number of tokens to compute the histogram for.
     */
    public PortParameter inputCount;

    /** The parameter that determines the consumption rate of the input.
     */
    public Parameter input_tokenConsumptionRate;

    /** The input port of type double. */
    public TypedIOPort input;

    /** The input port of type array of integer. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter is <i>binWidth</i> or <i>binOffset</i>, then
     *  configure the histogram with the specified bin width or offset.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the bin width is not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == minimumValue || 
                attribute == maximumValue ||
                attribute == numberOfBins) {
            _minimumValue = ((DoubleToken)minimumValue.getToken()).doubleValue();
            _maximumValue = ((DoubleToken)maximumValue.getToken()).doubleValue();
            _numberOfBins = ((IntToken)numberOfBins.getToken()).intValue();

            double width = (_maximumValue - _minimumValue) / _numberOfBins;
            if (width <= 0.0) {
                throw new IllegalActionException(this,
                        "Invalid bin width (must be positive): " + width);
            }
            _binWidth = width;
            _bins = new int[_numberOfBins];
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one input token from each input channel
     *  and update the histogram.
     *  This is done in postfire to ensure that data has settled.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _bins = new int[_numberOfBins];
      
        inputCount.update();

        int count = ((IntToken)inputCount.getToken()).intValue();
        for (int i = 0; i < count; i++) {
            if (input.hasToken(0)) {
                DoubleToken curToken = (DoubleToken)input.get(0);
                double curValue = curToken.doubleValue();
                
                _addPoint(curValue);
                
            }
        }
        // Send the output array.
        Token[] values = new Token[_bins.length];
        for(int i = 0; i < _bins.length; i++) {
            values[i] = new IntToken(_bins[i]);
        }
        output.send(0, new ArrayToken(values));
        
        return super.postfire();
    }

    /** Return false if the input does not have enough tokens to fire.
     *  Otherwise, return true.
     *  @return False if the number of input tokens available is not at least
     *   equal to the <i>decimation</i> parameter multiplied by the
     *   <i>blockSize</i> parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        int count = ((IntToken)inputCount.getToken()).intValue();
        if (input.hasToken(0, count)) {
            return super.prefire();
        } else {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addPoint(double value) {
        // Calculate the bin number.
        int bin = (int)(Math.round((value - (_minimumValue + _binWidth * 0.5)) / _binWidth));
        if(bin >= 0 && bin < _numberOfBins) {
            _bins[bin]++;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private int[] _bins;
    
    private double _minimumValue;
    private double _maximumValue;
    private double _binWidth;
    private int _numberOfBins;
}
