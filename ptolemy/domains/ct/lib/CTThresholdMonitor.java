/* Minitor integration steps so that the threshold is not crossed in one step.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTThresholdMonitor
/** 
Minitor integration steps so that the threshold is not crossed in one step.
@author  Jie Liu
@version %Id%
*/
public class CTThresholdMonitor extends CTActor 
        implements CTStepSizeControlActor {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public CTThresholdMonitor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setDeclaredType(DoubleToken.class);
        
        _thWidth = (double)1e-2;
        _paramThWidth = new CTParameter(this, "ThresholdWidth", 
                new DoubleToken(_thWidth));

        _thCenter = (double)0.0;
        _paramThCenter = new CTParameter(this, "ThresholdCenter", 
                new DoubleToken(_thCenter));

        _lowerBound = -5e-3;
        _upperBound = 5e-3;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** prepare for the first step.
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public void initialize() throws IllegalActionException {
        super.initialize();
        _first = true;
    }

    /** consume this input.
     */
    public void fire() throws IllegalActionException {
        //System.out.println("Monitor" + getFullName() + " fired.");
        _thisInput = ((DoubleToken) input.get(0)).doubleValue();
        //_success = true;
    }

    /** Return true always. Set this input to last input.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        _lastInput = _thisInput;
        _first = false;
        return true;
    }

    /** Return true if this step did not cross the threshold.
     */
    public boolean isThisStepSuccessful() {
        if (!_first) {
            if (((_lastInput >= _upperBound) && (_thisInput <= _lowerBound)) ||
                ((_lastInput <= _lowerBound) && (_thisInput >= _upperBound))) {
                _success = false;
                return false;
            }
        }
        _success = true;
        return true;
    }

    /** Return half the current step size if the step is not successful.
     *  Otherwise, return the current step size.
     *  @return Half the current step size if the step is not successful.
     */
    public double refinedStepSize() {
        CTDirector dir = (CTDirector)getDirector();
        if(!_success) {
            return 0.5*dir.getCurrentStepSize();
        }
        return dir.getCurrentStepSize();
    }

    /** Return java.lang.Double.MAX_VALUE, since this actor does not predict 
     *  step sizes.
     *  @return java.lang.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Never thrown.*
     */
    public void updateParameters() throws IllegalActionException {
        _thCenter = ((DoubleToken)_paramThCenter.getToken()).doubleValue();
        _thWidth = Math.abs(
            ((DoubleToken)_paramThWidth.getToken()).doubleValue());

        _lowerBound = _thCenter - _thWidth/(double)2.0;
        _upperBound = _thCenter + _thWidth/(double)2.0;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    private CTParameter _paramThWidth;
    private double _thWidth;

    private CTParameter _paramThCenter;
    private double _thCenter;

    private boolean _first;
    private boolean _success;

    private double _upperBound;
    private double _lowerBound;

    private double _lastInput;
    private double _thisInput;

}
