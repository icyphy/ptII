/* A CT star that outputs  a square wave.

 Copyright (c) 1997- The Regents of the University of California.
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
import java.util.Enumeration;
import java.lang.*;

//////////////////////////////////////////////////////////////////////////
//// CTSquareWave
/** 
    FIXME: Not consider break points yet.
A square wave source. The actor will regist a discontinuity to the 
director when the jump occurs. Single output source (Output type:double).

@author Jie Liu
@version: $Id$
@see ptolemy.domains.ct.kernel.CTActor
*/
public class CTSquareWave extends CTActor {

    public static final boolean VERBOSE = true;
    public static final boolean DEBUG = true;

    /** Construct the CTSquareWave actor
     * @param container container of this actor 
     * @param name The name
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public CTSquareWave(CompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new IOPort(this, "output");
        output.makeInput(false);
        output.makeOutput(true);
        _maxValue = (double)1.0;
        _paramMaxValue = new CTParameter(this, "MaximumValue",
                new DoubleToken(_maxValue));
        _minValue = (double)-1.0;
        _paramMinValue = new CTParameter(this, "MinimumValue",
                new DoubleToken(_minValue));
        _frequency = (double)1.0;
        _halfperiod = (double)1.0/((double)2.0*_frequency);
        _paramFrequency = new CTParameter(this, "Frequency", 
                new DoubleToken(_frequency));
        _startFromMin = true;
        _paramStartFromMin = new CTParameter(this, "StartFromMinimum",
                new BooleanToken(true));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Initalize the actor, start the waveform from the start time,
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
        _isMin = ((BooleanToken)_paramStartFromMin.getToken()).booleanValue();
    }
    
    /** Always returns true. If the currentTime is greate than lastFlipTime
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
        //System.out.println("half period="+_halfperiod);
        double now = dir.getCurrentTime();
        double nextfliptime = _lastfliptime + _halfperiod;
        //System.out.println("next flip time="+nextfliptime);
        if ((nextfliptime > now) && 
        (nextfliptime <(now+dir.getSuggestedNextStepSize()))) {
            dir.fireAfterDelay(this, nextfliptime-now);
        }  
        return true;
    }
        
    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     *  FIXME: default values? negative frquency?
     */
    public synchronized void updateParams() {
        if(VERBOSE) {
            System.out.println("SquareWave updating parameters..");
        }
        _maxValue = ((DoubleToken)_paramMaxValue.getToken()).doubleValue();
        _minValue = ((DoubleToken)_paramMinValue.getToken()).doubleValue();
        _frequency = ((DoubleToken)_paramFrequency.getToken()).doubleValue();
        _halfperiod = (double)1.0/((double)2.0*_frequency);
        if(DEBUG) {
            System.out.println("_maxVaue=" + _maxValue);
            System.out.println("_minVaue=" + _minValue);
            System.out.println("_Frequency=" + _frequency);
            System.out.println("_halfperiod=" + _halfperiod);
        }
    }

    /** Single output port.
     */
    public IOPort output;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private double _maxValue;
    private CTParameter _paramMaxValue;
    private double _minValue;
    private CTParameter _paramMinValue;
    private double _frequency;
    private CTParameter _paramFrequency;   
    private boolean _startFromMin;
    private CTParameter _paramStartFromMin;
    
    private double _halfperiod;
    private double _lastfliptime;
    private boolean _isMin;
}
