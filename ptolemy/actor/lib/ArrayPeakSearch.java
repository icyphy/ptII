/* An actor that identifies peaks in an array.

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

import java.util.ArrayList;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ArrayPeakSearch
/**
This actor outputs the indices and values of peaks in an input array.
The <i>dip</i> and <i>squelch</i> parameters control the sensitivity
to noise.  These are given either as a fraction or in decibels,
depending on the value of the <i>scale</i> parameter. For example,
if <i>dip</i> is given as 2.0 and <i>scale</i> has value "linear",
then a dip must drop to half of a local peak value to be considered a dip.
If <i>squelch</i> is given as 10.0 and <i>scale</i> has value "linear",
then any peaks that lie below 1/10 of the global peak are ignored.
Note that <i>dip</i> is relative to the most recently seen peak,
whereas <i>squelch</i> is relative to the global peak in the array.
If <i>scale</i> has value "amplitude decibels", then a value of
6.0 is equivalent to the linear value 2.0.
If <i>scale</i> has value "power decibels", then a value of
3.0 is equivalent to the linear value 2.0.
Other parameters control how the search is conducted.
This actor is based on Matlab code developed by John Signorotti of
Southwest Research Institute.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public class ArrayPeakSearch extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayPeakSearch(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set Parameters.
        dip = new Parameter(this, "dip");
        dip.setExpression("3.0");
        dip.setTypeEquals(BaseType.DOUBLE);

        squelch = new Parameter(this, "squelch");
        squelch.setExpression("40.0");
        squelch.setTypeEquals(BaseType.DOUBLE);
        
        scale = new Parameter(this, "scale");
        scale.setStringMode(true);
        scale.setExpression("power decibels");
        scale.addChoice("linear");
        scale.addChoice("amplitude decibels");
        scale.addChoice("power decibels");
        
        startIndex = new Parameter(this, "startIndex");
        startIndex.setExpression("0");
        startIndex.setTypeEquals(BaseType.INT);

        endIndex = new Parameter(this, "endIndex");
        endIndex.setExpression("MaxInt");
        endIndex.setTypeEquals(BaseType.INT);

        maximumNumberOfPeaks = new Parameter(this, "maximumNumberOfPeaks");
        maximumNumberOfPeaks.setExpression("MaxInt");
        maximumNumberOfPeaks.setTypeEquals(BaseType.INT);
        
        // Ports.
        input = new TypedIOPort(this, "input", true, false);
        peakValues = new TypedIOPort(this, "peakValues", false, true);
        peakIndices = new TypedIOPort(this, "peakIndices", false, true);
        
        new SingletonAttribute(peakValues, "_showName");
        new SingletonAttribute(peakIndices, "_showName");

        // Set Type Constraints.
        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        peakValues.setTypeAtLeast(input);
        peakIndices.setTypeEquals(new ArrayType(BaseType.INT));

        // NOTE: Consider constraining input element types.
        // This is a bit complicated to do, however.
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The fraction below a local peak that the signal must drop before a
     *  peak is detected. This is a double that can be interpreted on
     *  a linear or decibel scale, depending on the <i>scale</i>
     *  parameter. It defaults to 3.0.
     */
    public Parameter dip;
    
    /** The end point of the search. If this number is larger than
     *  the length of the input array, then the search is to the end
     *  of the array.  This is an integer that defaults to MaxInt.
     */
    public Parameter endIndex;
    
    /** The input port.  This is required to be an array of doubles
     */
    public TypedIOPort input;
    
    /** The maximum number of peaks to report.
     *  This is an integer that defaults to MaxInt.
     */
    public Parameter maximumNumberOfPeaks;
    
    /** The output port for the indices of the peaks. The type is
     *  {int} (array of int).
     */
    public TypedIOPort peakIndices;
    
    /** The output port for the values of the peaks. The type is the
     *  same as the input port.
     */
    public TypedIOPort peakValues;
    
    /** An indicator of whether <i>dip</i> and <i>squelch</i> should
     *  be interpreted on a linear scale, in amplitude decibels,
     *  or power decibels. If decibels are used, then the corresponding
     *  linear quentity is 10^(<i>dip</i>/<i>N</i>), where <i>N</i> is
     *  20 (for amplitude decibels) or 10 (for power decibels).
     *  This parameter is a string with possible values "linear",
     *  "amplitude decibels" or "power decibels". The default value
     *  is "power decibels".
     */
    public Parameter scale;
    
    /** The value below the highest peak that is ignored by the
     *  algorithm. This is a double that can be interpreted on a
     *  linear or decibel scale, depending on the <i>scale</i>
     *  parameter. It defaults to 40.0.
     */
    public Parameter squelch;
    
    /** The starting point of the search. If this number is larger than
     *  the value of <i>endIndex</i>, the search is conducted backwards
     *  (and the results presented in reverse order). If this number is
     *  larger than the length of the input array, then the search is
     *  started at the end of the input array.
     *  This is an integer that defaults to 0.
     */
    public Parameter startIndex;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayPeakSearch.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ArrayPeakSearch newObject = (ArrayPeakSearch)super.clone(workspace);
        newObject.input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        newObject.peakValues.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Consume at most one array from the input port and produce
     *  two arrays containing the indices and values of the identified
     *  peaks.  
     *  If there is no token on the input, then no output is produced.
     *  If the input is an empty array, then the same empty array token
     *  is produced on both outputs.
     *  @exception IllegalActionException If there is no director, or
     *   if sorting is not supported for the input array.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ArrayToken inputArray = (ArrayToken) input.get(0);
            int inputSize = inputArray.length();
            if (inputSize == 0) {
                peakValues.send(0, inputArray);
                peakIndices.send(0, inputArray);
                return;
            }
            int start = ((IntToken)startIndex.getToken()).intValue();
            int end = ((IntToken)endIndex.getToken()).intValue();
            int maxPeaks = ((IntToken)maximumNumberOfPeaks.getToken()).intValue();
            
            // Constrain start and end.
            if (end >= inputSize) {
                end = inputSize - 1;
            }
            if (start >= inputSize) {
                start = inputSize - 1;
            }
            if (end < 0) {
                end = 0;
            }
            if (start < 0) {
                start = 0;
            }
            int increment = 1;
            if (end < start) {
                increment = -1;
            }

            //  Originally these were reversed.
            boolean searchValley = true;
            boolean searchPeak = false;
            
            int localMaxIndex = start;
            int localMinIndex = start;
            
            double localMax = ((DoubleToken)inputArray.getElement(start)).doubleValue();
            double localMin = localMax;
            
            // Search for the global maximum value so squelch works properly.
            double maxValue = localMax;
            for (int i = 0; i <= inputSize - 1; i = i + increment) {
                double indata = ((DoubleToken)inputArray.getElement(i)).doubleValue();
                if (indata > maxValue) {
                    maxValue = indata;
                }
            }
            
            double dipThreshold = ((DoubleToken)dip.getToken()).doubleValue();
            double peakSquelch = ((DoubleToken)squelch.getToken()).doubleValue();
            String scaleValue = scale.getExpression();
            if (scaleValue.equals("amplitude decibels")) {
                dipThreshold = Math.pow(10.0, (dipThreshold/20));
                peakSquelch = Math.pow(10.0, (peakSquelch/20));
            } else if (scaleValue.equals("power decibels")) {
                dipThreshold = Math.pow(10.0, (dipThreshold/10));
                peakSquelch = Math.pow(10.0, (peakSquelch/10));
            }
            
            ArrayList resultIndices = new ArrayList();
            ArrayList resultPeaks = new ArrayList();

            for (int i = start; i <= end; i = i + increment) {
                double indata = ((DoubleToken)inputArray.getElement(i)).doubleValue();
                if (_debugging) {
                    _debug("-- Checking input with value "
                        + indata
                        + " at index "
                        + i);
                }
                if (searchValley) {
                    if (indata < localMin) {
                        localMin = indata;
                        localMinIndex = i;
                    }
                    if ((indata > dipThreshold*localMin) && (indata > maxValue/peakSquelch)) {
                        localMax = indata;
                        localMaxIndex = i;
                        searchValley = false;
                        searchPeak = true;
                    }  
                } else if (searchPeak) {
                    if (indata > localMax) {
                        localMax = indata;
                        localMaxIndex = i;
                    }
                    if (indata < localMax/dipThreshold) {
                        if (_debugging) {
                            _debug("** Found a peak with value "
                                    + localMax
                                    + " at index "
                                    + localMaxIndex);
                        }
                        resultIndices.add(new IntToken(localMaxIndex));
                        resultPeaks.add(new DoubleToken(localMax));
                        if (resultPeaks.size() > maxPeaks) {
                            break;
                        }
                        localMin = indata;
                        localMinIndex = i;
                        searchValley = true;
                        searchPeak = false;
                    }
                }
            }
            if (resultPeaks.isEmpty()) {
                resultPeaks.add(inputArray.getElement(start));
                resultIndices.add(startIndex.getToken());
            }
            
            Token[] resultPeaksArray = (Token[])resultPeaks
                    .toArray(new Token[resultPeaks.size()]);
            Token[] resultIndicesArray = (Token[])resultIndices
                    .toArray(new Token[resultIndices.size()]);
                    
            peakValues.send(0, new ArrayToken(resultPeaksArray));
            peakIndices.send(0, new ArrayToken(resultIndicesArray));
        }
    }
}
