/* A filter design demo of DFM domain.

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

package ptolemy.domains.dfm.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.lib.*;
import ptolemy.math.Complex;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DFMIIRFilter
/** 
 A simple demo of DFM domain that performs a IIR filter design.  
 It contains DFMIIRParamActor, DFMFreqActor, DFMImpulseActor, DFMFilterDesignActor,
 DFMPoleZeroActor, DFMTransActor.

@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
*/
public class DFMIIRFilter {

   public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {

        CompositeActor myUniverse = new CompositeActor();

        myUniverse.setName("Simple_example");
        Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
        DFMDirector local = new DFMDirector("Local");
        myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        DFMIIRParamActor iirparam = new DFMIIRParamActor(myUniverse, "IIRParameter");
        DFMFreqActor freq = new DFMFreqActor(myUniverse, "IIRFrequency");

        IOPort portanalogiir = (IOPort) iirparam.getPort("analogDesignMethod"); 
        IOPort portbandiir = (IOPort) iirparam.getPort("bandType"); 

        IOPort portbandfreq = (IOPort) freq.getPort("bandtype"); 
        IOPort portrespfreq = (IOPort) freq.getPort("freqResp");
        IOPort portcritfreqfreq = (IOPort) freq.getPort("criticalFreq");
        IOPort portcritgainfreq = (IOPort) freq.getPort("criticalGain");

        ComponentRelation relation1 =  myUniverse.connect(portbandfreq, portbandiir, "band_queue");

        DFMFilterDesignActor filterdesign = new DFMFilterDesignActor(myUniverse, "FilterDesign");
        IOPort portbandfilt = (IOPort) filterdesign.getPort("BandType"); 
        IOPort portanalogfilt = (IOPort) filterdesign.getPort("AnalogDesignMethod"); 
        IOPort portcritfreqfilt = (IOPort) filterdesign.getPort("CriticalFrequencies"); 
        IOPort portcritgainfilt = (IOPort) filterdesign.getPort("CriticalGains"); 
        IOPort portnumfilt = (IOPort) filterdesign.getPort("Numerator"); 
        IOPort portdenfilt = (IOPort) filterdesign.getPort("Denominator"); 
        IOPort portgainfilt = (IOPort) filterdesign.getPort("Gain"); 
        IOPort portfreqfilt = (IOPort) filterdesign.getPort("FrequencyResponse"); 
        IOPort portimpulsefilt = (IOPort) filterdesign.getPort("ImpulseResponse"); 
        IOPort portpolefilt = (IOPort) filterdesign.getPort("Poles"); 
        IOPort portzerofilt = (IOPort) filterdesign.getPort("Zeroes"); 

        // create the initial complex token.
        Complex [] initfreq = new Complex[300];
        for (int i=0;i<initfreq.length;i++){
            initfreq[i] = new Complex(0.0);
        }
 
        DFMAnnotateFeedbackActor feedback = new DFMAnnotateFeedbackActor(myUniverse, "annotatefeedback", new DFMComplexArrayToken("Annotate", initfreq));
        IOPort portfeedbackin = (IOPort) feedback.getPort("input");
        IOPort portfeedbackout = (IOPort) feedback.getPort("output");

        myUniverse.connect(portfeedbackin, portfreqfilt, "feedback_queue");
        myUniverse.connect(portrespfreq, portfeedbackout, "frequencyresp_queue");
        portbandfilt.link(relation1);
        myUniverse.connect(portanalogfilt, portanalogiir, "analogmethod_queue");
        myUniverse.connect(portcritfreqfilt, portcritfreqfreq, "criticalfreq_queue");
        myUniverse.connect(portcritgainfilt, portcritgainfreq, "criticalgain_queue");
 
        DFMTransActor transfer = new DFMTransActor(myUniverse, "Transfer Function");
        IOPort portnumtrans = (IOPort) transfer.getPort("Numerator");
        IOPort portdentrans = (IOPort) transfer.getPort("Denominator");
        IOPort portgaintrans = (IOPort) transfer.getPort("Gain");
       
        myUniverse.connect(portnumtrans, portnumfilt, "numerator_queue");
        myUniverse.connect(portdentrans, portdenfilt, "denominator_queue");
        myUniverse.connect(portgaintrans, portgainfilt, "gain_queue");

        DFMPoleZeroActor polezero = new DFMPoleZeroActor(myUniverse, "Pole Zero");
        IOPort portpolepz = (IOPort) polezero.getPort("pole");
        IOPort portzeropz = (IOPort) polezero.getPort("zero");
        myUniverse.connect(portpolepz, portpolefilt, "pole_queue");
        myUniverse.connect(portzeropz, portzerofilt, "zero_queue");
 
        DFMImpulseActor impulse = new DFMImpulseActor(myUniverse, "Impulse");
        IOPort portimpulseim = (IOPort) impulse.getPort("impulse");
        myUniverse.connect(portimpulseim, portimpulsefilt, "impulse_queue");

        exec.run();
        System.out.println("Bye World\n");
        return;
        
    }
}
