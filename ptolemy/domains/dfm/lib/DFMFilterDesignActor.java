/* The DFM actor that designs a IIR filter with given spec, from the 
   input ports.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.dfm.lib;

import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.math.filter.*;
import ptolemy.math.*;

//////////////////////////////////////////////////////////////////////////
//// DFMFilterDesignActor
/** 
  This actor will design a IIR filter with the given specification from
  the input ports.  The inputs are Analogy design method, band type,
  and frequency specfication: edge frequencies, and edge gains.
  <p>
  Analog filter design method is given as int token, value see ptolemy.math.Filter.
  Band type is also given in int, see ptolemy.math.Filter for enums
  Critical frequencies are given in array of doubles, and critical gains
  are given in array of doubles. <p>
  <p>
  The results are outputed on the output ports: Numerator, Denominator,
  Gain, Frequency Response, Impulse Response, Poles, Zeroes.
  <p> 
  Numerator: array of doubles token. <p> 
  Denominator: array of doubles token. <p> 
  Gain: double token. <p> 
  Frequency Response: array of complex token <p>
  Impulse Response: array of double token <p>  
  Poles: array of complex token <p>  
  Zeroes: array of complex token <p> 
  <p>
  Method in ptolemy.math.filter.DigitalFilter is used to design the
  filter.  Bilinear is used for analog-to-digital transformation.
  Sample frequency is at 1.0.
 
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.math.filter.DigitalFilter;
@see ptolemy.math.filter.Filter;
 
*/

public class DFMFilterDesignActor extends DFMActor {

    /** Constructor
     * @param container This is the CompositeActor containing this actor.
     * @param name This is the name of this actor.
     * @exception  
     */	
    public DFMFilterDesignActor(CompositeActor container, String name) 
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _criticalfreqin = new IOPort(this, "CriticalFrequencies", true, false);
        _criticalgainin = new IOPort(this, "CriticalGains", true, false);
        _bandtypein = new IOPort(this, "BandType", true, false);
        _analogdesignin = new IOPort(this, "AnalogDesignMethod", true, false);
        _numerout = new IOPort(this, "Numerator", false, true);
        _denomout = new IOPort(this, "Denominator", false, true);
        _gainout = new IOPort(this, "Gain", false, true);
        _freqrespout = new IOPort(this, "FrequencyResponse", false, true);
        _impulserespout = new IOPort(this, "ImpulseResponse", false, true);
        _poleout = new IOPort(this, "Poles", false, true);
        _zeroout = new IOPort(this, "Zeroes", false, true);

    }



    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Perform the operation that designs the filter, and get result
     * from filter and send them to outputs.
     */	
    protected void  _performProcess() {
        // assume the input tokens are double token
     
        double [] critfreq = (double []) _getData("CriticalFrequencies");
 
        double [] critgain = (double []) _getData("CriticalGains");
        int bandtype = ((Integer) _getData("BandType")).intValue();
        int analogdesign = ((Integer) _getData("AnalogDesignMethod")).intValue();
        RealDigitalFilter filter = DigitalFilter.designRealIIR(Filter.BILINEAR, analogdesign, 
                                       bandtype, critfreq, critgain, 
                                       1.0);

        DFMDoubleArrayToken num = new DFMDoubleArrayToken("New", filter.getNumerator());
        _outputTokens.put("Numerator", num);
        DFMDoubleArrayToken den = new DFMDoubleArrayToken("New", filter.getDenominator());
        _outputTokens.put("Denominator", den);
        
        DFMDoubleToken gain = new DFMDoubleToken("New", filter.getGain());
        _outputTokens.put("Gain", gain);

        DFMComplexArrayToken freqresp = new DFMComplexArrayToken("New", filter.getFrequencyResponse());
        _outputTokens.put("FrequencyResponse", freqresp);
        DFMDoubleArrayToken impulseresp = new DFMDoubleArrayToken("New", filter.getImpulseResponse());
        _outputTokens.put("ImpulseResponse", impulseresp);
        DFMComplexArrayToken poles = new DFMComplexArrayToken("New", filter.getPoles());
        _outputTokens.put("Poles", poles);
        DFMComplexArrayToken zeroes = new DFMComplexArrayToken("New", filter.getZeroes());
        _outputTokens.put("Zeroes", zeroes);

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _criticalfreqin, _criticalgainin, _bandtypein, _analogdesignin;
    private IOPort _numerout, _denomout, _gainout, _freqrespout, _impulserespout, _poleout, _zeroout;
}
