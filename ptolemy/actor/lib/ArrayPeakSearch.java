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
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
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
The <i>dip</i> and <i>squelch</i> parameters control the
sensitivity to noise.  These are given either as absolute numbers or
as relative numbers.  If they are absolute numbers, then a peak is
detected if a rise above <i>dip</i> is detected before the peak
and a dip below <i>dip</i> is detected after the peak.
If they are given as relative numbers, then a peak is detected when
a rise by a factor <i>dip</i> above the most recently seen minimum
(if there has been one) is seen before the peak, and if a dip
by a factor <i>dip</i> relative to the peak is seen
after the peak.  Relative numbers can be
either linear (a fraction) or in decibels.  This is determined by
the value of the <i>scale</i> parameter. For example,
if <i>dip</i> is given as 2.0 and <i>scale</i> has value "relative linear",
then a dip must drop to half of a local peak value to be considered a dip.
If <i>squelch</i> is given as 10.0 and <i>scale</i> has value "relative linear",
then any peaks that lie below 1/10 of the global peak are ignored.
Note that <i>dip</i> is relative to the most recently seen peak or valley,
and <i>squelch</i> is relative to the global peak in the array,
when relative values are used.
If <i>scale</i> has value "relative amplitude decibels", then a value of
6.0 is equivalent to the linear value 2.0.
If <i>scale</i> has value "relative power decibels", then a value of
3.0 is equivalent to the linear value 2.0.
In either decibel scale, 0.0 is equivalent to 0.0 linear.
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
        dip.setExpression("0.0");
        dip.setTypeEquals(BaseType.DOUBLE);

        squelch = new Parameter(this, "squelch");
        squelch.setExpression("-10.0");
        squelch.setTypeEquals(BaseType.DOUBLE);
        
        scale = new StringParameter(this, "scale");
        scale.setExpression("absolute");
        scale.addChoice("absolute");
        scale.addChoice("relative linear");
        scale.addChoice("relative amplitude decibels");
        scale.addChoice("relative power decibels");
        
        startIndex = new PortParameter(this, "startIndex");
        startIndex.setExpression("0");
        startIndex.setTypeEquals(BaseType.INT);

        endIndex = new PortParameter(this, "endIndex");
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

    /** The amount that the signal must drop below a local maximum before a
     *  peak is detected. This is a double that can be interpreted as an
     *  absolute threshold or relative to the local peak, and if relative, on
     *  a linear or decibel scale, depending on the <i>scale</i>
     *  parameter. It defaults to 0.0.
     */
    public Parameter dip;
    
    /** The end point of the search. If this number is larger than
     *  the length of the input array, then the search is to the end
     *  of the array.  This is an integer that defaults to MaxInt.
     */
    public PortParameter endIndex;
    
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
    
    /** An indicator of whether <i>dip</i> and <i>squelch</i>
     *  should be interpreted as absolute or relative, and if relative, then on a
     *  linear scale, in amplitude decibels, or power decibels.
     *  If decibels are used, then
     *  the corresponding linear threshold is 10^(<i>threshold</i>/<i>N</i>),
     *  where <i>N</i> is 20 (for amplitude decibels) or 10 (for power decibels).
     *  This parameter is a string with possible values "absolute",
     *  "relative linear", "relative amplitude decibels" or "relatitve
     *  power decibels". The default value is "absolute".
     */
    public StringParameter scale;
    
    /** The value below which the input is ignored by the
     *  algorithm. This is a double that can be interpreted as an
     *  absolute number or a relative number, and if relative, on a
     *  linear or decibel scale, depending on the <i>scale</i>
     *  parameter. For the relative case, the number is relative
     *  to the global peak. It defaults to -10.0.
     */
    public Parameter squelch;
    
    /** The starting point of the search. If this number is larger than
     *  the value of <i>endIndex</i>, the search is conducted backwards
     *  (and the results presented in reverse order). If this number is
     *  larger than the length of the input array, then the search is
     *  started at the end of the input array.
     *  This is an integer that defaults to 0.
     */
    public PortParameter startIndex;

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
        startIndex.update();
        endIndex.update();
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

            boolean searchValley = false;
            boolean searchPeak = true;
            
            int localMaxIndex = start;
            int localMinIndex = start;
            
            double localMax = ((DoubleToken)inputArray.getElement(start)).doubleValue();
            double localMin = localMax;
            
            double dipValue = ((DoubleToken)dip.getToken()).doubleValue();
            double squelchValue = ((DoubleToken)squelch.getToken()).doubleValue();
            
            // The following values change since they are relative to most recently
            // peaks or values.
            double dipThreshold = dipValue;
            double riseThreshold = dipValue;
            
            String scaleValue = scale.stringValue();
            
            // Index of what scale we are dealing with.
            int scaleIndicator = _ABSOLUTE;
            if (!scaleValue.equals("absolute")) {
                // Scale is relative so we adjust the thresholds.
                // Search for the global maximum value so squelch works properly.
                double maxValue = localMax;
                for (int i = 0; i <= inputSize - 1; i = i + increment) {
                    double indata = ((DoubleToken)inputArray.getElement(i)).doubleValue();
                    if (indata > maxValue) {
                        maxValue = indata;
                    }
                }

                if (scaleValue.equals("relative amplitude decibels")) {
                    scaleIndicator = _RELATIVE_DB;
                    dipThreshold = localMax * Math.pow(10.0, (-dipValue/20));
                    riseThreshold = localMin * Math.pow(10.0, (dipValue/20));
                    squelchValue = maxValue * Math.pow(10.0, (-squelchValue/20));
                } else if (scaleValue.equals("relative power decibels")) {
                    scaleIndicator = _RELATIVE_DB_POWER;
                    dipThreshold = localMax * Math.pow(10.0, (-dipValue/10));
                    riseThreshold = localMin * Math.pow(10.0, (dipValue/10));
                    squelchValue = maxValue * Math.pow(10.0, (-squelchValue/10));
                } else if (scaleValue.equals("relative linear")) {
                    scaleIndicator = _RELATIVE_LINEAR;
                    dipThreshold = localMax - dipValue;
                    riseThreshold = localMin + dipValue;
                    squelchValue = maxValue - squelchValue;
                }
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
                        switch(scaleIndicator) {
                            case _RELATIVE_DB:
                                riseThreshold = localMin * Math.pow(10.0, (dipValue/20));
                                break;
                            case _RELATIVE_DB_POWER:
                                riseThreshold = localMin * Math.pow(10.0, (dipValue/10));
                                break;
                            case _RELATIVE_LINEAR:
                                riseThreshold = localMin + dipValue;
                                break;
                        }
                        localMinIndex = i;
                    }
                    if (_debugging) {
                        _debug("-- Looking for a value above " + riseThreshold);
                    }
                    if (indata > riseThreshold && indata > squelchValue) {
                        localMax = indata;
                        switch(scaleIndicator) {
                            case _RELATIVE_DB:
                                dipThreshold = localMax * Math.pow(10.0, (-dipValue/20));
                                break;
                            case _RELATIVE_DB_POWER:
                                dipThreshold = localMax * Math.pow(10.0, (-dipValue/10));
                                break;
                            case _RELATIVE_LINEAR:
                                dipThreshold = localMax - dipValue;
                                break;
                        }
                        localMaxIndex = i;
                        searchValley = false;
                        searchPeak = true;
                    }  
                } else if (searchPeak) {
                    if (indata > localMax && indata > squelchValue) {
                        localMax = indata;
                        switch(scaleIndicator) {
                            case _RELATIVE_DB:
                                dipThreshold = localMax * Math.pow(10.0, (-dipValue/20));
                                break;
                            case _RELATIVE_DB_POWER:
                                dipThreshold = localMax * Math.pow(10.0, (-dipValue/10));
                                break;
                            case _RELATIVE_LINEAR:
                                dipThreshold = localMax - dipValue;
                                break;
                        }
                        localMaxIndex = i;
                    }
                    if (_debugging) {
                        _debug("-- Looking for a value below " + dipThreshold);
                    }
                    if (indata < dipThreshold && localMax > squelchValue) {
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
                        switch(scaleIndicator) {
                            case _RELATIVE_DB:
                                riseThreshold = localMin * Math.pow(10.0, (dipValue/20));
                                break;
                            case _RELATIVE_DB_POWER:
                                riseThreshold = localMin * Math.pow(10.0, (dipValue/10));
                                break;
                            case _RELATIVE_LINEAR:
                                riseThreshold = localMin + dipValue;
                                break;
                        }
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final int _ABSOLUTE = 0;
    private static final int _RELATIVE_DB = 1;
    private static final int _RELATIVE_DB_POWER = 2;
    private static final int _RELATIVE_LINEAR = 3;
}
