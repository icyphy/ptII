/* An actor that finds the index of the first item in an array to
   cross a specified threshold.
   
 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

//////////////////////////////////////////////////////////////////////////
//// ArrayLevelCrossing
/**
Search an array from the specified starting index and report the index of
the first item in the array that is below or above the specified threshold.
If there is no such item, then -1 is returned.
The threshold can be absolute or relative to the value at the starting
index.  If it is relative, it can be given on a linear scale or in decibels.
If the threshold is relative and we are looking for values above the threshold,
then values that are above the value at the starting index by more than the
threshold are reported.
If the threshold is relative and we are looking for values below the threshold,
then values that are below the value at the starting index by more than the
threshold are reported.
<p>
This actor is a generalization of Matlab code developed by John Signorotti of
Southwest Research Institute. The original function was called UFDipSearch.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class ArrayLevelCrossing extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayLevelCrossing(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        start = new PortParameter(this, "start");
        start.setExpression("0");
        start.setTypeEquals(BaseType.INT);
        new SingletonAttribute(start.getPort(), "_showName");
                
        forwards = new Parameter(this, "forwards");
        forwards.setExpression("true");
        forwards.setTypeEquals(BaseType.BOOLEAN);
        
        threshold = new Parameter(this, "threshold");
        threshold.setExpression("0.0");
        threshold.setTypeEquals(BaseType.DOUBLE);
        
        above = new Parameter(this, "above");
        above.setExpression("false");
        above.setTypeEquals(BaseType.BOOLEAN);
        
        scale = new StringParameter(this, "scale");
        scale.setExpression("absolute");
        scale.addChoice("absolute");
        scale.addChoice("relative linear");
        scale.addChoice("relative amplitude decibels");
        scale.addChoice("relative power decibels");
        
        // Ports
        array = new TypedIOPort(this, "array", true, false);
        new SingletonAttribute(array, "_showName");
        
        output = new TypedIOPort(this, "output", false, true);

        // Set Type Constraints.
        array.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An indicator of whether to look for values above or below the
     *  specified threshold.  This is a boolean that defaults to false,
     *  which specifies to find values below the threshold.
     */
    public Parameter above;
    
    /** The array to search for a threshold crossing.
     *  This has type {double}.
     */
    public TypedIOPort array;
    
    /** The direction to search from the start. If true, search forwards.
     *  Otherwise, search backwards.  This is a boolean that defaults to true.
     */
    public Parameter forwards;
    
    /** The output port producing the index of the first bin to break
     *  the threshold.  This has type int.
     */
    public TypedIOPort output;

    /** An indicator of whether <i>threshold</i> should be interpreted as
     *  absolute or relative, and if relative, then on a linear scale, in
     *  amplitude decibels, or power decibels. If decibels are used, then
     *  the corresponding linear threshold is 10^(<i>threshold</i>/<i>N</i>),
     *  where <i>N</i> is 20 (for amplitude decibels) or 10 (for power decibels).
     *  This parameter is a string with possible values "absolute",
     *  "relative linear", "relative amplitude decibels" or "relatitve
     *  power decibels". The default value is "absolute".
     */
    public StringParameter scale;

    /** The index from which to start looking for a threshold crossing.
     *  This is an integer that defaults to 0.
     */
    public PortParameter start;
        
    /** The threshold to look for. This is a double that can be interpreted on
     *  an absolute or relative scale, and if relative, on a linear or decibel
     *  scale, depending on the <i>scale</i> parameter. It defaults to 0.0.
     */
    public Parameter threshold;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one array from the input ports and produce
     *  the index of the first bin that breaks the threshold.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        start.update();
        if (array.hasToken(0)) {
            ArrayToken inputArray = (ArrayToken) array.get(0);
            int inputSize = inputArray.length();
            
            int startValue = ((IntToken)start.getToken()).intValue();
            
            if (startValue >= inputSize || startValue < 0) {
                throw new IllegalActionException(this,
                "start is out of range: " + startValue);
            }
            
            int increment = -1;
            if (((BooleanToken)forwards.getToken()).booleanValue()) {
                increment = 1;
            }
            
            double reference = ((DoubleToken)inputArray.getElement(startValue))
                    .doubleValue();

            double thresholdValue = ((DoubleToken)threshold.getToken())
                    .doubleValue();
            
            String scaleValue = scale.stringValue();
                    
            boolean aboveValue = ((BooleanToken)above.getToken())
                    .booleanValue();

            if (scaleValue.equals("relative amplitude decibels")) {
                if (aboveValue) {
                    thresholdValue = reference * Math.pow(10.0, (thresholdValue/20));
                } else {
                    thresholdValue = reference * Math.pow(10.0, (-thresholdValue/20));
                }
            } else if (scaleValue.equals("relative power decibels")) {
                if (aboveValue) {
                    thresholdValue = reference * Math.pow(10.0, (thresholdValue/10));
                } else {
                    thresholdValue = reference * Math.pow(10.0, (-thresholdValue/10));
                }
            } else if (scaleValue.equals("relative linear")) {
                if (aboveValue) {
                    thresholdValue = reference + thresholdValue;
                } else {
                    thresholdValue = reference - thresholdValue;
                }
            }
            
            // Default output if we don't find a crossing.
            int bin = -1;
            for (int i = startValue; i < inputSize && i >= 0; i += increment) {
                double currentValue
                        = ((DoubleToken)inputArray.getElement(i))
                        .doubleValue();
                if (aboveValue) {
                    // Searching for values above the threshold.
                    if (currentValue > thresholdValue) {
                        bin = i;
                        break;
                    }
               } else {
                    // Searching for values below the threshold.
                    if (currentValue < thresholdValue) {
                        bin = i;
                        break;
                    }
                }
            }

            output.send(0, new IntToken(bin));
        }
    }
}
