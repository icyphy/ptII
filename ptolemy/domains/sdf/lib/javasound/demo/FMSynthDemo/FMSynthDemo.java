/* A model that uses Ptolemy II SDF domain to FIXME

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

package ptolemy.domains.sdf.lib.javasound.demo.FMSynthDemo;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.javasound.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.applet.Applet;
import java.util.Enumeration;

import ptolemy.gui.*;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;


//////////////////////////////////////////////////////////////////////////
//// FMSynthDemo
/**
FIXME
<p>
Note: Requires Java 2 v1.3.0 RC1 or later.

@author Brian K. Vogel
@version $Id$
*/
public class FMSynthDemo extends TypedCompositeActor {   
    

    public FMSynthDemo() {
        super();
	create();
    }

    public FMSynthDemo(Workspace workspace) {
	super(workspace);
	create();
    }

    public FMSynthDemo(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	create();
    }


    public void create() {

	try {
	    


	    System.out.println("Create invoked");

            SDFDirector _sdfDirector = new SDFDirector(this, "SDFDirector");

            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _sdfDirector.setScheduler(scheduler);
            _sdfDirector.setScheduleValid(false);


	    // gui stuff goes here.

	    //Create the top-level container and add contents to it.
	    JFrame frame = new JFrame("FM Synthesizer");
	    
	     JPanel controlpanel = new JPanel();

	     controlpanel.setLayout(new BorderLayout());

	     //PtolemyQuery _ptQuery = new PtolemyQuery(_sdfDirector);
	     PtolemyQuery _ptQuery = new PtolemyQuery();
	    
	    controlpanel.add("West", _ptQuery);

	    _ptQuery.addSlider("carrierFreqSlider", "Carrier Frequency",
			     1884, 200, 8000);
	    _ptQuery.addSlider("harmonicitySlider", "Harmonicity Ratio",
			     1000, 200, 8000);
	    _ptQuery.addSlider("modulationSlider", "Modulation",
			     100, 0, 1000);

	    // _ptQuery.addLine("pitchLine", "Pitch Scale Factor x 1000",
	    //	     "1000");

	    frame.getContentPane().add(controlpanel, BorderLayout.CENTER);
	    //Finish setting up the frame, and show it.
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			System.exit(0);
		    }
		});
	    frame.pack();
	    frame.setVisible(true);

	   


	    // End of gui stuff.




	    // ********** FOR DEBUF ONLY ************
	    //_sdfDirector.iterations.setToken(new IntToken(1000));

	    // Set the sampling rate to use.
	    int sampleRate = 44100;

	    int channels = 1;
	    // Set the token consumption rate and production rate to use.
	    // Larger values may speed up execution.
	    int cPRate = 256;

	    // Size of internal Java Sound buffer (in samples) to use 
	    //for real-time capture/playback.
	    int buffSize = 4096;


	    //AudioSource soundSource = new AudioSource(this, "soundSource");
	    // Set the production rate(a performance optimization).
	    //soundSource.tokenProductionRate.setToken(new IntToken(cPRate));

            // Read audio data from a local file.
            //soundSource.source.setToken(new StringToken("file"));
	    //soundSource.pathName.setToken(new StringToken("22-new.aif"));
	    //soundSource.channels.setToken(new IntToken(channels));
	    // *** OR ***

	    // Read audio data from a URL.
	    //soundSource.source.setToken(new StringToken("URL"));
	    //soundSource.pathName.setToken(new StringToken("http://209.233.16.71/1-welcome.wav"));	
	   
	    // *** OR ***

	    // Read audio form microphone (real-time capture).
	    //soundSource.source.setToken(new StringToken("mic"));
	    //soundSource.sampleRate.setToken(new IntToken(sampleRate));
	    //soundSource.channels.setToken(new IntToken(channels));
	    //soundSource.bufferSize.setToken(new IntToken(buffSize));

            AudioSink soundSink = new AudioSink(this, "soundSink");
	  soundSink.pathName.setToken(new StringToken("outputFile.wav"));
	  soundSink.sink.setToken(new StringToken("speaker"));
	  //soundSink.sink.setToken(new StringToken("file"));
	  soundSink.sampleRate.setToken(new IntToken(sampleRate));
	  soundSink.sampleSizeInBits.setToken(new IntToken(16));
	  soundSink.tokenConsumptionRate.setToken(new IntToken(cPRate));
	  soundSink.channels.setToken(new IntToken(channels));
	  soundSink.bufferSize.setToken(new IntToken(buffSize));

	  //**************************************
	  //Ramp ramp1 = new Ramp(this, "rampSource1");
	  //   ** OR **
	  SDFRamp ramp1 = new SDFRamp(this, "ramp1");
	  ramp1.rate.setToken(new IntToken(cPRate));
	  ramp1.step.setToken(new DoubleToken(1.0/sampleRate));
	  //**************************************

	  //SDFRamp ramp2 = new SDFRamp(this, "ramp2");
	  //ramp2.rate.setToken(new IntToken(cPRate));
	  //ramp2.step.setToken(new DoubleToken(1.0/sampleRate));

	  SDFConst carrierFreq = new SDFConst(this, "carrierFreq");
	  carrierFreq.rate.setToken(new IntToken(cPRate));
	  carrierFreq.value.setToken(new DoubleToken(1884));
	  //carrierFreq.output.setTypeEquals(BaseType.DOUBLE);

	  SDFConst harmonicityRatio = new SDFConst(this, "harmonicityRatio");
	  harmonicityRatio.rate.setToken(new IntToken(cPRate));
	  harmonicityRatio.value.setToken(new DoubleToken(1000));
	  //harmonicityRatio.output.setTypeEquals(BaseType.DOUBLE);

	  SDFConst modulatorAmp = new SDFConst(this, "modulatorAmp");
	  modulatorAmp.rate.setToken(new IntToken(cPRate));
	  modulatorAmp.value.setToken(new DoubleToken(100));
	  //modulatorAmp.output.setTypeEquals(BaseType.DOUBLE);
	  
	  SDFConst modulatorConst = new SDFConst(this, "modulatorConst");
	  modulatorConst.rate.setToken(new IntToken(cPRate));
	  modulatorConst.value.setToken(new DoubleToken(0.001));
	  //modulatorConst.output.setTypeEquals(BaseType.DOUBLE);

	  SDFConst harmonicityConst = new SDFConst(this, "harmonicityConst");
	  harmonicityConst.rate.setToken(new IntToken(cPRate));
	  harmonicityConst.value.setToken(new DoubleToken(0.001));
	  //harmonicityConst.output.setTypeEquals(BaseType.DOUBLE);

	  SDFMultiply mult1 = new SDFMultiply(this, "mult1");
	  mult1.rate.setToken(new IntToken(cPRate));
	  //mult1.input.setTypeEquals(BaseType.DOUBLE);
	  //mult1.input2.setTypeEquals(BaseType.DOUBLE);

	  SDFMultiply mult2 = new SDFMultiply(this, "mult2");
	  mult2.rate.setToken(new IntToken(cPRate));
	  //mult2.input.setTypeEquals(BaseType.DOUBLE);
	  //mult2.input2.setTypeEquals(BaseType.DOUBLE);

	  SDFMultiply mult3 = new SDFMultiply(this, "mult3");
	  mult3.rate.setToken(new IntToken(cPRate));
	  //mult3.input.setTypeEquals(BaseType.DOUBLE);
	  //mult3.input2.setTypeEquals(BaseType.DOUBLE);

	  SDFMultiply mult4 = new SDFMultiply(this, "mult4");
	  mult4.rate.setToken(new IntToken(cPRate));
	  //mult4.input.setTypeEquals(BaseType.DOUBLE);
	  //mult4.input2.setTypeEquals(BaseType.DOUBLE);

	  SDFAdd add1 = new SDFAdd(this, "add1");
	  add1.rate.setToken(new IntToken(cPRate));
	  //add1.input.setTypeEquals(BaseType.DOUBLE);
	  //add1.input2.setTypeEquals(BaseType.DOUBLE);
	  


	  SineFM carrier = new SineFM(this, "carrier");
	  carrier.rate.setToken(new IntToken(cPRate));
	  carrier.amplitude.setToken(new DoubleToken(0.5));

	  SineFM modulator = new SineFM(this, "modulator");
	  modulator.rate.setToken(new IntToken(cPRate));


	  this.connect(carrier.output, soundSink.input);
	  this.connect(mult1.output, add1.input2);
	  this.connect(add1.output, carrier.omega);
	  this.connect(modulator.output, mult1.input);
	  //this.connect(modulatorAmp.output, mult1.input2);
	  this.connect(harmonicityRatio.output, mult2.input);
	  this.connect(harmonicityConst.output, mult2.input2);
	  this.connect(mult2.output, mult3.input2);
	  this.connect(mult3.output, modulator.omega);
	  this.connect(modulatorAmp.output, mult4.input);
	  this.connect(modulatorConst.output, mult4.input2);
	  this.connect(mult4.output, mult1.input2);

	  TypedIORelation rel1 =
	      (TypedIORelation)this.newRelation("rel1");
	  //rel1.setWidth(2);
	  carrierFreq.output.link(rel1);
	  mult3.input.link(rel1);
	  add1.input.link(rel1);

	  TypedIORelation rel2 =
	      (TypedIORelation)this.newRelation("rel2");
	  //rel1.setWidth(2);
	  ramp1.output.link(rel2);
	  carrier.input.link(rel2);
	  modulator.input.link(rel2);

	  //  TypedIORelation rel3 =
	  //  (TypedIORelation)this.newRelation("rel3");
	  //rel1.setWidth(2);
	  //mult3.output.link(rel3);
	  //mult2.input.link(rel3);
	  //modulator.omega.link(rel3);

	  _ptQuery.attachParameter(carrierFreq.value, "carrierFreqSlider");
	  _ptQuery.attachParameter(harmonicityRatio.value, "harmonicitySlider");
	  _ptQuery.attachParameter(modulatorAmp.value, "modulationSlider");
	 

	  //exportMoML(new OutputStreamWriter(System.out));
	  
	  try {
	      Thread.sleep(5000);
	  } catch (InterruptedException e){
	  }



	  //sine1.output.link(rel1);
	  //soundSink.input.link(rel1);

        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }




}

