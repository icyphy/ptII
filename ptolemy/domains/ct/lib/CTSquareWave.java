/* A CT star that outputs  a square wave.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
@ProposedCodeRate red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import java.lang.*;

//////////////////////////////////////////////////////////////////////////
//// CTSquareWave
/**
FIXME: This class may be replaced by the domain polymorphic TimedPulse.
A square wave source. The actor will regist a discontinuity to the
director when the jump occurs. Single output source (Output type:double).

@author Jie Liu
@version $Id$
*/
public class CTSquareWave extends CTActor {

    /** Construct the CTSquareWave actor
     * @param container container of this actor
     * @param name The name
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public CTSquareWave(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(DoubleToken.class);
        _maxValue = (double)1.0;
        MaxValue = new Parameter(this, "MaximumValue",
                new DoubleToken(_maxValue));
        _minValue = (double)-1.0;
        MinValue = new Parameter(this, "MinimumValue",
                new DoubleToken(_minValue));
        _frequency = (double)1.0;
        _halfperiod = (double)1.0/((double)2.0*_frequency);
        Frequency = new Parameter(this, "Frequency",
                new DoubleToken(_frequency));
        _startFromMin = true;
        StartFromMin = new Parameter(this, "StartFromMinimum",
                new BooleanToken(true));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Initialize the actor, start the waveform from the start time,
     *  and the first output is min-level.
     *
     *  @exception IllegalActionException If there's no director or no
     *       input token.
     */
    public void initialize() throws IllegalActionException{
        super.initialize();
        CTDirector dir = (CTDirector)getDirector();
        if(dir == null) {
            throw new IllegalActionException( this, " Has no director.");
        }
        _lastfliptime = dir.getStartTime();
        _isMin = ((BooleanToken)StartFromMin.getToken()).booleanValue();
    }

    /** Always returns true. If the currentTime is great than lastFlipTime
     *  + _halfPeriod, then flip, and reset lastFlipTime.
     */
    public boolean prefire() throws IllegalActionException{
        super.prefire();
        CTDirector dir = (CTDirector) getDirector();
        if(dir == null) {
            throw new IllegalActionException( this, " Has no director.");
        }
        double now = dir.getCurrentTime();
        double nextfliptime = _lastfliptime+_halfperiod;
        if(nextfliptime <= now) {
            //flip;
            _isMin = !_isMin;
            _lastfliptime = nextfliptime;
        }
        return true;
    }


    /** Output a token according to the waveform.
     *
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws  IllegalActionException{
        if(_isMin) {
            output.broadcast( new DoubleToken(_minValue) );
        } else {
            output.broadcast( new DoubleToken(_maxValue) );
        }
    }

    /** If a flip occurs during
     *  the currentTime and the currentTime+currentStepSize, then regist
     *  a jump to the director.
     *  @return Same as super.prefire()
     *  @exception IllegalActionException If there's no director.
     */
    public boolean postfire() throws IllegalActionException{
        CTDirector dir = (CTDirector) getDirector();
        if(dir == null) {
            throw new IllegalActionException( this, " Has no director.");
        }
        double now = dir.getCurrentTime();
        double nextfliptime = _lastfliptime + _halfperiod;
        _debug(getFullName() + "next flip time = " + nextfliptime);
        if ((nextfliptime > now) &&
                (nextfliptime <(now+dir.getSuggestedNextStepSize()))) {
            dir.fireAt(this, nextfliptime);
        }
        return true;
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     *  FIXME: default values? negative frequency?
     *  @exception IllegalActionException If the frequency id negative.
     */
    public synchronized void updateParams() throws IllegalActionException{
        _debug(getFullName() + " updates parameters..");
        _maxValue = ((DoubleToken)MaxValue.getToken()).doubleValue();
        _minValue = ((DoubleToken)MinValue.getToken()).doubleValue();

        double f  = ((DoubleToken)Frequency.getToken()).doubleValue();
        if(f < 0) {
            throw new IllegalActionException (this,
                    "Frequency: "+ f + " is illegal.");
        }
        _frequency = f;
        _halfperiod = (double)1.0/((double)2.0*_frequency);
        _debug("_maxValue = " + _maxValue);
        _debug("_minValue = " + _minValue);
        _debug("_Frequency = " + _frequency);
        _debug("_halfperiod = " + _halfperiod);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                 ////


    /** Single output port with type double.
     */
    public TypedIOPort output;

    /** Parameter for max value; the type is double; the default value is 1.0
     */
    public Parameter MaxValue;

    /** Parameter for min value; the type is double; the default value is -1.0
     */
    public Parameter MinValue;

    /** Parameter for the frequency of the square wave; the type is double;
     *  the default value is 1.0.
     */
    public Parameter Frequency;

    /** Parameter for whether the square wave start from the min value phase;
     *  the type is boolean; the default value is true.
     */
    public Parameter StartFromMin;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private double _maxValue;
    private double _minValue;
    private double _frequency;
    private boolean _startFromMin;

    private double _halfperiod;
    private double _lastfliptime;
    private boolean _isMin;
}
