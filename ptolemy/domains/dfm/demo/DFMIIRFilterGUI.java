/* A filter design demo of DFM domain, with a facet.

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
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// DFMIIRFilterGUI
/** 
 A simple demo of DFM domain that performs a IIR filter design with a facet.  
 It contains DFMIIRParamActor, DFMFreqActor, DFMImpulseActor, DFMFilterDesignActor,
 DFMPoleZeroActor, DFMTransActor.

@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
*/
public class DFMIIRFilterGUI implements ActionListener {

   public DFMIIRFilterGUI(int mode, Image facetpic) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {

        go = new Button(" Go ");
        go.setActionCommand("go");
        go.addActionListener(this);

        CompositeActor myUniverse = new CompositeActor();

        myUniverse.setName("Simple_example");
        exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
        local = new DFMDirector("Local");
        myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));

        if (mode == 1){ // frame mode
            quit = new Button("Quit");
            quit.setActionCommand("quit");
            quit.addActionListener(this);
            facet = new facetImage("/users/wbwu/ptII/ptolemy/domains/dfm/demo/filterfacet.gif", 640, 480);
        } else {
            facet = new facetImage(facetpic, 640, 480);
        }


        DFMIIRParamActor iirparam = new DFMIIRParamActor(myUniverse, "IIRParameter");
        DFMActorDrawer iirdraw = new DFMActorDrawer(34,28,86,182, facet);
        IOPort portanalogiir = (IOPort) iirparam.getPort("analogDesignMethod"); 
        DFMPortDrawer iiranalogportdraw = new DFMPortDrawer(228,47, facet, false);
        IOPort portbandiir = (IOPort) iirparam.getPort("bandType"); 
        DFMPortDrawer iirbandportdraw = new DFMPortDrawer(153,103, facet,false);
        iirparam.addActorDrawer(iirdraw);
        iirparam.addPortDrawer("analogDesignMethod", iiranalogportdraw);
        iirparam.addPortDrawer("bandType", iirbandportdraw);

        DFMFreqActor freq = new DFMFreqActor(myUniverse, "IIRFrequency");
        DFMActorDrawer freqdraw = new DFMActorDrawer(165,125,229,269, facet);

        IOPort portbandfreq = (IOPort) freq.getPort("bandtype"); 
        IOPort portrespfreq = (IOPort) freq.getPort("freqResp");
        IOPort portcritfreqfreq = (IOPort) freq.getPort("criticalFreq");
        DFMPortDrawer freqcritfreqportdraw = new DFMPortDrawer(263,126, facet, false);
        IOPort portcritgainfreq = (IOPort) freq.getPort("criticalGain");
        DFMPortDrawer freqcritgainportdraw = new DFMPortDrawer(265,188, facet, false);
        freq.addActorDrawer(freqdraw);
        freq.addPortDrawer("criticalFreq", freqcritfreqportdraw);
        freq.addPortDrawer("criticalGain", freqcritgainportdraw);

        ComponentRelation relation1 =  myUniverse.connect(portbandfreq, portbandiir, "band_queue");

        DFMFilterDesignActor filterdesign = new DFMFilterDesignActor(myUniverse, "FilterDesign");
        DFMActorDrawer filterdraw = new DFMActorDrawer(309,34,372,202, facet);
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
        DFMPortDrawer filtnumportdraw = new DFMPortDrawer(415,30, facet, false);
        DFMPortDrawer filtdenportdraw = new DFMPortDrawer(414,64, facet, false);
        DFMPortDrawer filtgainportdraw = new DFMPortDrawer(402,101, facet, false);

        DFMPortDrawer filtpoleportdraw = new DFMPortDrawer(415,138, facet, false);
        DFMPortDrawer filtzeroportdraw = new DFMPortDrawer(415,164, facet, false);
        DFMPortDrawer filtimpulseportdraw = new DFMPortDrawer(409,260, facet, false);
        DFMPortDrawer filtfreqportdraw = new DFMPortDrawer(377,222, facet, false);
        filterdesign.addActorDrawer(filterdraw);
        filterdesign.addPortDrawer("Numerator", filtnumportdraw);
        filterdesign.addPortDrawer("Denominator", filtdenportdraw);
        filterdesign.addPortDrawer("Gain", filtgainportdraw);
        filterdesign.addPortDrawer("ImpulseResponse", filtimpulseportdraw);
        filterdesign.addPortDrawer("FrequencyResponse", filtfreqportdraw);
        filterdesign.addPortDrawer("Poles", filtpoleportdraw);
        filterdesign.addPortDrawer("Zeroes", filtzeroportdraw);

        // create the initial complex token.
        Complex [] initfreq = new Complex[300];
        for (int i=0;i<initfreq.length;i++){
            initfreq[i] = new Complex(0.0);
        }
 
        DFMAnnotateFeedbackActor feedback = new DFMAnnotateFeedbackActor(myUniverse, "annotatefeedback", new DFMComplexArrayToken("Annotate", initfreq));
        DFMActorDrawer feedbackdraw = new DFMActorDrawer(317,277,379,380,facet);
        IOPort portfeedbackin = (IOPort) feedback.getPort("input");
        IOPort portfeedbackout = (IOPort) feedback.getPort("output");
        DFMPortDrawer feedbackoutportdraw = new DFMPortDrawer(130,329, facet, false);
        feedback.addActorDrawer(feedbackdraw); 
        feedback.addPortDrawer("output", feedbackoutportdraw); 

        myUniverse.connect(portfeedbackin, portfreqfilt, "feedback_queue");
        myUniverse.connect(portrespfreq, portfeedbackout, "frequencyresp_queue");
        portbandfilt.link(relation1);
        myUniverse.connect(portanalogfilt, portanalogiir, "analogmethod_queue");
        myUniverse.connect(portcritfreqfilt, portcritfreqfreq, "criticalfreq_queue");
        myUniverse.connect(portcritgainfilt, portcritgainfreq, "criticalgain_queue");
 
        DFMTransActor transfer = new DFMTransActor(myUniverse, "Transfer Function");
        DFMActorDrawer transferdraw = new DFMActorDrawer(479,28,540,111,facet);
        IOPort portnumtrans = (IOPort) transfer.getPort("Numerator");
        IOPort portdentrans = (IOPort) transfer.getPort("Denominator");
        IOPort portgaintrans = (IOPort) transfer.getPort("Gain");
        transfer.addActorDrawer(transferdraw);
 
        myUniverse.connect(portnumtrans, portnumfilt, "numerator_queue");
        myUniverse.connect(portdentrans, portdenfilt, "denominator_queue");
        myUniverse.connect(portgaintrans, portgainfilt, "gain_queue");

        DFMPoleZeroActor polezero = new DFMPoleZeroActor(myUniverse, "Pole Zero");
        DFMActorDrawer polezerodraw = new DFMActorDrawer(482,144,547,225,facet);
        IOPort portpolepz = (IOPort) polezero.getPort("pole");
        IOPort portzeropz = (IOPort) polezero.getPort("zero");
        polezero.addActorDrawer(polezerodraw);

        myUniverse.connect(portpolepz, portpolefilt, "pole_queue");
        myUniverse.connect(portzeropz, portzerofilt, "zero_queue");
 
        DFMImpulseActor impulse = new DFMImpulseActor(myUniverse, "Impulse");
        DFMActorDrawer impulsedraw = new DFMActorDrawer(485,265,550,376,facet);
        IOPort portimpulseim = (IOPort) impulse.getPort("impulse");
        impulse.addActorDrawer(impulsedraw); 
 
        myUniverse.connect(portimpulseim, portimpulsefilt, "impulse_queue");
        DFMActorDrawer [] adrawers = {iirdraw,
                                      freqdraw,
                                      filterdraw,
                                      transferdraw,
                                      polezerodraw,
                                      impulsedraw,
                                      feedbackdraw };

        DFMPortDrawer [] pdrawers = {iiranalogportdraw, 
                                     iirbandportdraw, 
                                     freqcritfreqportdraw, 
                                     freqcritgainportdraw, 
                                     filtnumportdraw, 
                                     filtdenportdraw, 
                                     filtgainportdraw, 
                                     filtpoleportdraw, 
                                     filtzeroportdraw, 
                                     filtfreqportdraw, 
                                     filtimpulseportdraw, 
                                     feedbackoutportdraw};

        facet.setActorDrawers(adrawers);
        facet.setPortDrawers(pdrawers);
  
        myPanel = new Panel();
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(5,5,5));
        buttonPanel.add(go);
        if (quit != null) buttonPanel.add(quit);

        myPanel.add("Center", facet);
        myPanel.add("South", buttonPanel);
        myPanel.setSize(725, 550);
        if (mode == 1){
            Frame frame = new Frame();
            frame.add("Center", myPanel);
            frame.setLocation(725, 550);
            frame.setSize(725, 550);
            frame.setVisible(true);
        }

   }

   public void actionPerformed(ActionEvent action){
        if (action.getActionCommand().equals("go")){
            facet.clearPortDrawerTokens();
            excutionThread = new MyThread();
            excutionThread.start();
            go.setEnabled(false);
        } else if (action.getActionCommand().equals("quit")){
            System.exit(0);
        }
   }
 
   public Panel getPanel(){
        return myPanel;
   }
 
   MyThread excutionThread;
   DFMDirector local;
   Manager exec;
   Panel myPanel;
   Button go, quit;
   facetImage facet;

   class MyThread extends Thread {
        public void run() {
            System.out.println("*********  Manager start **********");
            exec.run();
        }
   }

   public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {
        DFMIIRFilterGUI simple = new DFMIIRFilterGUI(1, null);
   }

}
